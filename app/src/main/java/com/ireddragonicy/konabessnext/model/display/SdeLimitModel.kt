package com.ireddragonicy.konabessnext.model.display

import androidx.compose.runtime.Stable

@Stable
data class SdeLimitModel(
    val maxBw: Long?,
    val perPipeBw: List<Long>,
    val clockMaxRates: List<Long>
)
