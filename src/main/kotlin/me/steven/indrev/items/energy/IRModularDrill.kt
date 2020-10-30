package me.steven.indrev.items.energy

import draylar.magna.api.MagnaTool
import me.steven.indrev.api.CustomEnchantmentProvider
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.Energy

class IRModularDrill(
    toolMaterial: ToolMaterial,
    tier: Tier,
    maxStored: Double,
    baseMiningSpeed: Float,
    settings: Settings
) : IRMiningDrill(toolMaterial, tier, maxStored, baseMiningSpeed, settings), MagnaTool, IRModularItem<Module>, CustomEnchantmentProvider {

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val material = state?.material
        val hasEnergy = Energy.of(stack).energy > 0
        val level = MiningToolModule.EFFICIENCY.getLevel(stack)
        var speedMultiplier = (level + 1) * 2
        if (level == 5) speedMultiplier *= 50
        return when {
            SUPPORTED_MATERIALS.contains(material) && hasEnergy -> baseMiningSpeed + speedMultiplier.toFloat()
            !hasEnergy -> 0F
            else -> super.getMiningSpeedMultiplier(stack, state)
        }
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        super.appendTooltip(stack, world, tooltip, context)
        getInstalledTooltip(getInstalled(stack), stack, tooltip)
    }

    override fun getSlotLimit(): Int = -1

    override fun getCompatibleModules(itemStack: ItemStack): Array<Module> = DrillModule.COMPATIBLE

    override fun getLevel(enchantment: Enchantment, itemStack: ItemStack): Int {
        val module =
            when {
                Enchantments.FORTUNE == enchantment -> DrillModule.FORTUNE
                Enchantments.SILK_TOUCH == enchantment -> DrillModule.SILK_TOUCH
                else -> return -1
            }
        return module.getLevel(itemStack)
    }

    override fun getRadius(stack: ItemStack): Int = DrillModule.RANGE.getLevel(stack)

    override fun playBreakEffects(): Boolean = false
}