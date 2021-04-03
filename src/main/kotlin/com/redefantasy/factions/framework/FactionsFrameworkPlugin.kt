package com.redefantasy.factions.framework

import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.PacketEvent
import com.redefantasy.core.spigot.misc.utils.PacketListener
import com.redefantasy.factions.framework.misc.tablist.PlayerList
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
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

                    playerList.removePlayer(player)

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

        CoreSpigotConstants.PROTOCOL_HANDLER.registerListener(
            object : PacketListener() {

                override fun onSent(
                    event: PacketEvent
                ) {
                    val packet = event.packet

                    if (packet is PacketPlayOutPlayerInfo) {
                        when (packet.a) {
                            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER -> {
                                val _packet = PacketPlayOutPlayerInfo()

                                _packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER
                                _packet.b = packet.b

                                event.packet = _packet
                            }
                        }
                    }
                }

            }
        )
    }

    override fun onDisable() {
        super.onDisable()

        CoreSpigotConstants.PROTOCOL_HANDLER.close()
    }

}