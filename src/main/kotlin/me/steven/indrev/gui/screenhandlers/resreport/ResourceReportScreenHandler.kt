package me.steven.indrev.gui.screenhandlers.resreport

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.RESOURCE_REPORT_HANDLER
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.entries
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class ResourceReportScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext,
    veinData: ChunkVeinData
) :
    IRGuiScreenHandler(
        RESOURCE_REPORT_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = object : WGridPanel() {
            override fun setBackgroundPainter(painter: BackgroundPainter?): WPanel {
                return super.setBackgroundPainter { matrices, left, top, panel ->
                    ScreenDrawing.texturedRect(
                        matrices,
                        left,
                        top,
                        panel.width,
                        panel.height,
                        identifier("textures/gui/paper.png"),
                        -1
                    )
                }
            }
        }
        setRootPanel(root)
        root.setSize(128, 0)
        val titleText = WText(TranslatableText(veinData.translationKey).append(LiteralText(" (").append(getSizeText(veinData.size)).append(LiteralText(")"))).formatted(Formatting.DARK_GRAY), HorizontalAlignment.CENTER)
        root.add(titleText, 3.0, 0.3)

        val outputs = VeinType.REGISTERED[veinData.veinIdentifier]!!.outputs
        val sum = outputs.entries.sumOf { it.weight }
        outputs.entries.sortedByDescending { it.weight }
            .forEachIndexed { index, entry ->
                val block = (entry.element as Block)
                val weight = entry.weight
                val text = LiteralText("${String.format("%.1f", (weight / sum.toDouble()) * 100)}% ").formatted(Formatting.DARK_GRAY)
                    .append(
                        TranslatableText(block.translationKey).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(block.defaultMapColor.color)))
                    )
                root.add(WText(text, HorizontalAlignment.LEFT), 0.2, 1.5 + index)
            }
        root.validate(this)
    }

    private fun getSizeText(size: Int): TranslatableText =
        TranslatableText(
            "gui.indrev.resourcereport.size.${
                when {
                    size <= 1500 -> "tiny"
                    size <= 3500 -> "small"
                    size <= 5000 -> "average"
                    size <= 6000 -> "big"
                    size <= 8000 -> "very_big"
                    size <= 9000 -> "enormous"
                    else -> "gigantic"
                }
            }"
        )

    companion object {
        val SCREEN_ID = identifier("resource_report_screen")
    }
}