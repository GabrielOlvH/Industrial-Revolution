package me.steven.indrev.mixin.common;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
    @Redirect(
            method = "offer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
            )
    )
    private void indrev_redirectSendPacket(ServerPlayNetworkHandler serverPlayNetworkHandler, Packet<?> packet) {
        if (serverPlayNetworkHandler != null)
            serverPlayNetworkHandler.sendPacket(packet);
    }
}
