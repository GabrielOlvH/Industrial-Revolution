package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.components.*
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.definitions.SolarPowerPlantTowerStructureDefinition
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlockEntity(pos: BlockPos, state: BlockState)
    : BaseBlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY, pos, state), ComponentProvider, Syncable {

    val guiSyncableComponent = GuiSyncableComponent()

    val propertyDelegate = ArrayPropertyDelegate(7)
    val temperatureComponent = TemperatureComponent(this, 0.06, 1100..1300, 1500)
    val multiblockComponent = SolarPowerPlantMultiblockComponent()
    val fluidComponent = FluidComponent({ this }, bucket * 16)

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

    override fun toTag(tag: NbtCompound) {
        temperatureComponent.readNbt(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.readNbt(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        multiblockComponent.writeNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        multiblockComponent.writeNbt(tag)
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