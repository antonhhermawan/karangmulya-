package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Aspiration
import com.example.data.model.AuditLog
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.VillagePost
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.KarangmulyaAmber
import com.example.ui.theme.KarangmulyaBlueDark
import com.example.ui.theme.KarangmulyaBlueDeep
import com.example.ui.theme.KarangmulyaBlueLight
import com.example.ui.theme.KarangmulyaBluePrimary
import com.example.ui.theme.KarangmulyaEmerald
import com.example.ui.theme.KarangmulyaEmeraldLight
import com.example.ui.theme.KarangmulyaRose
import com.example.ui.theme.KarangmulyaViolet
import com.example.util.DocumentExportUtil

enum class StaffTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Statistik", Icons.Default.Analytics),
    SURAT("Proses Surat", Icons.Default.Description),
    PENGADUAN("Aduan & Ide", Icons.Default.Campaign),
    WARGA_STAFF("Warga & RBAC", Icons.Default.Group),
    PUBLIKASI_AUDIT("Info & Audit", Icons.Default.History)
}

@Composable
fun StaffDashboardTab(
    citizens: List<CitizenRecord>,
    letters: List<LetterRequest>,
    complaints: List<Complaint>,
    aspirations: List<Aspiration>,
    posts: List<VillagePost>,
    auditLogs: List<AuditLog>,
    onExportPdfReport: () -> Unit,
    onExportCsvReport: () -> Unit,
    onNavigateStaffTab: (StaffTab) -> Unit
) {
    val verifiedCitizens = citizens.count { it.verificationStatus == "TERVERIFIKASI" }
    val pendingCitizens = citizens.count { it.verificationStatus == "MENUNGGU_VERIFIKASI" }
    val completedLetters = letters.count { it.status == "SELESAI" }
    val pendingLetters = letters.count { it.status == "DIAJUKAN" || it.status == "DIPROSES" || it.status == "DIVERIFIKASI" }
    val completedComplaints = complaints.count { it.status == "SELESAI" }
    val projects = posts.filter { it.postType == "PEMBANGUNAN" }
    val totalBudget = projects.sumOf { it.budgetAmount }
    val avgProgress = if (projects.isNotEmpty()) projects.map { it.progressPercent }.average().toInt() else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Executive Header & Report Exports (PDF / Excel CSV)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = KarangmulyaBlueDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Panel Staff",
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DASHBOARD EKSEKUTIF STAFF & APARATUR DESA",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF7DD3FC),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Statistik Pelayanan Real-Time Desa Karangmulya",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seluruh metrik dihitung langsung dari database aktif. Cetak laporan rekapitulasi resmi dalam format PDF atau Excel (CSV).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0F2FE)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExportPdfReport,
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_report_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Laporan PDF")
                        }
                        Button(
                            onClick = onExportCsvReport,
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_excel_report_button")
                        ) {
                            Icon(Icons.Default.TableView, contentDescription = "Excel CSV", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Excel")
                        }
                    }
                }
            }
        }

        // 2. 4 KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StaffKpiCard(
                    title = "Data Warga & Staff",
                    bigNumber = "${citizens.size}",
                    subtitle = "$verifiedCitizens Terverifikasi • $pendingCitizens Menunggu",
                    color = KarangmulyaBluePrimary,
                    onClick = { onNavigateStaffTab(StaffTab.WARGA_STAFF) },
                    modifier = Modifier.weight(1f)
                )
                StaffKpiCard(
                    title = "Permohonan Surat",
                    bigNumber = "${letters.size}",
                    subtitle = "$completedLetters Selesai • $pendingLetters Diproses",
                    color = KarangmulyaEmerald,
                    onClick = { onNavigateStaffTab(StaffTab.SURAT) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StaffKpiCard(
                    title = "Pengaduan Warga",
                    bigNumber = "${complaints.size}",
                    subtitle = "$completedComplaints Selesai Ditangani",
                    color = KarangmulyaAmber,
                    onClick = { onNavigateStaffTab(StaffTab.PENGADUAN) },
                    modifier = Modifier.weight(1f)
                )
                StaffKpiCard(
                    title = "Aspirasi & Pembangunan",
                    bigNumber = "${aspirations.size} Ide",
                    subtitle = "${projects.size} Proyek (Rata-rata $avgProgress%)",
                    color = KarangmulyaViolet,
                    onClick = { onNavigateStaffTab(StaffTab.PUBLIKASI_AUDIT) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Visual Service Distribution Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kinerja Pelayanan & Realisasi APBDes",
                        style = MaterialTheme.typography.titleMedium,
                        color = KarangmulyaBlueDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val letterRatio = if (letters.isNotEmpty()) completedLetters.toFloat() / letters.size else 1f
                    StatProgressRow(
                        label = "Penyelesaian Surat Administrasi Online",
                        rightText = "$completedLetters / ${letters.size} Surat",
                        progress = letterRatio,
                        color = KarangmulyaBluePrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val complaintRatio = if (complaints.isNotEmpty()) completedComplaints.toFloat() / complaints.size else 1f
                    StatProgressRow(
                        label = "Penyelesaian Pengaduan Masyarakat",
                        rightText = "$completedComplaints / ${complaints.size} Aduan",
                        progress = complaintRatio,
                        color = KarangmulyaAmber
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatProgressRow(
                        label = "Realisasi Fisik Pembangunan Desa (${DocumentExportUtil.formatRupiah(totalBudget)})",
                        rightText = "$avgProgress%",
                        progress = avgProgress / 100f,
                        color = KarangmulyaEmerald
                    )
                }
            }
        }

        // 4. Recent Audit Logs Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audit Log Aktivitas Terbaru (${auditLogs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = KarangmulyaBlueDeep
                )
                TextButton(onClick = { onNavigateStaffTab(StaffTab.PUBLIKASI_AUDIT) }) {
                    Text("Semua Log")
                }
            }
        }
        items(auditLogs.take(4), key = { it.id }) { log ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = KarangmulyaBlueLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = log.actionType,
                                tint = KarangmulyaBlueDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${log.actionType} • ${log.actorName} (${log.actorRole})",
                            style = MaterialTheme.typography.labelMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = log.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffKpiCard(
    title: String,
    bigNumber: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bigNumber,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatProgressRow(
    label: String,
    rightText: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = KarangmulyaBlueDeep)
            Text(text = rightText, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.16f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

// --- STAFF TAB 2: VERIFIKASI & PEMROSESAN SURAT ONLINE ---

@Composable
fun StaffLettersManagementTab(
    letters: List<LetterRequest>,
    onUpdateLetterStatus: (LetterRequest, String, String) -> Unit,
    onPreviewAndPrintPdf: (LetterRequest) -> Unit
) {
    var selectedLetterForEdit by remember { mutableStateOf<LetterRequest?>(null) }
    var filterStatus by remember { mutableStateOf("SEMUA") }
    val statuses = listOf("SEMUA", "DIAJUKAN", "DIVERIFIKASI", "DIPROSES", "SELESAI", "DITOLAK")
    val filtered = letters.filter { filterStatus == "SEMUA" || it.status == filterStatus }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Verifikasi & Pemrosesan Surat Online",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
            Text(
                text = "Periksa permohonan surat warga, berikan catatan tindak lanjut, perbarui status, dan terbitkan dokumen PDF resmi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { st ->
                    FilterChip(
                        selected = filterStatus == st,
                        onClick = { filterStatus = st },
                        label = { Text(st) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Tidak Ada Berkas Surat",
                    subtitle = "Belum ada permohonan surat pada filter status ini."
                )
            }
        } else {
            items(filtered, key = { it.id }) { srt ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = srt.regNumber,
                                style = MaterialTheme.typography.labelLarge,
                                color = KarangmulyaBluePrimary
                            )
                            StatusPillBadge(status = srt.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = srt.letterType,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pemohon: ${srt.applicantName} • NIK: ${srt.applicantNik}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keperluan: ${srt.purpose}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Berkas Lampiran: ${srt.attachmentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBluePrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Catatan Staff: ${srt.staffNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KarangmulyaBlueDeep,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onPreviewAndPrintPdf(srt) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cetak PDF")
                            }
                            Button(
                                onClick = { selectedLetterForEdit = srt },
                                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("process_letter_${srt.regNumber}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Proses", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verifikasi / Proses")
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedLetterForEdit != null) {
        val srt = selectedLetterForEdit!!
        val statusOptions = listOf("DIAJUKAN", "DIVERIFIKASI", "DIPROSES", "SELESAI", "DITOLAK")
        var newStatus by remember(srt) { mutableStateOf(srt.status) }
        var staffNote by remember(srt) { mutableStateOf(srt.staffNote) }

        AlertDialog(
            onDismissRequest = { selectedLetterForEdit = null },
            title = {
                Text("Proses Surat ${srt.regNumber}", style = MaterialTheme.typography.titleLarge, color = KarangmulyaBlueDeep)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${srt.letterType} — ${srt.applicantName}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text("Pilih Status Permohonan:", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        statusOptions.forEach { opt ->
                            FilterChip(
                                selected = newStatus == opt,
                                onClick = { newStatus = opt },
                                label = { Text(opt) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    OutlinedTextField(
                        value = staffNote,
                        onValueChange = { staffNote = it },
                        label = { Text("Tanggapan / Catatan Perangkat Desa") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateLetterStatus(srt, newStatus, staffNote)
                        selectedLetterForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLetterForEdit = null }) { Text("Batal") }
            }
        )
    }
}

// --- STAFF TAB 3: MANAJEMEN PENGADUAN & ASPIRASI WARGA ---

@Composable
fun StaffComplaintsAndAspirationsTab(
    complaints: List<Complaint>,
    aspirations: List<Aspiration>,
    onUpdateComplaint: (Complaint, String, String) -> Unit,
    onUpdateAspiration: (Aspiration, String, String, Long) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    var editingComplaint by remember { mutableStateOf<Complaint?>(null) }
    var editingAspiration by remember { mutableStateOf<Aspiration?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(KarangmulyaBlueLight)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Tindak Lanjut Pengaduan (${complaints.size})", "Kelola Aspirasi Warga (${aspirations.size})")
                    .forEachIndexed { idx, label ->
                        val selected = subTab == idx
                        Surface(
                            color = if (selected) KarangmulyaBluePrimary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { subTab = idx }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else KarangmulyaBlueDeep,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
            }
        }

        if (subTab == 0) {
            items(complaints, key = { it.id }) { adu ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${adu.regNumber} • Pelapor: ${adu.reporterName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaBluePrimary
                            )
                            StatusPillBadge(status = adu.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adu.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = adu.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lokasi: ${adu.locationName} (${"%.4f".format(adu.latitude)}, ${"%.4f".format(adu.longitude)}) • Foto: ${adu.photoLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = KarangmulyaAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tanggapan Saat Ini: ${adu.staffResponse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBlueDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { editingComplaint = adu },
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Tanggapi", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Beri Tanggapan & Perbarui Status")
                        }
                    }
                }
            }
        } else {
            items(aspirations, key = { it.id }) { asp ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${asp.regNumber} • ${asp.senderName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaEmerald
                            )
                            StatusPillBadge(status = asp.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = asp.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = asp.content, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tanggapan Desa: ${asp.staffFeedback}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBlueDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { editingAspiration = asp },
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Kelola", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verifikasi & Tanggapi Aspirasi")
                        }
                    }
                }
            }
        }
    }

    if (editingComplaint != null) {
        val adu = editingComplaint!!
        val statuses = listOf("MENUNGGU", "DITINDAKLANJUTI", "SELESAI", "DITOLAK")
        var status by remember(adu) { mutableStateOf(adu.status) }
        var response by remember(adu) { mutableStateOf(adu.staffResponse) }

        AlertDialog(
            onDismissRequest = { editingComplaint = null },
            title = { Text("Tindak Lanjut ${adu.regNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = response,
                        onValueChange = { response = it },
                        label = { Text("Tanggapan Resmi Tim Respon Desa") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateComplaint(adu, status, response)
                        editingComplaint = null
                    }
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { editingComplaint = null }) { Text("Batal") }
            }
        )
    }

    if (editingAspiration != null) {
        val asp = editingAspiration!!
        val statuses = listOf("DITERIMA", "DIKAJI_DESA", "DIREALISASIKAN")
        var status by remember(asp) { mutableStateOf(asp.status) }
        var feedback by remember(asp) { mutableStateOf(asp.staffFeedback) }

        AlertDialog(
            onDismissRequest = { editingAspiration = null },
            title = { Text("Tanggapi Aspirasi ${asp.regNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Tanggapan BPD / Pemerintah Desa") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateAspiration(asp, status, feedback, asp.upvotes)
                        editingAspiration = null
                    }
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { editingAspiration = null }) { Text("Batal") }
            }
        )
    }
}

// --- STAFF TAB 4: MANAJEMEN WARGA, VERIFIKASI & HAK AKSES STAFF (RBAC) ---

@Composable
fun StaffCitizensAndRbacTab(
    citizens: List<CitizenRecord>,
    onSaveRecord: (CitizenRecord?, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onDeleteRecord: (CitizenRecord) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterGroup by remember { mutableStateOf("SEMUA") }
    var showFullPii by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<CitizenRecord?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val groups = listOf("SEMUA", "WARGA", "STAFF_PELAYANAN", "KAUR_UMUM", "SEKRETARIS_DESA", "KEPALA_DESA")
    val filtered = citizens.filter { rec ->
        val matchesGroup = filterGroup == "SEMUA" || rec.directoryGroup == filterGroup
        val matchesQuery = searchQuery.isBlank() ||
            rec.fullName.contains(searchQuery, ignoreCase = true) ||
            rec.dusun.contains(searchQuery, ignoreCase = true) ||
            rec.nik.contains(searchQuery)
        matchesGroup && matchesQuery
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Manajemen Data Warga & Hak Akses Staff (RBAC)",
                                style = MaterialTheme.typography.titleMedium,
                                color = KarangmulyaBlueDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verifikasi akun warga baru, kelola perangkat desa, dan lindungi kerahasiaan NIK & KK.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showFullPii = !showFullPii }) {
                            Icon(
                                imageVector = if (showFullPii) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mask/Unmask NIK",
                                tint = KarangmulyaBluePrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            editingRecord = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_citizen_staff_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Data Warga / Akun Staff Desa")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama warga, NIK, atau dusun...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groups.forEach { grp ->
                    FilterChip(
                        selected = filterGroup == grp,
                        onClick = { filterGroup = grp },
                        label = { Text(grp.replace("_", " ")) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { rec ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (rec.directoryGroup == "WARGA") KarangmulyaBlueLight else Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = rec.directoryGroup.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = KarangmulyaBlueDeep,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        StatusPillBadge(status = rec.verificationStatus)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rec.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = KarangmulyaBlueDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "NIK: ${DocumentExportUtil.maskNik(rec.nik, showFullPii)} • KK: ${DocumentExportUtil.maskNik(rec.kkNumber, showFullPii)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${rec.dusun} (${rec.rtRw}) • ${rec.occupation} • ${rec.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (rec.verificationStatus != "TERVERIFIKASI") {
                            Button(
                                onClick = {
                                    onSaveRecord(
                                        rec,
                                        rec.fullName,
                                        rec.nik,
                                        rec.kkNumber,
                                        rec.rtRw,
                                        rec.dusun,
                                        rec.address,
                                        rec.phone,
                                        rec.occupation,
                                        rec.directoryGroup,
                                        "TERVERIFIKASI"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaEmerald),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Verifikasi", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verifikasi")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                editingRecord = rec
                                showAddDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit / Role")
                        }
                        if (!rec.isSelfProfile) {
                            IconButton(onClick = { onDeleteRecord(rec) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = KarangmulyaRose)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CitizenStaffEditorDialog(
            initial = editingRecord,
            onDismiss = { showAddDialog = false },
            onSave = { name, nik, kk, rtrw, dusun, addr, phone, job, group, verStatus ->
                onSaveRecord(editingRecord, name, nik, kk, rtrw, dusun, addr, phone, job, group, verStatus)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CitizenStaffEditorDialog(
    initial: CitizenRecord?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    val roles = listOf("WARGA", "STAFF_PELAYANAN", "KAUR_UMUM", "SEKRETARIS_DESA", "KEPALA_DESA")
    val statuses = listOf("TERVERIFIKASI", "MENUNGGU_VERIFIKASI", "DITOLAK")
    var fullName by remember(initial) { mutableStateOf(initial?.fullName ?: "") }
    var nik by remember(initial) { mutableStateOf(initial?.nik ?: "3205141234560001") }
    var kkNumber by remember(initial) { mutableStateOf(initial?.kkNumber ?: "3205146543210001") }
    var rtRw by remember(initial) { mutableStateOf(initial?.rtRw ?: "RT 01 / RW 01") }
    var dusun by remember(initial) { mutableStateOf(initial?.dusun ?: "Dusun Krajan") }
    var address by remember(initial) { mutableStateOf(initial?.address ?: "Jl. Desa Karangmulya") }
    var phone by remember(initial) { mutableStateOf(initial?.phone ?: "081234567890") }
    var occupation by remember(initial) { mutableStateOf(initial?.occupation ?: "Wiraswasta") }
    var directoryGroup by remember(initial) { mutableStateOf(initial?.directoryGroup ?: "WARGA") }
    var verificationStatus by remember(initial) { mutableStateOf(initial?.verificationStatus ?: "TERVERIFIKASI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Tambah Warga / Staff Desa" else "Kelola Data & Role ${initial.fullName}",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nik, onValueChange = { if (it.length <= 16) nik = it.filter { c -> c.isDigit() } }, label = { Text("NIK (16 Digit)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = kkNumber, onValueChange = { if (it.length <= 16) kkNumber = it.filter { c -> c.isDigit() } }, label = { Text("Nomor KK (16 Digit)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dusun, onValueChange = { dusun = it }, label = { Text("Dusun") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rtRw, onValueChange = { rtRw = it }, label = { Text("RT / RW") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat Lengkap") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor HP / WhatsApp") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Pekerjaan / Jabatan") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Hak Akses / Role Direktori:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.forEach { r ->
                        FilterChip(selected = directoryGroup == r, onClick = { directoryGroup = r }, label = { Text(r) })
                    }
                }

                Text("Status Verifikasi Kependudukan:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { st ->
                        FilterChip(selected = verificationStatus == st, onClick = { verificationStatus = st }, label = { Text(st) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.length >= 2) {
                        onSave(fullName, nik, kkNumber, rtRw, dusun, address, phone, occupation, directoryGroup, verificationStatus)
                    }
                },
                enabled = fullName.length >= 2
            ) {
                Text("Simpan Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// --- STAFF TAB 5: PUBLIKASI INFORMASI, PEMBANGUNAN & AUDIT LOG ---

@Composable
fun StaffPostsAndAuditTab(
    posts: List<VillagePost>,
    auditLogs: List<AuditLog>,
    onSavePost: (VillagePost?, String, String, String, String, String, String, String, Long, Long, String, String) -> Unit,
    onDeletePost: (VillagePost) -> Unit,
    onReloadDemoData: () -> Unit
) {
    var activeSection by remember { mutableIntStateOf(0) }
    var editingPost by remember { mutableStateOf<VillagePost?>(null) }
    var showPostDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(KarangmulyaBlueLight)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Kelola Info & Pembangunan (${posts.size})", "Audit Log & Pengaturan (${auditLogs.size})")
                    .forEachIndexed { idx, title ->
                        val selected = activeSection == idx
                        Surface(
                            color = if (selected) KarangmulyaBluePrimary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSection = idx }
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else KarangmulyaBlueDeep,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
            }
        }

        if (activeSection == 0) {
            item {
                Button(
                    onClick = {
                        editingPost = null
                        showPostDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_village_post_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Publikasi")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Berita, Pengumuman, Agenda, atau Proyek Pembangunan")
                }
            }

            items(posts, key = { it.id }) { post ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${post.postType} • ${post.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = post.statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = KarangmulyaEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = post.summary, style = MaterialTheme.typography.bodySmall)
                        if (post.postType == "PEMBANGUNAN") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pagu Anggaran: ${DocumentExportUtil.formatRupiah(post.budgetAmount)} • Progres Fisik: ${post.progressPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editingPost = post
                                    showPostDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit / Update Progres")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onDeletePost(post) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = KarangmulyaRose)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pengaturan Sistem & Sinkronisasi Database",
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seluruh aktivitas pelayanan tercatat otomatis pada Audit Log yang bersifat immutable (tidak dapat diubah/dihapus).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onReloadDemoData,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Muat Ulang Data Simulasi Desa Karangmulya")
                        }
                    }
                }
            }

            items(auditLogs, key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.actionType,
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.targetRef,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = KarangmulyaBlueDeep
                        )
                        Text(
                            text = "Pelaksana: ${log.actorName} (${log.actorRole}) • ${DocumentExportUtil.formatTimestamp(log.createdAt?.toDate())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        VillagePostEditorDialog(
            initial = editingPost,
            onDismiss = { showPostDialog = false },
            onSave = { pType, title, cat, sum, content, date, loc, budget, prog, statusLbl, author ->
                onSavePost(editingPost, pType, title, cat, sum, content, date, loc, budget, prog, statusLbl, author)
                showPostDialog = false
            }
        )
    }
}

@Composable
fun VillagePostEditorDialog(
    initial: VillagePost?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Long, Long, String, String) -> Unit
) {
    val postTypes = listOf("PENGUMUMAN", "BERITA", "AGENDA", "PEMBANGUNAN")
    var postType by remember(initial) { mutableStateOf(initial?.postType ?: "PENGUMUMAN") }
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var category by remember(initial) { mutableStateOf(initial?.category ?: "Layanan Desa") }
    var summary by remember(initial) { mutableStateOf(initial?.summary ?: "") }
    var content by remember(initial) { mutableStateOf(initial?.content ?: "") }
    var eventDate by remember(initial) { mutableStateOf(initial?.eventDate ?: "Oktober 2026") }
    var location by remember(initial) { mutableStateOf(initial?.location ?: "Balai Desa Karangmulya") }
    var budgetStr by remember(initial) { mutableStateOf((initial?.budgetAmount ?: 0L).toString()) }
    var progressStr by remember(initial) { mutableStateOf((initial?.progressPercent ?: 100L).toString()) }
    var statusLabel by remember(initial) { mutableStateOf(initial?.statusLabel ?: "Aktif") }
    var authorName by remember(initial) { mutableStateOf(initial?.authorName ?: "Pemdes Karangmulya") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Publikasi Informasi / Pembangunan" else "Edit Publikasi Desa",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    postTypes.forEach { t ->
                        FilterChip(selected = postType == t, onClick = { postType = t }, label = { Text(t) })
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Ringkasan Singkat") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Uraian Lengkap / Spesifikasi Proyek") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Tanggal / Tahun Anggaran") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi Dusun / Kegiatan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (postType == "PEMBANGUNAN") {
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Pagu Anggaran (Rp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = progressStr,
                        onValueChange = { progressStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Realisasi Fisik (0 - 100%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(value = statusLabel, onValueChange = { statusLabel = it }, label = { Text("Label Status") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.length >= 3 && summary.length >= 3 && content.length >= 3) {
                        onSave(
                            postType,
                            title,
                            category,
                            summary,
                            content,
                            eventDate,
                            location,
                            budgetStr.toLongOrNull() ?: 0L,
                            (progressStr.toLongOrNull() ?: 100L).coerceIn(0L, 100L),
                            statusLabel,
                            authorName
                        )
                    }
                },
                enabled = title.length >= 3 && summary.length >= 3 && content.length >= 3
            ) {
                Text("Publikasikan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
