package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntity
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantTowerBlockEntity
import me.steven.indrev.blocks.misc.HorizontalFacingBlock
import me.steven.indrev.components.multiblock.SolarPowerPlantTowerStructureDefinition
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.machines.SolarPowerPlantTowerScreenHandler
import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.LongTag
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class SolarPowerPlantTowerBlock(settings: Settings) : HorizontalFacingBlock(settings), BlockEntityProvider {

    override fun createBlockEntity(world: BlockView?): BlockEntity = SolarPowerPlantTowerBlockEntity()

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
            if (stack.item == IRItemRegistry.WRENCH && stack.orCreateTag.contains("SelectedHeliostats")) {
                val positions = stack.tag!!.getList("SelectedHeliostats", 4)?.map { BlockPos.fromLong((it as LongTag).long) }
                val receivers = SolarPowerPlantTowerStructureDefinition.getSolarReceiverPositions(pos, state)

                positions?.forEach { p ->
                    val heliostat = world.getBlockEntity(p) as HeliostatBlockEntity
                    val target = receivers.minByOrNull { it.getSquaredDistance(p) } ?: return@forEach
                    heliostat.targetBlock = target
                    heliostat.markDirty()
                    heliostat.sync()
                }
                player.sendMessage(LiteralText("Linked Heliostats!"), true)
            } else {
                val blockEntity = world.getBlockEntity(pos) as? SolarPowerPlantTowerBlockEntity ?: return ActionResult.PASS
                if (!blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                    player.sendMessage(TranslatableText("text.multiblock.not_built"), true)
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