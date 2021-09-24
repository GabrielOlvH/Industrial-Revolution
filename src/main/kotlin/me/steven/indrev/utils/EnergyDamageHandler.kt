package me.steven.indrev.utils

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import java.util.function.Consumer

object EnergyDamageHandler : CustomDamageHandler {
    override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity?, breakCallback: Consumer<LivingEntity>?): Int {
        /*val itemIo = energyOf(stack)
        Transaction.openOuter().use {
            itemIo?.extract(amount.toLong(), it)
            it.commit()
        }
        return 0*/
        return 0//TODO figure thsi out
    }
}