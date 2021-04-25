package com.redefantasy.factions.framework

import com.fasterxml.jackson.databind.module.SimpleModule
import com.redefantasy.core.shared.CoreConstants
import com.redefantasy.core.spigot.misc.jackson.ItemStackSerializer
import org.bukkit.inventory.ItemStack

/**
 * @author Gutyerrez
 */
object FactionsFrameworkConstants {

    val IGNORED_FACTION_IDS = arrayOf(
        "none",
        "safezone",
        "warzone"
    )

    init {
        CoreConstants.JACKSON.registerModule(SimpleModule().apply {
            this.addSerializer(
                ItemStack::class.java, ItemStackSerializer()
            )
        })
    }

}