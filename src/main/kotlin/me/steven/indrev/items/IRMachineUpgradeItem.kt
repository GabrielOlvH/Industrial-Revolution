package me.steven.indrev.items

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.FacingMachineBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.blocks.VerticalFacingMachineBlock
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
        var state = world?.getBlockState(blockPos)
        val block = state?.block as? MachineBlock ?: return ActionResult.PASS
        val blockEntity = world?.getBlockEntity(blockPos) as? MachineBlockEntity ?: return ActionResult.PASS
        if (block.tier == from) {
            if (!blockEntity.registry.upgradeable) return ActionResult.PASS
            state = blockEntity.registry.block(to).defaultState
            if (state.contains(VerticalFacingMachineBlock.FACING))
                state = state.with(VerticalFacingMachineBlock.FACING, state[VerticalFacingMachineBlock.FACING])
            else if (state.contains(FacingMachineBlock.HORIZONTAL_FACING))
                state = state.with(FacingMachineBlock.HORIZONTAL_FACING, state[FacingMachineBlock.HORIZONTAL_FACING])
            world.setBlockState(blockPos, state)
            val upgradedBlockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity
                ?: throw RuntimeException("This should never happen, what the fuck")
            upgradedBlockEntity.energy = blockEntity.energy
            upgradedBlockEntity.inventoryController?.fromTag(blockEntity.inventoryController?.toTag(CompoundTag()))
            upgradedBlockEntity.temperatureController?.fromTag(blockEntity.temperatureController?.toTag(CompoundTag()))
            context.player?.getStackInHand(context.hand)?.decrement(1)
        }
        return super.useOnBlock(context)
    }
}