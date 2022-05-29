package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntity
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantTowerBlockEntity
import me.steven.indrev.blocks.misc.HorizontalFacingBlock
import me.steven.indrev.components.multiblock.definitions.SolarPowerPlantTowerStructureDefinition
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.machines.SolarPowerPlantTowerScreenHandler
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtLong
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlock(settings: Settings) : HorizontalFacingBlock(settings), BlockEntityProvider {

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = SolarPowerPlantTowerBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else return BlockEntityTicker { world, pos, state, blockEntity -> SolarPowerPlantTowerBlockEntity.tick(world, pos, state, blockEntity as? SolarPowerPlantTowerBlockEntity ?: return@BlockEntityTicker) }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            val stack = player.getStackInHand(hand)
            if (stack.isIn(IndustrialRevolution.SCREWDRIVER_TAG) && stack.orCreateNbt.contains("SelectedHeliostats")) {
                val positions = stack.nbt!!.getList("SelectedHeliostats", 4)?.map { BlockPos.fromLong((it as NbtLong).longValue()) }
                val receivers = SolarPowerPlantTowerStructureDefinition.getSolarReceiverPositions(pos, state)

                positions?.forEach { p ->
                    val heliostat = world.getBlockEntity(p) as HeliostatBlockEntity
                    val target = receivers.minByOrNull { it.getManhattanDistance(p) } ?: return@forEach
                    heliostat.targetBlock = target
                    heliostat.markDirty()
                    heliostat.sync()
                }
                player.sendMessage(literal("Linked Heliostats!"), true)
            } else {
                val blockEntity = world.getBlockEntity(pos) as? SolarPowerPlantTowerBlockEntity ?: return ActionResult.PASS
                if (!blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                    player.sendMessage(translatable("text.multiblock.not_built"), true)
                    blockEntity.multiblockComponent.toggleRender(player.isSneaking)
                    blockEntity.markDirty()
                    blockEntity.sync()
                } else
                    player.openHandledScreen(IRScreenHandlerFactory(::SolarPowerPlantTowerScreenHandler, pos))
            }
        }
        return ActionResult.success(world.isClient)
    }
}