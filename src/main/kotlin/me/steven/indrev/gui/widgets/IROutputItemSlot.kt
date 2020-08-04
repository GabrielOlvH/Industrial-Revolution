package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.controllers.slots.OutputValidatedSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.screen.ScreenHandlerContext

class IROutputItemSlot private constructor(
    private val ctx: ScreenHandlerContext,
    private val player: PlayerEntity,
    inventory: Inventory? = null,
    startIndex: Int = 0,
    slotsWide: Int = 1,
    slotsHigh: Int = 1,
    big: Boolean = false
) : WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {
    override fun createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot =
        OutputValidatedSlot(player, ctx, inventory, index, x, y)

    companion object {
        fun outputOf(
            ctx: ScreenHandlerContext,
            player: PlayerEntity,
            inventory: Inventory?,
            index: Int
        ): IROutputItemSlot = IROutputItemSlot(ctx, player, inventory, index, 1, 1, false)
    }
}