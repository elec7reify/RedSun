package com.flexingstudios

import net.minecraft.server.v1_12_R1.InventoryEnderChest
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools
import net.minecraft.server.v1_12_R1.NBTTagCompound
import net.minecraft.server.v1_12_R1.NBTTagDouble
import net.minecraft.server.v1_12_R1.NBTTagFloat
import net.minecraft.server.v1_12_R1.NBTTagList
import net.minecraft.server.v1_12_R1.PlayerInventory
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer
import org.bukkit.entity.AnimalTamer
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.util.Vector
import org.jetbrains.annotations.NotNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class ImprovedPlayerData :
    AnimalTamer,
    InventoryHolder
{
    @get:JvmName("isDataExists")
    var dataExists = false
        private set
    private lateinit var player: String
    private lateinit var uuid: UUID
    private lateinit var file: File
    private lateinit var compound: NBTTagCompound

    constructor(player: String) {
        dataExists = loadData(player)
    }

    constructor(offlinePlayer: OfflinePlayer) {
        dataExists = loadData(offlinePlayer.name)
    }

    private fun loadData(name: String): Boolean {
        try {
            player = name
            uuid = Bukkit.getPlayerUniqueId(name)!!
            for (world in Bukkit.getWorlds()) {
                file = File(world.worldFolder, "playerdata" + File.separator + uuid + ".dat")
                if (file.exists()) {
                    compound = NBTCompressedStreamTools.a(FileInputStream(file))
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun saveData() {
        if (dataExists)
            try {
                NBTCompressedStreamTools.a(compound, FileOutputStream(file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    override fun getName(): String = compound.getCompound("bukkit").getString("lastKnownName")

    override fun getUniqueId(): UUID = uuid

    override fun getInventory(): Inventory {
        val inventory = PlayerInventory(null)
        inventory.b(compound.getList("Inventory", 10))
        return CraftInventoryPlayer(inventory)
    }

    /**
     * Get the player's EnderChest inventory
     *
     * @return The EnderChest of the player
     */
    fun getEnderChest(): Inventory {
        val enderChest = InventoryEnderChest(null)
        enderChest.a(compound.getList("EnderItems", 10))
        return CraftInventory(enderChest)
    }

    /**
     * Sets the players current food level
     *
     * @param value new food level
     */
    fun setFoodLevel(value: Int) = compound.setInt("foodLevel", value)

    /**
     * Gets the players current food level
     *
     * @return food level
     */
    fun getFoodLevel(): Int = compound.getInt("foodLevel")

    /**
     * Sets the players current exhaustion level
     *
     * @param value Exhaustion level
     */
    fun setExhaustion(value: Float) = compound.setFloat("foodExhaustionLevel", value)

    /**
     * Gets the players current exhaustion level
     *
     * @return Exhaustion level
     */
    fun getExhaustion(): Float = compound.getFloat("foodExhaustionLevel")

    /**
     * Sets the players current saturation level
     *
     * @param value Saturation level
     */
    fun setSaturation(value: Float) = compound.setFloat("foodSaturationLevel", value)

    /**
     * Gets the players current saturation level
     *
     * @return Saturation level
     */
    fun getSaturation(): Float = compound.getFloat("foodSaturationLevel")

    /**
     * Sets the speed at which a player will walk
     *
     * @param value The new speed value
     */
    fun setWalkSpeed(value: Float) = compound.getCompound("abilities").setFloat("walkSpeed", value)

    /**
     * Gets the current allowed speed that a player can walk
     *
     * @return The current allowed speed, from -1 to 1
     */
    fun getWalkSpeed(): Float = compound.getCompound("abilities").getFloat("walkSpeed")

    /**
     * Makes this player start or stop flying
     *
     * @param value true to fly
     */
    fun setFlying(value: Boolean) = compound.getCompound("abilities").setBoolean("flying", value)

    /**
     * Sets the speed at which a player will fly
     *
     * @param value The new speed
     */
    fun setFlySpeed(value: Float) = compound.getCompound("abilities").setFloat("flySpeed", value)

    /**
     * Gets the current allowed speed that a player can fly
     *
     * @return The current allowed speed, from -1 to 1
     */
    fun getFlySpeed(): Float = compound.getCompound("abilities").getFloat("flySpeed")

    /**
     * Checks to see if this player is currently flying or not
     *
     * @return true if the player is flying, else false
     */
    fun isFlying(): Boolean = compound.getCompound("abilities").getBoolean("flying")

    /**
     * Sets this human's current [GameMode]
     *
     * @param mode new game mode
     */
    fun setGameMode(mode: GameMode) = compound.setInt("playerGameType", mode.ordinal)

    /**
     * Gets this human's current [GameMode]
     *
     * @return current game mode
     */
    fun getGameMode(): GameMode = GameMode.values()[compound.getInt("playerGameType")]

    /**
     * Teleports this entity to the given location
     *
     * @param location new location to teleport this player to
     */
    fun teleport(location: Location) {
        val world: World = location.world
        val uuid: UUID = world.uid
        compound.setLong("WorldUUIDMost", uuid.mostSignificantBits)
        compound.setLong("WorldUUIDLeast", uuid.leastSignificantBits)
        compound.setInt("Dimension", world.environment.ordinal)

        val position = NBTTagList()
        position.add(NBTTagDouble(location.x))
        position.add(NBTTagDouble(location.y))
        position.add(NBTTagDouble(location.z))
        compound.set("Pos", position)

        val rotation = NBTTagList()
        rotation.add(NBTTagFloat(location.yaw))
        rotation.add(NBTTagFloat(location.pitch))
        compound.set("Rotation", rotation)
    }

    /**
     * Gets the player saved location
     *
     * @return a new copy of Location containing the position of this player
     */
    fun getLocation(): Location {
        val position: NBTTagList = compound.getList("Pos", 6)
        val rotation: NBTTagList = compound.getList("Rotation", 5)
        return Location(
            Bukkit.getWorld(
                UUID(
                    compound.getLong("WorldUUIDMost"),
                    compound.getLong("WorldUUIDLeast")
                )
            ),
            position.f(0),
            position.f(1),
            position.f(2),
            rotation.g(0),
            rotation.g(1)
        )
    }

    /**
     * Sets this entity's velocity in meters per tick
     *
     * @param velocity New velocity to travel with
     */
    fun setVelocity(velocity: Vector) {
        val motion = NBTTagList()
        motion.add(NBTTagDouble(velocity.x))
        motion.add(NBTTagDouble(velocity.y))
        motion.add(NBTTagDouble(velocity.z))
        compound.set("Motion", motion)
    }

    /**
     * Gets this entity's current velocity
     *
     * @return Current traveling velocity of this entity
     */
    fun getVelocity(): Vector {
        val list: NBTTagList = compound.getList("Motion", 6)
        return Vector(list.f(0), list.f(2), list.f(3))
    }

    /**
     * Sets the fall distance for this entity
     *
     * @param distance The new distance
     */
    fun setFallDistance(distance: Float) = compound.setFloat("FallDistance", distance)

    /**
     * Returns the distance this entity has falllen
     *
     * @return The distance
     */
    fun getFallDistance(): Float = compound.getFloat("FallDistance")

    /**
     * Sets whether the entity is invulnerable or not.
     *
     * @param flag if the entity is invulnerable
     */
    fun setInvulnerable(flag: Boolean) = compound.setBoolean("Invulnerable", flag)

    /**
     * Gets whether the entity is invulnerable or not.
     *
     * @return whether the entity is
     */
    fun isInvulnerable() = compound.getBoolean("Invulnerable")
}