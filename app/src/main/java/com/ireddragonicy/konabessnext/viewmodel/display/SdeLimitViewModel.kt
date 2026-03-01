package com.ireddragonicy.konabessnext.viewmodel.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ireddragonicy.konabessnext.model.display.SdeLimitModel
import com.ireddragonicy.konabessnext.repository.GpuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SdeLimitViewModel @Inject constructor(
    private val gpuRepository: GpuRepository
) : ViewModel() {

    val sdeLimits: StateFlow<SdeLimitModel?> = gpuRepository.sdeLimits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateMaxBandwidth(newMaxBw: Long) {
        val currentLimits = sdeLimits.value ?: return
        if (currentLimits.maxBw == newMaxBw) return

        gpuRepository.updateSdeLimits(
            maxBw = newMaxBw,
            perPipeBw = currentLimits.perPipeBw,
            clockMaxRates = currentLimits.clockMaxRates,
            historyDesc = "Updated SDE Max Bandwidth to 0x${newMaxBw.toString(16)}"
        )
    }

    fun updatePerPipeBandwidth(index: Int, newBw: Long) {
        val currentLimits = sdeLimits.value ?: return
        if (index !in currentLimits.perPipeBw.indices) return
        if (currentLimits.perPipeBw[index] == newBw) return

        val newList = currentLimits.perPipeBw.toMutableList().apply {
            set(index, newBw)
        }

        gpuRepository.updateSdeLimits(
            maxBw = currentLimits.maxBw,
            perPipeBw = newList,
            clockMaxRates = currentLimits.clockMaxRates,
            historyDesc = "Updated SDE Per-Pipe Bandwidth at index $index"
        )
    }

    fun updateClockMaxRate(index: Int, newRate: Long) {
        val currentLimits = sdeLimits.value ?: return
        if (index !in currentLimits.clockMaxRates.indices) return
        if (currentLimits.clockMaxRates[index] == newRate) return

        val newList = currentLimits.clockMaxRates.toMutableList().apply {
            set(index, newRate)
        }

        gpuRepository.updateSdeLimits(
            maxBw = currentLimits.maxBw,
            perPipeBw = currentLimits.perPipeBw,
            clockMaxRates = newList,
            historyDesc = "Updated SDE Clock Max Rate at index $index"
        )
    }
}
