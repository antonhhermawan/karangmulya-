package com.example.ui.warga

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.Aspiration
import com.example.data.model.CitizenRecord
import com.example.data.model.Complaint
import com.example.data.model.LetterRequest
import com.example.data.model.VillagePost
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QrCodeMatrixView
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.KarangmulyaAmber
import com.example.ui.theme.KarangmulyaBlueDark
import com.example.ui.theme.KarangmulyaBlueDeep
import com.example.ui.theme.KarangmulyaBlueLight
import com.example.ui.theme.KarangmulyaBluePrimary
import com.example.ui.theme.KarangmulyaEmerald
import com.example.ui.theme.KarangmulyaEmeraldLight
import com.example.ui.theme.KarangmulyaViolet
import com.example.ui.viewmodel.KarangmulyaViewModel
import com.example.util.DocumentExportUtil

enum class WargaTab(val label: String, val icon: ImageVector) {
    BERANDA("Beranda", Icons.Default.Newspaper),
    SURAT("Surat Online", Icons.Default.Description),
    PENGADUAN("Pengaduan", Icons.Default.Campaign),
    INFORMASI("Info Desa", Icons.Default.Construction),
    PROFIL("Profil & Bantuan", Icons.Default.Badge)
}

@Composable
fun WargaHomeTab(
    selfProfile: CitizenRecord?,
    letters: List<LetterRequest>,
    complaints: List<Complaint>,
    aspirations: List<Aspiration>,
    posts: List<VillagePost>,
    notifications: List<AppNotification>,
    onNavigateTab: (WargaTab) -> Unit,
    onOpenNewLetter: () -> Unit,
    onOpenNewComplaint: () -> Unit,
    onOpenNewAspiration: () -> Unit,
    onOpenQrLookup: () -> Unit,
    onSelectLetterForPdf: (LetterRequest) -> Unit
) {
    val unreadNotifCount = notifications.count { !it.isRead }
    val announcements = posts.filter { it.postType == "PENGUMUMAN" || it.postType == "BERITA" }.take(3)
    val agendas = posts.filter { it.postType == "AGENDA" }.take(2)
    val projects = posts.filter { it.postType == "PEMBANGUNAN" }.take(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_karangmulya_1791798379810),
                        contentDescription = "Balai Desa Karangmulya",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x440C4A6E),
                                        Color(0xE60C4A6E)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPillBadge(status = selfProfile?.verificationStatus ?: "TERVERIFIKASI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selfProfile?.dusun ?: "Dusun Krajan",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE0F2FE)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Halo, ${selfProfile?.fullName ?: "Warga Karangmulya"}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pelayanan Mudah, Informasi Terbuka, Desa Bersama Rakyat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0F2FE)
                        )
                    }
                }
            }
        }

        // 2. Quick Summary Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatCard(
                    label = "Surat Online",
                    value = "${letters.size}",
                    accent = KarangmulyaBluePrimary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(WargaTab.SURAT) }
                )
                MiniStatCard(
                    label = "Pengaduan",
                    value = "${complaints.size}",
                    accent = KarangmulyaAmber,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(WargaTab.PENGADUAN) }
                )
                MiniStatCard(
                    label = "Aspirasi",
                    value = "${aspirations.size}",
                    accent = KarangmulyaEmerald,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(WargaTab.PENGADUAN) }
                )
                MiniStatCard(
                    label = "Notifikasi",
                    value = "$unreadNotifCount Baru",
                    accent = KarangmulyaViolet,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(WargaTab.PROFIL) }
                )
            }
        }

        // 3. Layanan Cepat Warga (Quick Action Grid)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Layanan Mandiri Warga Desa",
                        style = MaterialTheme.typography.titleMedium,
                        color = KarangmulyaBlueDeep
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickServiceButton(
                            icon = Icons.Default.Description,
                            title = "Ajukan Surat",
                            subtitle = "SKD, SKU, SKTM",
                            color = KarangmulyaBluePrimary,
                            onClick = onOpenNewLetter,
                            testTag = "quick_ajukan_surat",
                            modifier = Modifier.weight(1f)
                        )
                        QuickServiceButton(
                            icon = Icons.Default.Campaign,
                            title = "Lapor Aduan",
                            subtitle = "Foto & GPS Lokasi",
                            color = KarangmulyaAmber,
                            onClick = onOpenNewComplaint,
                            testTag = "quick_lapor_aduan",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickServiceButton(
                            icon = Icons.Default.Forum,
                            title = "Kirim Aspirasi",
                            subtitle = "Usulan & Saran",
                            color = KarangmulyaEmerald,
                            onClick = onOpenNewAspiration,
                            testTag = "quick_kirim_aspirasi",
                            modifier = Modifier.weight(1f)
                        )
                        QuickServiceButton(
                            icon = Icons.Default.QrCode2,
                            title = "Lacak Status / QR",
                            subtitle = "Cek No. Registrasi",
                            color = KarangmulyaViolet,
                            onClick = onOpenQrLookup,
                            testTag = "quick_lacak_qr",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Pelacakan Permohonan Surat Terakhir
        if (letters.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status Pengajuan Surat Terbaru",
                        style = MaterialTheme.typography.titleMedium,
                        color = KarangmulyaBlueDeep
                    )
                    TextButton(onClick = { onNavigateTab(WargaTab.SURAT) }) {
                        Text("Lihat Semua")
                    }
                }
            }
            items(letters.take(2), key = { it.id }) { srt ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = srt.regNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaBluePrimary
                            )
                            StatusPillBadge(status = srt.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = srt.letterType,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = srt.purpose,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catatan: ${srt.staffNote.take(45)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = KarangmulyaBlueDark,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onSelectLetterForPdf(srt) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Cetak PDF",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Surat PDF & QR", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // 5. Pengumuman & Berita Desa
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pengumuman & Berita Desa",
                    style = MaterialTheme.typography.titleMedium,
                    color = KarangmulyaBlueDeep
                )
                TextButton(onClick = { onNavigateTab(WargaTab.INFORMASI) }) {
                    Text("Selengkapnya")
                }
            }
        }
        items(announcements, key = { it.id }) { post ->
            VillagePostSummaryCard(post = post)
        }

        // 6. Agenda Kegiatan & Transparansi Pembangunan Desa
        if (agendas.isNotEmpty() || projects.isNotEmpty()) {
            item {
                Text(
                    text = "Agenda & Pembangunan Desa Karangmulya",
                    style = MaterialTheme.typography.titleMedium,
                    color = KarangmulyaBlueDeep
                )
            }
            items(agendas + projects, key = { it.id }) { post ->
                VillagePostSummaryCard(post = post)
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickServiceButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.09f),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = KarangmulyaBlueDeep
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WargaLettersTab(
    selfProfile: CitizenRecord?,
    letters: List<LetterRequest>,
    onOpenNewLetter: () -> Unit,
    onSelectLetterForPdf: (LetterRequest) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("SEMUA") }
    val statusFilters = listOf("SEMUA", "DIAJUKAN", "DIVERIFIKASI", "DIPROSES", "SELESAI", "DITOLAK")

    val filtered = letters.filter { srt ->
        val matchesStatus = selectedStatus == "SEMUA" || srt.status == selectedStatus
        val matchesQuery = searchQuery.isBlank() ||
            srt.letterType.contains(searchQuery, ignoreCase = true) ||
            srt.regNumber.contains(searchQuery, ignoreCase = true) ||
            srt.purpose.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesQuery
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KarangmulyaBluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pelayanan Surat Administrasi Online",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ajukan SKD, SKU, SKTM, Pengantar SKCK, atau Surat Keterangan Desa dari rumah. Dilengkapi QR Code & Unduh PDF Resmi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0F2FE)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onOpenNewLetter,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = KarangmulyaBlueDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_letter_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Buat Surat")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajukan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nomor registrasi atau jenis surat...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_letter_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusFilters.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KarangmulyaBlueLight,
                            selectedLabelColor = KarangmulyaBlueDeep
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Belum Ada Permohonan Surat",
                    subtitle = "Tekan tombol 'Ajukan' di atas untuk membuat permohonan surat administrasi desa secara online."
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
                            Surface(
                                color = KarangmulyaBlueLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = srt.regNumber,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KarangmulyaBlueDeep,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            StatusPillBadge(status = srt.status)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = srt.letterType,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pemohon: ${srt.applicantName} • NIK: ${DocumentExportUtil.maskNik(srt.applicantNik, false)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Keperluan: ${srt.purpose}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (srt.attachmentName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Lampiran",
                                    tint = KarangmulyaBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dokumen: ${srt.attachmentName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KarangmulyaBluePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Tanggapan / Catatan Perangkat Desa:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KarangmulyaBlueDeep
                                )
                                Text(
                                    text = srt.staffNote.ifBlank { "Menunggu verifikasi petugas pelayanan." },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onSelectLetterForPdf(srt) },
                                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("download_pdf_${srt.regNumber}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Unduh Surat PDF",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau QR & Unduh Surat PDF")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WargaComplaintsAndAspirationsTab(
    complaints: List<Complaint>,
    aspirations: List<Aspiration>,
    onOpenNewComplaint: () -> Unit,
    onOpenNewAspiration: () -> Unit,
    onUpvoteAspiration: (Aspiration) -> Unit
) {
    var activeSubTab by remember { mutableIntStateOf(0) }

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
                val tabs = listOf("Pengaduan Masyarakat (${complaints.size})", "Aspirasi & Saran (${aspirations.size})")
                tabs.forEachIndexed { index, title ->
                    val selected = activeSubTab == index
                    Surface(
                        color = if (selected) KarangmulyaBluePrimary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = index }
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

        if (activeSubTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saluran Pengaduan Masyarakat",
                                style = MaterialTheme.typography.titleMedium,
                                color = KarangmulyaBlueDeep
                            )
                            Text(
                                text = "Laporkan kerusakan jalan, drainase, lampu PJU, atau pelayanan publik beserta foto bukti dan titik lokasi GPS.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onOpenNewComplaint,
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_complaint_button")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Lapor")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lapor Aduan")
                        }
                    }
                }
            }

            if (complaints.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Belum Ada Pengaduan",
                        subtitle = "Gunakan tombol 'Lapor Aduan' untuk mengirim laporan masyarakat beserta foto dan lokasi."
                    )
                }
            } else {
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
                                    text = "${adu.regNumber} • ${adu.category}",
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adu.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Lokasi",
                                    tint = KarangmulyaAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${adu.locationName} (GPS: ${"%.4f".format(adu.latitude)}, ${"%.4f".format(adu.longitude)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (adu.photoLabel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Foto",
                                        tint = KarangmulyaBluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lampiran Foto: ${adu.photoLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KarangmulyaBluePrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = KarangmulyaBlueLight.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Tindak Lanjut Pemerintah Desa:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KarangmulyaBlueDeep
                                    )
                                    Text(
                                        text = adu.staffResponse,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KarangmulyaBlueDeep
                                    )
                                }
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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aspirasi, Kritik & Saran Pembangunan",
                                style = MaterialTheme.typography.titleMedium,
                                color = KarangmulyaBlueDeep
                            )
                            Text(
                                text = "Sampaikan gagasan terbaik untuk kemajuan Desa Karangmulya dalam Musrenbangdes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onOpenNewAspiration,
                            colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_aspiration_button")
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = "Kirim Aspirasi")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Ide")
                        }
                    }
                }
            }

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
                                text = "${asp.regNumber} • ${asp.category}",
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
                        Text(
                            text = "Oleh: ${asp.senderName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = asp.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tanggapan BPD/Desa: ${asp.staffFeedback.take(55)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = KarangmulyaBlueDark,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onUpvoteAspiration(asp) },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Dukung Aspirasi",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${asp.upvotes} Dukungan")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WargaVillageInfoTab(
    posts: List<VillagePost>
) {
    var selectedType by remember { mutableStateOf("SEMUA") }
    val types = listOf("SEMUA", "PENGUMUMAN", "BERITA", "AGENDA", "PEMBANGUNAN")

    val filtered = posts.filter { selectedType == "SEMUA" || it.postType == selectedType }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Keterbukaan Informasi & Pembangunan Desa",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
            Text(
                text = "Pantau berita resmi, pengumuman, jadwal agenda kegiatan, serta transparansi anggaran & fisik pembangunan Desa Karangmulya.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = { selectedType = t },
                        label = { Text(t) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KarangmulyaBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { post ->
            VillagePostSummaryCard(post = post)
        }
    }
}

@Composable
fun VillagePostSummaryCard(post: VillagePost) {
    val isProject = post.postType == "PEMBANGUNAN"
    val isAgenda = post.postType == "AGENDA"

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
                    color = when (post.postType) {
                        "PEMBANGUNAN" -> KarangmulyaEmeraldLight
                        "AGENDA" -> Color(0xFFFEF3C7)
                        else -> KarangmulyaBlueLight
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${post.postType} • ${post.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KarangmulyaBlueDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = post.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = KarangmulyaBluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = KarangmulyaBlueDeep,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isAgenda || post.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Jadwal",
                        tint = KarangmulyaBluePrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.eventDate} • ${post.location}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KarangmulyaBlueDeep
                    )
                }
            }

            if (isProject) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Anggaran: ${DocumentExportUtil.formatRupiah(post.budgetAmount)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Realisasi Fisik: ${post.progressPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = KarangmulyaBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (post.progressPercent.coerceIn(0L, 100L)) / 100f },
                            color = KarangmulyaEmerald,
                            trackColor = Color(0xFFD1FAE5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WargaProfileAndHelpTab(
    selfProfile: CitizenRecord?,
    notifications: List<AppNotification>,
    onEditProfile: () -> Unit,
    onMarkNotificationRead: (String) -> Unit
) {
    var showFullNik by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Digital Citizen Identity Card (Kartu Warga Digital Karangmulya)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = KarangmulyaBlueDeep),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KARTU WARGA DIGITAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7DD3FC)
                            )
                            Text(
                                text = "DESA KARANGMULYA",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusPillBadge(status = selfProfile?.verificationStatus ?: "TERVERIFIKASI")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selfProfile?.fullName ?: "Warga Karangmulya",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NIK: ${DocumentExportUtil.maskNik(selfProfile?.nik ?: "3205142809940001", showFullNik)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE0F2FE)
                                )
                                IconButton(
                                    onClick = { showFullNik = !showFullNik },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showFullNik) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Tampilkan/Sembunyikan NIK",
                                        tint = Color(0xFF7DD3FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "No. KK: ${DocumentExportUtil.maskNik(selfProfile?.kkNumber ?: "3205140501200004", showFullNik)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE0F2FE)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selfProfile?.dusun ?: "Dusun Krajan"} • ${selfProfile?.rtRw ?: "RT 02 / RW 01"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                            Text(
                                text = "${selfProfile?.occupation ?: "Wiraswasta"} • ${selfProfile?.phone ?: "081288994411"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        QrCodeMatrixView(
                            data = "WARGA-${selfProfile?.nik ?: "320514"}",
                            qrSize = 92.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onEditProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KarangmulyaBluePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Perbarui Profil", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perbarui Data Profil & Kependudukan")
                    }
                }
            }
        }

        // 2. Notifikasi & Riwayat Pelayanan
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = KarangmulyaBluePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notifikasi & Riwayat Pelayanan (${notifications.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = KarangmulyaBlueDeep
                )
            }
        }
        items(notifications.take(6), key = { it.id }) { notif ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else KarangmulyaBlueLight.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMarkNotificationRead(notif.id) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = if (notif.isRead) Color(0xFFE2E8F0) else KarangmulyaBluePrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = KarangmulyaBlueDeep
                            )
                            if (notif.referenceNumber.isNotBlank()) {
                                Text(
                                    text = notif.referenceNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KarangmulyaBluePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Pusat Bantuan & Informasi Kontak Resmi Desa Karangmulya
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpCenter,
                            contentDescription = "Pusat Bantuan",
                            tint = KarangmulyaBluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pusat Bantuan & Kontak Resmi Desa Karangmulya",
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    ContactRowItem("Kantor Balai Desa", "Jl. Poros Balai Desa Karangmulya No. 1 (Senin-Jumat 08.00 - 15.30 WIB)")
                    ContactRowItem("Hotline Pelayanan Desa", "0811-2233-4455 (WhatsApp Loket Online Karangmulya)")
                    ContactRowItem("Bidan Desa & Polindes", "0812-9988-7711 (Layanan Kesehatan & Ambulans Siaga 24 Jam)")
                    ContactRowItem("Bhabinkamtibmas & Babinsa", "0813-5566-7788 (Keamanan & Ketertiban Masyarakat)")
                }
            }
        }
    }
}

