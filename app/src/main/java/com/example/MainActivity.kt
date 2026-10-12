package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.AppNotification
import com.example.data.model.Aspiration
import com.example.data.model.AuditLog
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.UiState
import com.example.data.model.VillagePost
import com.example.data.repository.KarangmulyaRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutFromApp
import com.example.ui.staff.CitizenStaffEditorDialog
import com.example.ui.staff.StaffCitizensAndRbacTab
import com.example.ui.staff.StaffComplaintsAndAspirationsTab
import com.example.ui.staff.StaffDashboardTab
import com.example.ui.staff.StaffLettersManagementTab
import com.example.ui.staff.StaffPostsAndAuditTab
import com.example.ui.staff.StaffTab
import com.example.ui.theme.KarangmulyaBlueDeep
import com.example.ui.theme.KarangmulyaBlueLight
import com.example.ui.theme.KarangmulyaBluePrimary
import com.example.ui.theme.KarangmulyaEmerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppInterfaceMode
import com.example.ui.viewmodel.KarangmulyaViewModel
import com.example.ui.warga.LetterPdfAndQrPreviewDialog
import com.example.ui.warga.NewAspirationDialog
import com.example.ui.warga.NewComplaintDialog
import com.example.ui.warga.NewLetterRequestDialog
import com.example.ui.warga.QrStatusLookupDialog
import com.example.ui.warga.WargaComplaintsAndAspirationsTab
import com.example.ui.warga.WargaHomeTab
import com.example.ui.warga.WargaLettersTab
import com.example.ui.warga.WargaProfileAndHelpTab
import com.example.ui.warga.WargaTab
import com.example.ui.warga.WargaVillageInfoTab
import com.example.util.DocumentExportUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRootNavigation()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppRootNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        MarkasRakyatMainScreen(
            currentUserId = user.uid,
            currentUserDisplayName = user.displayName ?: user.email?.substringBefore("@") ?: "Warga Karangmulya"
        )
    }
}

