package me.steven.indrev.packets.common

import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.armor.JetpackHandler
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.literal
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.Formatting

object ToggleJetpackPacket {
    val PACKET_ID = identifier("toggle_jetpack")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID) { server, player, _, _, _ ->
            server.execute {
                val stack = player.getEquippedStack(EquipmentSlot.CHEST)
                val item = stack.item
                if (item is JetpackHandler) {
                    item.toggle(stack)
                    val active = item.isActive(stack)
                    player.sendMessage(literal(if (item is IRModularArmorItem) "Builtin Jetpack Module" else item.getName(stack).string).formatted(Formatting.WHITE)
                        .append(
                            if (!active) literal(" OFF").formatted(Formatting.BOLD, Formatting.RED)
                            else literal(" ON").formatted(Formatting.BOLD, Formatting.GREEN)
                        ), true)
                }
            }
        }
    }
}