package me.bytebeats.plugin.osresource.model

data class ResourceUsage(
    @Volatile var systemCpu: Double = 0.0,
    @Volatile var processCpu: Double = 0.0,
    @Volatile var memory: Double = 0.0,
    @Volatile var swapSpace: Double = 0.0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceUsage

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
