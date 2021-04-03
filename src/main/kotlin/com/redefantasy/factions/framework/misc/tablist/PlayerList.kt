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
    private val size: Int = 80
) {

    private val COLOR_DECODER = arrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    )

    private val SEQUENCE_PREFIX = SequencePrefix()

    private val PLAYERS = MutableList(size) {
        val prefix = "__${SEQUENCE_PREFIX.next()}"

        println("Slot $it --> $prefix")

        PacketPlayOutPlayerInfo.PlayerInfoData(
            GameProfile(
                UUID.randomUUID(),
                prefix
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

    init {
        this.remove(player)
    }

    fun update(
        index: Int,
        text: String
    ) {
        val packet = PacketPlayOutPlayerInfo()

        val playerInfoData = PLAYERS[index]

        val field = playerInfoData::class.java.getDeclaredField("e")

        field.isAccessible = true
        field.set(playerInfoData, text)

        packet.channels.add(CHANNEL_NAME)

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
        packet.b = PLAYERS

        player.sendPacket(packet)
    }

    private fun remove(player: Player) {
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