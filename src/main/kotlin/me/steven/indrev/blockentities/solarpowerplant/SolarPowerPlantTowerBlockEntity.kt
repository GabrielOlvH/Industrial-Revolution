package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.blockentities.crafters.SmelterBlockEntity
import me.steven.indrev.components.*
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SolarPowerPlantTowerStructureDefinition
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.rawId
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY, pos, state), BlockEntityClientSerializable, ComponentProvider, Syncable {

    val guiSyncableComponent = GuiSyncableComponent()

    val propertyDelegate = ArrayPropertyDelegate(7)
    val temperatureComponent = TemperatureComponent(this, 0.06, 1100..1300, 1500)
    val multiblockComponent = SolarPowerPlantMultiblockComponent()
    val fluidComponent = FluidComponent({this}, bucket * 16)

    init {
        trackObject(TANK_ID, fluidComponent[0])
    }

    var heliostats = 0

    companion object {
        const val TANK_ID = 2

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: SolarPowerPlantTowerBlockEntity) {
            blockEntity.multiblockComponent.tick(world, pos, state)
            if (blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                SolarPowerPlantTowerStructureDefinition.getSmelterPositions(pos, state).forEach { smelterPos ->
                    val smelterBlockEntity =
                        world.getBlockEntity(smelterPos) as? SolarPowerPlantSmelterBlockEntity ?: return@forEach
                    smelterBlockEntity.tickStacks(blockEntity)
                }
                val limit = blockEntity.heliostats * 3
                blockEntity.temperatureComponent.tick(blockEntity.temperatureComponent.temperature < limit + (world.random.nextFloat() * 2 - 1) * 10)
                blockEntity.heliostats = 0
            }
        }
    }

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            ComponentKey.TEMPERATURE -> temperatureComponent
            ComponentKey.MULTIBLOCK -> multiblockComponent
            ComponentKey.GUI_SYNCABLE -> guiSyncableComponent
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
        multiblockComponent.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        multiblockComponent.writeNbt(tag)
        return tag
    }

    override fun markForUpdate(condition: () -> Boolean) {
        //TODO
    }

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