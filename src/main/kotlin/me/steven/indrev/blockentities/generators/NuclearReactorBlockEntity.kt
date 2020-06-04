package me.steven.indrev.blockentities.generators

import me.steven.indrev.blocks.nuclear.NuclearReactorCore
import me.steven.indrev.gui.nuclearreactor.NuclearReactorController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.UraniumRodItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class NuclearReactorBlockEntity : GeneratorBlockEntity(Tier.MK4, MachineRegistry.NUCLEAR_GENERATOR_REGISTRY) {

    private var loadedFuel = 0
    private var modifier = 0

    override fun shouldGenerate(): Boolean {
        if (this.hasWorld()) {
            val block = this.cachedState.block
            if (block is NuclearReactorCore && !block.isFormed(this.cachedState)) return false
        }
        if (loadedFuel <= 0) {
            modifier = 0
            for (slot in getInventory().inputSlots) {
                val itemStack = getInventory().getInvStack(slot)
                val item = itemStack.item
                if (item is UraniumRodItem) {
                    modifier++
                    loadedFuel += 10
                }
            }
        }
        loadedFuel--
        return loadedFuel > 0
    }

    override fun createContainer(i: Int, playerInventory: PlayerInventory): NuclearReactorController {
        return NuclearReactorController(i, playerInventory, BlockContext.create(world, pos))
    }

    override fun getContainerName(): Text = TranslatableText("")

    override fun getGenerationRatio(): Double = if (temperature.toInt() in getOptimalRange()) 300.0 else 150.0

    override fun getOptimalRange(): IntRange = 2000..3000

    override fun getBaseHeatingEfficiency(): Double = 15.1

    override fun getLimitTemperature(): Double = 4000.0

    override fun createInventory(): DefaultSidedInventory =
        DefaultSidedInventory(11, intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10), intArrayOf()) { slot, stack ->
            val item = stack?.item
            when {
                item is RechargeableItem && item.canOutput -> slot == 0
                item is CoolerItem -> slot == 1
                else -> item is UraniumRodItem
            }
        }
}