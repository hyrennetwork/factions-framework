package com.redefantasy.factions.framework.echo.packet.listener

import com.redefantasy.core.shared.echo.api.listener.EchoListener
import com.redefantasy.core.shared.echo.packets.UserPunishedPacket
import com.redefantasy.factions.framework.FactionsFrameworkConstants
import com.redefantasy.factions.framework.FactionsFrameworkPlugin
import org.bukkit.Bukkit
import org.greenrobot.eventbus.Subscribe
import kotlin.properties.Delegates

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
            val faction = mPlayer::class.java.getDeclaredMethod("getFaction").invoke(mPlayer) ?: return

            val factionId = faction::class.java.superclass.superclass.getMethod("getId").invoke(faction) as String? ?: return
            val factionTag = faction::class.java.getMethod("getTag").invoke(faction) as String
            val factionName = faction::class.java.getMethod("getName").invoke(faction) as String

            if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return

            val mPlayers = faction::class.java.getDeclaredMethod("getMPlayers").invoke(faction) as List<Any?>

            val registeredServiceProvider = Bukkit.getServer().servicesManager.getRegistration(
                Class.forName("net.milkbowl.vault.economy.Economy")
            ).provider

            val getBalance = registeredServiceProvider::class.java.getDeclaredMethod(
                "getBalance",
                String::class.java
            )
            val withdrawPlayer = registeredServiceProvider::class.java.getDeclaredMethod(
                "withdrawPlayer",
                String::class.java,
                Double::class.java
            )

            val name = mPlayer::class.java.superclass.getDeclaredMethod("getName").invoke(mPlayer) as String

            /**
             * Outdated
             */
            Bukkit.broadcastMessage(
                "\n" +
                "§7$name §cfoi punido por programas ilegais e como multa sua facção §f[$factionTag] $factionName §cperdeu §f25% §cde sua fortuna." +
                "\n\n"
            )

            mPlayers.forEach {
                val name = mPlayer::class.java.superclass.getDeclaredMethod("getName").invoke(it) as String

                var oldBalance = getBalance.invoke(registeredServiceProvider, name) as Double
                var newBalance by Delegates.notNull<Double>()

                withdrawPlayer.invoke(
                    registeredServiceProvider,
                    name,
                    if ((oldBalance - 25.0 * oldBalance / 100.0) <= 0.0) {
                        newBalance = oldBalance

                        newBalance
                    } else {
                        newBalance = oldBalance % (oldBalance - (25.0 * oldBalance / 100.0))

                        newBalance
                    }
                )

                println(
                    "$name -> ${
                        arrayOf(
                            "Balanço antigo: $oldBalance",
                            "Balanço corrigido: ${
                                if ((oldBalance - newBalance) <= 0.0) {
                                    0.0
                                } else {
                                    newBalance
                                }
                            }"
                        ).contentToString()
                    }"
                )
            }
        }
    }

}