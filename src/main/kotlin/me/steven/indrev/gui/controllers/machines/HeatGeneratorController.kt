package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class HeatGeneratorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiController(
        IndustrialRevolution.HEAT_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.heat_generator", ctx, playerInventory, blockInventory, propertyDelegate)

        val info = WStaticTooltip()
        root.add(info, 3, 1)
        info.setSize(70, 40)

        val generatingText = WText({
            val burnTime = propertyDelegate[3]
            if (burnTime > 0) TranslatableText("gui.indrev.heatgen.title").formatted(Formatting.RED)
            else TranslatableText("gui.indrev.heatgen.idle").formatted(Formatting.GRAY, Formatting.ITALIC)
        }, HorizontalAlignment.LEFT)
        root.add(generatingText, 3.0, 1.4)
        val amount = WText({
            val burnTime = propertyDelegate[3]
            val ratio = propertyDelegate[5]
            if (burnTime > 0 && ratio > 0) TranslatableText("gui.indrev.heatgen.lftick", LiteralText(ratio.toString()).formatted(Formatting.WHITE)).formatted(Formatting.BLUE)
            else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)
        root.add(amount, 3.0, 2.4)

        val fluid = WFluid(ctx, 0)
        root.add(fluid, 8, 0)

        root.validate(this)
    }

    override fun getEntry(): Identifier = identifier("machines/generators")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("heat_generator_screen")
    }
}