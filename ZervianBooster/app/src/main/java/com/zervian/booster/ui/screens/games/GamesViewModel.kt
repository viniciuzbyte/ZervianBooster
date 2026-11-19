package com.zervian.booster.ui.screens.games

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zervian.booster.core.apps.InstalledAppsRepository
import com.zervian.booster.core.boost.BoostProfile
import com.zervian.booster.core.boost.BoostResult
import com.zervian.booster.core.boost.GameModeManager
import com.zervian.booster.core.boost.RealisticBoostActions
import com.zervian.booster.data.model.AppEntry
import com.zervian.booster.data.model.BoostedGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamesUiState(
    val boostedGames: List<BoostedGame> = emptyList(),
    val installedApps: List<AppEntry> = emptyList(),
    val lastMessage: String? = null,
)

class GamesViewModel(app: Application) : AndroidViewModel(app) {

    private val appsRepo = InstalledAppsRepository(app)
    private val gameModeManager = GameModeManager(app)
    private val realisticActions = RealisticBoostActions(app)

    private val _state = MutableStateFlow(GamesUiState())
    val state: StateFlow<GamesUiState> = _state.asStateFlow()

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = appsRepo.listLaunchableApps()
            _state.value = _state.value.copy(installedApps = apps)
        }
    }

    fun addGame(entry: AppEntry) {
        val current = _state.value.boostedGames
        if (current.any { it.packageName == entry.packageName }) return
        _state.value = _state.value.copy(
            boostedGames = current + BoostedGame(entry.packageName, entry.label)
        )
    }

    fun removeGame(packageName: String) {
        _state.value = _state.value.copy(
            boostedGames = _state.value.boostedGames.filterNot { it.packageName == packageName }
        )
    }

    fun applyBoost(game: BoostedGame, profile: BoostProfile) {
        val result = gameModeManager.applyProfile(game.packageName, profile)
        val message = when (result) {
            is BoostResult.Applied -> "${game.label}: mode ${profile.name} diterapkan via sistem."
            is BoostResult.Unsupported -> {
                // Jatuh ke aksi yang memang bisa dilakukan app biasa.
                realisticActions.trimBackgroundOf(game.packageName)
                "${game.label}: ${result.reason}"
            }
        }
        _state.value = _state.value.copy(lastMessage = message)
    }
}