@Composable
private fun ContactRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = label,
            tint = KarangmulyaEmerald,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = KarangmulyaBlueDeep)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- DIALOGS FOR WARGA ACTIONS ---

@Composable
fun NewLetterRequestDialog(
    defaultName: String,
    defaultNik: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    val letterTypes = listOf(
        "Surat Keterangan Domisili (SKD)",
        "Surat Keterangan Usaha (SKU)",
        "Surat Keterangan Tidak Mampu (SKTM)",
        "Surat Pengantar SKCK",
        "Surat Keterangan Kelahiran / Kematian",
        "Surat Pengantar Nikah (N1-N4)"
    )
    var applicantName by remember { mutableStateOf(defaultName) }
    var applicantNik by remember { mutableStateOf(defaultNik.ifBlank { "3205142809940001" }) }
    var selectedType by remember { mutableStateOf(letterTypes.first()) }
    var purpose by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf("KTP_dan_KK_Terlampir.jpg") }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachmentName = "Dokumen_Warga_${uri.lastPathSegment ?: "lampiran"}.jpg"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajukan Surat Administrasi Desa",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = applicantName,
                    onValueChange = { applicantName = it },
                    label = { Text("Nama Lengkap Sesuai KTP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = applicantNik,
                    onValueChange = { if (it.length <= 16) applicantNik = it.filter { c -> c.isDigit() } },
                    label = { Text("NIK (16 Digit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Pilih Jenis Surat:",
                    style = MaterialTheme.typography.labelLarge,
                    color = KarangmulyaBlueDeep
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    letterTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Keperluan Pengajuan Surat") },
                    placeholder = { Text("Contoh: Persyaratan pengajuan KUR Bank / Administrasi Sekolah...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("letter_purpose_input")
                )
                OutlinedButton(
                    onClick = {
                        mediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Unggah Dokumen")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unggah Foto KTP/KK: $attachmentName", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (applicantName.isNotBlank() && purpose.isNotBlank()) {
                        onSubmit(applicantName, applicantNik, selectedType, purpose, attachmentName)
                        onDismiss()
                    }
                },
                enabled = applicantName.isNotBlank() && purpose.length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary),
                modifier = Modifier.testTag("submit_letter_button")
            ) {
                Text("Kirim Permohonan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun NewComplaintDialog(
    defaultReporter: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, Double, Double, String) -> Unit
) {
    val categories = listOf("Infrastruktur & Jalan", "Lingkungan & Drainase", "Penerangan Jalan (PJU)", "Pelayanan Publik", "Keamanan & Sosial")
    var reporterName by remember { mutableStateOf(defaultReporter) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("Dusun Krajan RT 02 / RW 01") }
    var latitude by remember { mutableDoubleStateOf(-7.2148) }
    var longitude by remember { mutableDoubleStateOf(107.9018) }
    var photoLabel by remember { mutableStateOf("foto_bukti_lapangan.jpg") }
    var gpsStatus by remember { mutableStateOf("Titik GPS Desa Karangmulya Aktif") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoLabel = "Foto_Aduan_${uri.lastPathSegment ?: "bukti"}.jpg"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            latitude = -7.2142
            longitude = 107.9024
            gpsStatus = "Koordinat GPS Akurat Diperbarui!"
        } else {
            gpsStatus = "Menggunakan Titik Koordinat Dusun Karangmulya"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Buat Pengaduan Masyarakat", style = MaterialTheme.typography.titleLarge, color = KarangmulyaBlueDeep)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Nama Pelapor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Keluhan / Pengaduan") },
                    placeholder = { Text("Contoh: Jalan Berlubang di Dusun Mekarsari") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Kronologi & Rincian Pengaduan") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Lokasi Kejadian (Dusun / RT / RW)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPS: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = KarangmulyaBluePrimary
                        )
                        Text(text = gpsStatus, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "GPS", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tag GPS")
                    }
                }
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Foto")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Foto Bukti: $photoLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSubmit(
                            reporterName.ifBlank { "Warga Karangmulya" },
                            selectedCategory,
                            title,
                            description,
                            locationName,
                            latitude,
                            longitude,
                            photoLabel
                        )
                        onDismiss()
                    }
                },
                enabled = title.length >= 3 && description.length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaAmber)
            ) {
                Text("Kirim Pengaduan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun NewAspirationDialog(
    defaultSender: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    val categories = listOf("Pembangunan Desa", "Ekonomi & UMKM", "Pendidikan & Pemuda", "Kesehatan & Posyandu", "Tata Kelola Desa")
    var senderName by remember { mutableStateOf(defaultSender) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Kirim Aspirasi, Kritik & Saran", style = MaterialTheme.typography.titleLarge, color = KarangmulyaBlueDeep)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Nama Pengirim") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Aspirasi / Usulan Program") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Aspirasi, Kritik Membangun, atau Saran") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSubmit(senderName.ifBlank { "Warga Karangmulya" }, selectedCategory, title, content)
                        onDismiss()
                    }
                },
                enabled = title.length >= 3 && content.length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaEmerald)
            ) {
                Text("Kirim Aspirasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun LetterPdfAndQrPreviewDialog(
    letter: LetterRequest,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pdfGeneratedMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Dokumen Surat & QR Verifikasi",
                style = MaterialTheme.typography.titleLarge,
                color = KarangmulyaBlueDeep
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = KarangmulyaBlueLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PEMERINTAH DESA KARANGMULYA",
                            style = MaterialTheme.typography.labelLarge,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = letter.letterType,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No. Registrasi: ${letter.regNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = KarangmulyaBlueDeep
                        )
                    }
                }

                QrCodeMatrixView(
                    data = letter.qrCodeData.ifBlank { letter.regNumber },
                    qrSize = 148.dp
                )
                Text(
                    text = letter.qrCodeData,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusPillBadge(status = letter.status)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Pemohon: ${letter.applicantName}", style = MaterialTheme.typography.labelLarge)
                    Text("NIK: ${letter.applicantNik}", style = MaterialTheme.typography.bodySmall)
                    Text("Keperluan: ${letter.purpose}", style = MaterialTheme.typography.bodySmall)
                    Text("Catatan Desa: ${letter.staffNote}", style = MaterialTheme.typography.bodySmall, color = KarangmulyaBlueDark)
                }

                if (pdfGeneratedMsg != null) {
                    Surface(
                        color = KarangmulyaEmeraldLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KarangmulyaEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pdfGeneratedMsg!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val res = DocumentExportUtil.generateAndShareLetterPdf(context, letter, launchShare = true)
                    res.onSuccess { file ->
                        pdfGeneratedMsg = "File PDF resmi berhasil dibuat (${file.name}) & siap dibagikan/dicetak!"
                    }.onFailure { err ->
                        pdfGeneratedMsg = "Gagal membuat PDF: ${err.localizedMessage}"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KarangmulyaBluePrimary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Unduh PDF", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unduh / Bagikan PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun QrStatusLookupDialog(
    letters: List<LetterRequest>,
    complaints: List<Complaint>,
    aspirations: List<Aspiration>,
    onDismiss: () -> Unit
) {
    var queryCode by remember { mutableStateOf(letters.firstOrNull()?.regNumber ?: "SRT-2026-4812") }
    val matchedLetter = letters.find {
        it.regNumber.equals(queryCode.trim(), ignoreCase = true) ||
            it.qrCodeData.equals(queryCode.trim(), ignoreCase = true)
    }
    val matchedComplaint = complaints.find {
        it.regNumber.equals(queryCode.trim(), ignoreCase = true)
    }
    val matchedAspiration = aspirations.find {
        it.regNumber.equals(queryCode.trim(), ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pelacakan Status & Cek QR Code", style = MaterialTheme.typography.titleLarge, color = KarangmulyaBlueDeep)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = queryCode,
                    onValueChange = { queryCode = it },
                    label = { Text("Masukkan Nomor Registrasi / Kode QR") },
                    placeholder = { Text("Contoh: SRT-2026-4812 atau ADU-2026-9104") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick chips from actual user items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    letters.take(2).forEach { srt ->
                        FilterChip(
                            selected = queryCode == srt.regNumber,
                            onClick = { queryCode = srt.regNumber },
                            label = { Text(srt.regNumber) }
                        )
                    }
                    complaints.take(2).forEach { adu ->
                        FilterChip(
                            selected = queryCode == adu.regNumber,
                            onClick = { queryCode = adu.regNumber },
                            label = { Text(adu.regNumber) }
                        )
                    }
                }

                when {
                    matchedLetter != null -> {
                        QrCodeMatrixView(data = matchedLetter.qrCodeData, qrSize = 120.dp)
                        StatusPillBadge(status = matchedLetter.status)
                        Text(
                            text = matchedLetter.letterType,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pemohon: ${matchedLetter.applicantName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Catatan Desa: ${matchedLetter.staffNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBlueDark
                        )
                    }
                    matchedComplaint != null -> {
                        QrCodeMatrixView(data = matchedComplaint.regNumber, qrSize = 120.dp)
                        StatusPillBadge(status = matchedComplaint.status)
                        Text(
                            text = matchedComplaint.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lokasi: ${matchedComplaint.locationName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Tindak Lanjut: ${matchedComplaint.staffResponse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBlueDark
                        )
                    }
                    matchedAspiration != null -> {
                        QrCodeMatrixView(data = matchedAspiration.regNumber, qrSize = 120.dp)
                        StatusPillBadge(status = matchedAspiration.status)
                        Text(
                            text = matchedAspiration.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = KarangmulyaBlueDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tanggapan: ${matchedAspiration.staffFeedback}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KarangmulyaBlueDark
                        )
                    }
                    else -> {
                        Text(
                            text = "Nomor registrasi tidak ditemukan. Pilih salah satu kode registrasi di atas untuk melihat status real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Selesai") }
        }
    )
}
