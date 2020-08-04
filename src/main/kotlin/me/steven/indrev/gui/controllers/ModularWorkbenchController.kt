package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.widgets.IRTooltipedItemSlot
import me.steven.indrev.gui.widgets.VerticalProcessWidget
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class ModularWorkbenchController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.MODULAR_WORKBENCH_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ) {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.modular_workbench", ctx, playerInventory, blockInventory, propertyDelegate)

        val armorSlot = IRTooltipedItemSlot.of(
            blockInventory,
            2,
            mutableListOf(
                TranslatableText("gui.indrev.modular_armor_slot_type").formatted(
                    Formatting.BLUE,
                    Formatting.ITALIC
                )
            )
        )
        root.add(armorSlot, 4.0, 3.5)

        val moduleSlot = IRTooltipedItemSlot.of(
            blockInventory,
            1,
            mutableListOf(TranslatableText("gui.indrev.module_slot_type").formatted(Formatting.BLUE, Formatting.ITALIC))
        )
        root.add(moduleSlot, 4, 1)

        val process = VerticalProcessWidget(propertyDelegate)
        root.add(process, 4, 2)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("modular_workbench_screen")
    }
}