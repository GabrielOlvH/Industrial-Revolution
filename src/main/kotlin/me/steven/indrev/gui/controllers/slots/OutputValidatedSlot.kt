package me.steven.indrev.gui.controllers.slots

import io.github.cottonmc.cotton.gui.ValidatedSlot
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerContext

class OutputValidatedSlot(
    private val player: PlayerEntity,
    private val ctx: ScreenHandlerContext,
    inventory: Inventory,
    index: Int,
    x: Int,
    y: Int
) : ValidatedSlot(inventory, index, x, y) {

    var amount: Int = 0

    override fun onCrafted(stack: ItemStack?, amount: Int) {
        this.amount += amount
        onCrafted(stack)
    }

    override fun onCrafted(stack: ItemStack?) {
        stack?.onCraft(player.world, player, amount)
        if (!player.world.isClient) {
            ctx.run { world, pos ->
                val blockEntity = world.getBlockEntity(pos) as? CraftingMachineBlockEntity<*> ?: return@run
                blockEntity.dropExperience(player)
            }
        }
        amount = 0
    }
}