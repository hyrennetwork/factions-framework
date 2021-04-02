package com.redefantasy.factions.framework

import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.factions.framework.misc.tablist.PlayerList
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    override fun onEnable() {
        super.onEnable()

        val pluginManager = Bukkit.getServer().pluginManager

        pluginManager.registerEvents(
            object : Listener {

                @EventHandler
                fun on(
                    event: PlayerJoinEvent
                ) {
                    val player = event.player

                    val playerList = PlayerList(player, PlayerList.SIZE_FOUR)

                    playerList.initTable()

                    playerList.resetTablist()

                    playerList.updateSlot(0,"T. left");
                    playerList.updateSlot(19,"B. left");
                    playerList.updateSlot(60,"T. right");
                    playerList.updateSlot(79,"B. right");
                }

            },
            this
        )
    }

    override fun onDisable() {
        super.onDisable()

        CoreSpigotConstants.PROTOCOL_HANDLER.close()
    }

}