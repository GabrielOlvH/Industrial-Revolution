package me.steven.indrev.packets

import me.steven.indrev.packets.client.*
import me.steven.indrev.packets.common.*

object PacketRegistry {
    fun registerServer() {
        ConfigureIOPackets.register()
        FluidGuiHandInteractionPacket.register()
        ItemPipePackets.register()
        SelectModuleOnWorkbenchPacket.register()
        ToggleFactoryStackSplittingPacket.register()
        UpdateAOEMachineRangePacket.register()
        UpdateMiningDrillBlockBlacklistPacket.register()
        UpdateKnobValue.register()
        UpdateModularToolLevelPacket.register()
        UpdateRancherConfigPacket.register()
    }

    fun registerClient() {
        ClientItemPipePackets.register()
        IntPropertyDelegateSyncPacket.register()
        MachineStateUpdatePacket.register()
        MiningRigSpawnBlockParticlesPacket.register()
        SyncAppliedModulesPacket.register()
        SyncConfigPacket.register()
        SyncNetworkServosPacket.register()
        SyncVeinTypesPacket.register()
    }
}