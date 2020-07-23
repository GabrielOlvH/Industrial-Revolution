package me.steven.indrev.mixin;

import com.mojang.authlib.GameProfile;
import me.steven.indrev.armor.IRArmorMaterial;
import me.steven.indrev.armor.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.Energy;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    private int ticks = 0;
    private final Set<Module> appliedEffects = new HashSet<>();

    public MixinServerPlayerEntity(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyArmorEffects(CallbackInfo ci) {
        ticks++;
        if (ticks % 100 == 0) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            Set<Module> effectsToRemove = new HashSet<>(appliedEffects);
            appliedEffects.clear();
            PlayerInventory inventory = player.inventory;
            for (ItemStack itemStack : inventory.armor) {
                if (!Energy.valid(itemStack)) continue;
                ArmorItem item = (ArmorItem) itemStack.getItem();
                if (item.getMaterial() == IRArmorMaterial.MODULAR) {
                    Module[] modules = Module.Companion.getInstalled(itemStack);
                    for (Module module : modules) {
                        HungerManager hunger = player.getHungerManager();
                        if (module == Module.AUTO_FEEDER && hunger.isNotFull()) {
                            for (int slot = 0; slot <= inventory.size(); slot++) {
                                ItemStack stack = inventory.getStack(slot);
                                FoodComponent food = stack.getItem().getFoodComponent();
                                if (food != null && food.getHunger() <= 20 - hunger.getFoodLevel() && Energy.of(itemStack).use(5.0))
                                    player.eatFood(world, stack);
                                if (!hungerManager.isNotFull()) break;
                            }
                        } else {
                            int level = Module.Companion.getLevel(itemStack, module);
                            StatusEffectInstance effect = module.getApply().invoke(player, level);
                            if (effect != null && Energy.of(itemStack).use(2.5)) {
                                if (!player.hasStatusEffect(effect.getEffectType()))
                                    player.addStatusEffect(effect);
                                appliedEffects.add(module);
                                effectsToRemove.remove(module);
                            }
                        }
                    }
                }
            }
            for (Module module : effectsToRemove) {
                StatusEffectInstance effect = module.getApply().invoke(player, 1);
                player.removeStatusEffect(effect.getEffectType());
            }
        }
    }
}
