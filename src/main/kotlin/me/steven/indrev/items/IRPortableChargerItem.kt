package me.steven.indrev.items

import me.steven.indrev.items.rechargeable.IRRechargeable
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.getShortEnergyDisplay
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyTier

class IRPortableChargerItem(
    settings: Settings,
    private val tier: Tier,
    private val maxStored: Double
) : Item(settings), EnergyHolder, IRRechargeable {

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val handler = Energy.of(stack)
        tooltip?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        tooltip?.add(LiteralText("${getShortEnergyDisplay(handler.energy)} / ${getShortEnergyDisplay(handler.maxStored)} LF"))
        tooltip?.add(TranslatableText("item.indrev.rechargeable.tooltip").formatted(Formatting.ITALIC, Formatting.GRAY))
    }

    override fun getMaxStoredPower(): Double = maxStored

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

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
}