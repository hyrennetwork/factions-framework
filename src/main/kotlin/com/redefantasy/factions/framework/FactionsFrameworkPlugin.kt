package com.redefantasy.factions.framework

import com.google.common.collect.Lists
import com.mojang.authlib.GameProfile
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.PacketEvent
import com.redefantasy.core.spigot.misc.utils.PacketListener
import com.redefantasy.factions.framework.misc.utils._HiddenString
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
                    val packet = event.packet

                    if (packet is PacketPlayOutPlayerInfo) {
                        val players = Lists.newArrayListWithCapacity<PlayerInfoData>(80)

                        when (packet.a) {
                            EnumPlayerInfoAction.ADD_PLAYER -> {
                                players[0] = this.createPlayerInfoDataFromText(
                                    "§e§lMINHA FACÇÃO"
                                )
                                players[1] = this.createPlayerInfoDataFromText(
                                    "§e[STF] STAFF"
                                )
                                players[2] = this.createPlayerInfoDataFromText(
                                    "§0"
                                )
                                players[3] = this.createPlayerInfoDataFromText(
                                    "§6[Master] Gutyerrez"
                                )

                                for (i in 4 until 19) {
                                    players[i] = this.createPlayerInfoDataFromText(
                                        _HiddenString.generate(10)
                                    )
                                }
                            }
                            EnumPlayerInfoAction.REMOVE_PLAYER -> {
                                println("Remover")
                            }
                        }

                        packet.b.clear()
                        packet.b.addAll(players)
                    }
                }

                private fun createPlayerInfoDataFromText(text: String): PlayerInfoData {
                    if (text.length > 16) throw IllegalArgumentException("text length is higher than 16!")

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