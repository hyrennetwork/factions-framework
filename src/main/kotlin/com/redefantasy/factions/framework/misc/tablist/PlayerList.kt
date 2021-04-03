package com.redefantasy.factions.framework.misc.tablist

import com.mojang.authlib.GameProfile
import com.redefantasy.core.shared.misc.utils.SequencePrefix
import com.redefantasy.core.spigot.misc.player.sendPacket
import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.entity.Player
import java.util.*

/**
 * @author Gutyerrez
 */
class PlayerList(
    private val player: Player,
    private val size: Int = 80
) {

    private val SEQUENCE_PREFIX = SequencePrefix()

    private val PLAYERS = MutableList(80) {
        PacketPlayOutPlayerInfo.PlayerInfoData(
            GameProfile(
                UUID.randomUUID(),
                "__${SEQUENCE_PREFIX.next()}"
            ),
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            ChatComponentText("§0")
        )
    }

    companion object {

        const val CHANNEL_NAME = "hyren_custom_tab_list"

    }

    fun update(
        index: Int,
        text: String
    ) {
        val packet = PacketPlayOutPlayerInfo()

        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
            GameProfile(
                UUID.randomUUID(),
                SEQUENCE_PREFIX.next()
            ),
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            ChatComponentText(text)
        )

        PLAYERS[index] = playerInfoData

        packet.channels.add(CHANNEL_NAME)

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
        packet.b = PLAYERS

        player.sendPacket(packet)
    }

}