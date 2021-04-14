package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.widget.WItemSlot
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.inventory.Inventory
import net.minecraft.text.Text

open class WTooltipedItemSlot(
    private val emptyTooltip: MutableList<Text>,
    private val inventory: Inventory,
    private val startIndex: Int = 0,
    private val slotsWide: Int = 1,
    private val slotsHigh: Int = 1,
    big: Boolean = false
) : WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {

    override fun renderTooltip(matrices: MatrixStack?, x: Int, y: Int, tX: Int, tY: Int) {
        val slots = startIndex until startIndex + (slotsHigh * slotsWide)
        if (emptyTooltip.size != 0 && slots.all { inventory.getStack(it).isEmpty }) {
            val screen = MinecraftClient.getInstance().currentScreen
            screen?.renderTooltip(matrices, emptyTooltip, tX + x, tY + y)
        }
    }

    companion object {
        fun of(inventory: Inventory, index: Int, vararg emptyTooltip: Text): WTooltipedItemSlot =
            WTooltipedItemSlot(emptyTooltip.toMutableList(), inventory, index)

        fun of(
            inventory: Inventory,
            startIndex: Int,
            slotsWide: Int,
            slotsHigh: Int,
            vararg emptyTooltip: Text
        ): WTooltipedItemSlot = WTooltipedItemSlot(emptyTooltip.toMutableList(), inventory, startIndex, slotsWide, slotsHigh)
    }
}