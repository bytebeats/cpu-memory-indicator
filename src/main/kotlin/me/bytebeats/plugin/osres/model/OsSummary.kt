package me.bytebeats.plugin.osres.model

data class OsSummary(
    var name: String = "",
    var arch: String = "",
    var version: String = "",
    var availableCores: Int = 0,
    var averageSystemLoad: Double = 0.0,
) {
     fun format(): String {
        return "CPU: name: '$name', arch: '$arch', version: '$version'\navailable cores: '$availableCores', average system load: '${
            "%3.1f".format(
                averageSystemLoad
            )
        }%'"
    }
}
