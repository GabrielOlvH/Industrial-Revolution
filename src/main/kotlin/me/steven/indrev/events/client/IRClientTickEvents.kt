package me.steven.indrev.events.client

import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.gui.IRModularControllerScreen
import me.steven.indrev.gui.screenhandlers.modular.ModularItemConfigurationScreenHandler
import me.steven.indrev.items.armor.JetpackHandler
import me.steven.indrev.packets.common.ToggleGamerAxePacket
import me.steven.indrev.packets.common.ToggleJetpackPacket
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.tools.modular.IRModularItem
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot

object IRClientTickEvents : ClientTickEvents.EndTick {
    override fun onEndTick(client: MinecraftClient) {
        while (IndustrialRevolutionClient.MODULAR_CONTROLLER_KEYBINDING.wasPressed()) {
            val playerInventory = MinecraftClient.getInstance().player?.inventory ?: break
            val hasModularItem = (0 until playerInventory.size())
                .associateWith { slot -> playerInventory.getStack(slot) }
                .filter { (_, stack) -> stack.item is IRModularItem<*> }
                .isNotEmpty()
            if (hasModularItem)
                MinecraftClient.getInstance()
                    .setScreen(IRModularControllerScreen(ModularItemConfigurationScreenHandler(client.player!!.inventory)))
        }

        while (IndustrialRevolutionClient.GAMER_AXE_TOGGLE_KEYBINDING.wasPressed()) {
            val itemStack = client.player?.mainHandStack ?: break
            if (itemStack.isOf(IRItemRegistry.GAMER_AXE_ITEM)) {
                ClientPlayNetworking.send(ToggleGamerAxePacket.PACKET_ID, PacketByteBufs.empty())
            }
        }

        while (IndustrialRevolutionClient.JETPACK_TOGGLE_KEYBINDING.wasPressed()) {
            val itemStack = client.player?.getEquippedStack(EquipmentSlot.CHEST) ?: break
            if (itemStack.item is JetpackHandler) {
                ClientPlayNetworking.send(ToggleJetpackPacket.PACKET_ID, PacketByteBufs.empty())
            }
        }
    }
}