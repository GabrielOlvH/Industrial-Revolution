package me.steven.indrev.blockentities.storage

import me.steven.indrev.IREnergyStorage
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.config.LFCConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorageUtil
import kotlin.math.floor

class LazuliFluxContainerBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    MachineBlockEntity<LFCConfig>(tier, MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY, pos, state) {

    init {
        this.inventoryComponent = inventory(this) {
            input { 0 filter { stack -> energyOf(stack) != null } }
        }
    }

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = config.maxOutput

    val transferConfig: SideConfiguration = SideConfiguration(ConfigurationType.ENERGY)
    private var lastWidth = 0f

    override val storage = object : IREnergyStorage() {

        override fun getAmount(): Long = energy

        override fun setAmount(v: Long) {
            energy = v
        }

        override fun getCapacity(): Long = this@LazuliFluxContainerBlockEntity.getCapacity()

        override fun getMaxInsert(side: Direction?): Long {
            return if (this@LazuliFluxContainerBlockEntity.transferConfig[side]?.input == true)
                maxInput
            else 0
        }

        override fun getMaxExtract(side: Direction?): Long {
            return if (side == null || this@LazuliFluxContainerBlockEntity.transferConfig[side]?.output == true)
                maxOutput
            else 0
        }

        override fun onFinalCommit() {
            super.onFinalCommit()
            markDirty()
            update()
        }
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val itemIo = energyOf(inventory, 0)
        if (itemIo != null)
            EnergyStorageUtil.move(storage.getSideStorage(null), itemIo, maxOutput, null)
    }

    private fun update() {
        val width = floor((((energy.toFloat() / getCapacity().toFloat()) * 0.5f) + 0.25f) * 16)
        if (width != lastWidth) {
            sync()
            lastWidth = width
        }
    }

    override fun isConfigurable(type: ConfigurationType): Boolean {
        return type == ConfigurationType.ENERGY
    }

    override fun getCurrentConfiguration(type: ConfigurationType): SideConfiguration {
        return transferConfig
    }

    override fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>) {
        val facing = state[FacingMachineBlock.FACING]
        Direction.values().forEach { dir ->
            if (dir == facing && tier != Tier.CREATIVE) configuration[dir] = TransferMode.INPUT
            else configuration[dir] = TransferMode.OUTPUT
        }
    }

    override fun toTag(tag: NbtCompound) {
        transferConfig.writeNbt(tag)
        super.toTag(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        super.fromTag(tag)
        transferConfig.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        tag.putLong("energy", energy)
        transferConfig.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        transferConfig.readNbt(tag)
        energy = tag.getLong("energy")
    }
}
