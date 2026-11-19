package com.zervian.booster.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zervian.booster.ui.theme.ZervianAccentCyan
import com.zervian.booster.ui.theme.ZervianBackground
import com.zervian.booster.ui.theme.ZervianSurface
import com.zervian.booster.ui.theme.ZervianTextPrimary
import com.zervian.booster.ui.theme.ZervianTextSecondary

@Composable
fun AddGameScreen(
    viewModel: GamesViewModel = viewModel(),
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadInstalledApps() }

    Column(Modifier.fillMaxSize().background(ZervianBackground).padding(16.dp)) {
        Text("Tambah Game", style = MaterialTheme.typography.titleLarge, color = ZervianTextPrimary)
        Text(
            "Diambil langsung dari app terpasang di perangkatmu",
            style = MaterialTheme.typography.bodySmall,
            color = ZervianTextSecondary
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(state.installedApps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.addGame(app)
                            onDone()
                        }
                        .background(ZervianSurface)
                        .padding(14.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = ZervianAccentCyan)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(app.label, color = ZervianTextPrimary)
                        if (app.isLikelyGame) {
                            Text("Terdeteksi sebagai game", color = ZervianAccentCyan, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
