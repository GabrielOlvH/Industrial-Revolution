package me.steven.indrev.blocks.furnace

import me.steven.indrev.blocks.ElectricBlock
import me.steven.indrev.identifier
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ElectricFurnaceBlock(settings: Settings, private val screenId: Identifier, maxBuffer: Double, blockEntityProvider: () -> ElectricFurnaceBlockEntity): ElectricBlock(settings, maxBuffer, blockEntityProvider) {

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        if (world.isClient) return ActionResult.PASS
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is ElectricFurnaceBlockEntity) {
            ContainerProviderRegistry.INSTANCE.openContainer(
                    screenId,
                    player
            ) { packetByteBuf -> packetByteBuf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }


    companion object {
        val SCREEN_ID = identifier("electric_furnace_screen")
    }
}