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
            val method = mPlayer::class.java.getDeclaredMethod("getFaction")

            method.isAccessible = true

            val faction = method.invoke(mPlayer) ?: return

            val _method = faction::class.java.getDeclaredMethod("getId()")

            _method.isAccessible = true

            val factionId = _method.invoke(faction) ?: return

            if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return

            val __method = faction::class.java.getDeclaredMethod("getMPlayers")

            __method.isAccessible = true

            val mPlayers = __method.invoke(faction) as List<Any?>

            mPlayers.forEach {
                println("MPlayer -> $it")
            }
        }
    }

}