package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.FacingMachineBlock
import me.steven.indrev.blocks.HorizontalFacingMachineBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.utils.Tier
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRMachineUpgradeItem(settings: Settings, private val from: Tier, private val to: Tier) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.tier_upgrade_${to.toString().toLowerCase()}.tooltip").formatted(Formatting.ITALIC, Formatting.BLUE))
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        val world = context?.world
        if (world?.isClient == true) return ActionResult.PASS
        val blockPos = context?.blockPos
        val state = world?.getBlockState(blockPos)
        val block = state?.block as? MachineBlock ?: return ActionResult.PASS
        val blockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity ?: return ActionResult.PASS
        if (block.tier == from) {
            if (!blockEntity.registry.upgradeable) return ActionResult.PASS
            var newState = blockEntity.registry.block(to).defaultState
            if (state.contains(FacingMachineBlock.FACING))
                newState = newState.with(FacingMachineBlock.FACING, state[FacingMachineBlock.FACING])
            else if (state.contains(HorizontalFacingMachineBlock.HORIZONTAL_FACING))
                newState = newState.with(HorizontalFacingMachineBlock.HORIZONTAL_FACING, state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])
            world.setBlockState(blockPos, newState)
            val upgradedBlockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity
                ?: throw RuntimeException("This should never happen, what the fuck")
            upgradedBlockEntity.energy = blockEntity.energy
            upgradedBlockEntity.inventoryComponent?.fromTag(blockEntity.inventoryComponent?.toTag(CompoundTag()))
            upgradedBlockEntity.temperatureComponent?.fromTag(blockEntity.temperatureComponent?.toTag(CompoundTag()))
            context.player?.getStackInHand(context.hand)?.decrement(1)
            return ActionResult.CONSUME
        }
        return super.useOnBlock(context)
    }
}