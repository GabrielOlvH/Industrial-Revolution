package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.InterfacedMachineBlockEntity
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.gui.battery.BatteryController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.container.PropertyDelegate
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    InterfacedMachineBlockEntity(tier, MachineRegistry.CONTAINER_REGISTRY) {

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = getInventory()
        val stack = inventory.getInvStack(0)
        if (stack.item is Rechargeable && stack.isDamaged) {
            inventory.setInvStack(0, stack.copy().apply { damage-- })
            takeEnergy(1.0)
        }
    }

    override fun createContainer(i: Int, playerInventory: PlayerInventory): Container = BatteryController(i, playerInventory, BlockContext.create(world, pos))

    override fun getContainerName(): Text = TranslatableText("block.indrev.battery")

    override fun createInventory(): DefaultSidedInventory =
        DefaultSidedInventory(1, intArrayOf(0), intArrayOf()) { _, stack -> stack?.item is Rechargeable }

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