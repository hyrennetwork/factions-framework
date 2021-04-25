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
				"Item teste"
			).amount(
			2
			).lore(
				arrayOf(
					"hm"
				)
			).build()

		commandSender.itemInHand = itemStack

		val serialized = CoreConstants.JACKSON.writeValueAsString(itemStack)

		println(serialized)
		return false
	}

	override fun getGroup() = Group.MASTER

}
