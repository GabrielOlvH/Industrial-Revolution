package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.armor.Module
import me.steven.indrev.gui.widgets.machines.VerticalProcessWidget
import me.steven.indrev.gui.widgets.misc.IRTooltipedItemSlot
import me.steven.indrev.gui.widgets.misc.StringWidget
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
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
        root.add(armorSlot, 1.5, 3.5)

        val moduleSlot = IRTooltipedItemSlot.of(
            blockInventory,
            1,
            mutableListOf(TranslatableText("gui.indrev.module_slot_type").formatted(Formatting.BLUE, Formatting.ITALIC))
        )
        root.add(moduleSlot, 1.5, 1.0)

        val process = VerticalProcessWidget(propertyDelegate)
        root.add(process, 1.5, 2.0)

        val info = WStaticTooltip()
        info.setSize(100, 60)
        root.add(info, 3, 1)

        addTextInfo(root)

        root.validate(this)
    }

    private fun addTextInfo(panel: WGridPanel) {
        val armorInfoText = StringWidget({
            val stack = blockInventory.getStack(2)
            if (!stack.isEmpty)
                TranslatableText(stack.item.translationKey).formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE)
            else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        val modulesInstalled = StringWidget({
            val stack = blockInventory.getStack(2)
            if (!stack.isEmpty) {
                val modules = Module.getInstalled(stack).size.toString()
                TranslatableText("gui.indrev.modules_installed").formatted(Formatting.BLUE).append(LiteralText(modules).formatted(Formatting.WHITE))
            } else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        val shield = StringWidget({
            val stack = blockInventory.getStack(2)
            val item = stack.item
            if (!stack.isEmpty && item is IRModularArmor) {
                val shield = item.getMaxShield(Module.getLevel(stack, Module.PROTECTION)).toString()
                TranslatableText("gui.indrev.shield").formatted(Formatting.BLUE).append(LiteralText(shield).formatted(Formatting.WHITE))
            } else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        val installing = StringWidget({
            val stack = blockInventory.getStack(1)
            val item = stack.item
            if (!stack.isEmpty && item is IRModuleItem) {
                TranslatableText("gui.indrev.installing").formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE)
            } else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        val moduleToInstall = StringWidget({
            val stack = blockInventory.getStack(1)
            val item = stack.item
            if (!stack.isEmpty && item is IRModuleItem) {
                TranslatableText(item.translationKey).formatted(Formatting.GRAY, Formatting.ITALIC)
            } else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        val progress = StringWidget({
            val stack = blockInventory.getStack(1)
            val item = stack.item
            if (!stack.isEmpty && item is IRModuleItem) {
                val progress = propertyDelegate[2]
                if (progress == -1)
                    TranslatableText("gui.indrev.incompatible").formatted(Formatting.RED)
                else {
                    val percent = ((progress / 1200f) * 100).toInt()
                    TranslatableText("gui.indrev.progress").formatted(Formatting.BLUE).append(LiteralText("$percent%"))
                }
            } else Text.EMPTY
        }, HorizontalAlignment.LEFT)

        panel.add(armorInfoText, 3, 1)
        panel.add(modulesInstalled, 3.0, 1.5)
        panel.add(shield, 3, 2)
        panel.add(installing, 3, 3)
        panel.add(moduleToInstall, 3.0, 3.5)
        panel.add(progress, 3, 4)
    }

    companion object {
        val SCREEN_ID = identifier("modular_workbench_screen")
    }
}