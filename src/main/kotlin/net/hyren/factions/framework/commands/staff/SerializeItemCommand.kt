package net.hyren.factions.framework.commands.staff

import net.hyren.core.shared.CoreConstants
import net.hyren.core.shared.commands.argument.Argument
import net.hyren.core.shared.commands.restriction.CommandRestriction
import net.hyren.core.shared.commands.restriction.entities.implementations.GroupCommandRestrictable
import net.hyren.core.shared.groups.Group
import net.hyren.core.shared.users.data.User
import net.hyren.core.spigot.command.CustomCommand
import net.hyren.core.spigot.misc.utils.ItemBuilder
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author Gutyerrez
 */
class SerializeItemCommand : CustomCommand("serialize"), GroupCommandRestrictable {

	override fun getCommandRestriction() = CommandRestriction.GAME

	override fun getSubCommands(): List<CustomCommand> = listOf(
		object : CustomCommand("modo") {

			override fun onCommand(
				commandSender: CommandSender,
				user: User?,
				args: Array<out String>
			): Boolean {
				commandSender as Player

				commandSender.gameMode = if (commandSender.gameMode == GameMode.CREATIVE) {
					GameMode.SURVIVAL
				} else GameMode.CREATIVE

				commandSender.sendMessage("Pronto!")
				return true
			}

		},
		object : CustomCommand("haste") {

			override fun getArguments() = listOf(
				Argument("usuário")
			)

			override fun onCommand(
				commandSender: CommandSender,
				user: User?,
				args: Array<out String>
			): Boolean {
				val player = Bukkit.getPlayerExact(args[0])

				player.addPotionEffect(PotionEffect(
					PotionEffectType.FAST_DIGGING,
					Int.MAX_VALUE,
					1
				))
				return true
			}

		}
	)

	override fun onCommand(
		commandSender: CommandSender,
		user: User?,
		args: Array<out String>
	): Boolean {
		commandSender as Player

		val itemStack = ItemBuilder(Material.DIAMOND_PICKAXE).name(
				"§bItem test"
			).amount(
			1
			).lore(
				arrayOf(
					"§7hm"
				)
			).NBT(
				"test", 1
			).build()

		val serializedItemStack = CoreConstants.JACKSON.writeValueAsString(itemStack)

		println(serializedItemStack)

		commandSender.sendMessage(
			ComponentBuilder("§7Clique aqui para copiar o item serializado.")
				.event(
					ClickEvent(
						ClickEvent.Action.COPY_TO_CLIPBOARD,
						serializedItemStack
					)
				)
				.create()
		)

		val deserializedItemStack = CoreConstants.JACKSON.readValue(
			serializedItemStack,
			ItemStack::class.java
		)

		commandSender.inventory.addItem(deserializedItemStack)

		commandSender.gameMode = GameMode.CREATIVE
		return false
	}

	override fun getGroup() = Group.MASTER

}