@Composable
fun MarkasRakyatMainScreen(
    currentUserId: String,
    currentUserDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: KarangmulyaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KarangmulyaViewModel(
                    repository = KarangmulyaRepository(db),
                    currentUserId = currentUserId,
                    currentUserDisplayName = currentUserDisplayName
                )
            }
        }
    )

    val interfaceMode by viewModel.interfaceMode.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val citizenState by viewModel.citizenRecordsState.collectAsStateWithLifecycle()
    val letterState by viewModel.letterRequestsState.collectAsStateWithLifecycle()
    val complaintState by viewModel.complaintsState.collectAsStateWithLifecycle()
    val aspirationState by viewModel.aspirationsState.collectAsStateWithLifecycle()
    val postState by viewModel.villagePostsState.collectAsStateWithLifecycle()
    val notificationState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val auditLogState by viewModel.auditLogsState.collectAsStateWithLifecycle()

    val citizens = (citizenState as? UiState.Success<List<CitizenRecord>>)?.data ?: emptyList()
    val letters = (letterState as? UiState.Success<List<LetterRequest>>)?.data ?: emptyList()
    val complaints = (complaintState as? UiState.Success<List<Complaint>>)?.data ?: emptyList()
    val aspirations = (aspirationState as? UiState.Success<List<Aspiration>>)?.data ?: emptyList()
    val posts = (postState as? UiState.Success<List<VillagePost>>)?.data ?: emptyList()
    val notifications = (notificationState as? UiState.Success<List<AppNotification>>)?.data ?: emptyList()
    val auditLogs = (auditLogState as? UiState.Success<List<AuditLog>>)?.data ?: emptyList()

    val selfProfile = citizens.find { it.isSelfProfile } ?: citizens.firstOrNull()

    var currentWargaTab by remember { mutableStateOf(WargaTab.BERANDA) }
    var currentStaffTab by remember { mutableStateOf(StaffTab.DASHBOARD) }

    // Modal Dialog States
    var showNewLetterDialog by remember { mutableStateOf(false) }
    var showNewComplaintDialog by remember { mutableStateOf(false) }
    var showNewAspirationDialog by remember { mutableStateOf(false) }
    var showQrLookupDialog by remember { mutableStateOf(false) }
    var showEditSelfProfileDialog by remember { mutableStateOf(false) }
    var selectedLetterForPdf by remember { mutableStateOf<LetterRequest?>(null) }

    // Handle Back navigation on secondary tabs or staff mode
    BackHandler(
        enabled = interfaceMode == AppInterfaceMode.STAFF_DESA || currentWargaTab != WargaTab.BERANDA
    ) {
        if (interfaceMode == AppInterfaceMode.STAFF_DESA) {
            if (currentStaffTab != StaffTab.DASHBOARD) {
                currentStaffTab = StaffTab.DASHBOARD
            } else {
                viewModel.setInterfaceMode(AppInterfaceMode.WARGA)
            }
        } else {
            currentWargaTab = WargaTab.BERANDA
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                MarkasRakyatTopHeader(
                    interfaceMode = interfaceMode,
                    onSwitchMode = { newMode -> viewModel.setInterfaceMode(newMode) },
                    onOpenQrScanner = { showQrLookupDialog = true },
                    onSignOut = {
                        signOutFromApp(
                            credentialManager = credentialManager,
                            onSignOutComplete = {},
                            scope = scope
                        )
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    if (interfaceMode == AppInterfaceMode.WARGA) {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 6.dp
                        ) {
                            WargaTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentWargaTab == tab,
                                    onClick = { currentWargaTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = KarangmulyaBluePrimary,
                                        selectedTextColor = KarangmulyaBlueDeep,
                                        indicatorColor = KarangmulyaBlueLight
                                    ),
                                    modifier = Modifier.testTag("nav_warga_${tab.name.lowercase()}")
                                )
                            }
                        }
                    } else {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 6.dp
                        ) {
                            StaffTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentStaffTab == tab,
                                    onClick = { currentStaffTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = KarangmulyaBluePrimary,
                                        selectedTextColor = KarangmulyaBlueDeep,
                                        indicatorColor = KarangmulyaBlueLight
                                    ),
                                    modifier = Modifier.testTag("nav_staff_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        if (interfaceMode == AppInterfaceMode.WARGA) {
                            WargaTab.entries.forEach { tab ->
                                NavigationRailItem(
                                    selected = currentWargaTab == tab,
                                    onClick = { currentWargaTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        } else {
                            StaffTab.entries.forEach { tab ->
                                NavigationRailItem(
                                    selected = currentStaffTab == tab,
                                    onClick = { currentStaffTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (isBusy) {
                        LinearProgressIndicator(
                            color = KarangmulyaBluePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AnimatedVisibility(visible = bannerMessage != null) {
                        bannerMessage?.let { msg ->
                            Surface(
                                color = KarangmulyaBlueDeep,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearBannerMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tutup pesan",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (interfaceMode == AppInterfaceMode.WARGA) {
                            when (currentWargaTab) {
                                WargaTab.BERANDA -> WargaHomeTab(
                                    selfProfile = selfProfile,
                                    letters = letters,
                                    complaints = complaints,
                                    aspirations = aspirations,
                                    posts = posts,
                                    notifications = notifications,
                                    onNavigateTab = { currentWargaTab = it },
                                    onOpenNewLetter = { showNewLetterDialog = true },
                                    onOpenNewComplaint = { showNewComplaintDialog = true },
                                    onOpenNewAspiration = { showNewAspirationDialog = true },
                                    onOpenQrLookup = { showQrLookupDialog = true },
                                    onSelectLetterForPdf = { selectedLetterForPdf = it }
                                )
                                WargaTab.SURAT -> WargaLettersTab(
                                    selfProfile = selfProfile,
                                    letters = letters,
                                    onOpenNewLetter = { showNewLetterDialog = true },
                                    onSelectLetterForPdf = { selectedLetterForPdf = it }
                                )
                                WargaTab.PENGADUAN -> WargaComplaintsAndAspirationsTab(
                                    complaints = complaints,
                                    aspirations = aspirations,
                                    onOpenNewComplaint = { showNewComplaintDialog = true },
                                    onOpenNewAspiration = { showNewAspirationDialog = true },
                                    onUpvoteAspiration = { asp ->
                                        viewModel.updateAspiration(
                                            aspirationId = asp.id,
                                            regNumber = asp.regNumber,
                                            newStatus = asp.status,
                                            staffFeedback = asp.staffFeedback,
                                            upvotes = asp.upvotes + 1L
                                        )
                                    }
                                )
                                WargaTab.INFORMASI -> WargaVillageInfoTab(posts = posts)
                                WargaTab.PROFIL -> WargaProfileAndHelpTab(
                                    selfProfile = selfProfile,
                                    notifications = notifications,
                                    onEditProfile = { showEditSelfProfileDialog = true },
                                    onMarkNotificationRead = { viewModel.markNotificationRead(it) }
                                )
                            }
                        } else {
                            when (currentStaffTab) {
                                StaffTab.DASHBOARD -> StaffDashboardTab(
                                    citizens = citizens,
                                    letters = letters,
                                    complaints = complaints,
                                    aspirations = aspirations,
                                    posts = posts,
                                    auditLogs = auditLogs,
                                    onExportPdfReport = {
                                        val res = DocumentExportUtil.generateAndShareReportPdf(
                                            context = context,
                                            letters = letters,
                                            complaints = complaints,
                                            aspirations = aspirations,
                                            posts = posts,
                                            citizens = citizens
                                        )
                                        res.onSuccess { f ->
                                            viewModel.showMessage("Laporan PDF berhasil dibuat: ${f.name}")
                                        }
                                    },
                                    onExportCsvReport = {
                                        val res = DocumentExportUtil.generateAndShareReportCsv(
                                            context = context,
                                            letters = letters,
                                            complaints = complaints,
                                            citizens = citizens
                                        )
                                        res.onSuccess { f ->
                                            viewModel.showMessage("Laporan Excel/CSV berhasil dibuat: ${f.name}")
                                        }
                                    },
                                    onNavigateStaffTab = { currentStaffTab = it }
                                )
                                StaffTab.SURAT -> StaffLettersManagementTab(
                                    letters = letters,
                                    onUpdateLetterStatus = { srt, newSt, note ->
                                        viewModel.updateLetterStatus(srt.id, srt.regNumber, srt.letterType, newSt, note)
                                    },
                                    onPreviewAndPrintPdf = { selectedLetterForPdf = it }
                                )
                                StaffTab.PENGADUAN -> StaffComplaintsAndAspirationsTab(
                                    complaints = complaints,
                                    aspirations = aspirations,
                                    onUpdateComplaint = { adu, st, resp ->
                                        viewModel.updateComplaintStatus(adu.id, adu.regNumber, adu.title, st, resp)
                                    },
                                    onUpdateAspiration = { asp, st, fb, up ->
                                        viewModel.updateAspiration(asp.id, asp.regNumber, st, fb, up)
                                    }
                                )
                                StaffTab.WARGA_STAFF -> StaffCitizensAndRbacTab(
                                    citizens = citizens,
                                    onSaveRecord = { existing, name, nik, kk, rtrw, dusun, addr, phone, job, grp, ver ->
                                        viewModel.saveCitizenOrStaffRecord(
                                            recordId = existing?.id,
                                            fullName = name,
                                            nik = nik,
                                            kkNumber = kk,
                                            rtRw = rtrw,
                                            dusun = dusun,
                                            address = addr,
                                            phone = phone,
                                            occupation = job,
                                            directoryGroup = grp,
                                            verificationStatus = ver,
                                            isSelfProfile = existing?.isSelfProfile ?: false
                                        )
                                    },
                                    onDeleteRecord = { rec ->
                                        viewModel.deleteCitizenRecord(rec.id, rec.fullName)
                                    }
                                )
                                StaffTab.PUBLIKASI_AUDIT -> StaffPostsAndAuditTab(
                                    posts = posts,
                                    auditLogs = auditLogs,
                                    onSavePost = { existing, pType, title, cat, sum, content, date, loc, budget, prog, statusLbl, author ->
                                        viewModel.saveVillagePost(
                                            postId = existing?.id,
                                            postType = pType,
                                            title = title,
                                            category = cat,
                                            summary = sum,
                                            content = content,
                                            eventDate = date,
                                            location = loc,
                                            budgetAmount = budget,
                                            progressPercent = prog,
                                            statusLabel = statusLbl,
                                            authorName = author
                                        )
                                    },
                                    onDeletePost = { pst ->
                                        viewModel.deleteVillagePost(pst.id, pst.title)
                                    },
                                    onReloadDemoData = { viewModel.reloadDemoData() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Modal Dialogs
    if (showNewLetterDialog) {
        NewLetterRequestDialog(
            defaultName = selfProfile?.fullName ?: currentUserDisplayName,
            defaultNik = selfProfile?.nik ?: "3205142809940001",
            onDismiss = { showNewLetterDialog = false },
            onSubmit = { name, nik, lType, purpose, attach ->
                viewModel.submitLetterRequest(name, nik, lType, purpose, attach)
            }
        )
    }

    if (showNewComplaintDialog) {
        NewComplaintDialog(
            defaultReporter = selfProfile?.fullName ?: currentUserDisplayName,
            onDismiss = { showNewComplaintDialog = false },
            onSubmit = { rep, cat, title, desc, loc, lat, lng, photo ->
                viewModel.submitComplaint(rep, cat, title, desc, loc, lat, lng, photo)
            }
        )
    }

    if (showNewAspirationDialog) {
        NewAspirationDialog(
            defaultSender = selfProfile?.fullName ?: currentUserDisplayName,
            onDismiss = { showNewAspirationDialog = false },
            onSubmit = { sender, cat, title, content ->
                viewModel.submitAspiration(sender, cat, title, content)
            }
        )
    }

    if (showQrLookupDialog) {
        QrStatusLookupDialog(
            letters = letters,
            complaints = complaints,
            aspirations = aspirations,
            onDismiss = { showQrLookupDialog = false }
        )
    }

    if (selectedLetterForPdf != null) {
        LetterPdfAndQrPreviewDialog(
            letter = selectedLetterForPdf!!,
            onDismiss = { selectedLetterForPdf = null }
        )
    }

    if (showEditSelfProfileDialog) {
        CitizenStaffEditorDialog(
            initial = selfProfile,
            onDismiss = { showEditSelfProfileDialog = false },
            onSave = { name, nik, kk, rtrw, dusun, addr, phone, job, grp, ver ->
                viewModel.saveCitizenOrStaffRecord(
                    recordId = selfProfile?.id,
                    fullName = name,
                    nik = nik,
                    kkNumber = kk,
                    rtRw = rtrw,
                    dusun = dusun,
                    address = addr,
                    phone = phone,
                    occupation = job,
                    directoryGroup = grp,
                    verificationStatus = ver,
                    isSelfProfile = true
                )
            }
        )
    }
}

@Composable
private fun MarkasRakyatTopHeader(
    interfaceMode: AppInterfaceMode,
    onSwitchMode: (AppInterfaceMode) -> Unit,
    onOpenQrScanner: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = KarangmulyaBluePrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Logo Desa Karangmulya",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MARKAS RAKYAT",
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "DESA KARANGMULYA",
                            style = MaterialTheme.typography.labelSmall,
                            color = KarangmulyaBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenQrScanner,
                        modifier = Modifier.testTag("top_qr_lookup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Cek Status QR",
                            tint = KarangmulyaBluePrimary
                        )
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Keluar Akun",
                            tint = KarangmulyaBlueDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dual Interface Switcher Pill Bar (1.A Interface Warga vs 1.B Interface Staff Desa)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isWarga = interfaceMode == AppInterfaceMode.WARGA
                Surface(
                    color = if (isWarga) KarangmulyaBluePrimary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSwitchMode(AppInterfaceMode.WARGA) }
                        .testTag("switch_interface_warga")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Interface Warga",
                            tint = if (isWarga) Color.White else KarangmulyaBlueDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interface Warga",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isWarga) Color.White else KarangmulyaBlueDeep
                        )
                    }
                }

                val isStaff = interfaceMode == AppInterfaceMode.STAFF_DESA
                Surface(
                    color = if (isStaff) KarangmulyaBlueDeep else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSwitchMode(AppInterfaceMode.STAFF_DESA) }
                        .testTag("switch_interface_staff")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Interface Staff Desa",
                            tint = if (isStaff) Color.White else KarangmulyaBlueDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interface Staff Desa",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isStaff) Color.White else KarangmulyaBlueDeep
                        )
                    }
                }
            }
        }
    }
}
