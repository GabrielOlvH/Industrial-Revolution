package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag

class TankBlockEntity(tier: Tier) : BlockEntity(
    when (tier) {
        Tier.MK1 -> IRRegistry.TANK_BLOCK_ENTITY_MK1
        Tier.MK2 -> IRRegistry.TANK_BLOCK_ENTITY_MK2
        Tier.MK3 -> IRRegistry.TANK_BLOCK_ENTITY_MK3
        else -> IRRegistry.TANK_BLOCK_ENTITY_MK4
    }
) {
    val fluidComponent = FluidComponent(
        FluidAmount(
            when (tier) {
                Tier.MK1 -> 4
                Tier.MK2 -> 8
                Tier.MK3 -> 16
                else -> 32 }
        )
    )

    override fun toTag(tag: CompoundTag): CompoundTag {
        fluidComponent.toTag(tag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        fluidComponent.fromTag(tag)
    }
}