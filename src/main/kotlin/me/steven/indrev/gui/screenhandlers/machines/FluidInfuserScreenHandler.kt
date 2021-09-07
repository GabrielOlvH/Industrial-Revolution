package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.FluidInfuserBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.FLUID_INFUSER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class FluidInfuserScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        FLUID_INFUSER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.fluid_infuser", ctx, playerInventory, blockInventory)

        val firstInput = WItemSlot.of(blockInventory, 2)
        root.add(firstInput, 3.7, 1.8)

        withBlockEntity<FluidInfuserBlockEntity> { be ->
            val fluid = fluidTank(be, FluidInfuserBlockEntity.INPUT_TANK_ID)
            root.add(fluid, 2.5, 1.0)

            val processWidget = processBar(be, FluidInfuserBlockEntity.CRAFTING_COMPONENT_ID)
            root.add(processWidget, 5.0, 1.8)

            val outputStack = WItemSlot.of(blockInventory, 3)
            root.add(outputStack, 6.4, 1.8)

            val outputFluid = fluidTank(be, FluidInfuserBlockEntity.OUTPUT_TANK_ID)
            root.add(outputFluid, 7.7, 1.0)
        }

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("fluid_infuser")
    }
}