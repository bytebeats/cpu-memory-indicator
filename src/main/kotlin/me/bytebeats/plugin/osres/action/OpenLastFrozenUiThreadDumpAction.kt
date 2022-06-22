package me.bytebeats.plugin.osres.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class OpenLastFrozenUiThreadDumpAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val eventProject = getEventProject(e)
        eventProject?.let { p ->
            val dir = Paths.get(PathManager.getLogPath())
            try {
                val lastFilePath = Files.list(dir)
                    .filter { Files.isDirectory(it) }
                    .filter { it.fileName.startsWith("threadDumps-frozen-") }
                    .max { o1, o2 -> (o1.toFile().lastModified() - o2.toFile().lastModified()).toInt() }

                if (lastFilePath.isPresent) {
                    val first = Files.list(lastFilePath.get())
                        .filter { it.fileName.startsWith("threadDump-") }
                        .findFirst()
                    first.ifPresent {
                        val f = LocalFileSystem.getInstance().findFileByIoFile(it.toFile())
                        if (f != null) {
                            FileEditorManager.getInstance(p).openFile(f, true)
                        }
                    }
                }
            } catch (ioe: IOException) {
                throw RuntimeException(ioe)
            }
        }
    }
}