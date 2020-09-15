package me.steven.indrev.gui.controllers.resreport

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.entries
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.weight
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText

class ResourceReportController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext,
    veinData: ChunkVeinData
) :
    IRGuiController(
        IndustrialRevolution.RESOURCE_REPORT_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(100, 150)
        root.add(WText(TranslatableText(veinData.translationKey), HorizontalAlignment.LEFT), 0, 0)
        root.add(
            WText(
                TranslatableText("gui.indrev.resourcereport.size", getSizeText(veinData.size)),
                HorizontalAlignment.LEFT
            ), 0, 1
        )

        val outputs = VeinType.REGISTERED[veinData.veinIdentifier]!!.outputs
        val sum = outputs.entries.sumBy { it.weight }
        outputs.entries.sortedByDescending { it.weight }
            .forEachIndexed { index, entry ->
                val block = (entry.element as Block)
                val weight = entry.weight
                val text = LiteralText("${((weight / sum.toDouble()) * 100).toInt()}% ")
                    .append(
                        TranslatableText(block.translationKey).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(block.defaultMaterialColor.color)))
                    )
                root.add(WText(text, HorizontalAlignment.LEFT), 0, 2 + index)
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