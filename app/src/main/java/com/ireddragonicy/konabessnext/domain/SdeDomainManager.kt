package com.ireddragonicy.konabessnext.domain

import com.ireddragonicy.konabessnext.model.display.SdeLimitModel
import com.ireddragonicy.konabessnext.model.dts.DtsNode
import com.ireddragonicy.konabessnext.utils.DtsHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdeDomainManager @Inject constructor() {

    fun findSdeNode(root: DtsNode): DtsNode? {
        val queue = ArrayDeque<DtsNode>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val compatible = node.getProperty("compatible")?.originalValue ?: ""
            
            if (node.name.startsWith("qcom,mdss_mdp") || compatible.contains("qcom,sde-kms")) {
                return node
            }
            queue.addAll(node.children)
        }
        return null
    }

    fun extractSdeLimits(node: DtsNode): SdeLimitModel {
        val maxBw = node.getLongValue("qcom,sde-max-bw-high-kbps")

        val perPipeBwProp = node.getProperty("qcom,sde-max-per-pipe-bw-high-kbps")
        val perPipeBw = if (perPipeBwProp != null && perPipeBwProp.isHexArray) {
            getHexArrayValues(perPipeBwProp)
        } else emptyList()

        val clockMaxRateProp = node.getProperty("clock-max-rate")
        val clockMaxRates = if (clockMaxRateProp != null && clockMaxRateProp.isHexArray) {
            getHexArrayValues(clockMaxRateProp)
        } else emptyList()

        return SdeLimitModel(
            maxBw = maxBw,
            perPipeBw = perPipeBw,
            clockMaxRates = clockMaxRates
        )
    }

    fun updateSdeLimits(
        node: DtsNode,
        maxBw: Long?,
        perPipeBw: List<Long>,
        clockMaxRates: List<Long>
    ): Boolean {
        var changed = false

        if (maxBw != null) {
            val currentMaxBw = node.getLongValue("qcom,sde-max-bw-high-kbps")
            if (currentMaxBw != maxBw) {
                node.setProperty("qcom,sde-max-bw-high-kbps", "<0x${maxBw.toString(16)}>")
                changed = true
            }
        }

        if (perPipeBw.isNotEmpty()) {
            val prop = node.getProperty("qcom,sde-max-per-pipe-bw-high-kbps")
            if (prop != null && prop.isHexArray) {
                val current = getHexArrayValues(prop)
                if (current != perPipeBw) {
                    val hexValues = perPipeBw.joinToString(" ") { "0x${it.toString(16)}" }
                    val newValue = "<$hexValues>"
                    node.setProperty("qcom,sde-max-per-pipe-bw-high-kbps", newValue)
                    changed = true
                }
            }
        }

        if (clockMaxRates.isNotEmpty()) {
            val prop = node.getProperty("clock-max-rate")
            if (prop != null && prop.isHexArray) {
                val current = getHexArrayValues(prop)
                if (current != clockMaxRates) {
                    val hexValues = clockMaxRates.joinToString(" ") { "0x${it.toString(16)}" }
                    val newValue = "<$hexValues>"
                    node.setProperty("clock-max-rate", newValue)
                    changed = true
                }
            }
        }

        return changed
    }

    private fun getHexArrayValues(prop: com.ireddragonicy.konabessnext.model.dts.DtsProperty): List<Long> {
        if (!prop.isHexArray) return emptyList()
        val inner = prop.originalValue.trim().removeSurrounding("<", ">").trim()
        if (inner.isEmpty()) return emptyList()
        return inner.split(Regex("\\s+")).mapNotNull { token ->
            val cleanToken = token.trim()
            if (cleanToken.startsWith("0x", ignoreCase = true)) {
                cleanToken.substring(2).toLongOrNull(16)
            } else {
                cleanToken.toLongOrNull()
            }
        }
    }
}
