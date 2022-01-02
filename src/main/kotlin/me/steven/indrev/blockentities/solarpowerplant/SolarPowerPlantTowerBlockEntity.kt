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
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SolarPowerPlantTowerBlockEntity(pos: BlockPos, state: BlockState)
    : BaseBlockEntity(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY, pos, state), Syncable {

    override val guiSyncableComponent = GuiSyncableComponent()

    val temperatureComponent = TemperatureComponent(this, 0.09, 1100..1300, 1500)
    val multiblockComponent = SolarPowerPlantMultiblockComponent()
    val fluidComponent = object : FluidComponent({ this }, bucket * 16, 2) {
        init {
            this.inputTanks = intArrayOf(0)
            this.outputTanks = intArrayOf(1)
            this.unsided = true
        }
    }

    var isMarkedForUpdate = false

    init {
        trackObject(INPUT_TANK_ID, fluidComponent[0])
        trackObject(OUTPUT_TANK_ID, fluidComponent[1])
    }

    var heliostats = 0

    companion object {
        const val INPUT_TANK_ID = 4
        const val OUTPUT_TANK_ID = 5

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: SolarPowerPlantTowerBlockEntity) {
            blockEntity.multiblockComponent.tick(world, pos, state)
            if (blockEntity.multiblockComponent.isBuilt(world, pos, state)) {
                val limit = blockEntity.heliostats * 6
                blockEntity.temperatureComponent.tick(blockEntity.temperatureComponent.temperature < limit + (world.random.nextFloat() * 2 - 1) * 10)
                blockEntity.markForUpdate()
                blockEntity.heliostats = 0
            }
        }
    }

    override fun fromTag(tag: NbtCompound) {
        temperatureComponent.readNbt(tag)
        fluidComponent.fromTag(tag)
        multiblockComponent.readNbt(tag)
    }

    override fun toTag(tag: NbtCompound) {
        temperatureComponent.writeNbt(tag)
        fluidComponent.toTag(tag)
        multiblockComponent.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        multiblockComponent.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        multiblockComponent.writeNbt(tag)
    }

    override fun markForUpdate(condition: () -> Boolean) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
    }

    inner class SolarPowerPlantMultiblockComponent : MultiBlockComponent({ _, _, _ -> SolarPowerPlantTowerStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            SolarPowerPlantTowerStructureDefinition.getSolarReceiverPositions(pos, blockState).forEach { receiverPos ->
                val blockEntity = world.getBlockEntity(receiverPos) as? SolarReceiverBlockEntity ?: return@forEach
                blockEntity.controllerPos = pos
            }

            SolarPowerPlantTowerStructureDefinition.getFluidInputPositions(pos, blockState).forEach { inputPos ->

            }
        }
    }
}