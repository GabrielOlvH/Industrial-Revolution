package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.machines.Tier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class IRBatteryItem(settings: Settings, maxStored: Double) :
    Item(settings), IREnergyItem {

    init {
        EnergyApi.ITEM.registerForItems(SimpleItemEnergyIo.getProvider(maxStored, Tier.MK1.io, Tier.MK1.io), this)
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false
}