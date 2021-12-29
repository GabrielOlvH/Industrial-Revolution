package me.steven.indrev.mixin.client;

import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler;
import me.steven.indrev.packets.client.GuiPropertySyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "setScreen", at = @At("RETURN"))
    private void indrev_requestProperties(Screen screen, CallbackInfo ci) {
        if (screen instanceof ScreenHandlerProvider<?> provider) {
            ScreenHandler handler = provider.getScreenHandler();
            if (handler instanceof IRGuiScreenHandler) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(handler.syncId);
                ClientPlayNetworking.send(GuiPropertySyncPacket.INSTANCE.getC2S_REQUEST_PROPERTIES(), buf);
            }
        }
    }
}
