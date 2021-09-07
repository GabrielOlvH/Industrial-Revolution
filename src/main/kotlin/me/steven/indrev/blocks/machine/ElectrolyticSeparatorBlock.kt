package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.ElectrolyticSeparatorScreenHandler
import me.steven.indrev.registry.MachineRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings

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
}