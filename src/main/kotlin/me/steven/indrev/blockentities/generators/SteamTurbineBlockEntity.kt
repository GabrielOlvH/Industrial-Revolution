package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.registry.MachineRegistry

class SteamTurbineBlockEntity : GeneratorBlockEntity(Tier.MK4, MachineRegistry.STEAM_TURBINE_REGISTRY) {

    init {
        this.multiblockComponent = MultiBlockComponent({ id -> id.structure == "steam_turbine"}, { _, _, _ -> SteamTurbineStructureDefinition })
    }

    override fun shouldGenerate(): Boolean = false
}