package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WSprite
import me.steven.indrev.blockentities.generators.GasBurningGeneratorBlockEntity
import me.steven.indrev.gui.screenhandlers.GAS_BURNING_GENERATOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.fuelBar
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
        GAS_BURNING_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.gas_generator", ctx, playerInventory, blockInventory)

        withBlockEntity<GasBurningGeneratorBlockEntity> { be ->
            val wFuel = fuelBar(be)
            root.add(wFuel, 3.5, 3.2)
            wFuel.setSize(14, 14)

            val fluid = fluidTank(be, GasBurningGeneratorBlockEntity.TANK_ID)
            root.add(fluid, 3.5, 0.8)
        }
        val processSprite = WSprite(identifier("textures/gui/widget_processing_empty.png"))
        root.add(processSprite, 4.9, 1.5)

        val ashSlot = WItemSlot.of(blockInventory, 1)
        root.add(ashSlot, 6.5, 1.5)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("gas_burning_generator_screen")
    }
}