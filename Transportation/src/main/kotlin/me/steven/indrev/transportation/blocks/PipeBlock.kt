package me.steven.indrev.transportation.blocks

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import me.steven.indrev.transportation.networks.*
import me.steven.indrev.transportation.networks.types.PipeNetwork
import me.steven.indrev.transportation.networks.types.StoragePipeNetwork
import me.steven.indrev.transportation.utils.PipeConnections
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.function.IntFunction

abstract class PipeBlock : Block(FabricBlockSettings.copyOf(Blocks.GLASS).solidBlock { _, _, _ -> false }.nonOpaque()) {

    abstract fun createNetwork(world: ServerWorld): PipeNetwork<*>

    override fun getAmbientOcclusionLightLevel(state: BlockState?, world: BlockView?, pos: BlockPos?): Float = 1f

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world is ServerWorld) {
            update(world, pos, state.block, this)
        }
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
        if (world is ServerWorld) {
            update(world, pos, newState.block, this)
        }
    }

    override fun hasDynamicBounds(): Boolean = true

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext?
    ): VoxelShape {
        if (world is ClientWorld) {
            return getShape(PipeConnections(ClientPipeNetworkData.renderData[pos.asLong()]))
        } else if (world is ServerWorld) {
            return getShape(world.networkManager.networksByPos[pos.asLong()]?.nodes?.get(pos.asLong())?.connections)
        }
        return super.getOutlineShape(state, world, pos, context)
    }

    private fun getShape(connections: PipeConnections?): VoxelShape {
        if (connections == null || connections.value == -1) return CENTER_SHAPE
        return SHAPE_CACHE.get().computeIfAbsent(connections.value, IntFunction {
            var shape = CENTER_SHAPE
            DIRECTIONS.forEach { dir ->
                if (connections.contains(dir))
                    shape = VoxelShapes.union(shape, Companion.getShape(dir))
            }
            shape
        })
    }

    companion object {

        val SHAPE_CACHE: ThreadLocal<Int2ObjectOpenHashMap<VoxelShape>> = ThreadLocal.withInitial {
            object : Int2ObjectOpenHashMap<VoxelShape>(64, 0.25f) {
                override fun rehash(newN: Int) {
                }
            }
        }

        val DOWN_SHAPE: VoxelShape = createCuboidShape(6.5, 0.0, 6.5, 9.5, 6.5, 9.5)
        val UP_SHAPE: VoxelShape = createCuboidShape(6.5, 9.5, 6.5, 9.5, 16.0, 9.5)
        val SOUTH_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 9.5, 9.5, 9.5, 16.0)
        val NORTH_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 0.0, 9.5, 9.5, 6.5)
        val EAST_SHAPE: VoxelShape = createCuboidShape(9.5, 6.5, 6.5, 16.0, 9.5, 9.5)
        val WEST_SHAPE: VoxelShape = createCuboidShape(0.0, 6.5, 6.5, 6.5, 9.5, 9.5)

        val CENTER_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 6.5, 9.5, 9.5, 9.5)

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

        fun getSideFromHit(hit: Vec3d, pos: BlockPos): Direction? {
            val x = hit.x - pos.x
            val y = hit.y - pos.y
            val z = hit.z - pos.z
            return when {
                y > 0.6625 -> Direction.UP
                y < 0.3375 -> Direction.DOWN
                x > 0.6793 -> Direction.EAST
                x < 0.3169 -> Direction.WEST
                z < 0.3169 -> Direction.NORTH
                z > 0.6625 -> Direction.SOUTH
                else -> null
            }
        }
    }
}