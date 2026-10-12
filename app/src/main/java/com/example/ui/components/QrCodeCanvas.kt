package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KarangmulyaAmber
import com.example.ui.theme.KarangmulyaAmberLight
import com.example.ui.theme.KarangmulyaBlueDeep
import com.example.ui.theme.KarangmulyaBlueLight
import com.example.ui.theme.KarangmulyaBluePrimary
import com.example.ui.theme.KarangmulyaEmerald
import com.example.ui.theme.KarangmulyaEmeraldLight
import com.example.ui.theme.KarangmulyaRose
import com.example.ui.theme.KarangmulyaRoseLight
import kotlin.math.abs

/**
 * Deterministic 21x21 QR Code Matrix Canvas Renderer for verification codes & registration numbers.
 * Uses DrawScope size.width and size.height directly per adaptive canvas guidelines.
 */
@Composable
fun QrCodeMatrixView(
    data: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = 136.dp
) {
    val seed = data.ifBlank { "KARANGMULYA-DEFAULT" }
    Box(
        modifier = modifier
            .size(qrSize)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.5.dp, KarangmulyaBlueLight, RoundedCornerShape(14.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grid = 21
            val cellW = size.width / grid
            val cellH = size.height / grid
            val darkColor = Color(0xFF0C4A6E)

            fun isFinderPattern(r: Int, c: Int): Boolean {
                val topLeft = r in 0..6 && c in 0..6
                val topRight = r in 0..6 && c in (grid - 7) until grid
                val bottomLeft = r in (grid - 7) until grid && c in 0..6
                return topLeft || topRight || bottomLeft
            }

            fun drawFinderModule(r: Int, c: Int, topR: Int, leftC: Int): Boolean {
                val lr = r - topR
                val lc = c - leftC
                val isBorder = lr == 0 || lr == 6 || lc == 0 || lc == 6
                val isInner = lr in 2..4 && lc in 2..4
                return isBorder || isInner
            }

            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    val filled = when {
                        r in 0..6 && c in 0..6 -> drawFinderModule(r, c, 0, 0)
                        r in 0..6 && c in (grid - 7) until grid -> drawFinderModule(r, c, 0, grid - 7)
                        r in (grid - 7) until grid && c in 0..6 -> drawFinderModule(r, c, grid - 7, 0)
                        r == 7 && (c in 0..7 || c in (grid - 8) until grid) -> false
                        c == 7 && (r in 0..7 || r in (grid - 8) until grid) -> false
                        r == (grid - 8) && c in 0..7 -> false
                        r == 6 -> c % 2 == 0
                        c == 6 -> r % 2 == 0
                        !isFinderPattern(r, c) -> {
                            val charIndex = (r * grid + c) % seed.length
                            val charCode = seed[charIndex].code
                            val hash = abs((r * 31 + c * 17 + charCode * 13) xor seed.hashCode())
                            hash % 2 == 0
                        }
                        else -> false
                    }

                    if (filled) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW * 0.96f, cellH * 0.96f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPillBadge(status: String, modifier: Modifier = Modifier) {
    val upper = status.uppercase()
    val (bgColor, textColor, icon) = when (upper) {
        "SELESAI", "TERVERIFIKASI", "DIREALISASIKAN" ->
            Triple(KarangmulyaEmeraldLight, Color(0xFF065F46), Icons.Default.CheckCircle)
        "DITOLAK" ->
            Triple(KarangmulyaRoseLight, Color(0xFF9F1239), Icons.Default.ErrorOutline)
        "DIPROSES", "DIVERIFIKASI", "DITINDAKLANJUTI", "DIKAJI_DESA" ->
            Triple(KarangmulyaBlueLight, KarangmulyaBlueDeep, Icons.Default.Verified)
        else ->
            Triple(KarangmulyaAmberLight, Color(0xFF92400E), Icons.Default.HourglassTop)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KarangmulyaBlueLight,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = title,
                        tint = KarangmulyaBluePrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
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
