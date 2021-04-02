package com.redefantasy.factions.framework.misc.tablist

import com.mojang.authlib.GameProfile
import com.redefantasy.core.shared.misc.kotlin.sizedArray
import com.redefantasy.core.shared.misc.utils.ChatColor
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
    private val size: Int = SIZE_FOUR
) {

    private val DATAS = mutableListOf<PacketPlayOutPlayerInfo.PlayerInfoData>()

    private val PLAYER_INFO_DATA_NAME = sizedArray<String>(80)

    companion object {

        const val SIZE_ONE = 20
        const val SIZE_TWO = 40
        const val SIZE_THREE = 60
        const val SIZE_FOUR = 80

        private val COLOR_DECODER = arrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )

    }

    init {
        for (i in 0..size) this.update(i, "")
    }

    fun update(
        index: Int,
        text: String
    ) {
        this.add(index, text, UUID.randomUUID())
    }

    fun add(
        index: Int,
        name: String,
        uuid: UUID
    ) {
        val packet = PacketPlayOutPlayerInfo()

        val players = mutableListOf<PacketPlayOutPlayerInfo.PlayerInfoData>()

        val gameProfile = (Bukkit.getPlayerExact(name) as CraftPlayer?)?.handle?.profile ?: GameProfile(
            uuid,
            this.getNameFromIndex(index) + name,
        )

        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
            gameProfile,
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            CraftChatMessage.fromString(
                this.getNameFromIndex(index) + name
            )[0]
        )

        PLAYER_INFO_DATA_NAME[this.getIndexFromName(gameProfile.name)] = gameProfile.name

        players.add(playerInfoData)

        DATAS.add(playerInfoData)

        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
        packet.b = players

        player.sendPacket(packet)
    }

    private fun getNameFromIndex(index: Int): String {
        val firstLetter = COLOR_DECODER[index / 15]
        val secondLetter = COLOR_DECODER[index % 15]

        return arrayOf(
            ChatColor.getByChar(firstLetter),
            ChatColor.getByChar(secondLetter),
            ChatColor.RESET
        ).contentToString()
    }

    private fun getIndexFromName(name: String): Int {
        val size = COLOR_DECODER.size

        var total = 0

        for (i in 0..size) {
            if (COLOR_DECODER[i] == name[i]) {
                total *= i

                break
            }
        }

        for (i in 0..size) {
            if (COLOR_DECODER[i] == name[1]) {
                total += i

                break
            }
        }

        return total
    }

}