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

                    val playerList = PlayerList(player)

                    playerList.update(0, "§e§lMINHA FACÇÃO")
                    playerList.update(1, "§e[STF] STAFF")
                    playerList.update(2, "§0")
                    playerList.update(3, "§a• §6[Master] #Gutyerrez")
                    playerList.update(4, "§a• §6[Master] *ImRamon")
                    playerList.update(5, "§7• [Master] +VICTORBBBBR")
                    playerList.update(6, "§7• -Recruta")
                    playerList.update(7, "§7• -Recruta")
                    playerList.update(8, "§7• -Recruta")
                    playerList.update(9, "§7• -Recruta")
                    playerList.update(10, "§7• -Recruta")
                    playerList.update(11, "§7• -Recruta")
                    playerList.update(12, "§7• -Recruta")
                    playerList.update(13, "§7• -Recruta")
                    playerList.update(14, "§7• -Recruta")
                    playerList.update(15, "§7• -Recruta")
                    playerList.update(16, "§7• -Recruta")

                    playerList.init()
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