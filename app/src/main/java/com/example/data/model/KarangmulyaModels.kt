package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@IgnoreExtraProperties
data class CitizenRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val nik: String = "",
    val kkNumber: String = "",
    val rtRw: String = "",
    val dusun: String = "",
    val address: String = "",
    val phone: String = "",
    val occupation: String = "",
    val directoryGroup: String = "WARGA",
    val verificationStatus: String = "MENUNGGU_VERIFIKASI",
    @get:PropertyName("isSelfProfile")
    @set:PropertyName("isSelfProfile")
    var isSelfProfile: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LetterRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val regNumber: String = "",
    val applicantName: String = "",
    val applicantNik: String = "",
    val letterType: String = "",
    val purpose: String = "",
    val attachmentName: String = "",
    val status: String = "DIAJUKAN",
    val staffNote: String = "",
    val qrCodeData: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Complaint(
    @DocumentId val id: String = "",
    val userId: String = "",
    val regNumber: String = "",
    val reporterName: String = "",
    val category: String = "",
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double = -7.2145,
    val longitude: Double = 107.9012,
    val photoLabel: String = "",
    val status: String = "MENUNGGU",
    val staffResponse: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Aspiration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val regNumber: String = "",
    val senderName: String = "",
    val category: String = "",
    val title: String = "",
    val content: String = "",
    val status: String = "DITERIMA",
    val staffFeedback: String = "",
    val upvotes: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class VillagePost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postType: String = "BERITA",
    val title: String = "",
    val category: String = "",
    val summary: String = "",
    val content: String = "",
    val eventDate: String = "",
    val location: String = "",
    val budgetAmount: Long = 0L,
    val progressPercent: Long = 0L,
    val statusLabel: String = "",
    val authorName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "INFO",
    val referenceNumber: String = "",
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorName: String = "",
    val actorRole: String = "WARGA",
    val actionType: String = "",
    val targetRef: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
