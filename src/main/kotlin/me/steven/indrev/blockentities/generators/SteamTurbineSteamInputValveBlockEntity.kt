package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class SteamTurbineSteamInputValveBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(IRBlockRegistry.STEAM_TURBINE_STEAM_INPUT_VALVE_BLOCK_ENTITY, pos, state) {

    var steamTurbinePos: BlockPos = BlockPos(-1, -1, -1)
    var inserted = false

    fun getSteamTurbine(): SteamTurbineBlockEntity? {
        val blockEntity = world!!.getBlockEntity(steamTurbinePos) as? SteamTurbineBlockEntity ?: return null
        return if (blockEntity.multiblockComponent?.isBuilt(world!!, pos, blockEntity.cachedState) == true)
            blockEntity
        else null
    }
}