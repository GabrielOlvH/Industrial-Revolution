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
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction

class LazuliFluxContainerBlockEntity(tier: Tier) :
    MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryComponent = inventory(this) {
            input { 0 filter { stack -> energyOf(stack) != null } }
        }
    }

    val transferConfig: SideConfiguration = SideConfiguration(ConfigurationType.ENERGY)
    private var lastWidth = 0f

    override val maxOutput: Double = getTransferRate()
    override val maxInput: Double = getTransferRate()

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

    private fun getTransferRate() = when (tier) {
        Tier.MK1 -> 128.0
        Tier.MK2 -> 512.0
        Tier.MK3 -> 4096.0
        else -> 16384.0
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
        MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, null, null, 8)
    }

    override fun getEnergyCapacity(): Double = when (tier) {
        Tier.MK1 -> 10000.0
        Tier.MK2 -> 100000.0
        Tier.MK3 -> 1000000.0
        Tier.MK4 -> 10000000.0
        Tier.CREATIVE -> Double.MAX_VALUE
    }

    class LFCEnergyIo(val blockEntity: LazuliFluxContainerBlockEntity, val direction: Direction) : EnergyIo {
        override fun getEnergy(): Double = blockEntity.energy

        override fun getEnergyCapacity(): Double = blockEntity.energyCapacity

        override fun extract(maxAmount: Double, simulation: Simulation?): Double {
            if (!supportsExtraction()) return 0.0
            var extracted = maxAmount.coerceAtMost(blockEntity.maxOutput)
            val overflow = energy - extracted
            if (overflow < 0)
                extracted += overflow

            if (simulation == Simulation.ACT)
                blockEntity.energy -= extracted
            blockEntity.update()

            return extracted
        }

        override fun insert(amount: Double, simulation: Simulation?): Double {
            if (!supportsInsertion()) return amount
            var inserted = amount.coerceAtMost(blockEntity.maxInput)
            val overflow = energy + inserted
            if (overflow > energyCapacity)
                inserted -= overflow - energyCapacity

            if (simulation == Simulation.ACT)
                blockEntity.energy += inserted

            blockEntity.update()
            return amount - inserted
        }

        override fun supportsExtraction(): Boolean = blockEntity.transferConfig[direction]?.output == true

        override fun supportsInsertion(): Boolean = blockEntity.transferConfig[direction]?.input == true
    }
}