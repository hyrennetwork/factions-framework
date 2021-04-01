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
import org.apache.commons.lang.RandomStringUtils
import java.util.*

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    val RANDOM = Random()

    override fun onEnable() {
        super.onEnable()

        CoreSpigotConstants.PROTOCOL_HANDLER.registerListener(
            object : PacketListener() {

                override fun onSent(
                    event: PacketEvent
                ) {
                    try {
                        val packet = event.packet

                        if (packet is PacketPlayOutPlayerInfo) {
                            val players = mutableMapOf<Int, PlayerInfoData>()

                            when (packet.a) {
                                EnumPlayerInfoAction.ADD_PLAYER -> {
                                    players[0] = this.createPlayerInfoDataFromText(
                                        "§e§lMINHA FACÇÃO"
                                    )
                                    players[1] = this.createPlayerInfoDataFromText(
                                        "§e[STF] STAFF"
                                    )
                                    players[2] = this.createPlayerInfoDataFromText(
                                        "§6[Master] Gutyerrez"
                                    )

                                    for (i in 3 until 80) {
                                        players[i] = this.createPlayerInfoDataFromText(
                                            "§0"
                                        )
                                    }
                                }
                                EnumPlayerInfoAction.REMOVE_PLAYER -> {
                                    println("Remover")
                                }
                            }

                            packet.b.clear()
                            packet.b.addAll(players.values)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun createPlayerInfoDataFromText(text: String): PlayerInfoData {
                    if (text.length > 32) throw IllegalArgumentException(
                        "\"$text\" length is higher than 32!"
                    )

                    return PlayerInfoData(
                        GameProfile(
                            UUID.randomUUID(),
                            RandomStringUtils.randomAlphabetic(16)
                        ),
                        0,
                        WorldSettings.EnumGamemode.SURVIVAL,
                        ChatComponentText(
                            text
                        )
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