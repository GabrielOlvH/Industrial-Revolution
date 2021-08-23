package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.SteamTurbineScreenHandler
import me.steven.indrev.registry.MachineRegistry

class SteamTurbineBlock(registry: MachineRegistry, settings: Settings)
    : HorizontalFacingMachineBlock(registry, settings, Tier.MK4, IRConfig.generators.steamTurbine, ::SteamTurbineScreenHandler)