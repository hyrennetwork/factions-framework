package com.redefantasy.factions.framework

import com.massivecraft.factions.entity.Faction
import com.massivecraft.factions.entity.MPlayer
import com.redefantasy.core.shared.CoreProvider
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.factions.framework.api.IFactionsAPI
import com.redefantasy.factions.framework.echo.packet.listener.UserPunishedEchoPacketListener
import java.util.*

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

    override fun onEnable() {
        super.onEnable()

        /**
         * Instanciando o massive com a api interna
         */

        try {
            Class.forName("com.massivecraft.massivecore.MassiveCore")
            Class.forName("com.massivecraft.factions.Factions")

            FACTIONS_API = object : IFactionsAPI<Faction, MPlayer> {

                override fun Faction.getMembersFromFaction(): List<MPlayer> {
                    return this.mPlayers
                }

                override fun MPlayer.getFaction(): Faction? = this.faction

                override fun getMPlayer(uuid: UUID?) = MPlayer.get(uuid)

            }
        } catch (ignored: Exception) { /* ignored */ }

        /**
         * ECHO
         */

        CoreProvider.Databases.Redis.ECHO.provide().registerListener(UserPunishedEchoPacketListener())
    }

    override fun onDisable() {
        super.onDisable()

        CoreSpigotConstants.PROTOCOL_HANDLER.close()
    }

}