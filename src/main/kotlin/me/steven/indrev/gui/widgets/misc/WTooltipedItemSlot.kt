package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.widget.WItemSlot
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.TooltipPositioner
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.inventory.Inventory
import net.minecraft.text.Text
import org.joml.Vector2i

open class WTooltipedItemSlot(
    private val emptyTooltip: MutableList<Text>,
    private val inventory: Inventory,
    private val startIndex: Int = 0,
    private val slotsWide: Int = 1,
    private val slotsHigh: Int = 1,
    big: Boolean = false
) : WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {

    override fun renderTooltip(ctx: DrawContext, x: Int, y: Int, tX: Int, tY: Int) {
        val slots = startIndex until startIndex + (slotsHigh * slotsWide)
        if (emptyTooltip.size != 0 && slots.all { inventory.getStack(it).isEmpty }) {
            val screen = MinecraftClient.getInstance().currentScreen
            screen?.setTooltip(emptyTooltip.map { it.asOrderedText() },
                { _, _, _, _, _, _-> Vector2i(tX + x, tY + y) }, false)
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