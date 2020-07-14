package me.steven.indrev.items.armor

import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.Module
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergyTier

class IRModularArmor(slot: EquipmentSlot, private val maxStored: Double, settings: Settings) :
    DyeableArmorItem(IRArmorMaterial.MODULAR, slot, settings), EnergyHolder {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        val upgrades = Module.getInstalled(stack)
        if (upgrades.isNotEmpty()) {
            tooltip?.add(TranslatableText("item.indrev.modular_armor.upgrade").formatted(Formatting.GOLD))
            upgrades.forEach { upgrade ->
                tooltip?.add(
                    TranslatableText(
                        "item.indrev.modular_armor.upgrade.${upgrade.key}",
                        Module.getLevel(stack, upgrade)
                    ).formatted(Formatting.BLUE)
                )
            }
        }
        val handler = Energy.of(stack)
        tooltip?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        tooltip?.add(LiteralText("${handler.energy} / ${handler.maxStored} LF"))
        tooltip?.add(TranslatableText("item.indrev.rechargeable.tooltip").formatted(Formatting.ITALIC, Formatting.GRAY))
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getColor(stack: ItemStack?): Int {
        val compoundTag = stack!!.getSubTag("display")
        return if (compoundTag != null && compoundTag.contains("color", 99)) compoundTag.getInt("color") else -1
    }

    override fun getMaxStoredPower(): Double = maxStored

    override fun getTier(): EnergyTier = EnergyTier.MEDIUM
}