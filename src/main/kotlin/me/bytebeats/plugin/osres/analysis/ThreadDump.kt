package me.bytebeats.plugin.osres.analysis

/**
 * Represents thread dump of the IDE captured by its performance
 * analysis tool.
 *
 * @property rawDump
 * @property stackTraces
 */
data class ThreadDump(val rawDump: String, private val stackTraces: Array<StackTraceElement>?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThreadDump

        if (rawDump != other.rawDump) return false
        if (stackTraces != null) {
            if (other.stackTraces == null) return false
            if (!stackTraces.contentEquals(other.stackTraces)) return false
        } else if (other.stackTraces != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawDump.hashCode()
        result = 31 * result + (stackTraces?.contentHashCode() ?: 0)
        return result
    }
}
