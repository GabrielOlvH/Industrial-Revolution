package me.steven.indrev.blocks

import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntity
import me.steven.indrev.utils.forEach
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockView
import net.minecraft.world.World

class SolarReflectorBlock(settings: Settings) : Block(settings), BlockEntityProvider {

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.ENTITYBLOCK_ANIMATED

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if (world.isClient) {
            val target = pos.mutableCopy()
            Box(pos).expand(7.0).forEach { x, y, z ->
                target.set(x, y, z)
                if (world.testBlockState(target) { it.isOf(Blocks.OBSIDIAN) }) {
                    val yaw = getYaw(pos, target)
                    val pitch = getPitch(pos, target)
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeFloat(yaw)
                    buf.writeFloat(pitch)
                    buf.writeBlockPos(pos)
                    ClientSidePacketRegistry.INSTANCE.sendToServer(SET_ANGLES_PACKET, buf)
                    return
                }
            }
        }
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? = HeliostatBlockEntity()

    private fun getYaw(origin: BlockPos, target: BlockPos): Float {
        val xOffset = target.x - origin.x.toDouble()
        val zOffset = target.z - origin.z.toDouble()
        return MathHelper.wrapDegrees((MathHelper.atan2(zOffset, xOffset) * RAD2DEG).toFloat() - 90.0f)
    }

    private fun getPitch(origin: BlockPos, target: BlockPos): Float {
        val xOffset = target.x - origin.x.toDouble()
        val yOffset = target.y - origin.y.toDouble()
        val zOffset = target.z - origin.z.toDouble()
        val g = MathHelper.sqrt(xOffset * xOffset + zOffset * zOffset).toDouble()
        return MathHelper.wrapDegrees((-(MathHelper.atan2(yOffset, g) * RAD2DEG)).toFloat())
    }

    companion object {
        val SET_ANGLES_PACKET = identifier("reflector_set_angles")
        private const val RAD2DEG = 57.2957763671875
    }
}