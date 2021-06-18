package me.steven.indrev.blocks.machine.pipes

import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.BasePipeBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.utils.fluidExtractableOf
import me.steven.indrev.utils.fluidInsertableOf
import me.steven.indrev.utils.pack
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.function.IntFunction

class FluidPipeBlock(settings: Settings, tier: Tier) : BasePipeBlock(settings, tier, Network.Type.FLUID) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxTransferRate").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.fluidsec", getMaxTransferRate()).formatted(Formatting.GRAY))
        )
    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction): Boolean {
        if (fluidInsertableOf(world, pos, dir.opposite) != RejectingFluidInsertable.NULL
            || fluidExtractableOf(world, pos, dir.opposite) != EmptyFluidExtractable.NULL
        ) return true
        if ((type.getNetworkState(world) as ServoNetworkState<*>).hasServo(pos.offset(dir.opposite), dir))
            return true
        val blockEntity = world.getBlockEntity(pos) as? BasePipeBlockEntity ?: return false
        if (!blockEntity.cachedState.isOf(this)) return false
        return blockEntity.connections[dir.opposite]!!.isConnectable()
    }

    private fun getMaxTransferRate() = when(tier) {
        Tier.MK1 -> IRConfig.cables.fluidPipeMk1
        Tier.MK2 -> IRConfig.cables.fluidPipeMk2
        Tier.MK3 -> IRConfig.cables.fluidPipeMk3
        else -> IRConfig.cables.fluidPipeMk4
    }

    override fun getShape(blockEntity: BasePipeBlockEntity): VoxelShape {
        val directions = Direction.values().filter { dir -> blockEntity.connections[dir] == ConnectionType.CONNECTED }
        return SHAPE_CACHE.computeIfAbsent(pack(directions).toInt(), IntFunction {
            var shape = CENTER_SHAPE
            directions.forEach { direction ->
                shape = VoxelShapes.union(shape, getShape(direction))
            }
            shape
        })
    }

    companion object {

        val SHAPE_CACHE = Int2ObjectOpenHashMap<VoxelShape>()

        val DOWN_SHAPE: VoxelShape = createCuboidShape(6.0, 0.0, 6.0, 10.0, 6.0, 10.0)
        val UP_SHAPE: VoxelShape = createCuboidShape(6.0, 10.0, 6.0, 10.0, 16.0, 10.0)
        val SOUTH_SHAPE: VoxelShape = createCuboidShape(6.0, 6.0, 10.5, 10.0, 10.0, 16.0)
        val NORTH_SHAPE: VoxelShape = createCuboidShape(6.0, 6.0, 0.0, 10.0, 10.0, 6.0)
        val EAST_SHAPE: VoxelShape = createCuboidShape(10.0, 6.0, 6.0, 16.0, 10.0, 10.0)
        val WEST_SHAPE: VoxelShape = createCuboidShape(0.0, 6.0, 6.0, 6.0, 10.0, 10.0)

        val CENTER_SHAPE: VoxelShape = createCuboidShape(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)

        private fun getShape(direction: Direction): VoxelShape {
            var shape = VoxelShapes.empty()
            if (direction == Direction.NORTH) shape = NORTH_SHAPE
            if (direction == Direction.SOUTH) shape = SOUTH_SHAPE
            if (direction == Direction.EAST) shape = EAST_SHAPE
            if (direction == Direction.WEST) shape = WEST_SHAPE
            if (direction == Direction.UP) shape = UP_SHAPE
            if (direction == Direction.DOWN) shape = DOWN_SHAPE
            return shape
        }

    }
}