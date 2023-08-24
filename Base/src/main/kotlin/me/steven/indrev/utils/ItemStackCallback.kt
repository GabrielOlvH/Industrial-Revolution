package me.steven.indrev.utils

import net.minecraft.item.ItemStack


fun interface ItemStackCallback {
    operator fun invoke(stack: ItemStack)
}