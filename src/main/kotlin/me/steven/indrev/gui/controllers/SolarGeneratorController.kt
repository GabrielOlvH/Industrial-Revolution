package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SolarGeneratorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    screenHandlerContext: ScreenHandlerContext
) :
    SyncedGuiDescription(
        IndustrialRevolution.SOLAR_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.solar_generator", screenHandlerContext, blockInventory, propertyDelegate)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("solar_generator")
    }
}