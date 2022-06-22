package me.bytebeats.plugin.osres.action

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.util.PathUtil
import me.bytebeats.plugin.osres.analysis.ThreadDumper
import java.lang.management.ManagementFactory

class DumpIdeThreadAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val eventProject = getEventProject(e)
        eventProject?.let {
            val bean = ManagementFactory.getThreadMXBean()
            val threadDump = ThreadDumper.dumpThread(bean)
            val dumpStr = threadDump.rawDump
            createNewScratch(it, dumpStr)
        }
    }

    companion object {
        private var currentBuffer = 0

        private fun nextBufferIndex(): Int {// from 1 to 5
            currentBuffer = currentBuffer % 5 + 1
            return currentBuffer
        }

        private fun createNewScratch(project: Project, text: String) {
            val f = ScratchRootType.getInstance().createScratchFile(
                project,
                PathUtil.makeFileName("threadDump${nextBufferIndex()}", "txt"),
                PlainTextLanguage.INSTANCE,
                text,
                ScratchFileService.Option.create_if_missing
            )
            f?.let {
                FileEditorManager.getInstance(project).openFile(it, true)
            }
        }
    }
}