package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.ProxyBlock
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EnergyMovement
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Tickable

class NuclearReactorProxyBlockEntity : BlockEntity(MachineRegistry.NUCLEAR_PART_BLOCK_ENTITY), Tickable {
    override fun tick() {
        if (world?.isClient == true) return

        val block = this.cachedState.block
        if (block !is ProxyBlock) return
        val parent = world?.getBlockEntity(block.getBlockEntityPos(this.cachedState, this.pos))
        if (parent is MachineBlockEntity)
            EnergyMovement.spreadNeighbors(parent, pos)
    }
}