package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.widgets.FuelWidget
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class BiomassGeneratorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    SyncedGuiDescription(
        IndustrialRevolution.BIOMASS_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.biomass_generator", ctx, blockInventory, propertyDelegate)

        // Fuel input
        root.add(WItemSlot.of(blockInventory, 2), 4, 2)
        // Burning widget
        root.add(FuelWidget(propertyDelegate), 4, 1)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("biomass_generator")
    }

    override fun canUse(player: PlayerEntity?): Boolean = true
}