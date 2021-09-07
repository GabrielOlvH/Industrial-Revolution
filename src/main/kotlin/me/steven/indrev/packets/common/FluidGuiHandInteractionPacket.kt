package me.steven.indrev.packets.common

import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil


object FluidGuiHandInteractionPacket  {

    val FLUID_CLICK_PACKET = identifier("fluid_widget_click") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(FLUID_CLICK_PACKET) { server, player, _, buf, _ ->
            val tank = buf.readInt()
            val world = player.world
            val screenHandler = player.currentScreenHandler as? IRGuiScreenHandler ?: return@registerGlobalReceiver
            server.execute {
                screenHandler.ctx.run { _, pos ->
                    if (world.isLoaded(pos)) {
                        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                        val fluidComponent = blockEntity.fluidComponent ?: return@run
                        val handStorage = ContainerItemContext.ofPlayerCursor(player, screenHandler).find(FluidStorage.ITEM)
                        val res = StorageUtil.move(handStorage, fluidComponent[tank], { true }, Long.MAX_VALUE, null)
                        if (res == 0L)
                            StorageUtil.move(fluidComponent[tank], handStorage, { true }, Long.MAX_VALUE, null)
                    }
                }
            }
        }
    }
}