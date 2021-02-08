package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.gui.controllers.machines.LazuliFluxContainerController
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LazuliFluxContainerBlock(registry: MachineRegistry, settings: Settings, tier: Tier) : FacingMachineBlock(
    registry, settings, tier, null, ::LazuliFluxContainerController
) {

    override fun onPlaced(
        world: World?,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world?.isClient == false) {
            val blockEntity = world.getBlockEntity(pos) as? LazuliFluxContainerBlockEntity ?: return
            blockEntity.sync()
        }
    }
}