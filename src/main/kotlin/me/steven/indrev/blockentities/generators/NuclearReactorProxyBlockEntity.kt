package me.steven.indrev.blockentities.generators

import me.steven.indrev.EnergyMovement
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MultiBlockPart
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction

class NuclearReactorProxyBlockEntity : BlockEntity(MachineRegistry.NUCLEAR_PART_BLOCK_ENTITY), Tickable {
    override fun tick() {
        if (world?.isClient == true) return

        val block = this.cachedState.block
        if (block !is MultiBlockPart) return
        val parent = world?.getBlockEntity(block.getBlockEntityPos(this.cachedState, this.pos))
        if (parent !is MachineBlockEntity) return
        EnergyMovement(parent, pos).spread(*Direction.values())
    }
}