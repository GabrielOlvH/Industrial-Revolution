package me.steven.indrev.utils

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import team.reborn.energy.Energy
import java.util.function.Consumer

object EnergyDamageHandler : CustomDamageHandler {
    override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity?, breakCallback: Consumer<LivingEntity>?): Int {
        if (!Energy.valid(stack)) return amount
        Energy.of(stack).extract(amount.toDouble())
        return 0
    }
}