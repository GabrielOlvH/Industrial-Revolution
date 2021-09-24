package me.steven.indrev.items.energy

import me.steven.indrev.api.machines.Tier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl

class IRBatteryItem(settings: Settings, maxStored: Long) :
    Item(settings), IREnergyItem {

    init {
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, maxStored, Tier.MK1.io, Tier.MK1.io) }, this)
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false
}