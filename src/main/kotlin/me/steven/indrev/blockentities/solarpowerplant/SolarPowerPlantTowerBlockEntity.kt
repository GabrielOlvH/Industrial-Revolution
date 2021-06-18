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
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY, pos, state),
    BlockEntityClientSerializable, PropertyDelegateHolder, ComponentProvider {

    val propertyDelegate = ArrayPropertyDelegate(4)
    val temperatureComponent = TemperatureComponent(this, 0.2, 1100..1300, 1500)
    val multiblockComponent = SolarPowerPlantMultiblockComponent()
    val fluidComponent = FluidComponent(this, FluidAmount.ofWhole(16))

    var heliostats = 0

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: SolarPowerPlantTowerBlockEntity) {
            blockEntity.multiblockComponent.tick(world, pos, state)
            if (blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                SolarPowerPlantTowerStructureDefinition.getSmelterPositions(pos, state).forEach { smelterPos ->
                    val smelterBlockEntity =
                        world.getBlockEntity(smelterPos) as? SolarPowerPlantSmelterBlockEntity ?: return@forEach
                    smelterBlockEntity.tickStacks(blockEntity)
                }
                val limit = blockEntity.heliostats * 3
                blockEntity.temperatureComponent.tick(blockEntity.temperatureComponent.temperature < limit)
                blockEntity.heliostats = 0
            }
        }
    }

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            ComponentKey.TEMPERATURE -> temperatureComponent
            ComponentKey.MULTIBLOCK -> multiblockComponent
            ComponentKey.PROPERTY_HOLDER -> this
            else -> null
        }
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        temperatureComponent.readNbt(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.readNbt(tag)
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
        return super.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        temperatureComponent.readNbt(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
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