package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag

class SolarReflectorBlockEntity : BlockEntity(IRBlockRegistry.SOLAR_REFLECTOR_BLOCK_ENTITY), BlockEntityClientSerializable {
    var pitch: Float = 0.0f
    var yaw: Float = 0.0f

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putFloat("pitch", pitch)
        tag?.putFloat("yaw", yaw)
        return super.toTag(tag)
    }
    
    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        pitch = tag?.getFloat("pitch") ?: 0.0f
        yaw = tag?.getFloat("yaw") ?: 0.0f
        super.fromTag(state, tag)
    }
    
    override fun fromClientTag(tag: CompoundTag?) {
        pitch = tag?.getFloat("pitch") ?: 0.0f
        yaw = tag?.getFloat("yaw") ?: 0.0f
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putFloat("pitch", pitch)
        tag.putFloat("yaw", yaw)
        return tag
    }
}