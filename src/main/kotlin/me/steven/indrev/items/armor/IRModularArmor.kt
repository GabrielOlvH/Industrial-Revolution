package me.steven.indrev.items.armor

import me.steven.indrev.armor.ArmorModule
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.tools.Module
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.DyeableArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyTier

class IRModularArmor(slot: EquipmentSlot, private val maxStored: Double, settings: Settings) :
    DyeableArmorItem(IRArmorMaterial.MODULAR, slot, settings), EnergyHolder {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        Module.getInstalledTooltip(ArmorModule.getInstalled(stack) as Array<Module>, stack, tooltip)
        buildEnergyTooltip(stack, tooltip)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getColor(stack: ItemStack?): Int {
        val compoundTag = stack!!.getSubTag("display")
        return if (compoundTag != null && compoundTag.contains("color", 99)) compoundTag.getInt("color") else -1
    }

    override fun getMaxStoredPower(): Double = maxStored

    override fun getMaxInput(side: EnergySide?): Double = Tier.MK4.io

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getTier(): EnergyTier = EnergyTier.HIGH

    override fun inventoryTick(stack: ItemStack, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        val handler = Energy.of(stack)
        stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
    }

    fun regenShield(itemStack: ItemStack, protectionLevel: Int) {
        val tag = itemStack.tag
        if (tag?.contains("shield") == false) {
            tag.putDouble("shield", 0.0)
        }
        val maxShield = getMaxShield(protectionLevel)
        val currentShield = tag?.getDouble("shield")!!
        if (currentShield >= maxShield || !Energy.of(itemStack).use(50.0)) return
        tag.putDouble("shield", currentShield + SHIELD_REGEN_RATE)
    }

    fun useShield(itemStack: ItemStack, amount: Double): Double {
        val tag = itemStack.tag
        if (tag?.contains("shield") == false) {
            return amount
        }
        val shield = tag?.getDouble("shield")!!
        val used = amount.coerceAtMost(shield)
        tag.putDouble("shield", shield - used)
        return used
    }

    fun getMaxShield(protectionLevel: Int) = protectionLevel * 100.0

    fun getShield(itemStack: ItemStack): Double {
        val tag = itemStack.tag
        return if (tag?.contains("shield") == false) return 0.0
        else tag!!.getDouble("shield")
    }

    companion object {
        const val SHIELD_REGEN_RATE = 10
    }
}