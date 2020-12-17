package me.steven.indrev.utils

import dev.technici4n.fasttransferlib.api.ContainerItemContext
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.item.ItemKey
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import java.util.function.Consumer

object EnergyDamageHandler : CustomDamageHandler {
    override fun damage(stack: ItemStack, amount: Int, entity: LivingEntity?, breakCallback: Consumer<LivingEntity>?): Int {
        val itemIo = EnergyApi.ITEM[ItemKey.of(stack), ContainerItemContext.ofStack(stack)]
        return amount - (itemIo?.extract(amount.toDouble(), Simulation.ACT)?.toInt() ?: 0)
    }
}