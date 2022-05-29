package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.miningrig.MiningRigBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.MiningRigComputerScreenHandler
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.itemStorageOf
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class MiningRigBlock(registry: MachineRegistry, settings: Settings, tier: Tier) : HorizontalFacingMachineBlock(
    registry,
    settings,
    tier,
    IRConfig.machines.miner,
    ::MiningRigComputerScreenHandler,
) {
    override fun appendTooltip(
        stack: ItemStack?,
        view: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        super.appendTooltip(stack, view, tooltip, options)
        tooltip?.add(
            translatable("block.indrev.mining_rig.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC)
        )
    }

    override fun neighborUpdate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        block: Block?,
        fromPos: BlockPos?,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify)

        if (world is ServerWorld) {
            val dir = Direction.fromVector(fromPos!!.subtract(pos)) ?: return
            if (itemStorageOf(world, fromPos, dir) != null) {
                val blockEntity = world.getBlockEntity(pos) as? MiningRigBlockEntity ?: return
                blockEntity.storageDirections.addAll(Direction.values())
            }
        }
    }
}