package me.bytebeats.plugin.osres.analysis

import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.lang.management.ThreadMXBean
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ThreadDumper {

    private fun readableState(state: Thread.State): String = when (state) {
        Thread.State.BLOCKED -> "blocked"
        Thread.State.TIMED_WAITING, Thread.State.WAITING -> "waiting on condition"
        Thread.State.RUNNABLE -> "runnable"
        Thread.State.NEW -> "new"
        Thread.State.TERMINATED -> "terminated"
        else -> ""
    }

    private fun printStackTrace(writer: Writer, stackTraceElements: Array<StackTraceElement>) {
        try {
            for (element in stackTraceElements) {
                writer.write("\tat $element\n")
            }
        } catch (ioe: IOException) {
            throw ioe
        }
    }

    private fun dumpThreadInfo(
        tInfo: ThreadInfo,
        writer: Writer,
        stackTraceElements: Array<StackTraceElement> = tInfo.stackTrace
    ) {
        try {
            val sb = StringBuilder("\"${tInfo.threadName}\"")
            sb.append(" prio=${tInfo.priority} tid=${tInfo.threadId} nid=0x0 ")
                .append(readableState(tInfo.threadState))
            tInfo.lockName?.let {
                sb.append(" on $it")
            }
            tInfo.lockOwnerName?.let {
                sb.append(" owned by \"$it\" Id=${tInfo.lockOwnerId}")
            }
            if (tInfo.isSuspended) {
                sb.append(" (suspended)")
            }
            if (tInfo.isInNative) {
                sb.append(" (in native)")
            }
            writer.write("$sb\n")
            printStackTrace(writer, stackTraceElements)
            writer.write("\n")
        } catch (ioe: IOException) {
            throw ioe
        }
    }

    private fun sortByTraceLength(infos: Array<ThreadInfo>): Array<ThreadInfo> {
        infos.sortWith { o1, o2 -> o2.stackTrace.size - o1.stackTrace.size }
        return infos
    }

    private fun moveEdtToEnd(infos: Array<ThreadInfo>): List<ThreadInfo> {
        val ans = mutableListOf<ThreadInfo>()
        for (info in infos) {
            if (info.threadName.startsWith("AWT-EventQueue")) {
                ans.remove(info)
                ans.add(info)
            }
        }
        return ans
    }

    private fun dumpThreadInfos(infos: Array<ThreadInfo>, writer: Writer): Array<StackTraceElement>? {
        val sorted = moveEdtToEnd(infos)
        try {
            writer.write("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))}\n")
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
        var traces: Array<StackTraceElement>? = null
        for (info in sorted) {
            if (info.threadName.startsWith("AWT-EventQueue")) {
                traces = info.stackTrace
            }
            dumpThreadInfo(info, writer)
        }
        return traces
    }

    private fun dumpThreadsToFile(bean: ThreadMXBean, writer: Writer): Array<StackTraceElement>? {
        var traces: Array<StackTraceElement>? = null
        var successful = false
        try {
            val threads = sortByTraceLength(bean.dumpAllThreads(false, false))
            traces = dumpThreadInfos(threads, writer)
            successful = true
        } catch (ignore: Exception) {

        }
        if (!successful) {
            val threadIds = bean.allThreadIds
            val threadInfos = sortByTraceLength(bean.getThreadInfo(threadIds, Int.MAX_VALUE))
            traces = dumpThreadInfos(threadInfos, writer)
        }
        return traces
    }

    fun dumpThread(bean: ThreadMXBean): ThreadDump {
        val writer = StringWriter()
        val traces = dumpThreadsToFile(bean, writer)
        return ThreadDump(writer.toString(), traces)
    }

    fun getThreadInfos(): Array<ThreadInfo> {
        return sortByTraceLength(ManagementFactory.getThreadMXBean().dumpAllThreads(false, false))
    }

    fun dumpEdtStackTrace(infos: Array<ThreadInfo>?): String {
        val writer = StringWriter()
        if (!infos.isNullOrEmpty()) {
            val trace = infos.first().stackTrace
            printStackTrace(writer, trace)
        }
        return writer.toString()
    }

    fun dumpThreadsToString(): String {
        val writer = StringWriter()
        dumpThreadsToFile(ManagementFactory.getThreadMXBean(), writer)
        return writer.toString()
    }
}