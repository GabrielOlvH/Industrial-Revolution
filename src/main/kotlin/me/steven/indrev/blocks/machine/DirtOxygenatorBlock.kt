package me.steven.indrev.blocks.machine

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.farms.DirtOxygenatorBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.registry.MachineRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DirtOxygenatorBlock(registry: MachineRegistry, settings: FabricBlockSettings)
    : FacingMachineBlock(registry, settings, Tier.MK1, IRConfig.machines.chopperMk1, null), AttributeProvider {

    override fun addAllAttributes(world: World?, pos: BlockPos?, blockState: BlockState?, to: AttributeList<*>) {
        if (to.attribute != FluidAttributes.INSERTABLE || blockState!![FACING] == to.searchDirection?.opposite) return
        val blockEntity = world?.getBlockEntity(pos) as? DirtOxygenatorBlockEntity ?: return
        to.offer(blockEntity)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerLookDirection)
    }
}