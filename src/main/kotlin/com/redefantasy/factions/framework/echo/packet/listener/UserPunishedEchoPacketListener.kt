package com.redefantasy.factions.framework.echo.packet.listener

import com.redefantasy.core.shared.echo.api.listener.EchoListener
import com.redefantasy.core.shared.echo.packets.UserPunishedPacket
import com.redefantasy.factions.framework.FactionsFrameworkConstants
import com.redefantasy.factions.framework.FactionsFrameworkPlugin
import org.greenrobot.eventbus.Subscribe

/**
 * @author Gutyerrez
 */
class UserPunishedEchoPacketListener : EchoListener {

    @Subscribe
    fun on(
        packet: UserPunishedPacket
    ) {
        val userId = packet.userId
        val mPlayer = FactionsFrameworkPlugin.FACTIONS_API.getMPlayer(userId)

        if (mPlayer !== null) {
            val field = mPlayer::class.java.getDeclaredField("faction")

            field.isAccessible = true

            val faction = field.get(mPlayer) ?: return

            val _field = faction::class.java.getField("id")

            _field.isAccessible = true

            val factionId = _field.get(faction) ?: return

            if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return

            val __field = faction::class.java.getDeclaredMethod("getMPlayers")

            __field.isAccessible = true

            val mPlayers = __field.invoke(faction, "getMPlayers") as List<Any?>

            mPlayers.forEach {
                println("MPlayer -> $it")
            }
        }
    }

}