package me.steven.indrev.blockentities.crafting

import me.steven.indrev.blocks.ALLOY_SMELTER
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.components.MachineRecipeCrafter
import me.steven.indrev.components.inSlots
import me.steven.indrev.components.outSlots
import me.steven.indrev.config.CraftingMachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.recipes.ALLOY_SMELTER_RECIPE_TYPE
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.INPUT_COLOR
import me.steven.indrev.utils.OUTPUT_COLOR
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class AlloySmelterBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity(ALLOY_SMELTER.type, pos, state) {

    override val config: CraftingMachineConfig get() = machinesConfig.alloySmelter.get(tier)

    override val inventory: MachineItemInventory = MachineItemInventory(3, inSlots(0, 1), outSlots(2), ::updateCrafters)

    override val crafters: Array<MachineRecipeCrafter> = arrayOf(
        properties.sync(MachineRecipeCrafter(2, ALLOY_SMELTER_RECIPE_TYPE.provider, intArrayOf(0, 1), intArrayOf(2)))
    )

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this)
        handler.addUpgradeSlots(upgrades)

        handler.add(WidgetSlot(0, inventory, INPUT_COLOR), grid(2) - 4, grid(1) + 9)
        handler.add(WidgetSlot(1, inventory, INPUT_COLOR), grid(3), grid(1) + 9)
        handler.addProcessBar(this, crafters[0], Text.literal("Smelting"), grid(4) + 6, grid(1) + 9)
        handler.add(WidgetSlot(2, inventory, OUTPUT_COLOR, true), grid(6), grid(1) + 9)

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Alloy Smelter")

}