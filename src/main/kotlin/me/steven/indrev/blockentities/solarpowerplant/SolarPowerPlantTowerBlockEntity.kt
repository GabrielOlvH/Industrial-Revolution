package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SolarPowerPlantTowerStructureDefinition
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlockEntity
    : BlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY),
    BlockEntityClientSerializable, Tickable, PropertyDelegateHolder, ComponentProvider {

    val propertyDelegate = ArrayPropertyDelegate(4)
    val temperatureComponent = TemperatureComponent({ null }, 0.2, 1100..1300, 1500.0, { this })
    val multiblockComponent = SolarPowerPlantMultiblockComponent()
    val fluidComponent = FluidComponent(this, FluidAmount.ofWhole(16))

    var heliostats = 0

    override fun tick() {
        multiblockComponent.tick(world!!, pos, cachedState)
        if (multiblockComponent.isBuilt(world!!, pos, cachedState)) {
            SolarPowerPlantTowerStructureDefinition.getSmelterPositions(pos, cachedState).forEach { smelterPos ->
                val blockEntity = world!!.getBlockEntity(smelterPos) as? SolarPowerPlantSmelterBlockEntity ?: return@forEach
                blockEntity.tickStacks(this)
            }
            val limit = heliostats * 3
            temperatureComponent.tick(temperatureComponent.temperature < limit)
            heliostats = 0
        }
    }

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            ComponentKey.TEMPERATURE -> temperatureComponent
            ComponentKey.MULTIBLOCK -> multiblockComponent
            else -> null
        }
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        temperatureComponent.fromTag(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        temperatureComponent.toTag(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.toTag(tag)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        temperatureComponent.fromTag(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        temperatureComponent.toTag(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.toTag(tag)
        return tag
    }

    override fun getPropertyDelegate(): PropertyDelegate = propertyDelegate

    inner class SolarPowerPlantMultiblockComponent : MultiBlockComponent({ id -> id.structure == "solar_power_plant" }, { _, _, _ -> SolarPowerPlantTowerStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            SolarPowerPlantTowerStructureDefinition.getSolarReceiverPositions(pos, blockState).forEach { receiverPos ->
                val blockEntity = world.getBlockEntity(receiverPos) as? SolarReceiverBlockEntity ?: return@forEach
                blockEntity.controllerPos = pos
            }
        }
    }
}