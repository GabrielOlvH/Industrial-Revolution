package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.blocks.HeliostatBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos

class HeliostatBlockEntity : BlockEntity(IRBlockRegistry.HELIOSTAT_BLOCK_ENTITY), BlockEntityClientSerializable, Tickable {

    var targetBlock: BlockPos = BlockPos.ORIGIN

    @Environment(EnvType.CLIENT)
    var pitch: Float = 0.0f
    @Environment(EnvType.CLIENT)
    var yaw: Float = 0.0f

    override fun tick() {
        //this doesn't work, i need another way to check if there's no obstruction
        //val hit = world!!.raycastBlock(pos.toVec3d().add(0.5, 1.5, 0.5), targetBlock.toVec3d().add(0.5, 0.5, 0.5), pos, cachedState.getOutlineShape(world, pos), cachedState)
        //if (hit !is BlockHitResult || hit.blockPos != targetBlock) return
        if (!world!!.isSkyVisible(pos.up())) return
        val receiver = world!!.getBlockEntity(targetBlock) as? SolarReceiverBlockEntity ?: return
        val controller = world!!.getBlockEntity(receiver.controllerPos) as? SolarPowerPlantTowerBlockEntity ?: return
        controller.heliostats++
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putLong("target", targetBlock.asLong())
        return super.toTag(tag)
    }
    
    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        targetBlock = BlockPos.fromLong(tag!!.getLong("target"))
        super.fromTag(state, tag)
    }
    
    override fun fromClientTag(tag: CompoundTag?) {
        targetBlock = BlockPos.fromLong(tag!!.getLong("target"))
        yaw = HeliostatBlock.getYaw(pos, targetBlock)
        pitch = HeliostatBlock.getPitch(pos, targetBlock)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putLong("target", targetBlock.asLong())
        return tag
    }
}