package me.bytebeats.plugin.osres

import com.intellij.ide.DataManager
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.ui.ColorUtil
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.ui.JreHiDpiUtil
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.update.Activatable
import com.intellij.util.ui.update.UiNotifyConnector
import me.bytebeats.plugin.osres.util.info
import org.apache.commons.lang.builder.ToStringBuilder
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

class UsagePanel(private val project: Project, private val projectName: String = project.name) : JButton(),
    CustomStatusBarWidget {
    private lateinit var systemColor: Color
    private lateinit var ideColor: Color
    private lateinit var memoryColor: Color
    private lateinit var swapColor: Color

    @Volatile
    private var lastBufferedImage: Image? = null

    @Volatile
    private var lastPressed: Boolean = false

    init {
        refreshColors()
        isOpaque = false
        isFocusable = false

        toolTipText = "Cpu: IDE / System; Memory; Swap Space"

        border = StatusBarWidget.WidgetBorder.INSTANCE

        UiNotifyConnector(this, object : Activatable {
            override fun showNotify() {
                UsageMonitor.observe(this@UsagePanel)
            }

            override fun hideNotify() {
                UsageMonitor.unobserve(this@UsagePanel)
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e != null) {
                    val ctx = DataManager.getInstance().getDataContext(this@UsagePanel)
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
        val dumpThreadAction = ActionManager.getInstance().getAction("DumpIdeThread")
        val frozenUiThreadDumpAction = ActionManager.getInstance().getAction("OpenLastFrozenUiThreadDump")
        val settingAction = ActionManager.getInstance().getAction("OpenPerformanceMonitorSettings")
        val osBasicAction = ActionManager.getInstance().getAction("BasicOsInformation")
        return DefaultActionGroup(dumpThreadAction, frozenUiThreadDumpAction, settingAction, osBasicAction)
    }

    private fun refreshColors() {
        systemColor = if (UIUtil.isUnderDarcula()) JBColor.CYAN.darker() else JBColor.BLUE.darker()
        ideColor =
            if (UIUtil.isUnderDarcula()) JBColor.CYAN.darker().darker().darker() else ColorUtil.softer(JBColor.BLUE)

        memoryColor = if (UIUtil.isUnderDarcula()) JBColor.MAGENTA.darker() else JBColor.PINK.darker()
        swapColor = if (UIUtil.isUnderDarcula()) JBColor.YELLOW.darker() else JBColor.ORANGE.darker()
    }

    override fun getComponent(): JComponent = this

    override fun dispose() {
        UsageMonitor.unobserve(this)
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
        val changed = lastPressed != pressed
        lastPressed = pressed
        var bufferedImage = lastBufferedImage
        if (bufferedImage == null || changed) {
            if (size.width < 0 || size.height < 0) {
                return
            }
            bufferedImage = ImageUtil.createImage(g, size.width, size.height, BufferedImage.TYPE_INT_ARGB)
            val g2 = bufferedImage.graphics.create() as Graphics2D
            val max = 100
            val otherProcessCpuLoad = UsageMonitor.usage.systemCpu - UsageMonitor.usage.processCpu
            val totalBarLength = size.width - insets.left - insets.right - 3

            val barHeight = size.height.coerceAtLeast(font.size + 2)

            //background
            g2.color = UIUtil.getPanelBackground()
            g2.fillRect(0, 0, size.width, size.height)


            val yOffset = (size.height - barHeight) / 2
            val xOffset = insets.left
            //label
            g2.font = font
            val message = UsageMonitor.usage.format()
            val chars = message.toCharArray()
            val firstCommaIndex = message.indexOf(';')
            val lastCommaIndex = message.lastIndexOf(';')
            if (firstCommaIndex >= lastCommaIndex) {
                return
            }

            val fontMetrics = g2.fontMetrics
            val msgWidth = fontMetrics.charsWidth(chars, 0, message.length)
            val cpuMsgWidth = fontMetrics.charsWidth(chars, 0, firstCommaIndex + 1)
            val memoryMsgWidth =
                fontMetrics.charsWidth(chars, firstCommaIndex + 2, lastCommaIndex - firstCommaIndex + 1)
            val swapMsgWidth = fontMetrics.charsWidth(chars, lastCommaIndex + 2, message.length - lastCommaIndex - 3)
            val processCpuBarLength = cpuMsgWidth * UsageMonitor.usage.processCpu / max
            val otherProcessCpuBarLength = cpuMsgWidth * otherProcessCpuLoad / max
            val memoryBarLength = memoryMsgWidth * UsageMonitor.usage.memory / max
            val swapBarLength = swapMsgWidth * UsageMonitor.usage.swapSpace / max
            //gauge ide cpu load
            g2.color = ideColor
            g2.fillRect(xOffset, yOffset, processCpuBarLength.toInt() + 1, barHeight)

            //gauge system cpu load
            g2.color = systemColor
            g2.fillRect(
                xOffset + processCpuBarLength.toInt(),
                yOffset,
                otherProcessCpuBarLength.toInt(),
                barHeight
            )

            //gauge memory
            g2.color = memoryColor
            g2.fillRect(xOffset + cpuMsgWidth + 2, yOffset, memoryBarLength.toInt() + 1, barHeight)

            //gauge swap space
            g2.color = swapColor
            g2.fillRect(xOffset + cpuMsgWidth + memoryMsgWidth, yOffset, swapBarLength.toInt() + 1, barHeight)

            val msgHeight = fontMetrics.ascent
            UISettings.setupAntialiasing(g2)

            val foreground = if (pressed) UIUtil.getLabelDisabledForeground() else JBColor.foreground()
            g2.color = foreground
            g2.drawString(
                message,
                xOffset + (totalBarLength - msgWidth) / 2,
                yOffset + msgHeight + (barHeight - msgHeight) / 2 - 1
            )

            //border
            g2.stroke = BasicStroke(1F)
            g2.color = JBColor.GRAY
            g2.drawRect(0, 0, size.width - 2, size.height - 1)
            g2.dispose()
            lastBufferedImage = bufferedImage
        }
        g?.let {
            draw(it, bufferedImage)
        }
    }

    override fun getPreferredSize(): Dimension {
        val width =
            getFontMetrics(widgetFont).stringWidth(RESOURCE_USAGE_SAMPLE) + insets.left + insets.right + JBUI.scale(2)
        val height = getFontMetrics(widgetFont).height + insets.top + insets.bottom + JBUI.scale(2)
        return Dimension(width, height)
    }

    override fun getMinimumSize(): Dimension = preferredSize

    override fun getMaximumSize(): Dimension = preferredSize

    @Synchronized
    private fun draw(g: Graphics, bufferedImage: Image) {
        UIUtil.drawImage(g, bufferedImage, 0, 0, null)
        if (JreHiDpiUtil.isJreHiDPI(g as Graphics2D) && !UIUtil.isUnderDarcula()) {
            val g2 = g.create(0, 0, width, height) as Graphics2D
            val scale = JBUIScale.sysScale(g2)
            g2.scale(1.0 / scale, 1.0 / scale)
            g2.color = if (UIUtil.isUnderIntelliJLaF()) Gray.xC9 else Gray.x91
            g2.drawLine(0, 0, (scale * width).toInt(), 0)
            g2.scale(1.0, 1.0)
            g2.dispose()
        }
    }

    fun update(): Boolean {
        var painted = false
        if (isShowing) {
            if (UsageMonitor.usage.isUsageRefreshed) {
                UsageMonitor.usage.isUsageRefreshed = false
                lastBufferedImage = null
                graphics?.let {
                    paint(it)
                    painted = true
                }
            }
        }
        return painted
    }

    override fun toString(): String = ToStringBuilder(this).append("projectName", projectName).toString()

    companion object {
        private const val WIDGET_ID = "me.bytebeats.plugin.osres.UsagePanel"
        private const val RESOURCE_USAGE_SAMPLE = "Cpu: 87.5% / 87.5%; Memory: 87.5%; Swap: 87.5%"
        const val RESOURCE_USAGE_SAMPLING_FORMATTER = "Cpu: %3.1f%% / %3.1f%%; Memory: %3.1f%%; Swap: %3.1f%%"
        private val widgetFont = JBUI.Fonts.label(11F)
    }
}