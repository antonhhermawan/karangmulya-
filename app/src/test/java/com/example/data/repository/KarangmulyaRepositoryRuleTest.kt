package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KarangmulyaRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveLetterRequest_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KarangmulyaRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createLetterRequest(
                applicantName = "Siti Aminah",
                applicantNik = "3205012345670001",
                letterType = "Surat Keterangan Usaha (SKU)",
                purpose = "Pengajuan modal usaha warung sembako Desa Karangmulya",
                attachmentName = "ktp_kk.pdf"
            )
        }
        assertTrue("Expected letter creation to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val createdLetter = createResult.getOrThrow()
        assertTrue(createdLetter.id.isNotEmpty())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLetterRequests(uid).first { list -> list.any { it.id == createdLetter.id } }
        }
        assertTrue(emitted.any { it.id == createdLetter.id })
    }

    @Test
    fun getLetterRequestById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = KarangmulyaRepository(firestore, auth)
        val createdLetter = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createLetterRequest(
                applicantName = "Siti Aminah",
                applicantNik = "3205012345670001",
                letterType = "Surat Keterangan Domisili (SKD)",
                purpose = "Keperluan administrasi bank",
                attachmentName = "pengantar_rt.pdf"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = KarangmulyaRepository(firestore, auth)
        val bobResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getLetterRequestById(createdLetter.id)
        }
        assertTrue("Expected cross-user access to fail", bobResult.isFailure)
        val ex = bobResult.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeLetterRequests_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = KarangmulyaRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeLetterRequests(uid).first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED when unauthenticated")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "warga_alice@karangmulya.id"
        const val BOB_EMAIL = "warga_bob@karangmulya.id"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
