package me.bytebeats.plugin.osres.model

import me.bytebeats.plugin.osres.UsagePanel

data class Usage(
    @Volatile var systemCpu: Double = 0.0,
    @Volatile var processCpu: Double = 0.0,
    @Volatile var memory: Double = 0.0,
    @Volatile var swapSpace: Double = 0.0
) {

    fun format(): String =
        UsagePanel.RESOURCE_USAGE_SAMPLING_FORMATTER.format(processCpu, systemCpu, memory, swapSpace)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usage

        if (systemCpu != other.systemCpu) return false
        if (processCpu != other.processCpu) return false
        if (memory != other.memory) return false
        if (swapSpace != other.swapSpace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = systemCpu.hashCode()
        result = 31 * result + processCpu.hashCode()
        result = 31 * result + memory.hashCode()
        result = 31 * result + swapSpace.hashCode()
        return result
    }
}
