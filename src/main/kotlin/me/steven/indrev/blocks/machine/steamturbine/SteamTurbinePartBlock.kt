package me.steven.indrev.blocks.machine.steamturbine

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbinePartBlock(settings: Settings) : Block(settings), AttributeProvider {
    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        if (to.attribute == FluidAttributes.INSERTABLE && SteamTurbineBlockEntity.INPUT_VALVES_MAPPER.containsKey(pos.asLong())) {
            val turbinePos = BlockPos.fromLong(SteamTurbineBlockEntity.INPUT_VALVES_MAPPER[pos.asLong()])
            val blockEntity = world.getBlockEntity(turbinePos) as? SteamTurbineBlockEntity ?: return
            to.offer(blockEntity.fluidComponent)
        }
    }
}