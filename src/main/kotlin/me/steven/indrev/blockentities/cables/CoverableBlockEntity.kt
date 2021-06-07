package me.steven.indrev.blockentities.cables

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry

class CoverableBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    BlockEntity(when (tier) {
        Tier.MK1 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK1
        Tier.MK2 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK2
        Tier.MK3 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK3
        Tier.MK4 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK4
        Tier.CREATIVE -> error("no creative cable")
    }, pos, state), BlockEntityClientSerializable {
    var coverState: BlockState? = null

    override fun readNbt(tag: NbtCompound?) {
        if (tag?.contains("cover") == true)
            Registry.BLOCK.getOrEmpty(Identifier(tag.getString("cover")))
                .ifPresent { block -> this.coverState = block.defaultState }
        if (tag?.contains("coverState") == true) {
            BlockState.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("coverState")).result().ifPresent { pair ->
                this.coverState = pair.first
            }
        }
        super.readNbt(tag)
    }

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        if (this.coverState != null) {
            BlockState.CODEC.encode(this.coverState, NbtOps.INSTANCE, NbtCompound()).result().ifPresent { t ->
                tag?.put("coverState", t)
            }
        }
        return super.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound?) {
        if (tag?.contains("cover") == true)
            Registry.BLOCK.getOrEmpty(Identifier(tag.getString("cover")))
                .ifPresent { block -> this.coverState = block.defaultState }
        if (tag?.contains("coverState") == true) {
            BlockState.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("coverState")).result().ifPresent { pair ->
                if (pair.first != null) this.coverState = pair.first
            }
        }
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        if (this.coverState != null) {
            BlockState.CODEC.encode(this.coverState, NbtOps.INSTANCE, NbtCompound()).result().ifPresent { t ->
                tag.put("coverState", t)
            }
        }
        return tag
    }
}