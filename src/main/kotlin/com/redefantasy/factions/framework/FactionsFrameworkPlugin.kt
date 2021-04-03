package com.redefantasy.factions.framework

import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.factions.framework.misc.tablist.PlayerList
import org.apache.commons.lang3.RandomStringUtils
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
                    playerList.update(2, "§1")
                    playerList.update(3, "§a• §6[Master] #Gutyerrez")
                    playerList.update(4, "§a• §6[Master] *ImRamon")
                    playerList.update(5, "§7• [Master] +VICTORBBBBR")

                    for (i in 6 until 16)
                        playerList.update(i, "§7• -${RandomStringUtils.randomAlphabetic(8)}")
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