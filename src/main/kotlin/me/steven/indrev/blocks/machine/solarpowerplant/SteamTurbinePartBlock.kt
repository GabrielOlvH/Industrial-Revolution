package me.steven.indrev.blocks.machine.solarpowerplant

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.CombinableAttribute
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.blocks.misc.HorizontalFacingBlock
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbinePartBlock(settings: Settings) : HorizontalFacingBlock(settings), AttributeProvider {
    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        if ((to.attribute == FluidAttributes.INSERTABLE || to.attribute == FluidAttributes.GROUPED_INV) && SteamTurbineBlockEntity.INPUT_VALVES_MAPPER.containsKey(pos.asLong())) {
            val turbinePos = BlockPos.fromLong(SteamTurbineBlockEntity.INPUT_VALVES_MAPPER[pos.asLong()])
            val blockEntity = world.getBlockEntity(turbinePos) as? SteamTurbineBlockEntity
            to.offer(blockEntity?.fluidComponent ?: (to.attribute as CombinableAttribute<*>).defaultValue)
        } else to.offer((to.attribute as CombinableAttribute<*>).defaultValue)
    }
}