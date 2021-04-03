package com.redefantasy.factions.framework.misc.tablist

import com.mojang.authlib.GameProfile
import com.redefantasy.core.shared.misc.utils.SequencePrefix
import com.redefantasy.core.spigot.misc.player.sendPacket
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import java.util.*

/**
 * @author Gutyerrez
 */
class PlayerList(
    private val player: Player,
    size: Int = 80
) {

    private val SEQUENCE_PREFIX = SequencePrefix()

    private val PLAYERS = MutableList(size) {
        PacketPlayOutPlayerInfo.PlayerInfoData(
            GameProfile(
                UUID.randomUUID(),
                "__${SEQUENCE_PREFIX.next()}"
            ),
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            CraftChatMessage.fromString(
                "§1"
            )[0]
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
            CraftChatMessage.fromString(
                text
            )[0]
        )

        PLAYERS[index] = playerInfoData

        packet.channels.add(CHANNEL_NAME)

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
        packet.b = PLAYERS

        player.sendPacket(packet)
    }

    fun removePlayer(player: Player) {
        val packet = PacketPlayOutPlayerInfo()

        val gameProfile = (player as CraftPlayer).handle.profile

        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
            gameProfile,
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            null
        )

        packet.channels.add(CHANNEL_NAME)

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER
        packet.b.add(playerInfoData)

        Bukkit.getOnlinePlayers().forEach { it.sendPacket(packet) }
    }

}