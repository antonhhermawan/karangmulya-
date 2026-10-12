package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.Aspiration
import com.example.data.model.AuditLog
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.UiState
import com.example.data.model.VillagePost
import com.example.data.repository.KarangmulyaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppInterfaceMode {
    WARGA,
    STAFF_DESA
}

class KarangmulyaViewModel(
    private val repository: KarangmulyaRepository,
    private val currentUserId: String,
    private val currentUserDisplayName: String
) : ViewModel() {

    private val _interfaceMode = MutableStateFlow(AppInterfaceMode.WARGA)
    val interfaceMode: StateFlow<AppInterfaceMode> = _interfaceMode.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val citizenRecordsState: StateFlow<UiState<List<CitizenRecord>>> =
        repository.observeCitizenRecords(currentUserId)
            .map<List<CitizenRecord>, UiState<List<CitizenRecord>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing citizen records", error)
                emit(UiState.Error(error.message ?: "Gagal memuat data warga"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val letterRequestsState: StateFlow<UiState<List<LetterRequest>>> =
        repository.observeLetterRequests(currentUserId)
            .map<List<LetterRequest>, UiState<List<LetterRequest>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing letter requests", error)
                emit(UiState.Error(error.message ?: "Gagal memuat data surat"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val complaintsState: StateFlow<UiState<List<Complaint>>> =
        repository.observeComplaints(currentUserId)
            .map<List<Complaint>, UiState<List<Complaint>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing complaints", error)
                emit(UiState.Error(error.message ?: "Gagal memuat data pengaduan"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val aspirationsState: StateFlow<UiState<List<Aspiration>>> =
        repository.observeAspirations(currentUserId)
            .map<List<Aspiration>, UiState<List<Aspiration>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing aspirations", error)
                emit(UiState.Error(error.message ?: "Gagal memuat data aspirasi"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val villagePostsState: StateFlow<UiState<List<VillagePost>>> =
        repository.observeVillagePosts(currentUserId)
            .map<List<VillagePost>, UiState<List<VillagePost>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing village posts", error)
                emit(UiState.Error(error.message ?: "Gagal memuat informasi desa"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val notificationsState: StateFlow<UiState<List<AppNotification>>> =
        repository.observeNotifications(currentUserId)
            .map<List<AppNotification>, UiState<List<AppNotification>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing notifications", error)
                emit(UiState.Error(error.message ?: "Gagal memuat notifikasi"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val auditLogsState: StateFlow<UiState<List<AuditLog>>> =
        repository.observeAuditLogs(currentUserId)
            .map<List<AuditLog>, UiState<List<AuditLog>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing audit logs", error)
                emit(UiState.Error(error.message ?: "Gagal memuat audit log"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty(currentUserDisplayName)
        }
    }

    fun setInterfaceMode(mode: AppInterfaceMode) {
        _interfaceMode.value = mode
        val label = if (mode == AppInterfaceMode.WARGA) "Interface Warga Desa" else "Interface Staff & Aparatur Desa"
        _statusBannerMessage.value = "Beralih ke $label"
    }

    fun clearBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun showMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun submitLetterRequest(
        applicantName: String,
        applicantNik: String,
        letterType: String,
        purpose: String,
        attachmentName: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createLetterRequest(
                applicantName = applicantName,
                applicantNik = applicantNik,
                letterType = letterType,
                purpose = purpose,
                attachmentName = attachmentName
            )
            _isBusy.value = false
            result.onSuccess { srt ->
                _statusBannerMessage.value = "Permohonan surat berhasil dikirim! Nomor Registrasi: ${srt.regNumber}"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal mengirim surat: ${err.localizedMessage}"
            }
        }
    }

    fun updateLetterStatus(
        letterId: String,
        regNumber: String,
        letterType: String,
        newStatus: String,
        staffNote: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateLetterStatus(
                letterId = letterId,
                regNumber = regNumber,
                letterType = letterType,
                newStatus = newStatus,
                staffNote = staffNote
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Status surat $regNumber diperbarui menjadi $newStatus"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal memperbarui surat: ${err.localizedMessage}"
            }
        }
    }

    fun submitComplaint(
        reporterName: String,
        category: String,
        title: String,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoLabel: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createComplaint(
                reporterName = reporterName,
                category = category,
                title = title,
                description = description,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                photoLabel = photoLabel
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Pengaduan masyarakat berhasil dikirim dan tercatat di sistem desa!"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal mengirim pengaduan: ${err.localizedMessage}"
            }
        }
    }

    fun updateComplaintStatus(
        complaintId: String,
        regNumber: String,
        title: String,
        newStatus: String,
        staffResponse: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updateComplaintStatus(
                complaintId = complaintId,
                regNumber = regNumber,
                title = title,
                newStatus = newStatus,
                staffResponse = staffResponse
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Tanggapan pengaduan $regNumber disimpan ($newStatus)"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal memperbarui pengaduan: ${err.localizedMessage}"
            }
        }
    }

    fun submitAspiration(
        senderName: String,
        category: String,
        title: String,
        content: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createAspiration(
                senderName = senderName,
                category = category,
                title = title,
                content = content
            )
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Aspirasi & saran Anda telah diterima Pemerintah Desa Karangmulya!"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal mengirim aspirasi: ${err.localizedMessage}"
            }
        }
    }

    fun updateAspiration(
        aspirationId: String,
        regNumber: String,
        newStatus: String,
        staffFeedback: String,
        upvotes: Long
    ) {
        viewModelScope.launch {
            val result = repository.updateAspiration(
                aspirationId = aspirationId,
                regNumber = regNumber,
                newStatus = newStatus,
                staffFeedback = staffFeedback,
                upvotes = upvotes
            )
            result.onSuccess {
                _statusBannerMessage.value = "Aspirasi $regNumber diperbarui ($newStatus)"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal memperbarui aspirasi: ${err.localizedMessage}"
            }
        }
    }

    fun saveCitizenOrStaffRecord(
        recordId: String?,
        fullName: String,
        nik: String,
        kkNumber: String,
        rtRw: String,
        dusun: String,
        address: String,
        phone: String,
        occupation: String,
        directoryGroup: String,
        verificationStatus: String,
        isSelfProfile: Boolean = false
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = if (recordId.isNullOrBlank()) {
                repository.saveCitizenRecord(
                    fullName = fullName,
                    nik = nik,
                    kkNumber = kkNumber,
                    rtRw = rtRw,
                    dusun = dusun,
                    address = address,
                    phone = phone,
                    occupation = occupation,
                    directoryGroup = directoryGroup,
                    verificationStatus = verificationStatus,
                    isSelfProfile = isSelfProfile
                ).map { }
            } else {
                repository.updateCitizenRecord(
                    recordId = recordId,
                    fullName = fullName,
                    nik = nik,
                    kkNumber = kkNumber,
                    rtRw = rtRw,
                    dusun = dusun,
                    address = address,
                    phone = phone,
                    occupation = occupation,
                    directoryGroup = directoryGroup,
                    verificationStatus = verificationStatus
                )
            }
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Data $fullName berhasil disimpan ($verificationStatus)"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal menyimpan data warga: ${err.localizedMessage}"
            }
        }
    }

    fun deleteCitizenRecord(recordId: String, fullName: String) {
        viewModelScope.launch {
            repository.deleteCitizenRecord(recordId, fullName).onSuccess {
                _statusBannerMessage.value = "Data $fullName dihapus dari direktori"
            }
        }
    }

    fun saveVillagePost(
        postId: String?,
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
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = if (postId.isNullOrBlank()) {
                repository.createVillagePost(
                    postType = postType,
                    title = title,
                    category = category,
                    summary = summary,
                    content = content,
                    eventDate = eventDate,
                    location = location,
                    budgetAmount = budgetAmount,
                    progressPercent = progressPercent,
                    statusLabel = statusLabel,
                    authorName = authorName
                ).map { }
            } else {
                repository.updateVillagePost(
                    postId = postId,
                    title = title,
                    category = category,
                    summary = summary,
                    content = content,
                    eventDate = eventDate,
                    location = location,
                    budgetAmount = budgetAmount,
                    progressPercent = progressPercent,
                    statusLabel = statusLabel
                )
            }
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Publikasi $postType '$title' berhasil disimpan!"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal menyimpan publikasi: ${err.localizedMessage}"
            }
        }
    }

    fun deleteVillagePost(postId: String, title: String) {
        viewModelScope.launch {
            repository.deleteVillagePost(postId, title).onSuccess {
                _statusBannerMessage.value = "Publikasi '$title' telah dihapus"
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.seedInitialDemoData(currentUserDisplayName)
            _isBusy.value = false
            result.onSuccess {
                _statusBannerMessage.value = "Data simulasi Desa Karangmulya berhasil dimuat ulang ke database!"
            }.onFailure { err ->
                _statusBannerMessage.value = "Gagal memuat data simulasi: ${err.localizedMessage}"
            }
        }
    }

    private companion object {
        const val TAG = "KarangmulyaVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
