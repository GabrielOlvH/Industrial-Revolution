package me.steven.indrev.blocks

import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class InterfacedMachineBlock(settings: Settings, private val screenId: Identifier, private val openInterface: (BlockEntity?) -> Boolean, blockEntityProvider: () -> BasicMachineBlockEntity) : BasicMachineBlock(settings, blockEntityProvider) {
    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        if (world.isClient) return ActionResult.PASS
        val blockEntity = world.getBlockEntity(pos)
        if (openInterface(blockEntity)) {
            ContainerProviderRegistry.INSTANCE.openContainer(
                    screenId,
                    player
            ) { packetByteBuf -> packetByteBuf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }

}