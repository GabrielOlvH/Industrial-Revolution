package me.steven.indrev.packets

import me.steven.indrev.packets.client.*
import me.steven.indrev.packets.common.*

object PacketRegistry {
    fun registerServer() {
        ConfigureIOPackets.register()
        DataCardWriteStartPacket.register()
        GuiPropertySyncPacket.registerServer()
        FluidGuiHandInteractionPacket.register()
        ItemPipePackets.register()
        SelectModuleOnWorkbenchPacket.register()
        ToggleFactoryStackSplittingPacket.register()
        ToggleGamerAxePacket.register()
        ToggleJetpackPacket.register()
        UpdateAOEMachineRangePacket.register()
        UpdateMiningDrillBlockBlacklistPacket.register()
        UpdateKnobValue.register()
        UpdateModularToolLevelPacket.register()
        UpdateRancherConfigPacket.register()
    }

    fun registerClient() {
        ClientItemPipePackets.register()
        GuiPropertySyncPacket.register()
        MachineStateUpdatePacket.register()
        MiningRigSpawnBlockParticlesPacket.register()
        SyncAppliedModulesPacket.register()
        SyncConfigPacket.register()
        SyncNetworkServosPacket.register()
    }
}