package me.steven.indrev.screens

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainer
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.screenHandlerType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.screen.ScreenHandler

val MACHINE_SCREEN_HANDLER = createType<MachineScreenHandler>()

fun registerScreenHandlers() {
    identifier("machine_screen_handler").screenHandlerType(MACHINE_SCREEN_HANDLER)
}

private fun <T : ScreenHandler> createType(): ExtendedScreenHandlerType<T> {
    return ExtendedScreenHandlerType<T>(
        ExtendedScreenHandlerType.ExtendedFactory { syncId, playerInv, buf ->
            val pos = buf.readBlockPos()
            val blockEntity =
                playerInv.player.world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@ExtendedFactory null
            if (blockEntity.inventory.exists()) blockEntity.inventory.sidedConfiguration.fromPacket(buf)
            if (blockEntity.fluidInventory.exists()) blockEntity.fluidInventory.sidedConfiguration.fromPacket(buf)
            if (blockEntity is LazuliFluxContainer) blockEntity.sideConfig.fromPacket(buf)
            blockEntity.createMenu(syncId, playerInv, playerInv.player) as T
        })
}