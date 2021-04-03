package com.redefantasy.factions.framework.echo.packet.listener

import com.redefantasy.core.shared.echo.api.listener.EchoListener
import com.redefantasy.core.shared.echo.packets.UserPunishedPacket
import com.redefantasy.factions.framework.FactionsFrameworkConstants
import com.redefantasy.factions.framework.FactionsFrameworkPlugin
import org.bukkit.Bukkit
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
            val name = mPlayer::class.java.superclass.getDeclaredMethod("getName").invoke(mPlayer) as String
            val faction = mPlayer::class.java.getDeclaredMethod("getFaction").invoke(mPlayer) ?: return
            val factionId = faction::class.java.superclass.superclass.getMethod("getId").invoke(faction) ?: return

            if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return

            val mPlayers = faction::class.java.getDeclaredMethod("getMPlayers").invoke(faction) as List<Any?>

            val registeredServiceProvider = Bukkit.getServer().servicesManager.getRegistration(
                Class.forName("net.milkbowl.vault.economy.Economy")
            ).provider

            val getBalance = registeredServiceProvider::class.java.getDeclaredMethod(
                "getBalance",
                String::class.java
            )

            mPlayers.forEach {
                val balance = getBalance.invoke(registeredServiceProvider, name) as Double

                println("$name -> Balanço: $balance")

                var newBalance = balance - (30.0 * balance / 100.0)

                println("$name -> Novo balanço: $newBalance")
            }
        }
    }

}