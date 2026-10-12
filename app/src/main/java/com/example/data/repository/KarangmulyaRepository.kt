package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.Aspiration
import com.example.data.model.AuditLog
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.VillagePost
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class KarangmulyaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
    }

    // --- REAL-TIME OBSERVATION FLOWS ---

    fun observeCitizenRecords(userId: String = requireUserId()): Flow<List<CitizenRecord>> = callbackFlow {
        val path = "citizen_records"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    CitizenRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeLetterRequests(userId: String = requireUserId()): Flow<List<LetterRequest>> = callbackFlow {
        val path = "letter_requests"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    LetterRequest::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeComplaints(userId: String = requireUserId()): Flow<List<Complaint>> = callbackFlow {
        val path = "complaints"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    Complaint::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeAspirations(userId: String = requireUserId()): Flow<List<Aspiration>> = callbackFlow {
        val path = "aspirations"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    Aspiration::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeVillagePosts(userId: String = requireUserId()): Flow<List<VillagePost>> = callbackFlow {
        val path = "village_posts"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    VillagePost::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeNotifications(userId: String = requireUserId()): Flow<List<AppNotification>> = callbackFlow {
        val path = "notifications"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    AppNotification::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    fun observeAuditLogs(userId: String = requireUserId()): Flow<List<AuditLog>> = callbackFlow {
        val path = "audit_logs"
        val registration = db.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(
                    AuditLog::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )?.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    // --- CITIZEN PROFILE & DIRECTORY CRUD ---

    suspend fun saveCitizenRecord(
        fullName: String,
        nik: String,
        kkNumber: String,
        rtRw: String,
        dusun: String,
        address: String,
        phone: String,
        occupation: String,
        directoryGroup: String = "WARGA",
        verificationStatus: String = "MENUNGGU_VERIFIKASI",
        isSelfProfile: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "cit_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("citizen_records").document(docId)
        val cleanNik = nik.filter { it.isDigit() }.padEnd(16, '0').take(16)
        val cleanKk = kkNumber.filter { it.isDigit() }.padEnd(16, '0').take(16)
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().take(100),
            "nik" to cleanNik,
            "kkNumber" to cleanKk,
            "rtRw" to rtRw.trim().ifBlank { "RT 01 / RW 01" }.take(30),
            "dusun" to dusun.trim().ifBlank { "Dusun Krajan" }.take(60),
            "address" to address.trim().ifBlank { "Desa Karangmulya" }.take(250),
            "phone" to phone.trim().ifBlank { "081200000000" }.take(30),
            "occupation" to occupation.trim().ifBlank { "Wiraswasta" }.take(80),
            "directoryGroup" to directoryGroup,
            "verificationStatus" to verificationStatus,
            "isSelfProfile" to isSelfProfile,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            logAudit(
                actorName = fullName.trim(),
                actorRole = directoryGroup,
                actionType = "REGISTER_WARGA",
                targetRef = cleanNik.take(6) + "******" + cleanNik.takeLast(4),
                description = "Menambahkan data warga/perangkat: ${fullName.trim()} ($dusun)"
            )
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateCitizenRecord(
        recordId: String,
        fullName: String,
        nik: String,
        kkNumber: String,
        rtRw: String,
        dusun: String,
        address: String,
        phone: String,
        occupation: String,
        directoryGroup: String,
        verificationStatus: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("citizen_records").document(recordId)
        val cleanNik = nik.filter { it.isDigit() }.padEnd(16, '0').take(16)
        val cleanKk = kkNumber.filter { it.isDigit() }.padEnd(16, '0').take(16)
        val updates = mapOf(
            "fullName" to fullName.trim().take(100),
            "nik" to cleanNik,
            "kkNumber" to cleanKk,
            "rtRw" to rtRw.trim().take(30),
            "dusun" to dusun.trim().take(60),
            "address" to address.trim().take(250),
            "phone" to phone.trim().take(30),
            "occupation" to occupation.trim().take(80),
            "directoryGroup" to directoryGroup,
            "verificationStatus" to verificationStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            createNotification(
                title = "Status Data Warga Diperbarui",
                message = "Data profil $fullName kini berstatus: $verificationStatus ($directoryGroup).",
                category = "VERIFIKASI",
                referenceNumber = recordId.take(10).uppercase()
            )
            logAudit(
                actorName = "Staff Pelayanan Desa",
                actorRole = "STAFF_PELAYANAN",
                actionType = "UPDATE_WARGA",
                targetRef = recordId,
                description = "Memperbarui profil & verifikasi warga $fullName -> $verificationStatus"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCitizenRecord(recordId: String, fullName: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("citizen_records").document(recordId)
        try {
            docRef.delete().await()
            logAudit(
                actorName = "Staff Pelayanan Desa",
                actorRole = "ADMIN_SISTEM",
                actionType = "DELETE_WARGA",
                targetRef = recordId,
                description = "Menghapus data warga/staff: $fullName"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- LETTER REQUESTS CRUD ---

    suspend fun createLetterRequest(
        applicantName: String,
        applicantNik: String,
        letterType: String,
        purpose: String,
        attachmentName: String
    ): Result<LetterRequest> = runCatching {
        val uid = requireUserId()
        val suffix = (1000..9999).random()
        val regNumber = "SRT-2026-$suffix"
        val qrCodeData = "KARANGMULYA-VERIFY-$regNumber"
        val docId = "srt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("letter_requests").document(docId)
        val cleanNik = applicantNik.filter { it.isDigit() }.padEnd(16, '0').take(16)
        val payload = mapOf(
            "userId" to uid,
            "regNumber" to regNumber,
            "applicantName" to applicantName.trim().take(100),
            "applicantNik" to cleanNik,
            "letterType" to letterType.trim().take(100),
            "purpose" to purpose.trim().take(500),
            "attachmentName" to attachmentName.trim().take(150),
            "status" to "DIAJUKAN",
            "staffNote" to "Permohonan diterima loket online Desa Karangmulya. Menunggu verifikasi berkas.",
            "qrCodeData" to qrCodeData,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            createNotification(
                title = "Pengajuan Surat Terkirim ($regNumber)",
                message = "Permohonan $letterType atas nama $applicantName berhasil diajukan.",
                category = "SURAT",
                referenceNumber = regNumber
            )
            logAudit(
                actorName = applicantName.trim(),
                actorRole = "WARGA",
                actionType = "CREATE_SURAT",
                targetRef = regNumber,
                description = "Mengajukan $letterType dengan keperluan: ${purpose.take(60)}"
            )
            LetterRequest(
                id = docId,
                userId = uid,
                regNumber = regNumber,
                applicantName = applicantName.trim(),
                applicantNik = cleanNik,
                letterType = letterType.trim(),
                purpose = purpose.trim(),
                attachmentName = attachmentName.trim(),
                status = "DIAJUKAN",
                staffNote = "Permohonan diterima loket online Desa Karangmulya.",
                qrCodeData = qrCodeData
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getLetterRequestById(letterId: String): Result<LetterRequest> = runCatching {
        val docRef = db.collection("letter_requests").document(letterId)
        try {
            val snap = docRef.get().await()
            snap.toObject(LetterRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: throw IllegalStateException("Surat tidak ditemukan")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun updateLetterStatus(
        letterId: String,
        regNumber: String,
        letterType: String,
        newStatus: String,
        staffNote: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("letter_requests").document(letterId)
        val updates = mapOf(
            "status" to newStatus,
            "staffNote" to staffNote.trim().take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            createNotification(
                title = "Update Status Surat $regNumber",
                message = "$letterType kini berstatus $newStatus. Catatan Staff: $staffNote",
                category = "SURAT",
                referenceNumber = regNumber
            )
            logAudit(
                actorName = "Perangkat Desa Karangmulya",
                actorRole = "KAUR_UMUM",
                actionType = "UPDATE_SURAT_STATUS",
                targetRef = regNumber,
                description = "Memperbarui status $regNumber menjadi $newStatus"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- COMPLAINTS CRUD ---

    suspend fun createComplaint(
        reporterName: String,
        category: String,
        title: String,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoLabel: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val suffix = (1000..9999).random()
        val regNumber = "ADU-2026-$suffix"
        val docId = "adu_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("complaints").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "regNumber" to regNumber,
            "reporterName" to reporterName.trim().take(100),
            "category" to category.trim().take(60),
            "title" to title.trim().take(150),
            "description" to description.trim().take(1000),
            "locationName" to locationName.trim().take(150),
            "latitude" to latitude,
            "longitude" to longitude,
            "photoLabel" to photoLabel.trim().take(200),
            "status" to "MENUNGGU",
            "staffResponse" to "Laporan pengaduan telah diterima Tim Respon Cepat Desa Karangmulya.",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            createNotification(
                title = "Pengaduan Masyarakat Terdaftar ($regNumber)",
                message = "Laporan '$title' di $locationName telah masuk ke sistem tindak lanjut desa.",
                category = "PENGADUAN",
                referenceNumber = regNumber
            )
            logAudit(
                actorName = reporterName.trim(),
                actorRole = "WARGA",
                actionType = "CREATE_PENGADUAN",
                targetRef = regNumber,
                description = "Melaporkan pengaduan: $title ($locationName)"
            )
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateComplaintStatus(
        complaintId: String,
        regNumber: String,
        title: String,
        newStatus: String,
        staffResponse: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("complaints").document(complaintId)
        val updates = mapOf(
            "status" to newStatus,
            "staffResponse" to staffResponse.trim().take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            createNotification(
                title = "Tindak Lanjut Pengaduan $regNumber",
                message = "Pengaduan '$title' kini berstatus $newStatus. Tanggapan Desa: $staffResponse",
                category = "PENGADUAN",
                referenceNumber = regNumber
            )
            logAudit(
                actorName = "Tim Respon Desa",
                actorRole = "STAFF_PELAYANAN",
                actionType = "UPDATE_PENGADUAN",
                targetRef = regNumber,
                description = "Menanggapi pengaduan $regNumber -> $newStatus"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- ASPIRATIONS CRUD ---

    suspend fun createAspiration(
        senderName: String,
        category: String,
        title: String,
        content: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val suffix = (1000..9999).random()
        val regNumber = "ASP-2026-$suffix"
        val docId = "asp_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("aspirations").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "regNumber" to regNumber,
            "senderName" to senderName.trim().take(100),
            "category" to category.trim().take(60),
            "title" to title.trim().take(150),
            "content" to content.trim().take(1000),
            "status" to "DITERIMA",
            "staffFeedback" to "Aspirasi dicatat untuk pembahasan Musrenbangdes Karangmulya.",
            "upvotes" to 1L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            createNotification(
                title = "Aspirasi & Saran Diterima ($regNumber)",
                message = "Terima kasih! Aspirasi '$title' telah tercatat di Markas Rakyat.",
                category = "ASPIRASI",
                referenceNumber = regNumber
            )
            logAudit(
                actorName = senderName.trim(),
                actorRole = "WARGA",
                actionType = "CREATE_ASPIRASI",
                targetRef = regNumber,
                description = "Mengirim aspirasi masyarakat: $title"
            )
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateAspiration(
        aspirationId: String,
        regNumber: String,
        newStatus: String,
        staffFeedback: String,
        upvotes: Long
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("aspirations").document(aspirationId)
        val updates = mapOf(
            "status" to newStatus,
            "staffFeedback" to staffFeedback.trim().take(500),
            "upvotes" to upvotes.coerceAtLeast(0L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            logAudit(
                actorName = "BPD & Pemdes Karangmulya",
                actorRole = "SEKRETARIS_DESA",
                actionType = "UPDATE_ASPIRASI",
                targetRef = regNumber,
                description = "Memperbarui aspirasi $regNumber -> $newStatus (Dukungan: $upvotes)"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- VILLAGE POSTS (BERITA, PENGUMUMAN, AGENDA, PEMBANGUNAN) CRUD ---

    suspend fun createVillagePost(
        postType: String,
        title: String,
        category: String,
        summary: String,
        content: String,
        eventDate: String,
        location: String,
        budgetAmount: Long,
        progressPercent: Long,
        statusLabel: String,
        authorName: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "pst_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("village_posts").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "postType" to postType,
            "title" to title.trim().take(150),
            "category" to category.trim().take(60),
            "summary" to summary.trim().take(300),
            "content" to content.trim().take(2000),
            "eventDate" to eventDate.trim().take(60),
            "location" to location.trim().take(150),
            "budgetAmount" to budgetAmount.coerceAtLeast(0L),
            "progressPercent" to progressPercent.coerceIn(0L, 100L),
            "statusLabel" to statusLabel.trim().take(60),
            "authorName" to authorName.trim().ifBlank { "Pemdes Karangmulya" }.take(100),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            logAudit(
                actorName = authorName.trim().ifBlank { "Pemdes Karangmulya" },
                actorRole = "STAFF_PELAYANAN",
                actionType = "PUBLISH_$postType",
                targetRef = docId,
                description = "Mempublikasikan $postType: $title"
            )
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateVillagePost(
        postId: String,
        title: String,
        category: String,
        summary: String,
        content: String,
        eventDate: String,
        location: String,
        budgetAmount: Long,
        progressPercent: Long,
        statusLabel: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("village_posts").document(postId)
        val updates = mapOf(
            "title" to title.trim().take(150),
            "category" to category.trim().take(60),
            "summary" to summary.trim().take(300),
            "content" to content.trim().take(2000),
            "eventDate" to eventDate.trim().take(60),
            "location" to location.trim().take(150),
            "budgetAmount" to budgetAmount.coerceAtLeast(0L),
            "progressPercent" to progressPercent.coerceIn(0L, 100L),
            "statusLabel" to statusLabel.trim().take(60),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            logAudit(
                actorName = "Perangkat Desa",
                actorRole = "KAUR_UMUM",
                actionType = "UPDATE_POST",
                targetRef = postId,
                description = "Memperbarui informasi/pembangunan: $title ($progressPercent%)"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteVillagePost(postId: String, title: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("village_posts").document(postId)
        try {
            docRef.delete().await()
            logAudit(
                actorName = "Perangkat Desa",
                actorRole = "SEKRETARIS_DESA",
                actionType = "DELETE_POST",
                targetRef = postId,
                description = "Menghapus publikasi: $title"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- NOTIFICATIONS & AUDIT LOGS ---

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("notifications").document(notificationId)
        try {
            docRef.update(
                mapOf(
                    "isRead" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun createNotification(
        title: String,
        message: String,
        category: String,
        referenceNumber: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val docId = "ntf_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("notifications").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "title" to title.take(150),
            "message" to message.take(500),
            "category" to category.take(50),
            "referenceNumber" to referenceNumber.take(50),
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun logAudit(
        actorName: String,
        actorRole: String,
        actionType: String,
        targetRef: String,
        description: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val docId = "log_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val docRef = db.collection("audit_logs").document(docId)
        val payload = mapOf(
            "userId" to uid,
            "actorName" to actorName.take(100),
            "actorRole" to actorRole.take(60),
            "actionType" to actionType.take(80),
            "targetRef" to targetRef.take(80),
            "description" to description.take(500),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    // --- INITIAL DEMO DATA SEEDING FOR DESA KARANGMULYA ---

    suspend fun seedDemoDataIfEmpty(displayName: String) {
        val uid = auth.currentUser?.uid ?: return
        val existingCitizens = try {
            db.collection("citizen_records")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "citizen_records")
            return
        }
        if (!existingCitizens.isEmpty) return
        seedInitialDemoData(displayName)
    }

    suspend fun seedInitialDemoData(displayName: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val citizenName = displayName.ifBlank { "Warga Karangmulya" }

        // 1. Seed Primary Self Profile + Sample Village Directory Records (Warga & Staff)
        val selfDoc = db.collection("citizen_records").document("cit_self_${uid.take(8)}")
        selfDoc.set(
            mapOf(
                "userId" to uid,
                "fullName" to citizenName,
                "nik" to "3205142809940001",
                "kkNumber" to "3205140501200004",
                "rtRw" to "RT 02 / RW 01",
                "dusun" to "Dusun Krajan",
                "address" to "Jl. Poros Balai Desa Karangmulya No. 18, Dusun Krajan",
                "phone" to "081288994411",
                "occupation" to "Wirausaha UMKM",
                "directoryGroup" to "WARGA",
                "verificationStatus" to "TERVERIFIKASI",
                "isSelfProfile" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val sampleDirectory = listOf(
            mapOf(
                "id" to "cit_warga_1_${uid.take(6)}",
                "fullName" to "Ahmad Fauzi",
                "nik" to "3205141103880002",
                "kkNumber" to "3205140902150008",
                "rtRw" to "RT 04 / RW 02",
                "dusun" to "Dusun Mekarsari",
                "address" to "Kp. Mekarsari Indah No. 7, Desa Karangmulya",
                "phone" to "081322445566",
                "occupation" to "Petani Hortikultura",
                "directoryGroup" to "WARGA",
                "verificationStatus" to "MENUNGGU_VERIFIKASI",
                "isSelfProfile" to false
            ),
            mapOf(
                "id" to "cit_staff_1_${uid.take(6)}",
                "fullName" to "H. Hendra Gunawan, S.IP",
                "nik" to "3205140107750001",
                "kkNumber" to "3205140101050001",
                "rtRw" to "RT 01 / RW 01",
                "dusun" to "Dusun Krajan",
                "address" to "Jl. Raya Karangmulya No. 1, Komplek Balai Desa",
                "phone" to "081122334455",
                "occupation" to "Kepala Desa Karangmulya",
                "directoryGroup" to "KEPALA_DESA",
                "verificationStatus" to "TERVERIFIKASI",
                "isSelfProfile" to false
            ),
            mapOf(
                "id" to "cit_staff_2_${uid.take(6)}",
                "fullName" to "Rina Kartika, S.AP",
                "nik" to "3205141505920003",
                "kkNumber" to "3205141208180002",
                "rtRw" to "RT 03 / RW 01",
                "dusun" to "Dusun Sukamaju",
                "address" to "Jl. Sukamaju Raya No. 24, Desa Karangmulya",
                "phone" to "085299887766",
                "occupation" to "Sekretaris Desa",
                "directoryGroup" to "SEKRETARIS_DESA",
                "verificationStatus" to "TERVERIFIKASI",
                "isSelfProfile" to false
            ),
            mapOf(
                "id" to "cit_staff_3_${uid.take(6)}",
                "fullName" to "Dedi Kurniawan",
                "nik" to "3205142011950005",
                "kkNumber" to "3205141904210006",
                "rtRw" to "RT 02 / RW 03",
                "dusun" to "Dusun Karanganyar",
                "address" to "Jl. Karanganyar Barat No. 9, Desa Karangmulya",
                "phone" to "087811223399",
                "occupation" to "Kaur Pelayanan & Tata Usaha",
                "directoryGroup" to "STAFF_PELAYANAN",
                "verificationStatus" to "TERVERIFIKASI",
                "isSelfProfile" to false
            )
        )
        for (item in sampleDirectory) {
            val docId = item["id"] as String
            db.collection("citizen_records").document(docId).set(
                mapOf(
                    "userId" to uid,
                    "fullName" to item["fullName"]!!,
                    "nik" to item["nik"]!!,
                    "kkNumber" to item["kkNumber"]!!,
                    "rtRw" to item["rtRw"]!!,
                    "dusun" to item["dusun"]!!,
                    "address" to item["address"]!!,
                    "phone" to item["phone"]!!,
                    "occupation" to item["occupation"]!!,
                    "directoryGroup" to item["directoryGroup"]!!,
                    "verificationStatus" to item["verificationStatus"]!!,
                    "isSelfProfile" to item["isSelfProfile"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 2. Seed Sample Letter Requests
        val sampleLetters = listOf(
            mapOf(
                "id" to "srt_demo_1_${uid.take(6)}",
                "regNumber" to "SRT-2026-4812",
                "applicantName" to citizenName,
                "applicantNik" to "3205142809940001",
                "letterType" to "Surat Keterangan Usaha (SKU)",
                "purpose" to "Persyaratan pengajuan pembiayaan modal UMKM kuliner desa di Bank BRI Unit Karangmulya",
                "attachmentName" to "Foto_KTP_KK_TempatUsaha.jpg",
                "status" to "SELESAI",
                "staffNote" to "Dokumen lengkap dan telah ditandatangani secara elektronik oleh Kepala Desa Karangmulya. Silakan unduh PDF surat.",
                "qrCodeData" to "KARANGMULYA-VERIFY-SRT-2026-4812"
            ),
            mapOf(
                "id" to "srt_demo_2_${uid.take(6)}",
                "regNumber" to "SRT-2026-4829",
                "applicantName" to citizenName,
                "applicantNik" to "3205142809940001",
                "letterType" to "Surat Keterangan Domisili (SKD)",
                "purpose" to "Administrasi pembukaan rekening koperasi tani dan perbaruan data kependudukan",
                "attachmentName" to "Pengantar_RT02_RW01.pdf",
                "status" to "DIPROSES",
                "staffNote" to "Berkas telah diverifikasi Kaur Pelayanan, sedang menunggu pengesahan Sekretaris Desa.",
                "qrCodeData" to "KARANGMULYA-VERIFY-SRT-2026-4829"
            ),
            mapOf(
                "id" to "srt_demo_3_${uid.take(6)}",
                "regNumber" to "SRT-2026-4850",
                "applicantName" to "Ahmad Fauzi",
                "applicantNik" to "3205141103880002",
                "letterType" to "Surat Keterangan Tidak Mampu (SKTM)",
                "purpose" to "Pengajuan beasiswa pendidikan anak berprestasi tingkat SMA",
                "attachmentName" to "KK_KartuKIP_Rapor.pdf",
                "status" to "DIAJUKAN",
                "staffNote" to "Permohonan baru masuk melalui aplikasi Markas Rakyat. Menunggu pemeriksaan berkas.",
                "qrCodeData" to "KARANGMULYA-VERIFY-SRT-2026-4850"
            )
        )
        for (srt in sampleLetters) {
            db.collection("letter_requests").document(srt["id"]!!).set(
                mapOf(
                    "userId" to uid,
                    "regNumber" to srt["regNumber"]!!,
                    "applicantName" to srt["applicantName"]!!,
                    "applicantNik" to srt["applicantNik"]!!,
                    "letterType" to srt["letterType"]!!,
                    "purpose" to srt["purpose"]!!,
                    "attachmentName" to srt["attachmentName"]!!,
                    "status" to srt["status"]!!,
                    "staffNote" to srt["staffNote"]!!,
                    "qrCodeData" to srt["qrCodeData"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 3. Seed Sample Complaints
        val sampleComplaints = listOf(
            mapOf(
                "id" to "adu_demo_1_${uid.take(6)}",
                "regNumber" to "ADU-2026-9104",
                "reporterName" to citizenName,
                "category" to "Infrastruktur & Jalan",
                "title" to "Lampu Penerangan Jalan Umum (PJU) Padam di Tikungan Dusun Krajan",
                "description" to "Terdapat 2 titik lampu PJU tenaga surya di dekat jembatan RT 02 / RW 01 yang padam sejak 3 hari lalu sehingga jalan gelap saat malam hari.",
                "locationName" to "Jl. Poros Dusun Krajan RT 02 / RW 01 (Dekat Jembatan Kali Mulya)",
                "latitude" to -7.2148,
                "longitude" to 107.9018,
                "photoLabel" to "foto_pju_krajan_rt02.jpg",
                "status" to "DITINDAKLANJUTI",
                "staffResponse" to "Tim Teknis Desa telah mengecek lokasi. Baterai panel surya sedang diganti dan dijadwalkan menyala kembali besok sore."
            ),
            mapOf(
                "id" to "adu_demo_2_${uid.take(6)}",
                "regNumber" to "ADU-2026-9088",
                "reporterName" to "Ahmad Fauzi",
                "category" to "Lingkungan & Drainase",
                "title" to "Saluran Drainase Tersumbat Endapan Lumpur di Dusun Mekarsari",
                "description" to "Saluran air di pinggir jalan usaha tani RT 04 / RW 02 tertutup sedimen tanah setelah hujan deras kemarin.",
                "locationName" to "Dusun Mekarsari RT 04 / RW 02",
                "latitude" to -7.2162,
                "longitude" to 107.9045,
                "photoLabel" to "drainase_mekarsari_rt04.jpg",
                "status" to "SELESAI",
                "staffResponse" to "Telah dibersihkan bersama warga dan Linmas dalam kegiatan Jumat Bersih. Aliran air kembali lancar."
            )
        )
        for (adu in sampleComplaints) {
            db.collection("complaints").document(adu["id"] as String).set(
                mapOf(
                    "userId" to uid,
                    "regNumber" to adu["regNumber"]!!,
                    "reporterName" to adu["reporterName"]!!,
                    "category" to adu["category"]!!,
                    "title" to adu["title"]!!,
                    "description" to adu["description"]!!,
                    "locationName" to adu["locationName"]!!,
                    "latitude" to adu["latitude"]!!,
                    "longitude" to adu["longitude"]!!,
                    "photoLabel" to adu["photoLabel"]!!,
                    "status" to adu["status"]!!,
                    "staffResponse" to adu["staffResponse"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 4. Seed Sample Aspirations
        val sampleAspirations = listOf(
            mapOf(
                "id" to "asp_demo_1_${uid.take(6)}",
                "regNumber" to "ASP-2026-3301",
                "senderName" to citizenName,
                "category" to "Ekonomi & UMKM Desa",
                "title" to "Usulan Pelatihan Pemasaran Digital & Kemasan Produk UMKM Karangmulya",
                "content" to "Mohon diadakan pelatihan foto produk dan pendaftaran sertifikasi Halal bagi pelaku UMKM olahan pangan di Balai Desa Karangmulya.",
                "status" to "DIREALISASIKAN",
                "staffFeedback" to "Usulan disetujui dalam program Pemberdayaan Masyarakat Tahun 2026. Pelatihan dilaksanakan bulan depan bersama Dinas Koperasi & UMKM.",
                "upvotes" to 24L
            ),
            mapOf(
                "id" to "asp_demo_2_${uid.take(6)}",
                "regNumber" to "ASP-2026-3315",
                "senderName" to "Siti Rahmawati",
                "category" to "Pendidikan & Kepemudaan",
                "title" to "Penambahan Koleksi Buku & Pojok Baca Digital di Perpustakaan Desa",
                "content" to "Saran agar Pojok Baca Balai Desa dilengkapi komputer belajar dan buku pertanian modern untuk Karang Taruna dan pelajar.",
                "status" to "DIKAJI_DESA",
                "staffFeedback" to "Sedang dikaji oleh Seksi Kesejahteraan untuk dimasukkan ke RKPDes perubahan.",
                "upvotes" to 15L
            )
        )
        for (asp in sampleAspirations) {
            db.collection("aspirations").document(asp["id"] as String).set(
                mapOf(
                    "userId" to uid,
                    "regNumber" to asp["regNumber"]!!,
                    "senderName" to asp["senderName"]!!,
                    "category" to asp["category"]!!,
                    "title" to asp["title"]!!,
                    "content" to asp["content"]!!,
                    "status" to asp["status"]!!,
                    "staffFeedback" to asp["staffFeedback"]!!,
                    "upvotes" to asp["upvotes"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 5. Seed Village Posts (BERITA, PENGUMUMAN, AGENDA, PEMBANGUNAN)
        val samplePosts = listOf(
            mapOf(
                "id" to "pst_demo_1_${uid.take(6)}",
                "postType" to "PENGUMUMAN",
                "title" to "Pelayanan Administrasi Desa Karangmulya Kini Terintegrasi Digital 24 Jam",
                "category" to "Layanan Publik",
                "summary" to "Warga kini dapat mengajukan surat pengantar, melacak status dokumen via QR Code, dan menyampaikan pengaduan langsung dari HP.",
                "content" to "Pemerintah Desa Karangmulya resmi meluncurkan aplikasi MARKAS RAKYAT DESA KARANGMULYA. Melalui platform ini, warga tidak perlu antre lama di Balai Desa untuk mengurus Surat Keterangan Domisili, SKU, SKTM, maupun Pengantar SKCK.",
                "eventDate" to "Oktober 2026",
                "location" to "Balai Desa Karangmulya",
                "budgetAmount" to 0L,
                "progressPercent" to 100L,
                "statusLabel" to "Pengumuman Resmi",
                "authorName" to "Sekretaris Desa Karangmulya"
            ),
            mapOf(
                "id" to "pst_demo_2_${uid.take(6)}",
                "postType" to "BERITA",
                "title" to "Panen Raya Padi Organik Kelompok Tani Mulya Makmur Meningkat 28%",
                "category" to "Ketahanan Pangan",
                "summary" to "Inovasi irigasi tetes dan pupuk organik desa berhasil mendongkrak hasil panen petani Dusun Mekarsari dan Krajan.",
                "content" to "Kelompok Tani Mulya Makmur bersama BUMDes Karangmulya berhasil melaksanakan panen raya di lahan seluas 42 hektar. Hasil panen diserap langsung oleh BUMDes untuk beras kemasan unggulan desa.",
                "eventDate" to "10 Oktober 2026",
                "location" to "Area Persawahan Dusun Mekarsari",
                "budgetAmount" to 0L,
                "progressPercent" to 100L,
                "statusLabel" to "Berita Utama",
                "authorName" to "Tim Humas Karangmulya"
            ),
            mapOf(
                "id" to "pst_demo_3_${uid.take(6)}",
                "postType" to "AGENDA",
                "title" to "Musyawarah Perencanaan Pembangunan Desa (Musrenbangdes) TA 2027",
                "category" to "Musyawarah Desa",
                "summary" to "Undangan terbuka bagi perwakilan RT/RW, tokoh masyarakat, pemuda, dan kelompok perempuan untuk menyusun prioritas pembangunan.",
                "content" to "Agenda pembahasan meliputi evaluasi realisasi APBDes 2026, penetapan prioritas infrastruktur dusun, program pemberdayaan UMKM, dan layanan kesehatan Posyandu.",
                "eventDate" to "Kamis, 22 Oktober 2026 • 08:30 WIB",
                "location" to "Pendopo Balai Desa Karangmulya",
                "budgetAmount" to 0L,
                "progressPercent" to 0L,
                "statusLabel" to "Terjadwal",
                "authorName" to "BPD & Pemdes Karangmulya"
            ),
            mapOf(
                "id" to "pst_demo_4_${uid.take(6)}",
                "postType" to "AGENDA",
                "title" to "Posyandu Terpadu Balita, Ibu Hamil & Pemeriksaan Kesehatan Lansia",
                "category" to "Kesehatan Masyarakat",
                "summary" to "Layanan imunisasi rutin, pemberian makanan tambahan (PMT) bergizi, serta cek tensi dan gula darah gratis bagi lansia.",
                "content" to "Bidan Desa bersama Kader Posyandu Karangmulya membuka layanan kesehatan gratis. Warga diharapkan membawa buku KIA dan KTP.",
                "eventDate" to "Sabtu, 17 Oktober 2026 • 08:00 WIB",
                "location" to "Polindes & Posyandu Dusun Krajan",
                "budgetAmount" to 0L,
                "progressPercent" to 0L,
                "statusLabel" to "Agenda Rutin",
                "authorName" to "Kader Posyandu Karangmulya"
            ),
            mapOf(
                "id" to "pst_demo_5_${uid.take(6)}",
                "postType" to "PEMBANGUNAN",
                "title" to "Rabat Beton Jalan Usaha Tani & Drainase Dusun Krajan - Mekarsari",
                "category" to "Infrastruktur Jalan Desa",
                "summary" to "Pengecoran jalan beton sepanjang 650 meter (lebar 3 meter) beserta saluran drainase batu kali untuk memperlancar distribusi hasil tani.",
                "content" to "Sumber Dana: Dana Desa (DD) Tahap II Tahun Anggaran 2026. Pelaksana: Tim Pelaksana Kegiatan (TPK) Desa Karangmulya dengan skema Padat Karya Tunai Desa (PKTD) melibatkan 35 warga lokal.",
                "eventDate" to "Tahun Anggaran 2026",
                "location" to "Poros Dusun Krajan s/d Dusun Mekarsari",
                "budgetAmount" to 185000000L,
                "progressPercent" to 85L,
                "statusLabel" to "Fisik 85% Berjalan",
                "authorName" to "TPK Pembangunan Desa"
            ),
            mapOf(
                "id" to "pst_demo_6_${uid.take(6)}",
                "postType" to "PEMBANGUNAN",
                "title" to "Revitalisasi Gedung Polindes, Taman Baca & Jaringan Internet Warga",
                "category" to "Sarana Pelayanan & Digital",
                "summary" to "Perbaikan atap dan ruang periksa Polindes, pengadaan fasilitas Pojok Baca Digital, serta titik WiFi publik gratis di sekitar Balai Desa.",
                "content" to "Sumber Dana: Alokasi Dana Desa (ADD) & Bagi Hasil Pajak Tahun 2026. Pekerjaan telah selesai 100% dan telah diserahterimakan untuk dimanfaatkan seluruh warga Desa Karangmulya.",
                "eventDate" to "Tahun Anggaran 2026",
                "location" to "Komplek Balai Desa & Polindes Karangmulya",
                "budgetAmount" to 92500000L,
                "progressPercent" to 100L,
                "statusLabel" to "Selesai 100%",
                "authorName" to "Kaur Perencanaan Desa"
            )
        )
        for (pst in samplePosts) {
            db.collection("village_posts").document(pst["id"] as String).set(
                mapOf(
                    "userId" to uid,
                    "postType" to pst["postType"]!!,
                    "title" to pst["title"]!!,
                    "category" to pst["category"]!!,
                    "summary" to pst["summary"]!!,
                    "content" to pst["content"]!!,
                    "eventDate" to pst["eventDate"]!!,
                    "location" to pst["location"]!!,
                    "budgetAmount" to pst["budgetAmount"]!!,
                    "progressPercent" to pst["progressPercent"]!!,
                    "statusLabel" to pst["statusLabel"]!!,
                    "authorName" to pst["authorName"]!!,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 6. Seed Sample Notification & Audit Log
        createNotification(
            title = "Selamat Datang di Markas Rakyat Desa Karangmulya",
            message = "Akun warga Anda telah aktif. Surat SKU (SRT-2026-4812) Anda telah selesai diproses dan siap diunduh dalam format PDF.",
            category = "SURAT",
            referenceNumber = "SRT-2026-4812"
        )
        logAudit(
            actorName = "Sistem Markas Rakyat",
            actorRole = "ADMIN_SISTEM",
            actionType = "INIT_DATABASE",
            targetRef = "KARANGMULYA-2026",
            description = "Menginisialisasi data layanan warga, surat online, pengaduan, dan transparansi pembangunan Desa Karangmulya."
        )
    }
}
