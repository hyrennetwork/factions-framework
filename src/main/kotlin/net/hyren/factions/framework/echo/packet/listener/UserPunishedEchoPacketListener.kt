package net.hyren.factions.framework.echo.packet.listener

import net.hyren.core.shared.CoreProvider
import net.hyren.core.shared.echo.api.listener.EchoPacketListener
import net.hyren.core.shared.echo.packets.UserPunishedPacket
import net.hyren.factions.framework.FactionsFrameworkConstants
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.greenrobot.eventbus.Subscribe
import kotlin.properties.Delegates

/**
 * @author Gutyerrez
 */
class UserPunishedEchoPacketListener : EchoPacketListener {

    @Subscribe
    fun on(
        packet: UserPunishedPacket
    ) {
        try {
            val userId = packet.userId
            val MPlayer = Class.forName("com.massivecraft.factions.entity.MPlayer")
            val _mPlayer = MPlayer.getMethod("get", Any::class.java).invoke(MPlayer, userId)
            val punishment = CoreProvider.Cache.Local.USERS_PUNISHMENTS.provide().fetchById(packet.id!!) ?: return

            if (_mPlayer !== null && punishment.punishCategory?.name?.value == CoreProvider.Cache.Local.PUNISH_CATEGORIES.provide().fetchByName("USO_DE_HACK")?.name?.value) {
                val faction = MPlayer.getMethod("getFaction").invoke(_mPlayer) ?: return
                val factionId = faction::class.java.superclass.superclass.getMethod("getId").invoke(faction) as String? ?: return
                val factionTag = faction::class.java.getMethod("getTag").invoke(faction) as String
                val factionName = faction::class.java.getMethod("getName").invoke(faction) as String

                if (FactionsFrameworkConstants.IGNORED_FACTION_IDS.contains(factionId)) return
                val mPlayers = faction::class.java.getDeclaredMethod("getMPlayers").invoke(faction) as List<Any?>
                val registeredServiceProvider = Bukkit.getServer().servicesManager.getRegistration(
                    Class.forName("net.milkbowl.vault.economy.Economy")
                ).provider
                val getBalance = registeredServiceProvider::class.java.getDeclaredMethod(
                    "getBalance", String::class.java
                )
                val withdrawPlayer = registeredServiceProvider::class.java.getDeclaredMethod(
                    "withdrawPlayer", String::class.java, Double::class.java
                )
                val name = MPlayer.superclass.getDeclaredMethod("getName").invoke(_mPlayer) as String

                /**
                 * Outdated
                 */

                Bukkit.getOnlinePlayers().forEach {
                    it.sendMessage(
                        ComponentBuilder()
                            .append("\n")
                            .append("§7$name §cfoi punido por programas ilegais e como multa sua facção §f[$factionTag] $factionName §cperdeu §f25% §cde sua fortuna.")
                            .append("\n\n")
                            .create()
                    )
                }

                var total = 0.0

                mPlayers.forEach {
                    val name = MPlayer.superclass.getDeclaredMethod("getName").invoke(it) as String
                    val oldBalance = getBalance.invoke(registeredServiceProvider, name) as Double
                    var newBalance by Delegates.notNull<Double>()

                    withdrawPlayer.invoke(
                        registeredServiceProvider, name, if ((oldBalance - 25.0 * oldBalance / 100.0) <= 0.0) {
                            newBalance = oldBalance
                        } else {
                            newBalance = oldBalance % (oldBalance - (25.0 * oldBalance / 100.0))
                        }
                    )

                    println(
                        "$name -> ${
                            arrayOf(
                                "Balanço antigo: $oldBalance", "Balanço corrigido: ${
                                    if ((oldBalance - newBalance) <= 0.0) {
                                        0.0
                                    } else {
                                        newBalance
                                    }
                                }"
                            ).contentToString()
                        }"
                    )

                    total += if ((oldBalance - newBalance) <= 0.0) {
                        0.0
                    } else {
                        newBalance
                    }
                }

                println("Balanço total perdido: $total")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
