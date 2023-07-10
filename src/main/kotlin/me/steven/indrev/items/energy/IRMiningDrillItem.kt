package me.steven.indrev.items.energy

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.gui.tooltip.energy.EnergyTooltipData
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.use
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.BlockTags
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

open class IRMiningDrillItem(
    toolMaterial: ToolMaterial,
    private val tier: Tier,
    private val maxStored: Double,
    val baseMiningSpeed: Float,
    settings: Settings
) : PickaxeItem(toolMaterial, 0, 0F, settings.maxDamage(-1)), IREnergyItem {


    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState): Float {
        val canMine = state.isIn(SUPPORTED_MATERIALS)
        val hasEnergy = (energyOf(stack)?.amount ?: 0) > 0
        return when {
            canMine && hasEnergy -> baseMiningSpeed
            !hasEnergy -> 0F
            else -> super.getMiningSpeedMultiplier(stack, state)
        }
    }

    override fun postHit(stack: ItemStack, target: LivingEntity?, attacker: LivingEntity): Boolean {
        if (attacker !is PlayerEntity) return false
        energyOf(attacker.inventory, attacker.inventory.selectedSlot)?.use(2) ?: return false
        return true
    }

    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos?,
        miner: LivingEntity
    ): Boolean {
        if (miner !is PlayerEntity) return false
        energyOf(miner.inventory, miner.inventory.selectedSlot)?.use(1) ?: return false
        return true
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun isEnchantable(stack: ItemStack?): Boolean = false

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        return Optional.of(EnergyTooltipData(handler.amount, handler.capacity))
    }

    companion object {
        val SUPPORTED_MATERIALS = BlockTags.PICKAXE_MINEABLE // TODO create custom tag with shovel, pickaxe and axe
    }
}