package com.zervian.booster.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zervian.booster.core.boost.BoostProfile
import com.zervian.booster.ui.theme.*

@Composable
fun GameListScreen(
    viewModel: GamesViewModel = viewModel(),
    onAddGame: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(ZervianBackground).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Daftar Game", style = MaterialTheme.typography.titleLarge, color = ZervianTextPrimary)
            FilledTonalButton(onClick = onAddGame) { Text("+ Tambah") }
        }

        state.lastMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ZervianAccentCyan, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))

        if (state.boostedGames.isEmpty()) {
            Text("Belum ada game. Tekan Tambah untuk pilih dari app terpasang.", color = ZervianTextSecondary)
        }

        LazyColumn {
            items(state.boostedGames) { game ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ZervianSurface)
                        .padding(14.dp)
                ) {
                    Text(game.label, color = ZervianTextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoostProfile.values().forEach { profile ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.applyBoost(game, profile) },
                                label = { Text(profile.name) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
