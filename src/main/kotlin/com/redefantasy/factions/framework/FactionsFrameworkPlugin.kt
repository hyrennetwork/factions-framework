package com.redefantasy.factions.framework

import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.player.sendPacket
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    val RANDOM = Random()

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

                    val tabs = java.lang.reflect.Array.newInstance(
                        String::class.java,
                        80
                    )

                    // Clear players

                    var packet = PacketPlayOutPlayerInfo()

                    packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER

                    Bukkit.getOnlinePlayers().forEach {
                        val craftPlayer = it as CraftPlayer
                        val gameProfile = craftPlayer.profile

                        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
                            gameProfile,
                            0,
                            WorldSettings.EnumGamemode.NOT_SET,
                            null
                        )

                        packet.b.add(playerInfoData )
                    }

                    player.sendPacket(packet)

                    // Clear players
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