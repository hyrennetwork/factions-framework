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
            val getName = mPlayer::class.java.superclass.getDeclaredMethod("getName")

            getName.isAccessible = true

            val name = getName.invoke(mPlayer) as String

            val getFaction = mPlayer::class.java.getDeclaredMethod("getFaction")

            getFaction.isAccessible = true

            val faction = getFaction.invoke(mPlayer) ?: return

            val getId = faction::class.java.superclass.superclass.getMethod("getId")

            getId.isAccessible = true

            val factionId = getId.invoke(faction) ?: return

            if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return

            val getMPlayers = faction::class.java.getDeclaredMethod("getMPlayers")

            getMPlayers.isAccessible = true

            val mPlayers = getMPlayers.invoke(faction) as List<Any?>

            val economyClass = Class.forName("net.milkbowl.vault.economy.Economy")

            val registeredServiceProvider = Bukkit.getServer().servicesManager.getRegistration(economyClass).provider

            val getBalance = economyClass.getMethod("getBalance")

            getBalance.isAccessible = true

            mPlayers.forEach {
                val balance = getBalance.invoke(registeredServiceProvider, name) as Double

                println("$name -> Balanço: $balance")

                var newBalance = balance - (30.0 * balance / 100.0)

                println("$name -> Novo balanço: $newBalance")
            }
        }
    }

}