package me.bytebeats.plugin.osres.model

data class OsSummary(
    var name: String = "",
    var arch: String = "",
    var version: String = "",
    var availableCores: Int = 0,
    var averageSystemLoad: Double = 0.0,
) {
    override fun toString(): String {
        return "CPU: name: '$name', arch: '$arch', version: '$version', Cores: $availableCores, system load: ${
            "%3.1f".format(
                averageSystemLoad * 100.0
            )
        }%"
    }
}
