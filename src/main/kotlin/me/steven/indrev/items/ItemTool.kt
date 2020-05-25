package me.steven.indrev.items

import me.steven.indrev.FabricRecipeRemainder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ItemTool(settings: Settings) : Item(settings), FabricRecipeRemainder {
    override fun getRemainder(stack: ItemStack?, craftingInventory: CraftingInventory?, playerEntity: PlayerEntity?): ItemStack? {
        return if (stack != null && stack.isDamageable) {
            val stackCopy = stack.copy().apply { damage++ }
            if (stackCopy.damage >= stackCopy.maxDamage) ItemStack.EMPTY
            else stackCopy
        } else stack
    }
}