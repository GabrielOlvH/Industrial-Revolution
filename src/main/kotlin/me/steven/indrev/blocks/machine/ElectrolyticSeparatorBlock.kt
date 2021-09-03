package me.steven.indrev.blocks.machine

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.ElectrolyticSeparatorScreenHandler
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.createWrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ElectrolyticSeparatorBlock(registry: MachineRegistry, settings: FabricBlockSettings, tier: Tier)
    : HorizontalFacingMachineBlock(registry, settings, tier,
    when (tier) {
        Tier.MK1 -> IRConfig.machines.electrolyticSeparatorMk1
        Tier.MK2 -> IRConfig.machines.electrolyticSeparatorMk2
        Tier.MK3 -> IRConfig.machines.electrolyticSeparatorMk3
        else -> IRConfig.machines.electrolyticSeparatorMk4
                },
    ::ElectrolyticSeparatorScreenHandler
) {

    override fun addAllAttributes(world: World?, pos: BlockPos?, blockState: BlockState?, to: AttributeList<*>) {
        if (to.attribute == FluidAttributes.EXTRACTABLE) {
            val blockEntity = world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
            val fluidComponent = blockEntity.fluidComponent ?: return
            val dir = to.searchDirection?.opposite
            val mode = fluidComponent.transferConfig[dir]

            if (mode == TransferMode.OUTPUT_FIRST) to.offer(fluidComponent.createWrapper(1, 0))
            else if (mode == TransferMode.OUTPUT_SECOND) to.offer(fluidComponent.createWrapper(2, 0))

        } else return super.addAllAttributes(world, pos, blockState, to)
    }
}