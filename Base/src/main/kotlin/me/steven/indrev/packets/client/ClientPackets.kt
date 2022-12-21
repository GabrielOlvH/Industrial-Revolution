package me.steven.indrev.packets.client

object ClientPackets {
    fun register() {
        SyncMachinePropertyPacket.register()
    }
}