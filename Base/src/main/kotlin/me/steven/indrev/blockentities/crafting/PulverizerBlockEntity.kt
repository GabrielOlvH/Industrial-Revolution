package me.steven.indrev.blockentities.crafting

import me.steven.indrev.blocks.PULVERIZER
import me.steven.indrev.components.*
import me.steven.indrev.config.CraftingMachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.recipes.PULVERIZER_RECIPE_TYPE
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class PulverizerBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity(PULVERIZER.type, pos, state) {

    override val config: CraftingMachineConfig get() = machinesConfig.pulverizer.get(tier)

    override val inventory: MachineItemInventory = MachineItemInventory(3, inSlots(0), outSlots(1, 2), ::updateCrafters)

    override val crafters: Array<MachineRecipeCrafter> = arrayOf(
        properties.sync(MachineRecipeCrafter(2, PULVERIZER_RECIPE_TYPE.provider, intArrayOf(0), intArrayOf(1, 2)))
    )

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this)
        handler.addUpgradeSlots(upgrades)

        handler.add(WidgetSlot(0, inventory, 0xFFAAAAFF.toInt()), grid(2) + 9, grid(1) +7)
        handler.addProcessBar(this, crafters[0], Text.literal("Pulverizing"), grid(3) + 8 + 8, grid(1) + 7)
        handler.add(WidgetSlot(1, inventory, 0xFFFFD4A8.toInt(), true), grid(5) + 9, grid(1) +7)
        handler.add(WidgetSlot(2, inventory, 0xFFFFD4A8.toInt()), grid(5) + 9, grid(3))

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Pulverizer")

}