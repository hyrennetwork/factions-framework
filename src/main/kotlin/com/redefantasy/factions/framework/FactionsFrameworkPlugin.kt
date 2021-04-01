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

                override fun onReceive(
                    event: PacketEvent
                ) {
                    try {
                        val packet = event.packet

                        println("Packet: $packet")

                        if (packet is PacketPlayOutPlayerInfo) {
                            for (i in 0 until 160) {
                                packet.b.add(i, this.createPlayerInfoDataFromText("§0"))
                            }

                            when (packet.a) {
                                EnumPlayerInfoAction.ADD_PLAYER -> {
                                    packet.b[0] = this.createPlayerInfoDataFromText(
                                        "§e§lMINHA FACÇÃO"
                                    )
                                    packet.b[1] = this.createPlayerInfoDataFromText(
                                        "§e[STF] STAFF"
                                    )
                                    packet.b[2] = this.createPlayerInfoDataFromText(
                                        "§6[Master] Gutyerrez"
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun createPlayerInfoDataFromText(text: String, i: Int = 0): PlayerInfoData {
                    if (text.length > 32) throw IllegalArgumentException(
                        "\"$text\" length (${text.length}) is higher than 32!"
                    )

                    return PlayerInfoData(
                        GameProfile(
                            UUID.randomUUID(),
                            RandomStringUtils.randomAlphabetic(16)
                        ),
                        i,
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