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
    size: Int = 80
) {

    private val DATAS = mutableListOf<PacketPlayOutPlayerInfo.PlayerInfoData>()

    private val PLAYER_INFO_DATA_NAME = sizedArray<String>(80)

    private val COLOR_DECODER = arrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    )

    init {
        for (i in 0 until size) this.update(i, "")
    }

    fun update(
        index: Int,
        text: String
    ) {
        this.add(index, text, UUID.randomUUID())
    }

    private fun add(
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

        val _name = this.getNameFromIndex(index) + name

        println(_name)

        val playerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData(
            gameProfile,
            0,
            WorldSettings.EnumGamemode.NOT_SET,
            CraftChatMessage.fromString(
                _name
            )[0]
        )

        val index = this.getIndexFromName(gameProfile.name)

        println("Index: $index")

        PLAYER_INFO_DATA_NAME[index] = gameProfile.name

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

        for (i in 0 until size) {
            if (COLOR_DECODER[i] == name[0]) {
                total = 15 * i

                break
            }
        }

        for (i in 0 until size) {
            if (COLOR_DECODER[i] == name[1]) {
                total += i

                break
            }
        }

        return total
    }

}