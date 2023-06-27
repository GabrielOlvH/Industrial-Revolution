package me.steven.indrev.blockentities

import me.steven.indrev.api.Tier
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.utils.Directions
import me.steven.indrev.utils.Upgrade
import me.steven.indrev.utils.energyOf
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil

abstract class MachineBlockEntity<T : MachineConfig>(type: BlockEntityType<*>, pos: BlockPos, state: BlockState)
    : BaseBlockEntity(type, pos, state), ExtendedScreenHandlerFactory {

    open val config: T? = null

    var ticks = 0

    private var prevIdle = true
    var idle = true

    var tier: Tier = Tier.MK1

    open val inventory: MachineItemInventory = NullItemInventory
    open val fluidInventory: MachineFluidInventory = NullFluidInventory
    open val temperatureController: MachineTemperatureController = NullTemperatureController
    open val upgrades: MachineUpgrades = NullMachineUpgrades

    val properties: MachineProperties = MachineProperties()

    var energy by properties.sync(ENERGY_ID, 0L)
    var capacity by properties.sync(ENERGY_CAPACITY_ID, 1000L)
    val energyInventories = Array(6) { EnergyInventory(Direction.byId(it)) }

    var connectedEnergyInventories = Directions.ALL

    open val maxInput = 1024L
    open val maxOutput = 1024L

    open fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (player.isSneaking) {
            tier = Tier.values()[(tier.ordinal + 1) % 4]
            sync()
            return ActionResult.SUCCESS
        }
        if (player.openHandledScreen(this).isPresent) {
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    open fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
    }

    fun tick() {
        ticks++
        if (world!!.isClient)
            return
        Direction.values().forEach { dir ->
            if (!connectedEnergyInventories.contains(dir)) return@forEach
            val storage = energyOf(world as ServerWorld, pos, dir)
            val neighbor = energyOf(world as ServerWorld, pos.offset(dir), dir.opposite)
            if (storage?.supportsExtraction() == true && neighbor?.supportsInsertion() == true) {
                EnergyStorageUtil.move(storage, neighbor, Long.MAX_VALUE, null)
            } else {
                connectedEnergyInventories = connectedEnergyInventories.without(dir)
            }
        }
        machineTick()
        if (temperatureController.exists())
            temperatureController.tick(world!!.random)

        if (ticks % 5 == 0) {
            transferItems()
        }

        if (idle != prevIdle && ticks % 10 == 0) {
            sync()
            prevIdle = idle
        }
    }

    private fun transferItems() {
        if (inventory.sidedConfiguration.forceDefault) return
        Direction.values().forEach { dir ->
            val mode = inventory.sidedConfiguration.getMode(dir)
            var to: Storage<ItemVariant>? = null
            var from: Storage<ItemVariant>? = null
            if (mode.allowOutput && inventory.sidedConfiguration.autoOutput) {
                from = inventory.getSide(dir)
                to = ItemStorage.SIDED.find(world, pos.offset(dir), dir.opposite)
            } else if (mode.allowInput && inventory.sidedConfiguration.autoInput) {
                to = inventory.getSide(dir)
                from = ItemStorage.SIDED.find(world, pos.offset(dir), dir.opposite)
            }
            if (from != null && to != null) {
                StorageUtil.move(from, to, { true }, 64, null)
            }
        }
    }

    open fun machineTick() {}

    fun insertEnergy(amount: Long) {
        energy += amount.coerceAtMost(capacity - energy)
    }

    fun hasEnergy(amount: Long): Boolean {
        if (energy < amount) return false
        return true
    }

    fun useEnergy(amount: Long): Boolean {
        if (energy < amount) return false
        energy -= amount
        return true
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        if (inventory.exists()) inventory.sidedConfiguration.toPacket(buf)
        if (fluidInventory.exists()) fluidInventory.sidedConfiguration.toPacket(buf)
    }

    override fun toTag(nbt: NbtCompound) {
        nbt.putInt("tier", tier.ordinal)
        if (inventory.exists()) nbt.put("inv", inventory.writeNbt())
        if (fluidInventory.exists()) nbt.put("fluidInv", fluidInventory.writeNbt())
        if (temperatureController.exists()) nbt.put("temperature", temperatureController.writeNbt())
        if (upgrades.exists()) nbt.put("upgrades", upgrades.writeNbt())
        nbt.putLong("energy", energy)
    }

    override fun fromTag(nbt: NbtCompound) {
        tier = Tier.values()[nbt.getInt("tier")]
        if (inventory.exists()) inventory.readNbt(nbt.getCompound("inv"))
        if (fluidInventory.exists()) fluidInventory.readNbt(nbt.getCompound("fluidInv"))
        if (temperatureController.exists()) temperatureController.readNbt(nbt.getCompound("temperature"))
        if (upgrades.exists()) upgrades.readNbt(nbt.getCompound("upgrades"))
        energy = nbt.getLong("energy")
    }

    override fun toClientTag(nbt: NbtCompound) {
        nbt.putInt("tier", tier.ordinal)
        nbt.putBoolean("idle", idle)
    }

    override fun fromClientTag(nbt: NbtCompound) {
        tier = Tier.values()[nbt.getInt("tier")]
        idle = nbt.getBoolean("idle")
    }

    open fun markForUpdate() {
        markDirty()
        if (fluidInventory.exists()) fluidInventory.isDirty = true
    }

    fun updateUpgrades() {
        markForUpdate()
        if (inventory.exists()) inventory.sidedConfiguration.forceDefault = !upgrades.contains(Upgrade.AUTOMATED_ITEM_TRANSFER)
        if (fluidInventory.exists()) fluidInventory.sidedConfiguration.forceDefault = !upgrades.contains(Upgrade.AUTOMATED_FLUID_TRANSFER)
    }

    inner class EnergyInventory(val dir: Direction) : SnapshotParticipant<Long>(), EnergyStorage {

        override fun createSnapshot(): Long = energy

        override fun readSnapshot(snapshot: Long) {
            energy = snapshot
        }

        override fun supportsInsertion(): Boolean = maxInput > 0

        override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val inserted = maxInput.coerceAtMost(maxAmount.coerceAtMost(capacity - amount))

            if (inserted > 0) {
                updateSnapshots(transaction)
                energy += inserted
                return inserted
            }

            return 0
        }

        override fun supportsExtraction(): Boolean = maxOutput > 0

        override fun extract(maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notNegative(maxAmount)

            val extracted = maxOutput.coerceAtMost(maxAmount.coerceAtMost(amount))

            if (extracted > 0) {
                updateSnapshots(transaction)
                energy -= extracted
                return extracted
            }

            return 0
        }

        override fun getAmount(): Long {
            return energy
        }

        override fun getCapacity(): Long {
            return this@MachineBlockEntity.capacity
        }

    }

    companion object {
        const val ENERGY_ID = 0
        const val ENERGY_CAPACITY_ID = 1
        const val TEMPERATURE_ID = 2
    }
}