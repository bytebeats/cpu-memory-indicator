package me.bytebeats.plugin.osres.ui

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class PerformanceMonitorSettingsConfigurable : Configurable {
    private val performMonitorForm = PerformanceMonitorForm()

    override fun createComponent(): JComponent? = performMonitorForm.root

    override fun isModified(): Boolean = performMonitorForm.isModified

    override fun apply() {
        performMonitorForm.apply()
    }

    override fun getDisplayName(): String = "Frozen UI Thread Dumper"
}