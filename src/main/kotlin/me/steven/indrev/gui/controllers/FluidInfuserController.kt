package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.gui.widgets.machines.WProcess
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class FluidInfuserController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.FLUID_INFUSER_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.fluid_infuser", ctx, playerInventory, blockInventory, propertyDelegate)

        val firstInput = WItemSlot.of(blockInventory, 2)
        root.add(firstInput, 3.0, 2.0)

        val fluid = WFluid(ctx, 0)
        root.add(fluid, 1.6, 0.5)

        val processWidget = WProcess(propertyDelegate)
        root.add(processWidget, 4.2, 2.0)

        val output = WFluid(ctx, 1)
        root.add(output, 6.0, 0.5)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 4

    companion object {
        val SCREEN_ID = identifier("fluid_infuser")
    }
}