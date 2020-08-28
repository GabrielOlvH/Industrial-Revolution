package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.blocks.TankBlock
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.IRRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction

class TankBlockEntity : BlockEntity(IRRegistry.TANK_BLOCK_ENTITY), BlockEntityClientSerializable, Tickable {
    val fluidComponent = FluidComponent(FluidAmount(8))

    override fun tick() {
        if (world?.isClient == true) return
        sync()
        if (!cachedState[TankBlock.DOWN]) return
        val tank = fluidComponent.tanks[0]
        val fluidAmount = tank.volume.amount()
        val insertable = FluidAttributes.INSERTABLE.getAllFromNeighbour(this, Direction.DOWN).firstOrNull ?: return
        val extractable = fluidComponent.extractable
        val extractionResult = extractable?.attemptAnyExtraction(fluidAmount, Simulation.SIMULATE)
        val insertionResult = insertable.attemptInsertion(extractionResult, Simulation.SIMULATE)
        if (extractionResult?.isEmpty == false) {
            val resultVolume = extractionResult.fluidKey.withAmount(extractionResult.amount().sub(insertionResult.amount()))
            if (!resultVolume.isEmpty) {
                insertable.insert(resultVolume)
                extractable.extract(resultVolume.amount())
            }
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        fluidComponent.toTag(tag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        fluidComponent.fromTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        fluidComponent.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        fluidComponent.toTag(tag)
        return tag
    }
}