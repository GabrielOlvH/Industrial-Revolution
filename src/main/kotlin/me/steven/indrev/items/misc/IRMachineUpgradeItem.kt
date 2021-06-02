package me.steven.indrev.items.misc

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.blocks.machine.MachineBlock
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.world.World
import java.util.*

class IRMachineUpgradeItem(settings: Settings, private val from: Tier, private val to: Tier) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.tier_upgrade_${to.toString().lowercase(Locale.getDefault())}.tooltip").formatted(Formatting.GREEN))
        super.appendTooltip(stack, world, tooltip, context)
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        val world = context?.world
        if (world?.isClient == true) return ActionResult.PASS
        val blockPos = context?.blockPos
        val state = world?.getBlockState(blockPos)
        val block = state?.block as? MachineBlock ?: return ActionResult.PASS
        val blockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity<*> ?: return ActionResult.PASS
        if (block.tier == from) {
            if (!blockEntity.registry.upgradeable) return ActionResult.PASS
            
            val inventoryTag = blockEntity.inventoryComponent?.writeNbt(NbtCompound())
            blockEntity.inventoryComponent?.inventory?.clear()
            val fluidTag = blockEntity.fluidComponent?.writeNbt(NbtCompound())
            val temperatureTag = blockEntity.temperatureComponent?.writeNbt(NbtCompound())
            val energy = blockEntity.energy

            var newState = blockEntity.registry.block(to).defaultState
            if (state.contains(FacingMachineBlock.FACING))
                newState = newState.with(FacingMachineBlock.FACING, state[FacingMachineBlock.FACING])
            else if (state.contains(HorizontalFacingMachineBlock.HORIZONTAL_FACING))
                newState = newState.with(HorizontalFacingMachineBlock.HORIZONTAL_FACING, state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])
            world.setBlockState(blockPos, newState)

            val upgradedBlockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity<*>
                ?: throw RuntimeException("This should never happen, what the fuck")
            upgradedBlockEntity.energy = energy
            upgradedBlockEntity.inventoryComponent?.readNbt(inventoryTag)
            upgradedBlockEntity.fluidComponent?.readNbt(fluidTag)
            upgradedBlockEntity.temperatureComponent?.readNbt(temperatureTag)

            context.player?.getStackInHand(context.hand)?.decrement(1)
            return ActionResult.CONSUME
        }
        return super.useOnBlock(context)
    }
}