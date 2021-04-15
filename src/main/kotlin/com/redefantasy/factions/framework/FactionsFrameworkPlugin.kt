package com.redefantasy.factions.framework

import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.shared.applications.status.ApplicationStatus
import com.redefantasy.core.shared.applications.status.task.ApplicationStatusTask
import com.redefantasy.core.shared.scheduler.AsyncScheduler
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.factions.framework.api.IFactionsAPI
import com.redefantasy.factions.framework.echo.packet.listener.UserPunishedEchoPacketListener
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    companion object {

        @JvmStatic lateinit var instance: CustomPlugin

        @JvmStatic lateinit var FACTIONS_API: IFactionsAPI<*, *>

    }

    init {
        instance = this
    }

    private var onlineSince = 0L

    override fun onEnable() {
        super.onEnable()

        /**
         * Instanciando o massive com a api interna
         */

        try {
            FACTIONS_API = object : IFactionsAPI<
                    com.massivecraft.factions.entity.Faction,
                    com.massivecraft.factions.entity.MPlayer
            > {

                override fun com.massivecraft.factions.entity.Faction.getMembersFromFaction(): List<com.massivecraft.factions.entity.MPlayer> = this.mPlayers

                override fun com.massivecraft.factions.entity.MPlayer.getFaction(): com.massivecraft.factions.entity.Faction? = this.faction

                override fun getMPlayer(uuid: UUID?) = com.massivecraft.factions.entity.MPlayer.get(uuid)

            }
        } catch (ignored: Exception) { /* ignored */ }

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

    override fun onDisable() {
        super.onDisable()

        CoreSpigotConstants.PROTOCOL_HANDLER.close()
    }

}