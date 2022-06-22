package me.bytebeats.plugin.osres.model

data class OsSummary(
    val name: String,
    val arch: String,
    val version: String,
    val availableCores: Int,
    var averageSystemLoad: Double,
)
