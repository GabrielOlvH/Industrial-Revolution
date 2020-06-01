package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.InterfacedMachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.SidedInventory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    InterfacedMachineBlockEntity(MachineRegistry.CONTAINER_REGISTRY[tier], tier, 256 * 3.0) {
    val inventory: SidedInventory =
        DefaultSidedInventory(1, intArrayOf(0), intArrayOf()) { _, stack -> stack?.item is Rechargeable }

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val stack = inventory.getInvStack(0)
        if (stack.item is Rechargeable && stack.isDamaged) {
            inventory.setInvStack(0, stack.copy().apply { damage-- })
            takeEnergy(1.0)
        }
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxOutput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side != EnergySide.fromMinecraft(state[MachineBlock.FACING])) super.getMaxOutput(side) else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side == EnergySide.fromMinecraft(state[MachineBlock.FACING])) super.getMaxInput(side) else 0.0
    }
}