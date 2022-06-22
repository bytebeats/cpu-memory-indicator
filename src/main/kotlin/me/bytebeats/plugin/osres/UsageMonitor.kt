package me.bytebeats.plugin.osres

import com.intellij.concurrency.JobScheduler
import com.sun.management.OperatingSystemMXBean
import me.bytebeats.plugin.osres.model.OsSummary
import me.bytebeats.plugin.osres.model.Usage
import me.bytebeats.plugin.osres.util.info
import java.awt.Toolkit
import java.lang.management.ManagementFactory
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit

object UsageMonitor {
    private val osMXBean =
        ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java) as OperatingSystemMXBean

    @Volatile
    internal var usage = Usage()

    @Volatile
    internal var osSummary: OsSummary = OsSummary()
    private var broken = false

    private val scheduledFuture =
        JobScheduler.getScheduler().scheduleWithFixedDelay({ update() }, 1L, 1L, TimeUnit.SECONDS)

    private val usagePanelSet = CopyOnWriteArraySet<UsagePanel>()

    @Synchronized
    internal fun update() {
        try {
            if (usagePanelSet.isEmpty()) return
            osSummary.name = osMXBean.name
            osSummary.arch = osMXBean.arch
            osSummary.version = osMXBean.version
            osSummary.availableCores = osMXBean.availableProcessors
            osSummary.averageSystemLoad = osMXBean.systemLoadAverage

            usage.systemCpu = osMXBean.systemCpuLoad * 100.0
            usage.processCpu = osMXBean.processCpuLoad * 100.0// this operation took too much time
            usage.memory = osMXBean.freePhysicalMemorySize * 100.0 / osMXBean.totalPhysicalMemorySize
            usage.swapSpace = osMXBean.freeSwapSpaceSize * 100.0 / osMXBean.totalSwapSpaceSize
            usage.isUsageRefreshed = true

//            info("${osMXBean.freePhysicalMemorySize}, ${osMXBean.totalPhysicalMemorySize}, ${osMXBean.freeSwapSpaceSize}, ${osMXBean.totalSwapSpaceSize}")
//            info(usage.format())

            val painted = usagePanelSet.any { it.update() }
            if (painted) {
                Toolkit.getDefaultToolkit().sync()
            }
            broken = false
        } catch (e: Throwable) {
            if (e is Exception) {
                if (broken) {
                    info(e.stackTraceToString())
                    throw e
                } else {
                    info(e.stackTraceToString())
                    broken = true
                    try {
                        Thread.sleep(1000L)
                    } catch (ignore: InterruptedException) {

                    }
                }
            } else {
                info(e.stackTraceToString())
                throw e
            }
        }
    }

    fun observe(panel: UsagePanel) {
        usagePanelSet.add(panel)
    }

    fun unobserve(panel: UsagePanel) {
        usagePanelSet.remove(panel)
    }
}