package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.widgets.ProcessWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText

class HeatGeneratorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    screenHandlerContext: ScreenHandlerContext
) :
    SyncedGuiDescription(
        IndustrialRevolution.HEAT_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.heat_generator", screenHandlerContext, blockInventory, propertyDelegate)

        root.add(StringWidget(TranslatableText("Heating up...")), 4.0, 2.0)
        val processWidget = ProcessWidget(propertyDelegate)
        root.add(processWidget, 4.0, 2.5)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("heat_generator_screen")
    }
}