package me.steven.indrev.packets.client

object ClientPackets {
    fun register() {
        SyncEnergyPacket.register()
        SyncMachinePropertyPacket.register()
    }
}