package me.steven.indrev.mixin;

import draylar.magna.api.BlockBreaker;
import draylar.magna.api.BlockProcessor;
import draylar.magna.api.BreakValidator;
import me.steven.indrev.registry.IRRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = BlockBreaker.class, remap = false)
public abstract class MixinBlockBreaker {
    @Shadow
    private static void dropItems(World world, List<ItemStack> stacks, BlockPos pos) { }

    @Redirect(method = "breakInRadius", at = @At(value = "INVOKE", target = "Ldraylar/magna/api/BlockBreaker;dropItems(Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/util/math/BlockPos;)V"))
    private static void indrev_sendItemsToInventory(World world, List<ItemStack> stacks, BlockPos pos, World world1, PlayerEntity player, int radius, BreakValidator breakValidator, BlockProcessor smelter, boolean damageTool) {
        if (player.getStackInHand(Hand.MAIN_HAND).getItem() == IRRegistry.INSTANCE.getMINING_DRILL_MK4()) {
            stacks.forEach(player.inventory::insertStack);
        }
        dropItems(world, stacks, pos);
    }
}
