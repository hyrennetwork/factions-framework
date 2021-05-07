package net.hyren.factions.framework

import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.applications.ApplicationType
import net.hyren.core.shared.applications.status.ApplicationStatus
import net.hyren.core.shared.applications.status.task.ApplicationStatusTask
import net.hyren.core.shared.scheduler.AsyncScheduler
import net.hyren.core.spigot.command.registry.CommandRegistry
import net.hyren.core.spigot.misc.plugin.CustomPlugin
import net.hyren.core.spigot.misc.skin.command.SkinCommand
import net.hyren.factions.framework.commands.staff.SerializeItemCommand
import net.hyren.factions.framework.echo.packet.listener.UserPunishedEchoPacketListener
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    companion object {

        @JvmStatic lateinit var instance: CustomPlugin

    }

    init {
        instance = this
    }

    private var onlineSince = 0L

    override fun onEnable() {
        super.onEnable()

        /**
         * Commands
         */

        CommandRegistry.registerCommand(SkinCommand())

        if (CoreProvider.application.applicationType == ApplicationType.SERVER_TESTS) {
            CommandRegistry.registerCommand(SerializeItemCommand())
        }

        /**
         * ECHO
         */

        CoreProvider.Databases.Redis.ECHO.provide().registerListener(UserPunishedEchoPacketListener())

        /**
         * Application status
         */

        AsyncScheduler.scheduleAsyncRepeatingTask(
            object : ApplicationStatusTask(
                ApplicationStatus(
                    CoreProvider.application.name,
                    CoreProvider.application.applicationType,
                    CoreProvider.application.server,
                    CoreProvider.application.address,
                    this.onlineSince
                )
            ) {
                override fun buildApplicationStatus(
                    applicationStatus: ApplicationStatus
                ) {
                    val runtime = Runtime.getRuntime()

                    applicationStatus.heapSize = runtime.totalMemory()
                    applicationStatus.heapMaxSize = runtime.maxMemory()
                    applicationStatus.heapFreeSize = runtime.freeMemory()

                    applicationStatus.onlinePlayers = Bukkit.getOnlinePlayers().size
                }
            },
            0,
            1,
            TimeUnit.SECONDS
        )
    }

}