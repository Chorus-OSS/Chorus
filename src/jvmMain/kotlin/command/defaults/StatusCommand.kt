package org.chorus_oss.chorus.command.defaults

import com.sun.jna.platform.win32.COM.WbemcliUtil
import org.chorus_oss.chorus.Chorus
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.data.CommandParameter
import org.chorus_oss.chorus.math.round
import org.chorus_oss.chorus.utils.TextFormat
import oshi.SystemInfo
import oshi.hardware.HardwareAbstractionLayer
import oshi.util.platform.windows.WmiQueryHandler
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.util.concurrent.TimeUnit


class StatusCommand(name: String) :
    TestCommand(name, "%chorus.command.status.description", "%chorus.command.status.usage"),
    CoreCommand {
    private val systemInfo = SystemInfo()

    init {
        this.permission = "chorus.command.status"
        commandParameters.clear()
        this.addCommandParameters(
            "default", arrayOf(
                CommandParameter.Companion.newEnum("mode", true, arrayOf("full", "simple"))
            )
        )
    }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
        if (!this.testPermission(sender)) {
            return false
        }

        val simpleMode = args.size == 0 || !args[0].equals("full", ignoreCase = true)
        val server = Server.instance

        if (simpleMode) {
            sender.sendMessage(TextFormat.GREEN.toString() + "---- " + TextFormat.WHITE + "Server status" + TextFormat.GREEN + " ----")

            val time: Long = System.currentTimeMillis() - Chorus.START_TIME

            sender.sendMessage(TextFormat.GOLD.toString() + "Uptime: " + formatUptime(time))

            var tpsColor = TextFormat.GREEN
            val tps = Server.instance.ticksPerSecond
            if (tps < 12) {
                tpsColor = TextFormat.RED
            } else if (tps < 17) {
                tpsColor = TextFormat.GOLD
            }

            sender.sendMessage(
                TextFormat.GOLD.toString() + "Current TPS: " + tpsColor + round(
                    tps.toDouble(), 2
                )
            )

            sender.sendMessage(TextFormat.GOLD.toString() + "Load: " + tpsColor + server.tickUsage + "%")


            val runtime = Runtime.getRuntime()
            val totalMB = round((runtime.totalMemory().toDouble()) / 1024 / 1024, 2)
            val usedMB = round((runtime.totalMemory() - runtime.freeMemory()).toDouble() / 1024 / 1024, 2)
            val maxMB = round((runtime.maxMemory().toDouble()) / 1024 / 1024, 2)
            val usage = usedMB / maxMB * 100
            var usageColor = TextFormat.GREEN

            if (usage > 85) {
                usageColor = TextFormat.GOLD
            }

            sender.sendMessage(
                TextFormat.GOLD.toString() + "Used VM memory: " + usageColor + usedMB + " MB. (" + round(
                    usage, 2
                ) + "%)"
            )

            sender.sendMessage(TextFormat.GOLD.toString() + "Total VM memory: " + TextFormat.RED + totalMB + " MB.")


            var playerColor = TextFormat.GREEN
            if ((server.onlinePlayers.size.toFloat() / server.maxPlayers.toFloat()) > 0.85) {
                playerColor = TextFormat.GOLD
            }

            sender.sendMessage(
                TextFormat.GOLD.toString() + "Players: " + playerColor + server.onlinePlayers.size + TextFormat.GREEN + " online, " +
                        TextFormat.RED + server.maxPlayers + TextFormat.GREEN + " max. "
            )

            for (level in server.levels.values) {
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "World \"" + level.folderName + "\"" + (if (level.folderName != level.name) " (" + level.name + ")" else "") + ": " +
                            TextFormat.RED + level.chunks.size + TextFormat.GREEN + " chunks, " +
                            TextFormat.RED + level.entities.size + TextFormat.GREEN + " entities, " +
                            TextFormat.RED + level.blockEntities.size + TextFormat.GREEN + " blockEntities." +
                            " Time " + (if (level.tickRate > 1 || level.tickRateTime > 40) TextFormat.RED else TextFormat.YELLOW) + round(
                        level.tickRateTime.toDouble(), 2
                    ) + "ms" +
                            (" [delayOpt " + (level.tickRateOptDelay - 1) + "]") +
                            (if (level.tickRate > 1) " (tick rate " + (19 - level.tickRate) + ")" else "") +
                            (if (level.baseTickGameLoop.isRunning()) " (" + (if (level.baseTickGameLoop.tps >= 19) TextFormat.GREEN else (if (level.baseTickGameLoop.tps < 5) TextFormat.RED else TextFormat.YELLOW)) + level.baseTickGameLoop.tps + " TPS, " + level.baseTickGameLoop.msPt + " MSPT)" else "")
                )
            }
        } else {
            // 完整模式
            sender.sendMessage(TextFormat.GREEN.toString() + "---- " + TextFormat.WHITE + "Server status" + TextFormat.GREEN + " ----")

            // PNX服务器信息
            run {
                sender.sendMessage(TextFormat.YELLOW.toString() + ">>> " + TextFormat.WHITE + "PNX Server Info" + TextFormat.YELLOW + " <<<" + TextFormat.RESET)
                // 运行时间
                val time: Long = System.currentTimeMillis() - Chorus.START_TIME
                sender.sendMessage(TextFormat.GOLD.toString() + "Uptime: " + formatUptime(time))
                // TPS
                var tpsColor = TextFormat.GREEN
                val tps = Server.instance.ticksPerSecond
                if (tps < 12) {
                    tpsColor = TextFormat.RED
                } else if (tps < 17) {
                    tpsColor = TextFormat.GOLD
                }
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "Current TPS: " + tpsColor + round(
                        tps.toDouble(), 2
                    )
                )
                // 游戏刻负载
                sender.sendMessage(TextFormat.GOLD.toString() + "Tick Load: " + tpsColor + server.tickUsage + "%")
                // 在线玩家情况
                var playerColor = TextFormat.GREEN
                if ((server.onlinePlayers.size.toFloat() / server.maxPlayers.toFloat()) > 0.85) {
                    playerColor = TextFormat.GOLD
                }
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "Players: " + playerColor + server.onlinePlayers.size + TextFormat.GREEN + " online, " +
                            TextFormat.RED + server.maxPlayers + TextFormat.GREEN + " max. "
                )
                // 各个世界的情况
                for (level in server.levels.values) {
                    sender.sendMessage(
                        TextFormat.GOLD.toString() + "World \"" + level.folderName + "\"" + (if (level.folderName != level.name) " (" + level.name + ")" else "") + ": " +
                                TextFormat.RED + level.chunks.size + TextFormat.GREEN + " chunks, " +
                                TextFormat.RED + level.entities.size + TextFormat.GREEN + " entities, " +
                                TextFormat.RED + level.blockEntities.size + TextFormat.GREEN + " blockEntities." +
                                " Time " + (if (level.tickRate > 1 || level.tickRateTime > 40) TextFormat.RED else TextFormat.YELLOW) + round(
                            level.tickRateTime.toDouble(), 2
                        ) + "ms" +
                                (" [delayOpt " + (level.tickRateOptDelay - 1) + "]") +
                                (if (level.tickRate > 1) " (tick rate " + (19 - level.tickRate) + ")" else "")
                    )
                }
                sender.sendMessage("")
            }
            // 操作系统&JVM信息
            run {
                val os = systemInfo.operatingSystem
                val mxBean = ManagementFactory.getRuntimeMXBean()
                sender.sendMessage(TextFormat.YELLOW.toString() + ">>> " + TextFormat.WHITE + "OS & JVM Info" + TextFormat.YELLOW + " <<<" + TextFormat.RESET)
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "OS: " + TextFormat.AQUA + os.family + " " + os.manufacturer + " " +
                            os.versionInfo.version + " " + os.versionInfo.codeName + " " + os.bitness + "bit, " +
                            "build " + os.versionInfo.buildNumber
                )
                sender.sendMessage(TextFormat.GOLD.toString() + "JVM: " + TextFormat.AQUA + mxBean.vmName + " " + mxBean.vmVendor + " " + mxBean.vmVersion)
                try {
                    val vm = isInVM(systemInfo.hardware)
                    if (vm == null) {
                        sender.sendMessage(TextFormat.GOLD.toString() + "Virtual environment: " + TextFormat.GREEN + "no")
                    } else {
                        sender.sendMessage(TextFormat.GOLD.toString() + "Virtual environment: " + TextFormat.YELLOW + "yes (" + vm + ")")
                    }
                } catch (ignore: Exception) {
                }
                sender.sendMessage("")
            }
            // 网络信息
            try {
                val network = Server.instance.network
                if (network.getHardWareNetworkInterfaces() != null) {
                    sender.sendMessage(TextFormat.YELLOW.toString() + ">>> " + TextFormat.WHITE + "Network Info" + TextFormat.YELLOW + " <<<" + TextFormat.RESET)
                    sender.sendMessage(
                        TextFormat.GOLD.toString() + "Network upload: " + TextFormat.GREEN + formatKB(
                            network.upload
                        ) + "/s"
                    )
                    sender.sendMessage(
                        TextFormat.GOLD.toString() + "Network download: " + TextFormat.GREEN + formatKB(
                            network.download
                        ) + "/s"
                    )
                    sender.sendMessage(TextFormat.GOLD.toString() + "Network hardware list: ")
                    var list: List<String?>
                    for (each in network.getHardWareNetworkInterfaces()!!) {
                        list = listOf(*each.iPv4addr, *each.iPv6addr)
                        sender.sendMessage(TextFormat.AQUA.toString() + "  " + each.displayName)
                        sender.sendMessage(
                            TextFormat.RESET.toString() + "    " + formatKB(each.speed) + "/s " + TextFormat.GRAY + java.lang.String.join(
                                ", ",
                                list
                            )
                        )
                    }
                    sender.sendMessage("")
                }
            } catch (ignored: Exception) {
                sender.sendMessage(TextFormat.RED.toString() + "    Failed to get network info.")
            }
            // CPU信息
            run {
                val cpu = systemInfo.hardware.processor
                sender.sendMessage(TextFormat.YELLOW.toString() + ">>> " + TextFormat.WHITE + "CPU Info" + TextFormat.YELLOW + " <<<" + TextFormat.RESET)
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "CPU: " + TextFormat.AQUA + cpu.processorIdentifier
                        .name + TextFormat.GRAY +
                            " (" + formatFreq(cpu.maxFreq) + " baseline; " + cpu.physicalProcessorCount + " cores, " + cpu.logicalProcessorCount + " logical cores)"
                )
                sender.sendMessage(TextFormat.GOLD.toString() + "Thread count: " + TextFormat.GREEN + Thread.getAllStackTraces().size)
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "CPU Features: " + TextFormat.RESET + (if (cpu.processorIdentifier
                            .isCpu64bit
                    ) "64bit, " else "32bit, ") +
                            cpu.processorIdentifier.model + ", micro-arch: " + cpu.processorIdentifier
                        .microarchitecture
                )
                sender.sendMessage("")
            }
            // 内存信息
            run {
                val globalMemory = systemInfo.hardware.memory
                val physicalMemories = globalMemory.physicalMemory
                val virtualMemory = globalMemory.virtualMemory
                val allPhysicalMemory: Long = globalMemory.total / 1000
                val usedPhysicalMemory: Long = (globalMemory.total - globalMemory.available) / 1000
                val allVirtualMemory: Long = virtualMemory.virtualMax / 1000
                val usedVirtualMemory: Long = virtualMemory.virtualInUse / 1000
                sender.sendMessage(TextFormat.YELLOW.toString() + ">>> " + TextFormat.WHITE + "Memory Info" + TextFormat.YELLOW + " <<<" + TextFormat.RESET)
                //JVM内存
                val runtime = Runtime.getRuntime()
                val totalMB = round((runtime.totalMemory().toDouble()) / 1024 / 1024, 2)
                val usedMB =
                    round((runtime.totalMemory() - runtime.freeMemory()).toDouble() / 1024 / 1024, 2)
                val maxMB = round((runtime.maxMemory().toDouble()) / 1024 / 1024, 2)
                var usage = usedMB / maxMB * 100
                var usageColor = TextFormat.GREEN
                if (usage > 85) {
                    usageColor = TextFormat.GOLD
                }
                sender.sendMessage(TextFormat.GOLD.toString() + "JVM memory: ")
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "  Used JVM memory: " + usageColor + usedMB + " MB. (" + round(
                        usage, 2
                    ) + "%)"
                )
                sender.sendMessage(TextFormat.GOLD.toString() + "  Total JVM memory: " + TextFormat.RED + totalMB + " MB.")
                sender.sendMessage(TextFormat.GOLD.toString() + "  Maximum JVM memory: " + TextFormat.RED + maxMB + " MB.")
                // 操作系统内存
                usage = usedPhysicalMemory.toDouble() / allPhysicalMemory * 100
                usageColor = TextFormat.GREEN
                if (usage > 85) {
                    usageColor = TextFormat.GOLD
                }
                sender.sendMessage(TextFormat.GOLD.toString() + "OS memory: ")
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "  Physical memory: " + TextFormat.GREEN + usageColor + formatMB(
                        usedPhysicalMemory
                    ) + " / " + formatMB(allPhysicalMemory) + ". (" + round(usage, 2) + "%)"
                )
                usage = usedVirtualMemory.toDouble() / allVirtualMemory * 100
                usageColor = TextFormat.GREEN
                if (usage > 85) {
                    usageColor = TextFormat.GOLD
                }
                sender.sendMessage(
                    TextFormat.GOLD.toString() + "  Virtual memory: " + TextFormat.GREEN + usageColor + formatMB(
                        usedVirtualMemory
                    ) + " / " + formatMB(allVirtualMemory) + ". (" + round(usage, 2) + "%)"
                )
                if (physicalMemories.size > 0) sender.sendMessage(TextFormat.GOLD.toString() + "  Hardware list: ")
                for (each in physicalMemories) {
                    sender.sendMessage(
                        TextFormat.AQUA.toString() + "    " + each.bankLabel + " @ " + formatFreq(
                            each.clockSpeed
                        ) + TextFormat.WHITE + " " + formatMB(each.capacity / 1000)
                    )
                    sender.sendMessage(TextFormat.GRAY.toString() + "      " + each.memoryType + ", " + each.manufacturer)
                }
                sender.sendMessage("")
            }
        }

        return true
    }

    enum class ComputerSystemEntry {
        HYPERVISORPRESENT
    }

    companion object {
        private val UPTIME_FORMAT = TextFormat.RED.toString() + "%d" + TextFormat.GOLD + " days " +
                TextFormat.RED + "%d" + TextFormat.GOLD + " hours " +
                TextFormat.RED + "%d" + TextFormat.GOLD + " minutes " +
                TextFormat.RED + "%d" + TextFormat.GOLD + " seconds"
        private val vmVendor: MutableMap<String, String> = HashMap(10, 0.99f)
        private val vmMac: MutableMap<String, String> = HashMap(10, 0.99f)
        private val vmModelArray = arrayOf(
            "Linux KVM", "Linux lguest", "OpenVZ", "Qemu",
            "Microsoft Virtual PC", "VMWare", "linux-vserver", "Xen", "FreeBSD Jail", "VirtualBox", "Parallels",
            "Linux Containers", "LXC", "Bochs"
        )

        init {
            vmVendor["bhyve"] = "bhyve"
            vmVendor["KVM"] = "KVM"
            vmVendor["TCG"] = "QEMU"
            vmVendor["Microsoft Hv"] =
                "Microsoft Hyper-V or Windows Virtual PC"
            vmVendor["lrpepyh vr"] = "Parallels"
            vmVendor["VMware"] = "VMware"
            vmVendor["XenVM"] = "Xen HVM"
            vmVendor["ACRN"] = "Project ACRN"
            vmVendor["QNXQVMBSQG"] = "QNX Hypervisor"
        }

        init {
            vmMac["00:50:56"] = "VMware ESX 3"
            vmMac["00:0C:29"] = "VMware ESX 3"
            vmMac["00:05:69"] = "VMware ESX 3"
            vmMac["00:03:FF"] = "Microsoft Hyper-V"
            vmMac["00:1C:42"] = "Parallels Desktop"
            vmMac["00:0F:4B"] = "Virtual Iron 4"
            vmMac["00:16:3E"] = "Xen or Oracle VM"
            vmMac["08:00:27"] = "VirtualBox"
            vmMac["02:42:AC"] = "Docker Container"
        }

        private fun formatKB(bytes: Double): String {
            return round((bytes / 1024 * 1000), 2).toString() + " KB"
        }

        private fun formatKB(bytes: Long): String {
            return round((bytes / 1024.0 * 1000), 2).toString() + " KB"
        }

        private fun formatMB(bytes: Double): String {
            return round((bytes / 1024 / 1024 * 1000), 2).toString() + " MB"
        }

        private fun formatMB(bytes: Long): String {
            return round((bytes / 1024.0 / 1024 * 1000), 2).toString() + " MB"
        }

        private fun formatFreq(hz: Long): String {
            return if (hz >= 1000000000) {
                String.format("%.2fGHz", hz / 1000000000.0)
            } else if (hz >= 1000 * 1000) {
                String.format("%.2fMHz", hz / 1000000.0)
            } else if (hz >= 1000) {
                String.format("%.2fKHz", hz / 1000.0)
            } else {
                String.format("%dHz", hz)
            }
        }

        private fun formatUptime(uptime: Long): String {
            var uptime = uptime
            val days = TimeUnit.MILLISECONDS.toDays(uptime)
            uptime -= TimeUnit.DAYS.toMillis(days)
            val hours = TimeUnit.MILLISECONDS.toHours(uptime)
            uptime -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(uptime)
            uptime -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(uptime)
            return String.format(UPTIME_FORMAT, days, hours, minutes, seconds)
        }

        private fun isInVM(hardware: HardwareAbstractionLayer): String? {
            // CPU型号检测
            val vendor: String = hardware.processor.processorIdentifier.vendor.trim { it <= ' ' }
            if (vmVendor.containsKey(vendor)) {
                return vmVendor[vendor]
            }

            // MAC地址检测
            val nifs = hardware.networkIFs
            for (nif in nifs) {
                val mac: String = nif.macaddr.uppercase()
                val oui = if (mac.length > 7) mac.substring(0, 8) else mac
                if (vmMac.containsKey(oui)) {
                    return vmMac[oui]
                }
            }

            // 模型检测
            val model: String = hardware.computerSystem.model
            for (vm in vmModelArray) {
                if (model.contains(vm)) {
                    return vm
                }
            }
            val manufacturer: String = hardware.computerSystem.manufacturer
            if ("Microsoft Corporation" == manufacturer && "Virtual Machine" == model) {
                return "Microsoft Hyper-V"
            }

            //内存型号检测
            if (hardware.memory.physicalMemory[0].manufacturer == "QEMU") {
                return "QEMU"
            }

            //检查Windows系统参数
            //Wmi虚拟机查询只能在Windows上使用，Linux上不执行这个部分即可
            if (System.getProperties().getProperty("os.name").uppercase().contains("WINDOWS")) {
                val computerSystemQuery = WbemcliUtil.WmiQuery(
                    "Win32_ComputerSystem",
                    ComputerSystemEntry::class.java
                )
                val result = WmiQueryHandler.createInstance().queryWMI(computerSystemQuery)
                val tmp = result.getValue(ComputerSystemEntry.HYPERVISORPRESENT, 0)
                if (tmp != null && tmp.toString() == "true") {
                    return "Hyper-V"
                }
            } else {
                val file = File("/.dockerenv")
                if (file.exists()) {
                    return "Docker Container"
                }
                val cgroupFile = File("/proc/1/cgroup")
                if (cgroupFile.exists()) {
                    try {
                        Files.lines(cgroupFile.toPath()).use { lineStream ->
                            val searchResult =
                                lineStream.filter { line: String -> line.contains("docker") || line.contains("lxc") }
                            if (searchResult.findAny().isPresent) {
                                return "Docker Container"
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            return null
        }
    }
}
