package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.SteamTurbineScreenHandler
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineBlock(registry: MachineRegistry, settings: Settings)
    : HorizontalFacingMachineBlock(registry, settings, Tier.MK4, IRConfig.generators.steamTurbine, ::SteamTurbineScreenHandler) {
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(this)) {
            val blockEntity = world.getBlockEntity(pos) as? SteamTurbineBlockEntity ?: return
            SteamTurbineStructureDefinition.getInputValvePositions(pos, state, blockEntity.multiblockComponent!!.getSelectedMatcher(world, pos, state)).forEach { valvePos ->
                SteamTurbineBlockEntity.FLUID_VALVES_MAPPER.remove(valvePos.asLong())
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }
}