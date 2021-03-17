package me.steven.indrev.mixin.client;

import me.steven.indrev.blockentities.GlobalStateController;
import me.steven.indrev.utils.UtilsKt;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class MixinBuiltChunk {
    @Shadow public abstract BlockPos getOrigin();

    @Inject(method = "rebuild", at = @At("INVOKE"))
    private void indrev_removeBuiltChunk(CallbackInfo ci) {
        long chunkPos = ChunkPos.toLong(getOrigin().getX() >> 4, getOrigin().getZ() >> 4);
        UtilsKt.runClient(() -> GlobalStateController.INSTANCE.getChunksToUpdate().remove(chunkPos));
    }
}
