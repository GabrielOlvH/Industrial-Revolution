package me.steven.indrev.items.energy

import draylar.magna.api.BlockProcessor
import draylar.magna.api.MagnaTool
import me.steven.indrev.api.CustomEnchantmentProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class IRModularDrillItem(
    toolMaterial: ToolMaterial,
    tier: Tier,
    maxStored: Double,
    baseMiningSpeed: Float,
    settings: Settings
) : IRMiningDrillItem(toolMaterial, tier, maxStored, baseMiningSpeed, settings), MagnaTool, IRModularItem<Module>, CustomEnchantmentProvider {

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val material = state?.material
        val hasEnergy = (energyOf(stack)?.energy ?: 0.0) > 0
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

    override fun getCenterPosition(
        world: World,
        player: PlayerEntity,
        blockHitResult: BlockHitResult,
        toolStack: ItemStack
    ): BlockPos {
        val pos = blockHitResult.blockPos
        val radius = getRadius(toolStack)
        return if (blockHitResult.side.axis == Direction.Axis.Y || radius < 1) pos
        else pos.up(radius - 1)
    }

    override fun attemptBreak(
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity,
        breakRadius: Int,
        processor: BlockProcessor?
    ): Boolean {
        val mainHandStack = player.mainHandStack
        return if (getRadius(mainHandStack) > 0)
            super.attemptBreak(world, pos, player, breakRadius, processor)
        else false
    }
}