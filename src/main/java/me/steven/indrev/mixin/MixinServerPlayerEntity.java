package me.steven.indrev.mixin;

import com.mojang.authlib.GameProfile;
import me.steven.indrev.armor.IRArmorMaterial;
import me.steven.indrev.armor.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.Energy;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    private int ticks = 0;

    public MixinServerPlayerEntity(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyArmorEffects(CallbackInfo ci) {
        ticks++;
        if (ticks % 180 == 0) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            PlayerInventory inventory = player.inventory;
            inventory.armor.forEach(itemStack -> {
                if (!Energy.valid(itemStack)) return;
                ArmorItem item = (ArmorItem) itemStack.getItem();
                if (item.getMaterial() == IRArmorMaterial.MODULAR) {
                    Module[] upgrades = Module.Companion.getInstalled(itemStack);
                    for (Module module : upgrades) {
                        int level = Module.Companion.getLevel(itemStack, module);
                        if (module.getApply().invoke(player, itemStack, level)) {
                            Energy.of(itemStack).use(1.0);
                        }
                    }
                }
            });
        }
    }
}
