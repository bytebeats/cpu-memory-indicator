package me.bytebeats.plugin.osres.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import me.bytebeats.plugin.osres.ui.dialog.BasicOsInformationDialog
import java.awt.Dimension
import java.awt.Toolkit

class BasicOsInformationAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = BasicOsInformationDialog()
        dialog.pack()
        val screenerSize: Dimension = Toolkit.getDefaultToolkit().screenSize
        val x = (screenerSize.getWidth() / 2 - dialog.width / 2).toInt()
        val y = (screenerSize.getHeight() / 2 - dialog.height / 2).toInt()
        dialog.setLocation(x, y)
        dialog.isVisible = true
    }
}