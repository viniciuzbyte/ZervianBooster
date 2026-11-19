package com.zervian.booster.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zervian.booster.core.monitor.CpuEstimator
import com.zervian.booster.core.monitor.CpuReading
import com.zervian.booster.core.monitor.MemoryMonitor
import com.zervian.booster.core.monitor.MemorySnapshot
import com.zervian.booster.core.monitor.ThermalLevel
import com.zervian.booster.core.monitor.ThermalMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val memory: MemorySnapshot? = null,
    val thermal: ThermalLevel = ThermalLevel.UNKNOWN,
    val cpu: CpuReading = CpuReading.Unavailable,
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val memoryMonitor = MemoryMonitor(app)
    private val thermalMonitor = ThermalMonitor(app)
    private val cpuEstimator = CpuEstimator()

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            thermalMonitor.observe().collect { level ->
                _state.value = _state.value.copy(thermal = level)
            }
        }
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(
                    memory = memoryMonitor.snapshot(),
                    cpu = cpuEstimator.read(),
                )
                delay(1500)
            }
        }
    }

    fun trimMemoryNow() = memoryMonitor.requestSystemTrim()
}
