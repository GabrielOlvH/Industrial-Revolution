package me.steven.indrev.blocks.machine

import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.storage.BatteryBlockEntity
import me.steven.indrev.gui.controllers.machines.BatteryController
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BatteryBlock(settings: Settings, tier: Tier) :  FacingMachineBlock(
    settings, tier, null, ::BatteryController, { BatteryBlockEntity(tier) }) {

    override fun onPlaced(
        world: World?,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world?.isClient == false && placer is ServerPlayerEntity) {
            val buf = PacketByteBuf(Unpooled.buffer())
            buf.writeInt(5)
            buf.writeBlockPos(pos)
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(placer, IndustrialRevolution.SCHEDULE_RERENDER_CHUNK_PACKET, buf)
        }
    }
}