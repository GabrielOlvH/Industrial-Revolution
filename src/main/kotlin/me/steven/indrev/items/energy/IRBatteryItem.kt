package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.energyOf
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

class IRBatteryItem(settings: Settings, maxStored: Double) :
    Item(settings), IREnergyItem {

    init {
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(maxStored, Tier.MK1.io, Tier.MK1.io), this)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        buildEnergyTooltip(stack, tooltip)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val energyIo = energyOf(stack) ?: return
        if (energyIo.energy > 0) {
            stack.damage = (stack.maxDamage - energyIo.energy.toInt()).coerceAtLeast(1)
        } else stack.damage = 0
    }

}