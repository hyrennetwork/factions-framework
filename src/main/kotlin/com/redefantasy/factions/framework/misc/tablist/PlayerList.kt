package com.redefantasy.factions.framework.misc.tablist

import com.google.common.base.Strings
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.Maps
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/*
 *  Copyright (C) 2017 Zombie_Striker
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
class PlayerList(player: Player, size: Int) {

    companion object {
        private val PACKET_PLAYER_INFO_CLASS = if (a(7)) ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo") else ReflectionUtil.getNMSClass("Packet201PlayerInfo")
        private val PACKET_PLAYER_INFO_DATA_CLASS = if (a()) ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo\$PlayerInfoData") else null
        private var WORLD_GAME_MODE_CLASS: Class<*>? = null
        protected val GAMEPROFILECLASS = if (a()) ReflectionUtil.getMojangAuthClass("GameProfile") else null
        protected val PROPERTYCLASS = if (a()) ReflectionUtil.getMojangAuthClass("properties.Property") else null
        private val GAMEPROPHILECONSTRUCTOR = if (a()) ReflectionUtil.getConstructor(GAMEPROFILECLASS, UUID::class.java, String::class.java)
                .get() as Constructor<*> else null
        private val CRAFTPLAYERCLASS = ReflectionUtil.getCraftbukkitClass("CraftPlayer", "entity")
        private var WORLD_GAME_MODE_NOT_SET: Any? = null
        private val CRAFT_CHAT_MESSAGE_CLASS = if (a()) ReflectionUtil.getCraftbukkitClass("CraftChatMessage", "util") else null
        private val PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS = if (a()) ReflectionUtil.getNMSClass("PacketPlayOutPlayerInfo\$EnumPlayerInfoAction") else null
        private val PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER = if (a()) ReflectionUtil.getEnumConstant(
            PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS, "REMOVE_PLAYER"
        ) else null
        private val PACKET_PLAYER_INFO_ACTION_ADD_PLAYER = if (a()) ReflectionUtil.getEnumConstant(
            PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS, "ADD_PLAYER"
        ) else null
        private val PACKET_CLASS = ReflectionUtil.getNMSClass("Packet")
        private val I_CHAT_BASE_COMPONENT_CLASS = if (a()) ReflectionUtil.getNMSClass("IChatBaseComponent") else null
        private var PACKET_PLAYER_INFO_DATA_CONSTRUCTOR: Constructor<*>? = null
        private var PACKET_HEADER_FOOTER_CLASS: Class<*>? = null
        private var PACKET_HEADER_FOOTER_CONSTRUCTOR: Constructor<*>? = null
        private var CHAT_SERIALIZER: Class<*>? = null
        private var PROPERTY: Class<*>? = null
        private var PROPERTY_CONSTRUCTOR: Constructor<*>? = null
        private var PROPERTY_MAP: Class<*>? = null
        private fun invokeChatSerializerA(text: String): Any? {
            return ReflectionUtil.invokeMethod(
                CHAT_SERIALIZER, null, "a", arrayOf(
                    String::class.java
                ),
                "{\"text\":\"$text\"}"
            )
        }

        // TODO: This bit of code has been added to check specifically for 1.7.10.
        // update. Since this update has changes to it's spawnplayer packet, this
        // hopefully will fix issues with player disconnection on that update
        //
        // http://wiki.vg/Protocol_History#14w04a
        // ||ReflectionUtil.SERVER_VERSION.contains("7_R4")
        var plugin: Plugin? = null
        private val colorcodeOrder = "0123456789abcdef".split("").toTypedArray()
        private val inviscodeOrder = arrayOf(",", ".", "\'", "`", " ")
        var SIZE_DEFAULT = 20
        var SIZE_TWO = 40
        var SIZE_THREE = 60
        var SIZE_FOUR = 80
        private val lookUpTable = HashMap<UUID, PlayerList>()

        /**
         * Due to the amount of times I have to check if a version is higher than 1.8,
         * all reflection calls will be replace with this method.
         *
         * @param update
         * @return
         */
        fun a(vararg update: Int): Boolean {
            return ReflectionUtil.isVersionHigherThan(1, if (update.size > 0) update[0] else 8)
        }

        /**
         * Tries to return an existing table instance for a player. If one does not
         * exist, it will create a new one with a default size.
         *
         * @param player
         * @return null or the player's tablist.
         */
        fun getPlayerList(player: Player): PlayerList? {
            return if (!lookUpTable.containsKey(player.uniqueId)) PlayerList(
                player,
                SIZE_TWO
            ) else lookUpTable[player.uniqueId]
        }

        private fun sendNEWTabPackets(player: Player, packet: Any?, players: List<*>?, action: Any?) {
            try {
                ReflectionUtil.setInstanceField(packet, "a", action)
                ReflectionUtil.setInstanceField(packet, "b", players)
                sendPacket(packet, player)
            } catch (e: Exception) {
                error()
                e.printStackTrace()
            }
        }

        private fun sendOLDTabPackets(player: Player, packet: Any?, name: String?, isOnline: Boolean) {
            try {
                ReflectionUtil.setInstanceField(packet, "a", name)
                ReflectionUtil.setInstanceField(packet, "b", isOnline)
                ReflectionUtil.setInstanceField(packet, "c", 0.toShort())
                sendPacket(packet, player)
            } catch (e: Exception) {
                error()
                e.printStackTrace()
            }
        }

        private fun sendPacket(packet: Any?, player: Player) {
            val handle = getHandle(player)
            val playerConnection = ReflectionUtil.getInstanceField(handle, "playerConnection")
            ReflectionUtil.invokeMethod(playerConnection, "sendPacket", arrayOf(PACKET_CLASS), packet)
        }

        private fun getHandle(player: Player): Any? {
            return ReflectionUtil.invokeMethod(CRAFTPLAYERCLASS!!.cast(player), "getHandle", arrayOfNulls<Class<*>?>(0))
        }

        private fun getNameFromID(id: Int): String {
            var a = colorcodeOrder
            var size1 = 15
            if (!a()) {
                a = inviscodeOrder
                size1 = 5
            }
            val firstletter = a[id / size1]
            val secondletter = a[id % size1]
            return if (a()) ChatColor.getByChar(firstletter).toString() + "" + ChatColor.getByChar(
                secondletter
            ) + ChatColor.RESET else firstletter + secondletter
        }

        private fun getIDFromName(id: String?): Int {
            var a = colorcodeOrder
            var size1 = 15
            var indexAdder = 0
            if (!a()) {
                a = inviscodeOrder
                size1 = 5
                indexAdder = 1
            }
            var total = 0
            for (i in a.indices) {
                if (a[i].equals(id!![0 + indexAdder].toString() + "", ignoreCase = true)) {
                    total = size1 * i
                    break
                }
            }
            for (i in a.indices) {
                if (a[i].equals(id!![1 + (indexAdder + indexAdder)].toString() + "", ignoreCase = true)) {
                    total += i
                    break
                }
            }
            return total
        }

        private fun error() {
            Bukkit.broadcastMessage(
                "PLEASE REPORT THIS ISSUE TO" + ChatColor.RED + " ZOMBIE_STRIKER" + ChatColor.RESET
                        + " ON THE BUKKIT FORUMS"
            )
        }

        init {
            // It's hacky, I know, but atleast it gets a plugin instance.
            try {
                val f = File(Skin::class.java.protectionDomain.codeSource.location.toURI().path)
                for (p in Bukkit.getPluginManager().plugins) {
                    if (f.getName().contains(p.getName())
                    ) {
                        plugin = p
                        break
                    }
                }
            } catch (e: URISyntaxException) {
            }
            if (plugin == null) plugin = Bukkit.getPluginManager().plugins[0]
            WORLD_GAME_MODE_CLASS = ReflectionUtil.getNMSClass("EnumGamemode")
            if (WORLD_GAME_MODE_CLASS == null) WORLD_GAME_MODE_CLASS =
                ReflectionUtil.getNMSClass("WorldSettings\$EnumGamemode")
            CHAT_SERIALIZER = ReflectionUtil.getNMSClass("IChatBaseComponent\$ChatSerializer")
            if (CHAT_SERIALIZER == null) CHAT_SERIALIZER = ReflectionUtil.getNMSClass("ChatSerializer")
            PROPERTY = ReflectionUtil.getMojangAuthClass("properties.Property")
            PROPERTY_CONSTRUCTOR = ReflectionUtil.getConstructor(
                PROPERTY, *arrayOf<Class<*>>(
                    String::class.java, String::class.java, String::class.java
                )
            ).get() as Constructor<*>
            if (PROPERTY == null || PROPERTY_CONSTRUCTOR == null) {
                PROPERTY = ReflectionUtil.getOLDAuthlibClass("properties.Property")
                PROPERTY_CONSTRUCTOR = ReflectionUtil.getConstructor(
                    PROPERTY, *arrayOf<Class<*>>(
                        String::class.java, String::class.java, String::class.java
                    )
                ).get() as Constructor<*>
            } else {
                PROPERTY_MAP = ReflectionUtil.getMojangAuthClass("properties.PropertyMap")
            }
            WORLD_GAME_MODE_NOT_SET =
                if (a()) ReflectionUtil.getEnumConstant(WORLD_GAME_MODE_CLASS, "NOT_SET") else null
            PACKET_PLAYER_INFO_DATA_CONSTRUCTOR = if (a()) ReflectionUtil.getConstructor(
                PACKET_PLAYER_INFO_DATA_CLASS, PACKET_PLAYER_INFO_CLASS, GAMEPROFILECLASS,
                Int::class.javaPrimitiveType, WORLD_GAME_MODE_CLASS, I_CHAT_BASE_COMPONENT_CLASS
            )
                .get() as Constructor<*> else null
            if (ReflectionUtil.isVersionHigherThan(1, 7)) {
                try {
                    PACKET_HEADER_FOOTER_CLASS = ReflectionUtil.getNMSClass("PacketPlayOutPlayerListHeaderFooter")
                    PACKET_HEADER_FOOTER_CONSTRUCTOR = PACKET_HEADER_FOOTER_CLASS!!.constructors[0]
                } catch (e: Exception) {
                } catch (e: Error) {
                }
            }
        }
    }

    private val datas: MutableList<Any?> = ArrayList()
    private val datasOLD: MutableMap<Int, String> = HashMap()
    private var ownerUUID: UUID? = null
    private val tabs: Array<String?>
    private val hasCustomTexture: BooleanArray
    private var size = 0
    fun setHeaderFooter(header: String, footer: String) {
        val packet = ReflectionUtil.instantiate(PACKET_HEADER_FOOTER_CONSTRUCTOR)
        ReflectionUtil.setInstanceField(packet, "a", invokeChatSerializerA(header))
        ReflectionUtil.setInstanceField(packet, "b", invokeChatSerializerA(footer))
        sendPacket(
            packet, Bukkit.getPlayer(
                ownerUUID
            )
        )
    }

    /**
     * Returns the name of the tab at the index 'index'
     *
     * @param index
     * - the index of the entry in the tablist
     *
     * @return
     */
    fun getTabName(index: Int): String? {
        return tabs[index]
    }

    /**
     * Resets a player's tablist. Use this if you have want the tablist to return to
     * the base-minecraft tablist
     */
    fun resetTablist() {
        clearAll()
        var i = 0
        for (player in Bukkit.getOnlinePlayers()) {
            addExistingPlayer(i, player)
            i++
        }
    }

    /**
     * Clears all players from the player's tablist.
     */
    fun clearPlayers() {
        val packet =
            ReflectionUtil.instantiate(ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>)
        if (ReflectionUtil.getInstanceField(packet, "b") is List<*>) {
            val players = ReflectionUtil.getInstanceField(packet, "b") as MutableList<Any?>?
            val olp = ReflectionUtil.invokeMethod(Bukkit.getServer(), "getOnlinePlayers", null)

            var olpa: Array<Player>

            if (olp is Collection<*>) {
                olpa = (olp as Collection<Player>).toTypedArray()
            } else {
                olpa = olp as Array<Player>
            }

            for (player2 in olpa) {
                val player = player2
                val gameProfile = GAMEPROFILECLASS?.cast(
                    ReflectionUtil.invokeMethod(
                        player,
                        "getProfile",
                        arrayOfNulls<Class<*>?>(0)
                    )
                )
                val array = ReflectionUtil.invokeMethod(
                    CRAFT_CHAT_MESSAGE_CLASS, null, "fromString", arrayOf(
                        String::class.java
                    ), player.name
                ) as Array<Any>?
                val data = ReflectionUtil.instantiate(
                    PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet, gameProfile, 1,
                    WORLD_GAME_MODE_NOT_SET, array!![0]
                )
                players!!.add(data)
            }
            sendNEWTabPackets(player, packet, players, PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER)
        } else {
            val olp = ReflectionUtil.invokeMethod(Bukkit.getServer(), "getOnlinePlayers", null)

            val players: Array<Player> = if (olp is Collection<*>) {
                (olp as Collection<Player>).toTypedArray()
            } else olp as Array<Player>

            for (i in players.indices) {
                try {
                    val packetLoop = ReflectionUtil.instantiate(
                        ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>
                    )
                    sendOLDTabPackets(player, packetLoop, players[i].name, false)
                } catch (e: Exception) {
                    error()
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Clears all the custom tabs from the player's tablist.
     */
    fun clearCustomTabs() {
        val packet =
            ReflectionUtil.instantiate(ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>)
        if (ReflectionUtil.getInstanceField(packet, "b") is List<*>) {
            val players = ReflectionUtil.getInstanceField(packet, "b") as MutableList<Any?>?
            for (playerData in ArrayList(datas)) tabs[getIDFromName(
                ReflectionUtil.invokeMethod(
                    GAMEPROFILECLASS!!.cast(ReflectionUtil.invokeMethod(playerData, "a", arrayOfNulls<Class<*>?>(0))),
                    "getName",
                    null
                ) as String?
            )] = ""
            players!!.addAll(datas)
            datas.clear()
            sendNEWTabPackets(player, packet, players, PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER)
        } else {
            for (i in 0 until size) if (datasOLD.containsKey(i)) try {
                val packetLoop = ReflectionUtil.instantiate(
                    ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>
                )
                sendOLDTabPackets(player, packetLoop, datasOLD[i], false)
                tabs[i] = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            datasOLD.clear()
        }
    }

    /**
     * Clears all the values for a player's tablist. Use this whenever a player
     * first joins if you want to create your own tablist.
     *
     * This is here to remind you that you MUST call either this method or the
     * "clearCustomTabs" method. If you do not, the player will continue to see the
     * custom tabs until they relog.
     */
    fun clearAll() {
        clearPlayers()
        clearCustomTabs()
    }
    /**
     * Use this for changing a value at a specific tab.
     *
     * @param id
     * @param newName
     */
    /**
     * Use this for changing a value at a specific tab.
     *
     * @param id
     * @param newName
     */
    @JvmOverloads
    fun updateSlot(id: Int, newName: String, usePlayersSkin: Boolean = false) {
        updateSlot(id, newName, UUID.randomUUID(), usePlayersSkin)
    }
    /**
     * Use this for changing a value at a specific tab.
     *
     * @param id
     * @param newName
     */
    /**
     * Use this for changing a value at a specific tab.
     *
     * @param id
     * @param newName
     */
    @JvmOverloads
    fun updateSlot(id: Int, newName: String, uuid: UUID, usePlayersSkin: Boolean = false) {
        if (a()) {
            removeCustomTab(id, true)
            addValue(id, newName, uuid, usePlayersSkin)
            hasCustomTexture[id] = usePlayersSkin
        } else {
            for (i in id until size) removeCustomTab(i, false)
            for (i in id until size) addValue(i, if (i == id) newName else datasOLD[i]!!.substring(2), false)
            // This is for pre 1.8, no textures needed
        }
    }

    /**
     * removes a specific player from the player's tablist.
     *
     * @param player
     */
    fun removePlayer(player: Player) {
        val packet =
            ReflectionUtil.instantiate(ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>)
        if (ReflectionUtil.getInstanceField(packet, "b") is List<*>) {
            val players = ReflectionUtil.getInstanceField(packet, "b") as MutableList<Any?>?
            val gameProfile =
                GAMEPROFILECLASS!!.cast(ReflectionUtil.invokeMethod(player, "getProfile", arrayOfNulls<Class<*>?>(0)))
            val array = ReflectionUtil.invokeMethod(
                CRAFT_CHAT_MESSAGE_CLASS, null, "fromString", arrayOf(
                    String::class.java
                ), player.name
            ) as Array<Any>?
            val data = ReflectionUtil.instantiate(
                PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet, gameProfile, 1,
                WORLD_GAME_MODE_NOT_SET, array!![0]
            )
            players!!.add(data)
            sendNEWTabPackets(player, packet, players, PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER)
        } else {
            try {
                sendOLDTabPackets(player, packet, player.name, false)
            } catch (e: Exception) {
                error()
                e.printStackTrace()
            }
        }
    }

    /**
     * Removes a custom tab from a player's tablist.
     *
     * @param id
     */
    fun removeCustomTab(id: Int) {
        removeCustomTab(id, true)
    }

    /**
     * Removes a custom tab from a player's tablist.
     *
     * @param id
     */
    private fun removeCustomTab(id: Int, remove: Boolean) {
        val packet =
            ReflectionUtil.instantiate(ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>)
        if (ReflectionUtil.getInstanceField(packet, "b") is List<*>) {
            val players = ReflectionUtil.getInstanceField(packet, "b") as MutableList<Any?>?
            for (playerData in ArrayList(datas)) {
                val gameProfile =
                    GAMEPROFILECLASS!!.cast(ReflectionUtil.invokeMethod(playerData, "a", arrayOfNulls<Class<*>?>(0)))
                val getname = ReflectionUtil.invokeMethod(gameProfile, "getName", null) as String?
                if (getname!!.startsWith(getNameFromID(id))) {
                    tabs[getIDFromName(getname)] = ""
                    players!!.add(playerData)
                    if (remove) datas.remove(playerData)
                    break
                }
            }
            sendNEWTabPackets(player, packet, players, PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER)
        } else {
            try {
                sendOLDTabPackets(player, packet, datasOLD[id], false)
                if (remove) {
                    tabs[id] = null
                    datasOLD.remove(id)
                }
            } catch (e: Exception) {
                error()
                e.printStackTrace()
            }
        }
    }

    /**
     *
     * Use this to add an existing offline player to a player's tablist. The name
     * variable is so you can modify a player's name in the tablist. If you want the
     * player-tab to be the same as the player's name, use the other method
     *
     * @param id
     * @param name
     * @param player
     */
    fun addExistingPlayer(id: Int, name: String, player: OfflinePlayer) {
        addValue(id, name, player.uniqueId, true)
    }

    /**
     *
     * Use this to add an existing offline player to a player's tablist.
     *
     * @param id
     * @param player
     */
    fun addExistingPlayer(id: Int, player: OfflinePlayer) {
        addExistingPlayer(id, player.name, player)
    }

    /**
     * Use this to add a new player to the list
     *
     * @param id
     * @param name
     */
    @Deprecated("")
//        (
//        """If all 80 slots have been taken, new values will not be shown and
//                  may have the potential to go out of the registered bounds. Use
//                  the """ updateSlot() " method to change a slot."
//    )
    private fun addValue(id: Int, name: String, shouldUseSkin: Boolean) {
        val uuid = if (name.length > 0 && Bukkit.getOfflinePlayer(name)
                .hasPlayedBefore()
        ) Bukkit.getOfflinePlayer(name).uniqueId else UUID.randomUUID()
        this.addValue(id, name, uuid, shouldUseSkin)
    }

    /**
     * Use this to add a new player to the list
     *
     * @param id
     * @param name
     */
    @Deprecated("")
//        (
//        """If all 80 slots have been taken, new values will not be shown and
//                  may have the potential to go out of the registered bounds. Use
//                  the """ updateSlot() " method to change a slot."
//    )
    private fun addValue(id: Int, name: String, uuid: UUID, updateProfToAddCustomSkin: Boolean) {
        val packet =
            ReflectionUtil.instantiate(ReflectionUtil.getConstructor(PACKET_PLAYER_INFO_CLASS).get() as Constructor<*>)
        if (ReflectionUtil.getInstanceField(packet, "b") is List<*>) {
            val players = ReflectionUtil.getInstanceField(packet, "b") as MutableList<Any?>?
            val gameProfile = if (Bukkit.getPlayer(uuid) != null) ReflectionUtil.invokeMethod(
                getHandle(Bukkit.getPlayer(uuid)),
                "getProfile",
                arrayOfNulls<Class<*>?>(0)
            ) else ReflectionUtil.instantiate(
                GAMEPROPHILECONSTRUCTOR, uuid, getNameFromID(id) + name
            )
            val array = ReflectionUtil.invokeMethod(
                CRAFT_CHAT_MESSAGE_CLASS, null, "fromString", arrayOf(
                    String::class.java
                ), getNameFromID(id) + name
            ) as Array<Any>?
            val data = ReflectionUtil.instantiate(
                PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet, gameProfile, 1,
                WORLD_GAME_MODE_NOT_SET, array!![0]
            )
            val call: SkinCallBack = object : SkinCallBack {
                override fun callBack(skin: Skin?, successful: Boolean, exception: Exception?) {
                    val profile =
                        GAMEPROFILECLASS!!.cast(ReflectionUtil.invokeMethod(data, "a", arrayOfNulls<Class<*>?>(0)))
                    if (successful) {
                        try {
                            val map = ReflectionUtil.invokeMethod(profile, "getProperties", arrayOfNulls<Class<*>?>(0))
                            if (skin!!.base64 != null && skin.signedBase64 != null) {
                                if (!ReflectionUtil.isVersionHigherThan(1, 13)) {
                                    ReflectionUtil.invokeMethod(
                                        map, "removeAll", arrayOf(
                                            String::class.java
                                        ),
                                        "textures"
                                    )
                                } else {
                                    ReflectionUtil.invokeMethod(
                                        map, "removeAll", arrayOf(
                                            Any::class.java
                                        ),
                                        "textures"
                                    )
                                }
                                val prop = ReflectionUtil.instantiate(
                                    PROPERTY_CONSTRUCTOR, "textures",
                                    skin.base64, skin.signedBase64
                                )
                                var m: Method? = null
                                for (mm in PROPERTY_MAP!!.methods) if (mm.name == "put") m = mm
                                try {
                                    m?.invoke(map, "textures", prop)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: Error) {
                        }
                    }
                    val getname = ReflectionUtil.invokeMethod(profile, "getName", null) as String?
                    tabs[getIDFromName(getname)] = getname
                    players!!.add(data)
                    datas.add(data)
                    sendNEWTabPackets(player, packet, players, PACKET_PLAYER_INFO_ACTION_ADD_PLAYER)
                }
            }
            if (updateProfToAddCustomSkin) {
                Skin.getSkin(name, call)
            } else {
                Skin.getSkin("aaa", call)
            }
        } else {
            sendOLDTabPackets(player, packet, getNameFromID(id) + name, true)
            tabs[id] = name
            datasOLD[id] = getNameFromID(id) + name
        }
    }

    /**
     * This is used to create the table. If you want to create a custom tablist,
     * then this should be called right after the playlist instance has been
     * created.
     */
    fun initTable() {
        clearAll()
        for (i in 0 until size) updateSlot(i, "", false)
    }

    /**
     * Returns the player.
     *
     * @return the player (if they are online), or null (if they are offline)
     */
    val player: Player
        get() = Bukkit.getPlayer(ownerUUID)

    /**
     * This returns the ID of a slot at [Row,Columb].
     *
     * @param row
     * @param col
     *
     * @return
     */
    fun getID(row: Int, col: Int): Int {
        return col * 20 + row
    }

    init {
        lookUpTable[player.uniqueId.also {
            ownerUUID = it
        }] = this
        tabs = arrayOfNulls(80)
        hasCustomTexture = BooleanArray(80)
        this.size = size
    }
}

/**
 * A small help with reflection
 */
internal object ReflectionUtil {
    private lateinit var SERVER_VERSION: String

    fun isVersionHigherThan(mainVersion: Int, secondVersion: Int): Boolean {
        val firstChar = SERVER_VERSION.substring(1, 2)
        val fInt = firstChar.toInt()
        if (fInt < mainVersion) return false
        val secondChar = StringBuilder()

        for (i in 2..8) {
            if (SERVER_VERSION[i] == '_' || SERVER_VERSION[i] == '.') break
            secondChar.append(SERVER_VERSION[i])
        }

        val sInt = secondChar.toString().toInt()
        return if (sInt < secondVersion) false else true
    }

    /**
     * Returns the NMS class.
     *
     * @param name
     * The name of the class
     *
     * @return The NMS class or null if an error occurred
     */
    fun getNMSClass(name: String): Class<*>? {
        return try {
            Class.forName("net.minecraft.server." + SERVER_VERSION + "." + name)
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    /**
     * Returns the NMS class.
     *
     * @param name
     * The name of the class
     *
     * @return The NMS class or null if an error occurred
     */
    fun getOLDAuthlibClass(name: String): Class<*>? {
        return try {
            Class.forName("net.minecraft.util.com.mojang.authlib.$name")
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    /**
     * Returns the CraftBukkit class.
     *
     * @param name
     * The name of the class
     *
     * @return The CraftBukkit class or null if an error occurred
     */
    fun getCraftbukkitClass(name: String, packageName: String): Class<*>? {
        return try {
            Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + "." + packageName + "." + name)
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    /**
     * Returns the mojang.authlib class.
     *
     * @param name
     * The name of the class
     *
     * @return The mojang.authlib class or null if an error occurred
     */
    fun getMojangAuthClass(name: String): Class<*>? {
        return try {
            if (PlayerList.a()) {
                Class.forName("com.mojang.authlib.$name")
            } else {
                Class.forName("net.minecraft.util.com.mojang.authlib.$name")
            }
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    /**
     * Invokes the method
     *
     * @param handle
     * The handle to invoke it on
     * @param methodName
     * The name of the method
     * @param parameterClasses
     * The parameter types
     * @param args
     * The arguments
     *
     * @return The resulting object or null if an error occurred / the method didn't
     * return a thing
     */
    fun invokeMethod(handle: Any?, methodName: String?, parameterClasses: Array<Class<*>?>?, vararg args: Any?): Any? {
        return invokeMethod(handle!!.javaClass, handle, methodName, parameterClasses, *args)
    }

    /**
     * Invokes the method
     *
     * @param clazz
     * The class to invoke it from
     * @param handle
     * The handle to invoke it on
     * @param methodName
     * The name of the method
     * @param parameterClasses
     * The parameter types
     * @param args
     * The arguments
     *
     * @return The resulting object or null if an error occurred / the method didn't
     * return a thing
     */
    fun invokeMethod(
        clazz: Class<*>?, handle: Any?, methodName: String?, parameterClasses: Array<Class<*>?>?,
        vararg args: Any?
    ): Any? {
        val methodOptional = getMethod(clazz, methodName, *parameterClasses!!)
        if (!methodOptional.isPresent) return null
        val method = methodOptional.get()
        try {
            return method.invoke(handle, *args)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Sets the value of an instance field
     *
     * @param handle
     * The handle to invoke it on
     * @param name
     * The name of the field
     * @param value
     * The new value of the field
     */
    fun setInstanceField(handle: Any?, name: String?, value: Any?) {
        val clazz: Class<*> = handle!!.javaClass
        val fieldOptional = getField(clazz, name)
        if (!fieldOptional.isPresent) return
        val field = fieldOptional.get()
        if (!field.isAccessible) field.isAccessible = true
        try {
            field[handle] = value
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Sets the value of an instance field
     *
     * @param handle
     * The handle to invoke it on
     * @param name
     * The name of the field
     *
     * @return The result
     */
    fun getInstanceField(handle: Any?, name: String?): Any? {
        val clazz: Class<*> = handle!!.javaClass
        val fieldOptional = getField(clazz, name)
        if (!fieldOptional.isPresent) return handle
        val field = fieldOptional.get()
        if (!field.isAccessible) field.isAccessible = true
        try {
            return field[handle]
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Returns an enum constant
     *
     * @param enumClass
     * The class of the enum
     * @param name
     * The name of the enum constant
     *
     * @return The enum entry or null
     */
    fun getEnumConstant(enumClass: Class<*>?, name: String): Any? {
        if (!enumClass!!.isEnum) return null
        for (o in enumClass.enumConstants) if (name == invokeMethod(o, "name", arrayOfNulls<Class<*>?>(0))) return o
        return null
    }

    /**
     * Returns the constructor
     *
     * @param clazz
     * The class
     * @param params
     * The Constructor parameters
     *
     * @return The Constructor or an empty Optional if there is none with these
     * parameters
     */
    fun getConstructor(clazz: Class<*>?, vararg params: Class<*>?): Optional<*> {
        try {
            return Optional.of(clazz!!.getConstructor(*params))
        } catch (e: NoSuchMethodException) {
            try {
                return Optional.of(clazz!!.getDeclaredConstructor(*params))
            } catch (e2: NoSuchMethodException) {
                e2.printStackTrace()
            }
        }
        return Optional.empty<Any>()
    }

    /**
     * Instantiates the class. Will print the errors it gets
     *
     * @param constructor
     * The constructor
     * @param arguments
     * The initial arguments
     *
     * @return The resulting object, or null if an error occurred.
     */
    fun instantiate(constructor: Constructor<*>?, vararg arguments: Any?): Any? {
        try {
            return constructor!!.newInstance(*arguments)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    internal fun getMethod(clazz: Class<*>?, name: String?, vararg params: Class<*>?): Optional<Method> {
        try {
            return Optional.of(clazz!!.getMethod(name, *params))
        } catch (e: NoSuchMethodException) {
            try {
                return Optional.of(clazz!!.getDeclaredMethod(name, *params))
            } catch (e2: NoSuchMethodException) {
                e2.printStackTrace()
            }
        }
        return Optional.empty()
    }

    internal fun getField(clazz: Class<*>, name: String?): Optional<Field> {
        try {
            return Optional.of(clazz.getField(name))
        } catch (e: NoSuchFieldException) {
            try {
                return Optional.of(clazz.getDeclaredField(name))
            } catch (e2: NoSuchFieldException) {
            }
        }
        return Optional.empty()
    }

    init {
        val name = Bukkit.getServer().javaClass.name

        SERVER_VERSION = name.substring(
            name.indexOf("craftbukkit.") + "craftbukkit.".length
        ).substring(
            0,
            name.indexOf(".")
        )
    }
}

internal interface SkinCallBack {
    fun callBack(skin: Skin?, successful: Boolean, exception: Exception?)
}

/**
 * Stores information about a minecraft user's skin.
 *
 * This class does implement ConfigurationSerializable, which means that you can
 * use it to save skins in config, but do however note that for the class to be
 * registered correctly, you should always call NameTagChanger.INSTANCE.enable()
 * in your onEnable() (not before checking if it is already enabled, of course)
 * and call NameTagChanger.INSTANCE.disable() in your onDisable (and again,
 * check if NameTagChanger is already disabled first).
 *
 * @author AlvinB
 */
internal class Skin : ConfigurationSerializable {
    companion object {
        // Access to this must be asynchronous!
        // private static final LoadingCache<UUID, Skin> SKIN_CACHE = CacheBuilder
        var SKIN_CACHE: Any? = null
        var callbacksUUID: MutableMap<UUID, String> = HashMap()
        var callbacks: MutableMap<String?, MutableList<SkinCallBack>> = HashMap()

        /**
         * Gets the skin for a username.
         *
         *
         * Since fetching this skin requires making asynchronous requests to Mojang's
         * servers, a call back mechanism using the SkinCallBack class is implemented.
         * This call back allows you to also handle any errors that might have occurred
         * while fetching the skin. If no users with the specified username can be
         * found, the skin passed to the callback will be Skin.EMPTY_SKIN.
         *
         *
         * The call back will always be fired on the main thread.
         *
         * @param username
         * the username to get the skin of
         * @param callBack
         * the call back to handle the result of the request
         */
        fun getSkin(username: String, callBack: SkinCallBack) {
            var newcall = false
            if (!callbacks.containsKey(username)) {
                callbacks[username] = ArrayList()
                newcall = true
            }
            callbacks[username]!!.add(callBack)
            if (newcall) {
                object : BukkitRunnable() {
                    var u = username
                    override fun run() {
                        val result = MojangAPIUtil.getUUID(listOf(username))
                        if (result.wasSuccessful()) {
                            if (result.value == null || result.value.isEmpty()) {
                                object : BukkitRunnable() {
                                    override fun run() {
                                        val calls: List<SkinCallBack> = callbacks[u]!!
                                        callbacks.remove(u)
                                        for (s in calls) {
                                            s.callBack(EMPTY_SKIN, true, null)
                                        }
                                    }
                                }.runTask(PlayerList.plugin)
                                return
                            }
                            for ((key, value) in result.value) {
                                if (key.equals(username, ignoreCase = true)) {
                                    callbacksUUID[value.uUID] = u
                                    getSkin(value.uUID, callBack)
                                    return
                                }
                            }
                        } else {
                            object : BukkitRunnable() {
                                override fun run() {
                                    val calls: List<SkinCallBack> = callbacks[u]!!
                                    callbacks.remove(u)
                                    for (s in calls) {
                                        s.callBack(null, false, result.exception)
                                    }
                                }
                            }.runTask(PlayerList.plugin)
                        }
                    }
                }.runTaskAsynchronously(PlayerList.plugin)
            }
        }

        /**
         * Gets the skin for a UUID.
         *
         *
         * Since fetching this skin might require making asynchronous requests to
         * Mojang's servers, a call back mechanism using the SkinCallBack class is
         * implemented. This call back allows you to also handle any errors that might
         * have occurred while fetching the skin.
         *
         *
         * The call back will always be fired on the main thread.
         *
         * @param uuid
         * the uuid to get the skin of
         * @param callBack
         * the call back to handle the result of the request
         */
        fun getSkin(uuid: UUID, callBack: SkinCallBack) {
            /*
         * if(!skin_Enabled) { callBack.callBack(Skin.EMPTY_SKIN, false, null); return;
         * } // Map<UUID, Skin> asMap = SKIN_CACHE.asMap(); try {
         * SKIN_CACHE.getClass().getDeclaredMethod("asMap", new Class[0]); } catch
         * (Exception e1) { callBack.callBack(Skin.EMPTY_SKIN, false, null); return; }
         */

            // @SuppressWarnings("unchecked")
            // Map<UUID, Skin> asMap = (Map<UUID, Skin>)
            // ReflectionUtil.invokeMethod(SKIN_CACHE, "asMap", new Class[0]);
            var asMap: Map<UUID?, Skin?>? = null
            try {
                asMap = (SKIN_CACHE as LoadingCache<UUID?, Skin?>?)?.asMap()
            } catch (e4: Exception) {
                callBack.callBack(EMPTY_SKIN, true, null)
                return
            } catch (e4: Error) {
                callBack.callBack(EMPTY_SKIN, true, null)
                return
            }
            if (asMap?.containsKey(uuid) == true) {
                for (s in callbacks[callbacksUUID[uuid]]!!) {
                    s.callBack(asMap[uuid], true, null)
                }
            } else {
                object : BukkitRunnable() {
                    override fun run() {
                        try {
                            // Skin skin = SKIN_CACHE.get(uuid);
                            // = (Skin) ReflectionUtil.invokeMethod(SKIN_CACHE, "get", new Class[] {
                            // UUID.class },uuid);
                            val skin = (SKIN_CACHE as LoadingCache<UUID?, Skin?>?)!![uuid]
                            object : BukkitRunnable() {
                                override fun run() {
                                    for (s in callbacks[callbacksUUID[uuid]]!!) {
                                        s.callBack(skin, true, null)
                                    }
                                }
                            }.runTask(PlayerList.plugin)
                        } catch (e: Exception) {
                            object : BukkitRunnable() {
                                override fun run() {
                                    for (s in callbacks[callbacksUUID[uuid]]!!) {
                                        s.callBack(null, false, e)
                                    }
                                }
                            }.runTask(PlayerList.plugin)
                        }
                    }
                }.runTaskAsynchronously(PlayerList.plugin)
            }
        }

        val EMPTY_SKIN = Skin()
        fun deserialize(map: Map<String?, Any?>): Skin {
            return if (map.containsKey("empty")) {
                EMPTY_SKIN
            } else {
                Skin(
                    UUID.fromString(map["uuid"] as String?), map["base64"] as String?,
                    if (map.containsKey("signedBase64")) map["signedBase64"] as String? else null
                )
            }
        }

        // private static boolean skin_Enabled = false;
        init {
            try {
                SKIN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
                    .build(object : CacheLoader<UUID, Skin>() {
                        @Throws(Exception::class)
                        override fun load(uuid: UUID): Skin {
                            val result = MojangAPIUtil.getSkinData(uuid)
                            if (result.wasSuccessful()) {
                                if (result.value != null) {
                                    val data = result.value
                                    return if (data.skinURL == null && data.capeURL == null) {
                                        EMPTY_SKIN
                                    } else Skin(data.uUID, data.base64, data.signedBase64)
                                }
                            } else {
                                throw result.exception!!
                            }
                            return EMPTY_SKIN
                        }
                    })
                // skin_Enabled = true;
            } catch (e5: Exception) {
            } catch (e5: Error) {
            }
        }
    }

    var uUID: UUID? = null
        private set
    var base64: String? = null
        private set
    var signedBase64: String? = null
        private set

    /**
     * Initializes this class with the specified skin.
     *
     * @param uuid
     * The uuid of the user who this skin belongs to
     * @param base64
     * the base64 data of the skin, as returned by Mojang's servers.
     * @param signedBase64
     * the signed data of the skin, as returned by Mojang's servers.
     */
    constructor(uuid: UUID?, base64: String?, signedBase64: String?) {
        Validate.notNull(uuid, "uuid cannot be null")
        Validate.notNull(base64, "base64 cannot be null")
        uUID = uuid
        this.base64 = base64
        this.signedBase64 = signedBase64
    }

    private constructor() {}

    fun hasSignedBase64(): Boolean {
        return signedBase64 != null
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj !is Skin) {
            return false
        }
        val skin = obj
        return if (skin === EMPTY_SKIN) {
            this === EMPTY_SKIN
        } else skin.base64 == base64 && skin.uUID == uUID && skin.signedBase64 == signedBase64
    }

    override fun hashCode(): Int {
        return Objects.hash(base64, uUID, signedBase64)
    }

    override fun toString(): String {
        return "Skin{uuid=" + uUID + ",base64=" + base64 + ",signedBase64=" + signedBase64 + "}"
    }

    override fun serialize(): Map<String, Any> {
        val map: MutableMap<String, Any> = Maps.newHashMap()
        if (this === EMPTY_SKIN) {
            map["empty"] = "true"
        } else {
            map["uuid"] = uUID!!
            map["base64"] = base64!!
            if (hasSignedBase64()) {
                map["signedBase64"] = signedBase64!!
            }
        }
        return map
    }
}

/**
 * Implementation to make requests to Mojang's API servers. See
 * http://wiki.vg/Mojang_API for more information.
 *
 *
 * Since all of these methods require connections to Mojang's servers, all of
 * them execute asynchronously, and do therefor not return any values. Instead,
 * a callback mechanism is implemented, which allows for processing of data
 * returned from these requests. If an error occurs when retrieving the data,
 * the 'successful' boolean in the callback will be set to false. In these
 * cases, null will be passed to the callback, even if some data has been
 * received.
 *
 *
 * Each method has an synchronous and an asynchronous version. It is recommended
 * that you use the synchronous version unless you're intending to do more tasks
 * that should be executed asynchronously.
 *
 * @author AlvinB
 */
internal object MojangAPIUtil {
    private var API_STATUS_URL: URL? = null
    private var GET_UUID_URL: URL? = null
    private val PARSER = JSONParser()
    private var plugin: Plugin? = null

    /**
     * Sets the plugin instance to use for scheduler tasks.
     *
     *
     * The plugin instance in the same jar as this class should automatically be
     * found, so only use this if you for whatever reason need to use another plugin
     * instance.
     *
     * @param plugin
     * the plugin instance
     */
    fun setPlugin(plugin: Plugin?) {
        MojangAPIUtil.plugin = plugin
    }

    /**
     * Same as #getAPIStatusAsync, but the callback is executed synchronously
     */
    fun getAPIStatusWithCallBack(callBack: ResultCallBack<Map<String?, APIStatus?>?>) {
        getAPIStatusAsyncWithCallBack { successful: Boolean, result: Map<String?, APIStatus?>?, exception: Exception? ->
            object : BukkitRunnable() {
                override fun run() {
                    callBack.callBack(successful, result, exception)
                }
            }.runTask(plugin)
        }
    }

    /**
     * Gets the current state of Mojang's API
     *
     *
     * The keys of the map passed to the callback is the service, and the value is
     * the current state of the service. Statuses can be either RED (meaning service
     * unavailable), YELLOW (meaning service available, but with some issues) and
     * GREEN (meaning service fully functional).
     *
     * @param callBack
     * the callback of the request
     * @see APIStatus
     */
    fun getAPIStatusAsyncWithCallBack(callBack: ResultCallBack<Map<String?, APIStatus?>?>?) {
        if (plugin == null) {
            return
        }
        makeAsyncGetRequest(API_STATUS_URL) { successful: Boolean, response: String?, exception: Exception?, responseCode: Int ->
            if (callBack == null) {
                return@makeAsyncGetRequest
            }
            if (successful && responseCode == 200) {
                try {
                    val map: MutableMap<String?, APIStatus?> = Maps.newHashMap()
                    val jsonArray = PARSER.parse(response) as JSONArray
                    for (jsonObject in jsonArray as List<JSONObject>) {
                        for ((key, value) in jsonObject as Map<String?, String>) {
                            map[key] = APIStatus.fromString(value)
                        }
                    }
                    callBack.callBack(true, map, null)
                } catch (e: Exception) {
                    callBack.callBack(false, null, e)
                }
            } else {
                if (exception != null) {
                    callBack.callBack(false, null, exception)
                } else {
                    callBack.callBack(
                        false, null,
                        IOException("Failed to obtain Mojang data! Response code: $responseCode")
                    )
                }
            }
        }
    }

    /**
     * Same as #getUUIDAtTimeAsync, but the callback is executed synchronously
     */
    fun getUUIDAtTimeWithCallBack(username: String, timeStamp: Long, callBack: ResultCallBack<UUIDAtTime?>) {
        getUUIDAtTimeAsyncWithCallBack(
            username,
            timeStamp
        ) { successful: Boolean, result: UUIDAtTime?, exception: Exception? ->
            object : BukkitRunnable() {
                override fun run() {
                    callBack.callBack(successful, result, exception)
                }
            }.runTask(plugin)
        }
    }

    /**
     * Gets the UUID of a name at a certain point in time
     *
     *
     * The timestamp is in UNIX Time, and if -1 is used as the timestamp, it will
     * get the current user who has this name.
     *
     *
     * The callback contains the UUID and the current username of the UUID. If the
     * username was not occupied at the specified time, the next person to occupy
     * the name will be returned, provided that the name has been changed away from
     * at least once or is legacy. If the name hasn't been changed away from and is
     * not legacy, the value passed to the callback will be null.
     *
     * @param username
     * the username of the player to do the UUID lookup on
     * @param timeStamp
     * the timestamp when the name was occupied
     * @param callBack
     * the callback of the request
     */
    fun getUUIDAtTimeAsyncWithCallBack(
        username: String, timeStamp: Long,
        callBack: ResultCallBack<UUIDAtTime?>?
    ) {
        if (plugin == null) {
            return
        }
        Validate.notNull(username)
        Validate.isTrue(!username.isEmpty(), "username cannot be empty")
        try {
            val url = URL(
                "https://api.mojang.com/users/profiles/minecraft/" + username
                        + if (timeStamp != -1L) "?at=$timeStamp" else ""
            )
            makeAsyncGetRequest(url) { successful: Boolean, response: String?, exception: Exception?, responseCode: Int ->
                if (callBack == null) {
                    return@makeAsyncGetRequest
                }
                if (successful && (responseCode == 200 || responseCode == 204)) {
                    try {
                        val uuidAtTime = arrayOfNulls<UUIDAtTime>(1)
                        if (responseCode == 200) {
                            val `object` = PARSER.parse(response) as JSONObject
                            val uuidString = `object`["id"] as String
                            uuidAtTime[0] = UUIDAtTime(`object`["name"] as String, getUUIDFromString(uuidString))
                        }
                        callBack.callBack(true, uuidAtTime[0], null)
                    } catch (e: Exception) {
                        callBack.callBack(false, null, e)
                    }
                } else {
                    if (exception != null) {
                        callBack.callBack(false, null, exception)
                    } else {
                        callBack.callBack(
                            false, null,
                            IOException("Failed to obtain Mojang data! Response code: $responseCode")
                        )
                    }
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    /**
     * Same as #getNameHistoryAsync, but the callback is executed synchronously
     */
    fun getNameHistoryWithCallBack(uuid: UUID, callBack: ResultCallBack<Map<String?, Long?>?>) {
        getNameHistoryAsyncWithCallBack(uuid) { successful: Boolean, result: Map<String?, Long?>?, exception: Exception? ->
            object : BukkitRunnable() {
                override fun run() {
                    callBack.callBack(successful, result, exception)
                }
            }.runTask(plugin)
        }
    }

    /**
     * Gets the name history of a certain UUID
     *
     *
     * The callback is passed a Map<String></String>, Long>, the String being the name, and
     * the long being the UNIX millisecond timestamp the user changed to that name.
     * If the name was the original name of the user, the long will be -1L.
     *
     *
     * If an unused UUID is supplied, an empty Map will be passed to the callback.
     *
     * @param uuid
     * the uuid of the account
     * @param callBack
     * the callback of the request
     */
    fun getNameHistoryAsyncWithCallBack(uuid: UUID, callBack: ResultCallBack<Map<String?, Long?>?>?) {
        if (plugin == null) {
            return
        }
        Validate.notNull(uuid, "uuid cannot be null!")
        try {
            val url = URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names")
            makeAsyncGetRequest(url) { successful: Boolean, response: String?, exception: Exception?, responseCode: Int ->
                if (callBack == null) {
                    return@makeAsyncGetRequest
                }
                if (successful && (responseCode == 200 || responseCode == 204)) {
                    try {
                        val map: MutableMap<String?, Long?> = Maps.newHashMap()
                        if (responseCode == 200) {
                            val jsonArray = PARSER.parse(response) as JSONArray
                            for (jsonObject in jsonArray as List<JSONObject>) {
                                val name = jsonObject["name"] as String
                                if (jsonObject.containsKey("changedToAt")) {
                                    map[name] = jsonObject["changedToAt"] as Long?
                                } else {
                                    map[name] = -1L
                                }
                            }
                        }
                        callBack.callBack(true, map, null)
                    } catch (e: Exception) {
                        callBack.callBack(false, null, e)
                    }
                } else {
                    if (exception != null) {
                        callBack.callBack(false, null, exception)
                    } else {
                        callBack.callBack(
                            false, null,
                            IOException("Failed to obtain Mojang data! Response code: $responseCode")
                        )
                    }
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    fun getUUIDWithCallBack(callBack: ResultCallBack<Map<String?, Profile?>?>, vararg usernames: String?) {
        getUUIDWithCallBack(Arrays.asList(*usernames), callBack)
    }

    /**
     * Same as #getUUIDAsync, but the callback is executed synchronously
     */
    fun getUUIDWithCallBack(usernames: List<String?>, callBack: ResultCallBack<Map<String?, Profile?>?>) {
        getUUIDAsyncWithCallBack(usernames) { successful: Boolean, result: Map<String?, Profile?>?, exception: Exception? ->
            object : BukkitRunnable() {
                override fun run() {
                    callBack.callBack(successful, result, exception)
                }
            }.runTask(plugin)
        }
    }

    fun getUUIDAsyncWithCallBack(callBack: ResultCallBack<Map<String?, Profile?>?>?, vararg usernames: String?) {
        getUUIDAsyncWithCallBack(Arrays.asList(*usernames), callBack)
    }

    /**
     * Same as #getUUIDWithCallBack but is entirely executed on the current thread.
     * Should be used with caution to avoid blocking any important activities on the
     * current thread.
     */
    fun getUUID(usernames: List<String?>): Result<Map<String, Profile>?> {
        if (plugin == null) {
            return Result(null, false, RuntimeException("No plugin instance found!"))
        }
        Validate.notNull(usernames, "usernames cannot be null")
        Validate.isTrue(usernames.size <= 100, "cannot request more than 100 usernames at once")
        val usernameJson = JSONArray()
        usernameJson.addAll(usernames.stream().filter { s: String? -> !Strings.isNullOrEmpty(s) }
            .collect(Collectors.toList()))
        val result = makeSyncPostRequest(GET_UUID_URL, usernameJson.toJSONString())
            ?: return Result(null, false, RuntimeException("No plugin instance found!"))
        return try {
            if (result.successful && result.responseCode == 200) {
                val map: MutableMap<String, Profile> = Maps.newHashMap()
                val jsonArray = PARSER.parse(result.response) as JSONArray
                // noinspection Duplicates
                for (jsonObject in jsonArray as List<JSONObject>) {
                    val uuidString = jsonObject["id"] as String
                    val name = jsonObject["name"] as String
                    var legacy = false
                    if (jsonObject.containsKey("legacy")) {
                        legacy = jsonObject["legacy"] as Boolean
                    }
                    var unpaid = false
                    if (jsonObject.containsKey("demo")) {
                        unpaid = jsonObject["demo"] as Boolean
                    }
                    map[name] = Profile(getUUIDFromString(uuidString), name, legacy, unpaid)
                }
                Result(map, true, null)
            } else {
                if (result.exception != null) {
                    Result(null, false, result.exception)
                } else {
                    Result(
                        null, false,
                        IOException("Failed to obtain Mojang data! Response code: " + result.responseCode)
                    )
                }
            }
        } catch (e: Exception) {
            Result(null, false, e)
        }
    }

    /**
     * Gets the Profiles of up to 100 usernames.
     *
     * @param usernames
     * the usernames
     * @param callBack
     * the callback
     */
    fun getUUIDAsyncWithCallBack(usernames: List<String?>, callBack: ResultCallBack<Map<String?, Profile?>?>?) {
        if (plugin == null) {
            return
        }
        Validate.notNull(usernames, "usernames cannot be null")
        Validate.isTrue(usernames.size <= 100, "cannot request more than 100 usernames at once")
        val usernameJson = JSONArray()
        usernameJson.addAll(usernames.stream().filter { s: String? -> !Strings.isNullOrEmpty(s) }
            .collect(Collectors.toList()))
        makeAsyncPostRequest(
            GET_UUID_URL, usernameJson.toJSONString()
        ) { successful: Boolean, response: String?, exception: Exception?, responseCode: Int ->
            if (callBack == null) {
                return@makeAsyncPostRequest
            }
            try {
                if (successful && responseCode == 200) {
                    val map: MutableMap<String?, Profile?> = Maps.newHashMap()
                    val jsonArray = PARSER.parse(response) as JSONArray
                    // noinspection Duplicates
                    for (jsonObject in jsonArray as List<JSONObject>) {
                        val uuidString = jsonObject["id"] as String
                        val name = jsonObject["name"] as String
                        var legacy = false
                        if (jsonObject.containsKey("legacy")) {
                            legacy = jsonObject["legacy"] as Boolean
                        }
                        var unpaid = false
                        if (jsonObject.containsKey("demo")) {
                            unpaid = jsonObject["demo"] as Boolean
                        }
                        map[name] = Profile(getUUIDFromString(uuidString), name, legacy, unpaid)
                    }
                    callBack.callBack(true, map, null)
                } else {
                    if (exception != null) {
                        callBack.callBack(false, null, exception)
                    } else {
                        callBack.callBack(
                            false, null, IOException(
                                "Failed to obtain Mojang data! Response code: $responseCode"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                callBack.callBack(false, null, e)
            }
        }
    }

    /**
     * Same as #getSkinDataWithCallBack but is entirely executed on the current
     * thread. Should be used with caution to avoid blocking any important
     * activities on the current thread.
     */
    fun getSkinData(uuid: UUID): Result<SkinData?> {
        if (plugin == null) {
            return Result(null, false, RuntimeException("No plugin instance found!"))
        }
        val url: URL
        url = try {
            URL(
                "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + uuid.toString().replace("-", "") + "?unsigned=false"
            )
        } catch (e: MalformedURLException) {
            return Result(null, false, e)
        }
        val result = makeSyncGetRequest(url)
            ?: return Result(null, false, RuntimeException("No plugin instance found!"))
        return try {
            if (result.successful && (result.responseCode == 200 || result.responseCode == 204)) {
                if (result.responseCode == 204) {
                    return Result(null, true, null)
                }
                val `object` = PARSER.parse(result.response) as JSONObject
                val propertiesArray = `object`["properties"] as JSONArray
                var base64: String? = null
                var signedBase64: String? = null
                // noinspection Duplicates
                for (property in propertiesArray as List<JSONObject>) {
                    val name = property["name"] as String
                    if (name == "textures") {
                        base64 = property["value"] as String?
                        signedBase64 = property["signature"] as String?
                    }
                }
                if (base64 == null) {
                    return Result(null, true, null)
                }
                val decodedBase64 = String(Base64.getDecoder().decode(base64), Charsets.UTF_8)
                val base64json = PARSER.parse(decodedBase64) as JSONObject
                val timeStamp = base64json["timestamp"] as Long
                val profileName = base64json["profileName"] as String
                val profileId = getUUIDFromString(base64json["profileId"] as String)
                val textures = base64json["textures"] as JSONObject
                var skinURL: String? = null
                var capeURL: String? = null
                if (textures.containsKey("SKIN")) {
                    val skinObject = textures["SKIN"] as JSONObject
                    skinURL = skinObject["url"] as String?
                }
                if (textures.containsKey("CAPE")) {
                    val capeObject = textures["CAPE"] as JSONObject
                    capeURL = capeObject["url"] as String?
                }
                Result(
                    SkinData(profileId, profileName, skinURL, capeURL, timeStamp, base64, signedBase64), true,
                    null
                )
            } else {
                if (result.exception != null) {
                    Result(null, false, result.exception)
                } else {
                    Result(
                        null, false,
                        IOException("Failed to obtain Mojang data! Response code: " + result.responseCode)
                    )
                }
            }
        } catch (e: Exception) {
            Result(null, false, e)
        }
    }

    /**
     * Same as #getSkinDataAsync, but the callback is executed synchronously
     */
    fun getSkinData(uuid: UUID, callBack: ResultCallBack<SkinData?>) {
        getSkinDataAsync(uuid) { successful: Boolean, result: SkinData?, exception: Exception? ->
            object : BukkitRunnable() {
                override fun run() {
                    callBack.callBack(successful, result, exception)
                }
            }.runTask(plugin)
        }
    }

    /**
     * Gets the Skin data for a certain user. If the user cannot be found, the value
     * passed to the callback will be null.
     *
     * @param uuid
     * the uuid of the user
     * @param callBack
     * the callback
     */
    fun getSkinDataAsync(uuid: UUID, callBack: ResultCallBack<SkinData?>) {
        if (plugin == null) {
            return
        }
        val url: URL
        url = try {
            URL(
                "https://sessionserver.mojang.com/session/minecraft/profile/"
                        + uuid.toString().replace("-", "") + "?unsigned=false"
            )
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return
        }
        makeAsyncGetRequest(url) { successful: Boolean, response: String?, exception: Exception?, responseCode: Int ->
            try {
                if (successful && (responseCode == 200 || responseCode == 204)) {
                    if (responseCode == 204) {
                        callBack.callBack(true, null, null)
                        return@makeAsyncGetRequest
                    }
                    val `object` = PARSER.parse(response) as JSONObject
                    val propertiesArray = `object`["properties"] as JSONArray
                    var base64: String? = null
                    var signedBase64: String? = null
                    // noinspection Duplicates
                    for (property in propertiesArray as List<JSONObject>) {
                        val name = property["name"] as String
                        if (name == "textures") {
                            base64 = property["value"] as String?
                            signedBase64 = property["signature"] as String?
                        }
                    }
                    if (base64 == null) {
                        callBack.callBack(true, null, null)
                        return@makeAsyncGetRequest
                    }
                    val decodedBase64 = String(Base64.getDecoder().decode(base64), Charsets.UTF_8)
                    val base64json = PARSER.parse(decodedBase64) as JSONObject
                    val timeStamp = base64json["timestamp"] as Long
                    val profileName = base64json["profileName"] as String
                    val profileId = getUUIDFromString(base64json["profileId"] as String)
                    val textures = base64json["textures"] as JSONObject
                    var skinURL: String? = null
                    var capeURL: String? = null
                    if (textures.containsKey("SKIN")) {
                        val skinObject = textures["SKIN"] as JSONObject
                        skinURL = skinObject["url"] as String?
                    }
                    if (textures.containsKey("CAPE")) {
                        val capeObject = textures["CAPE"] as JSONObject
                        capeURL = capeObject["url"] as String?
                    }
                    callBack.callBack(
                        true,
                        SkinData(profileId, profileName, skinURL, capeURL, timeStamp, base64, signedBase64),
                        null
                    )
                } else {
                    if (exception != null) {
                        callBack.callBack(false, null, exception)
                    } else {
                        callBack.callBack(
                            false, null,
                            IOException("Failed to obtain Mojang data! Response code: $responseCode")
                        )
                    }
                }
            } catch (e: Exception) {
                callBack.callBack(false, null, e)
            }
        }
    }

    private fun makeSyncGetRequest(url: URL): RequestResult? {
        if (plugin == null) {
            return null
        }
        val response = StringBuilder()
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    response.append(line)
                    line = reader.readLine()
                }
                val result = RequestResult()
                result.successful = true
                result.responseCode = connection.responseCode
                result.response = response.toString()
                return result
            }
        } catch (e: IOException) {
            val result = RequestResult()
            result.exception = e
            result.successful = false
            return result
        }
    }

    private fun makeAsyncGetRequest(url: URL?, asyncCallBack: RequestCallBack) {
        if (plugin == null) {
            return
        }
        object : BukkitRunnable() {
            override fun run() {
                val response = StringBuilder()
                try {
                    val connection = url!!.openConnection() as HttpURLConnection
                    connection.connect()
                    BufferedReader(
                        InputStreamReader(connection.inputStream)
                    ).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            response.append(line)
                            line = reader.readLine()
                        }
                        asyncCallBack.callBack(true, response.toString(), null, connection.responseCode)
                    }
                } catch (e: Exception) {
                    asyncCallBack.callBack(false, response.toString(), e, -1)
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    private fun makeSyncPostRequest(url: URL?, payload: String): RequestResult? {
        if (plugin == null) {
            return null
        }
        val response = StringBuilder()
        try {
            val connection = url!!.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connect()
            PrintWriter(connection.outputStream).use { writer -> writer.write(payload) }
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    response.append(line)
                    line = reader.readLine()
                }
                val result = RequestResult()
                result.successful = true
                result.responseCode = connection.responseCode
                result.response = response.toString()
                return result
            }
        } catch (e: IOException) {
            val result = RequestResult()
            result.successful = false
            result.exception = e
            return result
        }
    }

    private fun makeAsyncPostRequest(url: URL?, payload: String, asyncCallBack: RequestCallBack) {
        if (plugin == null) {
            return
        }
        object : BukkitRunnable() {
            override fun run() {
                val response = StringBuilder()
                try {
                    val connection = url!!.openConnection() as HttpURLConnection
                    connection.doOutput = true
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.connect()
                    PrintWriter(connection.outputStream).use { writer -> writer.write(payload) }
                    BufferedReader(
                        InputStreamReader(connection.inputStream)
                    ).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            response.append(line)
                            line = reader.readLine()
                        }
                        asyncCallBack.callBack(true, response.toString(), null, connection.responseCode)
                    }
                } catch (e: Exception) {
                    asyncCallBack.callBack(false, response.toString(), e, -1)
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun getUUIDFromString(string: String): UUID {
        val uuidString = (string.substring(0, 8) + "-" + string.substring(8, 12) + "-" + string.substring(12, 16)
                + "-" + string.substring(16, 20) + "-" + string.substring(20))
        return UUID.fromString(uuidString)
    }

    /**
     * The statuses of Mojang's API used by getAPIStatus().
     */
    enum class APIStatus {
        RED, YELLOW, GREEN;

        companion object {
            fun fromString(string: String): APIStatus {
                return when (string) {
                    "red" -> RED
                    "yellow" -> YELLOW
                    "green" -> GREEN
                    else -> throw IllegalArgumentException("Unknown status: $string")
                }
            }
        }
    }

    class UUIDAtTime(val name: String, val uUID: UUID) {
        override fun toString(): String {
            return "UUIDAtTime{name=" + name + ",uuid=" + uUID + "}"
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) {
                return true
            }
            if (obj !is UUIDAtTime) {
                return false
            }
            val uuidAtTime = obj
            return name == uuidAtTime.name && uUID == uuidAtTime.uUID
        }

        override fun hashCode(): Int {
            return Objects.hash(name, uUID)
        }
    }

    class Profile internal constructor(val uUID: UUID, val name: String, val isLegacy: Boolean, val isUnpaid: Boolean) {
        override fun toString(): String {
            return "Profile{uuid=" + uUID + ", name=" + name + ", legacy=" + isLegacy + ", unpaid=" + isUnpaid + "}"
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) {
                return true
            }
            if (obj !is Profile) {
                return false
            }
            val otherProfile = obj
            return uUID == otherProfile.uUID && name == otherProfile.name && isLegacy == otherProfile.isLegacy && isUnpaid == otherProfile.isUnpaid
        }

        override fun hashCode(): Int {
            return Objects.hash(uUID, name, isLegacy, isUnpaid)
        }
    }

    class SkinData(
        val uUID: UUID,
        val name: String,
        val skinURL: String?,
        val capeURL: String?,
        val timeStamp: Long,
        val base64: String,
        val signedBase64: String?
    ) {
        fun hasSkinURL(): Boolean {
            return skinURL != null
        }

        fun hasCapeURL(): Boolean {
            return capeURL != null
        }

        fun hasSignedBase64(): Boolean {
            return signedBase64 != null
        }

        override fun toString(): String {
            return ("SkinData{uuid=" + uUID + ",name=" + name + ",skinURL=" + skinURL + ",capeURL=" + capeURL
                    + ",timeStamp=" + timeStamp + ",base64=" + base64 + ",signedBase64=" + signedBase64 + "}")
        }

        override fun equals(obj: Any?): Boolean {
            if (obj === this) {
                return true
            }
            if (obj !is SkinData) {
                return false
            }
            val skinData = obj
            return (uUID == skinData.uUID && name == skinData.name && (if (skinURL == null) skinData.skinURL == null else skinURL == skinData.skinURL)
                    && (if (capeURL == null) skinData.capeURL == null else capeURL == skinData.skinURL)
                    && timeStamp == skinData.timeStamp && base64 == skinData.base64 && if (signedBase64 == null) skinData.signedBase64 == null else signedBase64 == skinData.signedBase64)
        }

        override fun hashCode(): Int {
            return Objects.hash(uUID, name, skinURL, capeURL, timeStamp, base64, signedBase64)
        }
    }

    private fun interface RequestCallBack {
        fun callBack(successful: Boolean, response: String?, exception: Exception?, responseCode: Int)
    }

    private class RequestResult {
        var successful = false
        var response: String? = null
        var exception: Exception? = null
        var responseCode = 0
    }

    /**
     * The callback interface
     *
     *
     * Once some data is received (or an error is thrown) the callBack method is
     * fired with the following data:
     *
     *
     * boolean successful - If the data arrived and was interpreted correctly.
     *
     *
     * <T> result - The data. Only present if successful is true, otherwise null.
    </T> *
     *
     * Exception e - The exception. Only present if successful is false, otherwise
     * null.
     *
     *
     * This interface is annotated with @FunctionalInterface, which allows for
     * instantiation using lambda expressions.
     */
    fun interface ResultCallBack<T> {
        fun callBack(successful: Boolean, result: T, exception: Exception?)
    }

    class Result<T>(val value: T, private val successful: Boolean, val exception: Exception?) {
        fun wasSuccessful(): Boolean {
            return successful
        }
    }

    init {
        for (plugin in Bukkit.getPluginManager().plugins) {
            if (plugin::class.java.getProtectionDomain().getCodeSource()
                == MojangAPIUtil::class.java.protectionDomain.codeSource
            ) {
                MojangAPIUtil.plugin = plugin
            }
        }
        try {
            API_STATUS_URL = URL("https://status.mojang.com/check")
            GET_UUID_URL = URL("https://api.mojang.com/profiles/minecraft")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }
}