package com.redefantasy.factions.framework.commands.staff

import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.shared.commands.restriction.entities.implementations.GroupCommandRestrictable
import com.redefantasy.core.shared.groups.Group
import com.redefantasy.core.shared.users.data.User
import com.redefantasy.core.spigot.command.CustomCommand
import com.redefantasy.core.spigot.misc.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author Gutyerrez
 */
class SerializeItemCommand : CustomCommand("serialize"), GroupCommandRestrictable {

	override fun onCommand(
		commandSender: CommandSender,
		user: User?,
		args: Array<out String>
	): Boolean {
		commandSender as Player

		val itemStack = ItemBuilder(Material.DIAMOND_SWORD).name(
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

		val deserializedItemStack = CoreConstants.JACKSON.readValue(
			serializedItemStack,
			ItemStack::class.java
		)

		val _serializedItemStack = CoreConstants.JACKSON.writeValueAsString(deserializedItemStack)

		println(_serializedItemStack)

		commandSender.itemInHand = deserializedItemStack
		return false
	}

	override fun getGroup() = Group.MASTER

}
