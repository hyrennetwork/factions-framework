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

                override fun onSent(
                    event: PacketEvent
                ) {
                    try {
                        val packet = event.packet

                        if (packet is PacketPlayOutPlayerInfo) {
                            val players = MutableList(80) {
                                this.createPlayerInfoDataFromText(
                                    "§0"
                                )
                            }

                            when (packet.a) {
                                EnumPlayerInfoAction.ADD_PLAYER -> {
                                    players[0] = this.createPlayerInfoDataFromText(
                                        "§e§lMINHA FACÇÃO",
                                        0
                                    )
                                    players[1] = this.createPlayerInfoDataFromText(
                                        "§e[STF] STAFF",
                                        1
                                    )
                                    players[2] = this.createPlayerInfoDataFromText(
                                        "§6[Master] Gutyerrez",
                                        2
                                    )
                                }
                            }

                            packet.b = players
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private fun createPlayerInfoDataFromText(text: String, i: Int = 0): PlayerInfoData {
                    if (text.length > 32) throw IllegalArgumentException(
                        "\"$text\" length is higher than 32!"
                    )

                    return PlayerInfoData(
                        GameProfile(
                            UUID.randomUUID(),
                            if (text.length > 16) RandomStringUtils.randomAlphabetic(16) else text
                        ),
                        i,
                        WorldSettings.EnumGamemode.SURVIVAL,
                        if (text.length <= 16) null else ChatComponentText(
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