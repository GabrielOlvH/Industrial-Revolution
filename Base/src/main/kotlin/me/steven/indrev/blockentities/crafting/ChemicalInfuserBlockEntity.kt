package me.steven.indrev.blockentities.crafting

import me.steven.indrev.blocks.CHEMICAL_INFUSER
import me.steven.indrev.components.*
import me.steven.indrev.config.CraftingMachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.recipes.CHEMICAL_INFUSER_RECIPE_TYPE
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetBar
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.INPUT_COLOR
import me.steven.indrev.utils.OUTPUT_COLOR
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class ChemicalInfuserBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity(CHEMICAL_INFUSER.type, pos, state) {

    override val config: CraftingMachineConfig get() = machinesConfig.chemicalInfuser.get(tier)

    override val inventory: MachineItemInventory = MachineItemInventory(2, inSlots(0), outSlots(1), ::updateCrafters)
    override val fluidInventory: MachineFluidInventory = properties.sync(MachineFluidInventory(2, 2, { bucket * 4 }, inSlots(0), inSlots(1), ::updateCrafters))
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1000, ::markForUpdate))

    override val crafters: Array<MachineRecipeCrafter> = arrayOf(
        properties.sync(MachineRecipeCrafter(3, CHEMICAL_INFUSER_RECIPE_TYPE.provider, intArrayOf(0), intArrayOf(1), intArrayOf(0), intArrayOf(1)))
    )

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.extended = true
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.add(WidgetBar.fluidTank(fluidInventory, 0, pos), grid(2), grid(0) + 4)
        handler.add(WidgetSlot(0, inventory, INPUT_COLOR), grid(4)-8, grid(1) + 9)
        handler.addProcessBar(this, crafters[0], Text.literal("Infusing"), grid(5)+5, grid(1) + 9)
        handler.add(WidgetSlot(1, inventory, OUTPUT_COLOR), grid(6)+16, grid(1) + 9)
        handler.add(WidgetBar.fluidTank(fluidInventory, 1, pos), grid(8)+4, grid(0) + 4)

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Chemical Infuser")

}