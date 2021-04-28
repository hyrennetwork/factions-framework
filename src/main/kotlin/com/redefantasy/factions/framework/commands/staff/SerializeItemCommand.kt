package com.redefantasy.factions.framework.commands.staff

import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.shared.commands.restriction.CommandRestriction
import com.redefantasy.core.shared.commands.restriction.entities.implementations.GroupCommandRestrictable
import com.redefantasy.core.shared.groups.Group
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.command.CustomCommand
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
