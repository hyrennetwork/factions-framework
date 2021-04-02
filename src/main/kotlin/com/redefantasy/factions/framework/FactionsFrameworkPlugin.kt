package com.redefantasy.factions.framework

import com.mojang.authlib.GameProfile
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.PacketEvent
import com.redefantasy.core.spigot.misc.utils.PacketListener
import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData
import net.minecraft.server.v1_8_R3.WorldSettings
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import java.util.*

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    val RANDOM = Random()

    override fun onEnable() {
        super.onEnable()

        val CUSTOM_METADATA_KEY = "hyze_custom_packet"

        CoreSpigotConstants.PROTOCOL_HANDLER.registerListener(
            object : PacketListener() {

                override fun onSent(
                    event: PacketEvent
                ) {
                    try {
                        val player = event.player
                        val packet = event.packet

                        if (packet is PacketPlayOutPlayerInfo) {
                            println(packet.a)
                            println(packet.channels.contains(CUSTOM_METADATA_KEY))

                            event.cancelled = true

                            if (packet.channels.contains(CUSTOM_METADATA_KEY)) {
                                return
                            }

                            val packet = PacketPlayOutPlayerInfo()

                            val players = MutableList(20) {
                                this.createPlayerInfoDataFromText(
                                    "§0",
                                    it
                                )
                            }

                            players[0] = this.createPlayerInfoDataFromText(
                                "§e§lMINHA FACÇÃO",
                                79
                            )
                            players[1] = this.createPlayerInfoDataFromText(
                                "§e[STF] STAFF",
                                78
                            )
                            players[2] = this.createPlayerInfoDataFromText(
                                "§6[Master] Gutyerrez",
                                77
                            )

                            packet.a = EnumPlayerInfoAction.REMOVE_PLAYER
                            packet.b = players

                            packet.channels.add(CUSTOM_METADATA_KEY)

                            val craftPlayer = player as CraftPlayer
                            val handle = craftPlayer.handle
                            val playerConnection = handle.playerConnection
                            val networkManager = playerConnection.networkManager

                            networkManager.handle(packet)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun createPlayerInfoDataFromText(text: String, position: Int = 0, ping: Int = 0): PlayerInfoData {
                    if (text.length > 32) throw IllegalArgumentException(
                        "\"$text\" length (${text.length}) is higher than 32!"
                    )

                    return PlayerInfoData(
                        GameProfile(
                            UUID.randomUUID(),
                            RandomStringUtils.randomAlphabetic(16)
                        ),
                        0,
                        ping,
                        WorldSettings.EnumGamemode.SURVIVAL,
                        ChatComponentText(text)
                    )
                }

            }
        )
    }

    override fun onDisable() {
        super.onDisable()

        CoreSpigotConstants.PROTOCOL_HANDLER.close()
    }

}