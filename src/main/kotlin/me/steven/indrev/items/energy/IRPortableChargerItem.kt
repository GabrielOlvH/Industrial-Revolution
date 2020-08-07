package me.steven.indrev.items.energy

import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import team.reborn.energy.*

class IRPortableChargerItem(
    settings: Settings,
    private val tier: Tier,
    private val maxStored: Double
) : Item(settings), EnergyHolder, IREnergyItem {

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        buildEnergyTooltip(stack, tooltip)
    }

    override fun getMaxStoredPower(): Double = maxStored

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getTier(): EnergyTier = EnergyTier.HIGH

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val handler = Energy.of(stack)
        stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)

        val player = entity as? PlayerEntity ?: return
        if (player.inventory.offHand[0] != stack) return
        val items = (0 until player.inventory.size())
            .map { s -> player.inventory.getStack(s) }
            .filter { s -> s.item !is IRPortableChargerItem && Energy.valid(s) }
            .map { s -> Energy.of(s) }
        val sum = items.sumByDouble { it.maxInput.coerceAtLeast(it.energy) }
        val amount = sum / items.size.toDouble()
        items.forEach { h ->
            handler.into(h).move(amount)
        }
    }

    companion object {
        fun chargeItemsInInv(handler: EnergyHandler, inventory: DefaultedList<ItemStack>) {
            val items = (0 until inventory.size)
                .map { s -> inventory[s] }
                .filter { s -> s.item !is IRPortableChargerItem && Energy.valid(s) }
                .map { s -> Energy.of(s) }
            val sum = items.sumByDouble { it.maxInput.coerceAtLeast(it.energy) }
            val amount = sum / items.size.toDouble()
            items.forEach { h ->
                handler.into(h).move(amount)
            }
        }
    }
}