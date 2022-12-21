package me.steven.indrev.blockentities.crafting

import me.steven.indrev.blocks.COMPRESSOR
import me.steven.indrev.components.*
import me.steven.indrev.config.CraftingMachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.recipes.COMPRESSOR_RECIPE_TYPE
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

class CompressorBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity(COMPRESSOR.type, pos, state) {

    override val config: CraftingMachineConfig get() = machinesConfig.compressor.get(tier)

    override val inventory: MachineItemInventory = MachineItemInventory(2, inSlots(0), outSlots(1), ::updateCrafters)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))

    override val crafters: Array<MachineRecipeCrafter> = arrayOf(
        properties.sync(MachineRecipeCrafter(3, COMPRESSOR_RECIPE_TYPE.provider, intArrayOf(0), intArrayOf(1)))
    )

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addDefaultBackground()
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.add(WidgetSlot(0, inventory, INPUT_COLOR), grid(2) + 4, grid(1) + 12)
        handler.addProcessBar(this, crafters[0], Text.literal("Compressing"), grid(3) + 8 + 6, grid(1) + 12)
        handler.add(WidgetSlot(1, inventory, OUTPUT_COLOR, true), grid(5) + 9, grid(1) + 12)

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Compressor")

}