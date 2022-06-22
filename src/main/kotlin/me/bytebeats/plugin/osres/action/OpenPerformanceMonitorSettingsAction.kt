package me.bytebeats.plugin.osres.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import me.bytebeats.plugin.osres.ui.PerformanceMonitorSettingsConfigurable

class OpenPerformanceMonitorSettingsAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().editConfigurable(
            getEventProject(e),
            "PerformanceMonitorSettings",
            PerformanceMonitorSettingsConfigurable(),
            true
        )
    }
}