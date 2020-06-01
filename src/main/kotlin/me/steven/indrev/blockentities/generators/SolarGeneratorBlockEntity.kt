package me.steven.indrev.blockentities.generators

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.inventory.SidedInventory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import team.reborn.energy.EnergySide

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {
    private val inventory = DefaultSidedInventory(0, intArrayOf(), intArrayOf())

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun getMaxOutput(side: EnergySide?): Double = 32.0

    override fun getGenerationRatio(): Double = 0.1
}