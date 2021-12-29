package me.steven.indrev.blockentities.generators

import me.steven.indrev.registry.IRBlockRegistry
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