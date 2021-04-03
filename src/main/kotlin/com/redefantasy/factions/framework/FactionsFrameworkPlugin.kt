package com.redefantasy.factions.framework

import com.mojang.authlib.GameProfile
import com.redefantasy.core.shared.misc.utils.SequencePrefix
import com.redefantasy.core.spigot.CoreSpigotConstants
import com.redefantasy.core.spigot.misc.plugin.CustomPlugin
import com.redefantasy.core.spigot.misc.utils.PacketEvent
import com.redefantasy.core.spigot.misc.utils.PacketListener
import com.redefantasy.factions.framework.misc.tablist.PlayerList
import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_8_R3.WorldSettings
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage
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
                    val player = event.player
                    val packet = event.packet

                    if (packet is PacketPlayOutPlayerInfo && !packet.channels.contains(PlayerList.CHANNEL_NAME)) {
                        val packet = PacketPlayOutPlayerInfo()

                        packet.a = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER

                        packet.channels.add(PlayerList.CHANNEL_NAME)
                        packet.b = MutableList(80) {
                            PacketPlayOutPlayerInfo.PlayerInfoData(
                                GameProfile(
                                    UUID.randomUUID(),
                                    "__${SequencePrefix().next()}"
                                ),
                                0,
                                WorldSettings.EnumGamemode.SURVIVAL,
                                CraftChatMessage.fromString(
//                                    this.generateHiddenString()
                                    "§0"
                                )[0]
                            )
                        }

                        val playerInfoData = packet.b[0]

                        val field = playerInfoData::class.java.getDeclaredField("e")

                        field.isAccessible = true

                        field.set(playerInfoData, ChatComponentText("§e§lMINHA FACÇÃO"))

//                        val playerList = PlayerList.getPlayerList(player)

//                        playerList.update(0, "§e§lMINHA FACÇÃO")
//                        playerList.update(1, "§e[STF] STAFF")
//                        playerList.update(2, "§1")
//                        playerList.update(3, "§a• §6[Master] #Gutyerrez")
//                        playerList.update(4, "§a• §6[Master] *ImRamon")
//                        playerList.update(5, "§7• [Master] +VICTORBBBBR")

//                        for (i in 6 until 16)
//                            playerList.update(i, "§7• -${RandomStringUtils.randomAlphabetic(8)}")

                        event.packet = packet
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