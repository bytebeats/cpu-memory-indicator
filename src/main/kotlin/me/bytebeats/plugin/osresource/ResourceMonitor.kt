package me.bytebeats.plugin.osresource

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.diagnostic.Logger
import com.sun.management.OperatingSystemMXBean
import me.bytebeats.plugin.osresource.model.OsSummary
import me.bytebeats.plugin.osresource.model.ResourceUsage
import java.awt.Toolkit
import java.lang.management.ManagementFactory
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit

object ResourceMonitor {
    private val logger = Logger.getInstance(ResourceMonitor::class.java)
    private val osMXBean =
        ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java) as OperatingSystemMXBean

    internal val usage: ResourceUsage = ResourceUsage()
    internal lateinit var osSummary: OsSummary
    internal var broken = false

    private val scheduledFuture =
        JobScheduler.getScheduler().scheduleWithFixedDelay({ update() }, 1L, 1L, TimeUnit.SECONDS)

    private val usagePanelSet = CopyOnWriteArraySet<ResourceUsagePanel>()

    internal fun update() {
        try {
            if (usagePanelSet.isEmpty()) return
            if (!::osSummary.isInitialized) {
                osSummary = OsSummary(
                    osMXBean.name,
                    osMXBean.arch,
                    osMXBean.version,
                    osMXBean.availableProcessors,
                    osMXBean.systemLoadAverage
                )
            } else {
                osSummary.averageSystemLoad = osMXBean.systemLoadAverage
            }
            usage.systemCpu = osMXBean.systemCpuLoad * 100.0
            usage.processCpu = osMXBean.processCpuLoad * 100.0// this operation took too much time
            usage.memory = osMXBean.freePhysicalMemorySize / osMXBean.totalPhysicalMemorySize * 100.0
            usage.swapSpace = osMXBean.freeSwapSpaceSize / osMXBean.totalSwapSpaceSize * 100.0

            val painted = usagePanelSet.any { it.update() }
            if (painted) {
                Toolkit.getDefaultToolkit().sync()
            }
            broken = false
        } catch (e: Throwable) {
            if (e is Exception) {
                if (broken) {
                    logger.error(e)
                    throw e
                } else {
                    logger.info(e)
                    broken = true
                    try {
                        Thread.sleep(1000L)
                    } catch (ignore: InterruptedException) {

                    }
                }
            } else {
                logger.error(e)
                throw e
            }
        }
    }

    fun observe(panel: ResourceUsagePanel) {
        usagePanelSet.add(panel)
    }

    fun unobserve(panel: ResourceUsagePanel) {
        usagePanelSet.remove(panel)
    }
}