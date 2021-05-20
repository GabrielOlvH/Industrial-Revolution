package me.steven.indrev.blockentities.storage

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
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
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction

class LazuliFluxContainerBlockEntity(tier: Tier) :
    MachineBlockEntity<LFCConfig>(tier, MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryComponent = inventory(this) {
            input { 0 filter { stack -> energyOf(stack) != null } }
        }
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = config.maxOutput

    val transferConfig: SideConfiguration = SideConfiguration(ConfigurationType.ENERGY)
    private var lastWidth = 0f

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        val itemIo = energyOf(stack)
        if (itemIo != null)
            EnergyMovement.move(this, itemIo, maxOutput)
    }

    override fun insert(amount: Double, simulation: Simulation?): Double {
        val insert = super.insert(amount, simulation)
        update()
        return insert
    }

    override fun extract(maxAmount: Double, simulation: Simulation?): Double {
        val extract = super.extract(maxAmount, simulation)
        update()
        return extract
    }

    private fun update() {
        val width = ((energy.toFloat() / energyCapacity.toFloat()) * 0.5f) + 0.25f
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

    class LFCEnergyIo(val blockEntity: LazuliFluxContainerBlockEntity, val direction: Direction) : EnergyIo {
        override fun getEnergy(): Double = blockEntity.energy

        override fun getEnergyCapacity(): Double = blockEntity.energyCapacity

        override fun extract(maxAmount: Double, simulation: Simulation?): Double {
            val extracted = maxAmount.coerceAtMost(blockEntity.maxOutput).coerceAtMost(energy)
            if (simulation?.isActing == true) {
                blockEntity.energy -= extracted
                blockEntity.update()
            }
            return extracted
        }

        override fun insert(amount: Double, simulation: Simulation?): Double {
            val inserted = amount.coerceAtMost(blockEntity.maxInput).coerceAtMost(this.energyCapacity - energy)
            if (simulation?.isActing == true) {
                blockEntity.energy += inserted
                blockEntity.update()
            }
            return amount - inserted
        }

        override fun supportsExtraction(): Boolean = blockEntity.transferConfig[direction]?.output == true

        override fun supportsInsertion(): Boolean = blockEntity.transferConfig[direction]?.input == true
    }
}