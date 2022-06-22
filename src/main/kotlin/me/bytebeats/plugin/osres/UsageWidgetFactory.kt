package me.bytebeats.plugin.osres

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class UsageWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = "me.bytebeats.plugin.osres"

    override fun getDisplayName(): String = "Cpu & Memory Indicator"

    override fun isAvailable(project: Project): Boolean = true

    override fun isEnabledByDefault(): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = UsagePanel(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}