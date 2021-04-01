package com.redefantasy.factions.framework

import com.mojang.authlib.GameProfile
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.PacketEvent
import com.redefantasy.core.spigot.misc.utils.PacketListener
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.apache.commons.lang.RandomStringUtils
import java.util.*

/**
 * @author Gutyerrez
 */
class FactionsFrameworkPlugin : CustomPlugin(false) {

    override fun onEnable() {
        super.onEnable()

        CoreSpigotConstants.PROTOCOL_HANDLER.registerListener(
            object : PacketListener() {

                override fun onSent(
                    event: PacketEvent
                ) {
                    val packet = event.packet

                    if (packet is PacketPlayOutPlayerInfo) {
                        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
                            GameProfile(
                                UUID.randomUUID(),
                                RandomStringUtils.random(9)
                            ),
                            0,
                            WorldSettings.EnumGamemode.SURVIVAL,
                            null
                        )

                        for (i in 0 until 10) {
                            packet.b.add(playerInfoData)
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