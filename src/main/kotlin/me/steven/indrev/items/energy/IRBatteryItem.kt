package me.steven.indrev.items.energy

import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyTier

open class IRBatteryItem(settings: Settings, private val maxStored: Double, val canOutput: Boolean = false) :
    Item(settings), EnergyHolder, IREnergyItem {

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        buildEnergyTooltip(stack, tooltip)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getMaxStoredPower(): Double = maxStored

    override fun getMaxInput(side: EnergySide?): Double = Tier.MK1.io

    override fun getMaxOutput(side: EnergySide?): Double = Tier.MK1.io

    override fun getTier(): EnergyTier = EnergyTier.LOW

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val handler = Energy.of(stack)
        if (handler.energy > 0) {
            stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
        } else stack.damage = 0
    }

}