package me.steven.indrev.gui.widgets.slots

import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.mixin.AccessorWItemSlot
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.inventory.Inventory
import net.minecraft.text.StringRenderable

class IRTooltipedItemSlot private constructor(
    private val emptyTooltip: MutableList<StringRenderable>,
    inventory: Inventory? = null,
    startIndex: Int = 0,
    slotsWide: Int = 1,
    slotsHigh: Int = 1,
    big: Boolean = false
) : WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {

    override fun renderTooltip(matrices: MatrixStack?, x: Int, y: Int, tX: Int, tY: Int) {
        if (emptyTooltip.size != 0 && (this as AccessorWItemSlot).peers.none { it.hasStack() }) {
            val screen = MinecraftClient.getInstance().currentScreen
            screen?.renderTooltip(matrices, emptyTooltip, tX + x, tY + y)
        }
    }

    companion object {
        fun of(inventory: Inventory?, index: Int, emptyTooltip: MutableList<StringRenderable>): IRTooltipedItemSlot =
            IRTooltipedItemSlot(emptyTooltip, inventory, index)

        fun of(
            inventory: Inventory?,
            startIndex: Int,
            slotsWide: Int,
            slotsHigh: Int,
            emptyTooltip: MutableList<StringRenderable>
        ): IRTooltipedItemSlot = IRTooltipedItemSlot(emptyTooltip, inventory, startIndex, slotsWide, slotsHigh)
    }
}