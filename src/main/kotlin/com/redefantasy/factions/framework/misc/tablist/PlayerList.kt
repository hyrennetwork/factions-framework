package com.redefantasy.factions.framework.misc.tablist

import com.mojang.authlib.GameProfile
import com.redefantasy.core.shared.misc.utils.ChatColor
import com.redefantasy.core.shared.misc.utils.SequencePrefix
import com.redefantasy.core.spigot.misc.player.sendPacket
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage
import org.bukkit.entity.Player
import java.util.*

/**
 * @author Gutyerrez
 */
class PlayerList private constructor(
    private val player: Player,
    size: Int = 80
) {

    private val COLOR_CODES = arrayOf(
        'a', 'b', 'c', 'd', 'e', 'f',
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9'
    )
    private val SEQUENCE_PREFIX = SequencePrefix()

    private val PLAYERS = MutableList(size) {
        PacketPlayOutPlayerInfo.PlayerInfoData(
            GameProfile(
                UUID.randomUUID(),
                "__${SEQUENCE_PREFIX.next()}"
            ),
            0,
            WorldSettings.EnumGamemode.SURVIVAL,
            CraftChatMessage.fromString(
                this.generateHiddenString()
            )[0]
        )
    }

    private fun generateHiddenString(size: Int = 4): String {
        val stringBuilder = StringBuilder()

        for (i in 0..size) {
            COLOR_CODES.shuffle()

            stringBuilder.append(
                ChatColor.getByChar(
                    COLOR_CODES[i]
                )
            )
        }

        return stringBuilder.toString()
    }

    companion object {

        private val PLAYER_LIST = mutableMapOf<UUID, PlayerList>()

        const val CHANNEL_NAME = "hyren_custom_tab_list"

        fun getPlayerList(player: Player) = PLAYER_LIST.getOrDefault(
            player.uniqueId,
            PlayerList(player)
        )

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

        packet.b = PLAYERS
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

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER

        packet.b.add(playerInfoData)

        player.sendPacket(packet)
    }

}