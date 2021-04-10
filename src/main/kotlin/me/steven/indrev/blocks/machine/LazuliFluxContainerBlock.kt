package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.LazuliFluxContainerScreenHandler
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LazuliFluxContainerBlock(registry: MachineRegistry, settings: Settings, tier: Tier) : FacingMachineBlock(
    registry, settings, tier, when (tier) {
        Tier.MK1 -> IRConfig.machines.lazuliFluxContainerMk1
        Tier.MK2 -> IRConfig.machines.lazuliFluxContainerMk2
        Tier.MK3 -> IRConfig.machines.lazuliFluxContainerMk3
        else -> IRConfig.machines.lazuliFluxContainerMk4
    }, ::LazuliFluxContainerScreenHandler
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