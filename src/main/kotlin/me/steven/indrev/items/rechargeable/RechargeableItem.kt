package me.steven.indrev.items.rechargeable

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class RechargeableItem(settings: Settings) : Item(settings), Rechargeable {
    override fun onCraft(stack: ItemStack?, world: World?, player: PlayerEntity?) {
        if (stack == null) return
        stack.damage = stack.maxDamage - 1
    }
}