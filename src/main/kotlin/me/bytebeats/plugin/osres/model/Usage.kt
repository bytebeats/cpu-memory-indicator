package me.bytebeats.plugin.osres.model

import me.bytebeats.plugin.osres.UsagePanel

data class Usage(
    @Volatile var systemCpu: Double = 0.0,
    @Volatile var processCpu: Double = 0.0,
    @Volatile var memory: Double = 0.0,
    @Volatile var swapSpace: Double = 0.0
) {

    @Volatile
    var isUsageRefreshed = false

    fun format(): String =
        UsagePanel.RESOURCE_USAGE_SAMPLING_FORMATTER.format(processCpu, systemCpu, memory, swapSpace)

}
