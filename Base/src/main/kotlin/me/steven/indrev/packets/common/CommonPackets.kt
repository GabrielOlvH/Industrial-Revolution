package me.steven.indrev.packets.common

object CommonPackets {
    fun register() {
        ClickFluidWidgetPacket.register()
        ToggleAutoInputOutputPacket.register()
        UpdateMachineIOPacket.register()
    }
}