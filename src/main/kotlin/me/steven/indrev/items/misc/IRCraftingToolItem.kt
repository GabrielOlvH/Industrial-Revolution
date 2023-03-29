package me.steven.indrev.items.misc

import me.steven.indrev.FabricRecipeRemainder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.random.Random

class IRCraftingToolItem(settings: Settings) : Item(settings), FabricRecipeRemainder {
    override fun getRemainder(stack: ItemStack?, craftingInventory: CraftingInventory?, playerEntity: PlayerEntity?): ItemStack? {
        return if (stack != null && stack.isDamageable) {
            val stackCopy = stack.copy().also { it.damage(1, Random.create(), playerEntity as? ServerPlayerEntity) }
            if (stackCopy.damage >= stackCopy.maxDamage) ItemStack.EMPTY
            else stackCopy
        } else stack
    }
}