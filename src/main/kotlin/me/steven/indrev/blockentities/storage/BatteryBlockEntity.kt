package me.steven.indrev.blockentities.storage

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.TransferMode
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryComponent = inventory(this) {
            0 filter { stack -> Energy.valid(stack) }
        }
    }

    private val transferConfig: SideConfiguration = SideConfiguration(ConfigurationType.ENERGY)

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        if (Energy.valid(stack)) {
            val handler = Energy.of(stack)
            Energy.of(this).into(handler).move()
            stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double {
        return if (transferConfig[Direction.values()[side!!.ordinal]] == TransferMode.OUTPUT) getTransferRate() else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        return if (transferConfig[Direction.values()[side!!.ordinal]] == TransferMode.INPUT) getTransferRate() else 0.0
    }

    private fun getTransferRate() = when (tier) {
        Tier.MK1 -> 128.0
        Tier.MK2 -> 512.0
        Tier.MK3 -> 4096.0
        else -> 16384.0
    }

    override fun isConfigurable(type: ConfigurationType): Boolean {
        return type == ConfigurationType.ENERGY
    }

    override fun getCurrentConfiguration(type: ConfigurationType): MutableMap<Direction, TransferMode> {
        return transferConfig
    }

    override fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>) {
        val facing = state[FacingMachineBlock.FACING]
        Direction.values().forEach { dir ->
            if (dir == facing) configuration[dir] = TransferMode.INPUT
            else configuration[dir] = TransferMode.OUTPUT
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        transferConfig.toTag(tag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        transferConfig.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        transferConfig.toTag(tag)
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        transferConfig.fromTag(tag)
    }

    override fun getBaseBuffer(): Double = when (tier) {
        Tier.MK1 -> 10000.0
        Tier.MK2 -> 100000.0
        Tier.MK3 -> 1000000.0
        Tier.MK4 -> 10000000.0
        Tier.CREATIVE -> Double.MAX_VALUE
    }
}