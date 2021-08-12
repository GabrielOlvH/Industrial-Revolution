package me.steven.indrev.mixin.common;

import draylar.magna.api.BlockBreaker;
import draylar.magna.api.MagnaTool;
import me.steven.indrev.items.energy.IRModularDrillItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BlockBreaker.class, remap = false)
public class MixinBlockBreaker {
    @Inject(method = "findPositions(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;II)Ljava/util/List;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void indrev_ohno(World world, PlayerEntity playerEntity, int radius, int depth, CallbackInfoReturnable<List<BlockPos>> cir, ArrayList<BlockPos> potentialBrokenBlocks, Vec3d cameraPos, Vec3d rotation, double dist, Vec3d combined, BlockHitResult hitResult) {
        List<BlockPos> blocks = cir.getReturnValue();
        ItemStack handStack = playerEntity.getStackInHand(Hand.MAIN_HAND);
        Item item = handStack.getItem();
        if (item instanceof MagnaTool tool) {
            BlockPos centerPosition = tool.getCenterPosition(world, playerEntity, hitResult, handStack);
            IRModularDrillItem.Companion.filterBlacklistedBlocks(centerPosition, hitResult, playerEntity, handStack, blocks);
        }
    }
}