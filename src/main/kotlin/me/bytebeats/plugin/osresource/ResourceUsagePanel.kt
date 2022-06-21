package me.bytebeats.plugin.osresource

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.update.Activatable
import com.intellij.util.ui.update.UiNotifyConnector
import me.bytebeats.plugin.osresource.model.ResourceUsage
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent

class ResourceUsagePanel(val project: Project, private val projectName: String = project.name) : JButton(),
    CustomStatusBarWidget {
    private lateinit var systemColor: Color
    private lateinit var ideColor: Color

    @Volatile
    private var lastUsage: ResourceUsage = ResourceUsage()

    @Volatile
    private var lastBufferedImage: Image? = null

    @Volatile
    private var isPressed: Boolean = false

    init {
        refreshColors()
        isOpaque = false
        isFocusable = false

        toolTipText = "Usage: IDE Cpu / System Cpu; Memory; Swap Space"

        border = StatusBarWidget.WidgetBorder.INSTANCE

        UiNotifyConnector(this, object : Activatable {
            override fun showNotify() {
                ResourceMonitor.observe(this@ResourceUsagePanel)
            }

            override fun hideNotify() {
                ResourceMonitor.unobserve(this@ResourceUsagePanel)
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e != null) {
                    val ctx = DataManager.getInstance().getDataContext(this@ResourceUsagePanel)
                    val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        null,
                        actionGroup(),
                        ctx,
                        JBPopupFactory.ActionSelectionAid.MNEMONICS,
                        false
                    )
                    val dimen = popup.content.preferredSize
                    val at = Point(0, -dimen.height)
                    popup.show(RelativePoint(e.component, at))
                }
            }
        })
    }

    private fun actionGroup(): DefaultActionGroup {
        val dumpThreadAction = ActionManager.getInstance().getAction("DumpThread")
        val frozenUiThreadDumpAction = ActionManager.getInstance().getAction("OpenLastFrozenUiThreadDump")
        val settingAction = ActionManager.getInstance().getAction("OpenPerformanceWatcherSettings")
        return DefaultActionGroup(dumpThreadAction, frozenUiThreadDumpAction, settingAction)
    }

    private fun refreshColors() {
        systemColor = if (UIUtil.isUnderDarcula()) JBColor.BLUE.darker() else JBColor.CYAN.darker()
        ideColor =
            if (UIUtil.isUnderDarcula()) JBColor.BLUE.darker().darker().darker() else ColorUtil.softer(JBColor.CYAN)
    }

    override fun getComponent(): JComponent = this

    override fun dispose() {
        project.dispose()
        ResourceMonitor.unobserve(this)
    }

    override fun ID(): String = WIDGET_ID

    override fun install(statusBar: StatusBar) {

    }

    override fun updateUI() {
        refreshColors()
        lastBufferedImage = null
        super.updateUI()
        font = widgetFont
        border = BorderFactory.createEmptyBorder()
    }

    override fun paintComponent(g: Graphics?) {
        val pressed = getModel().isPressed
        val changed = isPressed != pressed
        isPressed = pressed
        var bufferedImage = lastBufferedImage
        if (bufferedImage == null || changed) {
            if (size.width < 0 || size.height < 0) {
                return
            }
            bufferedImage = ImageUtil.createImage(g, size.width, size.height, BufferedImage.TYPE_INT_ARGB)
            val g = bufferedImage.graphics.create() as Graphics2D
            val max = 100
            val otherProcessCpuLoad = lastUsage.systemCpu - lastUsage.processCpu
            val totalBarLength = size.width - insets.left - insets.right - 3
            // TODO: 2022/6/21 finish this computation
        }
    }

    fun setShown(shown: Boolean) {
        if (shown != isVisible) {
            isVisible = shown
            revalidate()
        }
    }

    fun update(): Boolean {
        return false
    }

    companion object {
        private const val WIDGET_ID = "me.bytebeats.plugin.osresource.ResourceUsagePanel"
        private const val RESOURCE_USAGE_SAMPLE = "Cpu: 87.5% / 87.5%; Memory: 87.5%; Swap: 87.5%"
        private const val RESOURCE_USAGE_SAMPLING_FORMATTER = "Cpu: %3.1f% / %3.1f%; Memory: %3.1f%; Swap: %3.1f%"
        private val widgetFont = JBUI.Fonts.label(11F)
    }
}