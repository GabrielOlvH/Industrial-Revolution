package me.steven.indrev.packets.common

import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object FluidGuiHandInteractionPacket  {

    val FLUID_CLICK_PACKET = identifier("fluid_widget_click") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(FLUID_CLICK_PACKET) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val tank = buf.readInt()
            val world = player.world
            server.execute {
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                    val fluidComponent = blockEntity.fluidComponent ?: return@execute
                    FluidInvUtil.interactCursorWithTank(
                        fluidComponent.getInteractInventory(tank),
                        player,
                        fluidComponent.getFilterForTank(tank)
                    )
                }
            }
        }
    }
}