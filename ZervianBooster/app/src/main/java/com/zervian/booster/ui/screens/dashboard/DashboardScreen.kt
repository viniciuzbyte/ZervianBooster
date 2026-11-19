package com.zervian.booster.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zervian.booster.core.monitor.CpuReading
import com.zervian.booster.core.monitor.ThermalLevel
import com.zervian.booster.ui.theme.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZervianBackground)
            .padding(20.dp)
    ) {
        Text(
            "Zervian",
            style = MaterialTheme.typography.headlineMedium,
            color = ZervianTextPrimary
        )
        Text(
            "Status perangkat real-time",
            style = MaterialTheme.typography.bodySmall,
            color = ZervianTextSecondary
        )

        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                MetricCard(
                    title = "RAM",
                    value = state.memory?.let { "${it.usedPercent}%" } ?: "…",
                    subtitle = state.memory?.let { "${it.usedMb}/${it.totalMb} MB" } ?: "Membaca…",
                    accent = ZervianAccentBlue,
                )
            }
            item {
                MetricCard(
                    title = "Suhu Sistem",
                    value = state.thermal.label,
                    subtitle = thermalSubtitle(state.thermal),
                    accent = thermalColor(state.thermal),
                )
            }
            item {
                val (value, subtitle) = when (val c = state.cpu) {
                    is CpuReading.Value -> "${c.usedPercent}%" to "Estimasi beban CPU"
                    CpuReading.Unavailable -> "N/A" to "Butuh akses sistem (dibatasi OS sejak Android 8)"
                }
                MetricCard(title = "CPU", value = value, subtitle = subtitle, accent = ZervianAccentCyan)
            }
            item {
                MetricCard(
                    title = "GPU",
                    value = "—",
                    subtitle = "Tidak ada API publik tanpa root. Kami tidak akan menampilkan angka palsu.",
                    accent = ZervianTextSecondary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = { viewModel.trimMemoryNow() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bersihkan Memori Sekarang")
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, accent: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ZervianSurface)
            .padding(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = ZervianTextSecondary)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = accent
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ZervianTextSecondary, maxLines = 2)
        }
    }
}

private fun thermalColor(level: ThermalLevel) = when (level) {
    ThermalLevel.NONE -> ZervianAccentGreen
    ThermalLevel.LIGHT, ThermalLevel.MODERATE -> ZervianAccentAmber
    else -> ZervianAccentRed
}

private fun thermalSubtitle(level: ThermalLevel) = when (level) {
    ThermalLevel.UNKNOWN -> "Butuh Android 10+"
    else -> "Data resmi PowerManager"
}
