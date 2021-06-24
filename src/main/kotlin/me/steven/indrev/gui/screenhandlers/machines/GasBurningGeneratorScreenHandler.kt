package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WSprite
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.gui.widgets.misc.WFuel
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class GasBurningGeneratorScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        IndustrialRevolution.GAS_BURNING_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.gas_generator", ctx, playerInventory, blockInventory)

        val wFuel = WFuel()
        root.add(wFuel, 3.5, 3.2)
        wFuel.setSize(14, 14)

        val fluid = WFluid(ctx, 0)
        root.add(fluid, 3.5, 0.8)

        val processSprite = WSprite(identifier("textures/gui/widget_processing_empty.png"))
        root.add(processSprite, 4.9, 1.5)

        val ashSlot = WItemSlot.of(blockInventory, 1)
        root.add(ashSlot, 6.5, 1.5)

        root.validate(this)
    }

    override fun getEntry(): Identifier = identifier("machines/generators")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("gas_burning_generator_screen")
    }
}