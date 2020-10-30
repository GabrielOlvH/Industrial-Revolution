package me.steven.indrev.mixin;

import com.mojang.authlib.GameProfile;
import me.steven.indrev.armor.IRArmorMaterial;
import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.items.energy.IRGamerAxeItem;
import me.steven.indrev.items.energy.IRPortableChargerItem;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.Energy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    @Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);

    private int ticks = 0;
    private int lastDamageTick = 0;
    private final Set<ArmorModule> appliedEffects = new HashSet<>();

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void indrev_applyEffects(CallbackInfo ci) {
        ticks++;

        if (ticks % 40 == 0) {
            applyArmorEffects();
            useActiveAxeEnergy();
        }
    }

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float indrev_absorbDamage(float amount, DamageSource source) {
        if (isInvulnerableTo(source)) return amount;
        lastDamageTick = ticks;
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.inventory;
        float damageAbsorbed = 0;
        for (ItemStack itemStack : inventory.armor) {
            Item item = itemStack.getItem();
            if (!(item instanceof IRModularArmor)) continue;
            int level = ArmorModule.PROTECTION.getLevel(itemStack);
            double absorb = amount * (0.25 * (level / 3f));
            if (level > 0
                    && ((IRModularArmor) item).getShield(itemStack) > absorb
                    && canUseShield(itemStack, source)
            ) {
                if (source.equals(DamageSource.FALL))
                    damageAbsorbed += ((IRModularArmor) item).useShield(itemStack, amount);
                else if (source.isFire())
                    damageAbsorbed += (((IRModularArmor) item).useShield(itemStack, amount * 0.1)) / 0.1;
                else
                    damageAbsorbed += ((IRModularArmor) item).useShield(itemStack, absorb);
            }
        }
        return Math.max(amount - damageAbsorbed, 0);
    }

    private boolean canUseShield(ItemStack itemStack, DamageSource source) {
        if (source.equals(DamageSource.FALL)) return ArmorModule.FEATHER_FALLING.isInstalled(itemStack);
        else if (source.isFire()) return ArmorModule.FIRE_RESISTANCE.isInstalled(itemStack);
        else return !source.bypassesArmor();
    }

    private void useActiveAxeEnergy() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.inventory;
        for (ItemStack itemStack : inventory.main) {
            if (itemStack.getItem() instanceof IRGamerAxeItem) {
                CompoundTag tag = itemStack.getOrCreateTag();
                if (tag.contains("Active") && tag.getBoolean("Active") && !Energy.of(itemStack).use(5.0)) {
                    tag.putBoolean("Active", false);
                }
            }
        }
    }

    private void applyArmorEffects() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.inventory;
        Set<ArmorModule> effectsToRemove = new HashSet<>(appliedEffects);
        appliedEffects.clear();
        for (ItemStack itemStack : inventory.armor) {
            if (itemStack.getItem() instanceof IRModularArmor && ((ArmorItem) itemStack.getItem()).getMaterial() == IRArmorMaterial.MODULAR) {
                List<ArmorModule> modules = ((IRModularArmor) itemStack.getItem()).getInstalled(itemStack);
                for (ArmorModule module : modules) {
                    int level = module.getLevel(itemStack);
                    if (level <= 0) continue;
                    switch (module) {
                        case SPEED:
                        case BREATHING:
                        case JUMP_BOOST:
                        case NIGHT_VISION:
                        case FIRE_RESISTANCE:
                            Energy.of(itemStack).use(20.0);
                            break;
                        case AUTO_FEEDER:
                            HungerManager hunger = player.getHungerManager();
                            if (hunger.isNotFull()) {
                                for (int slot = 0; slot <= inventory.size(); slot++) {
                                    ItemStack stack = inventory.getStack(slot);
                                    FoodComponent food = stack.getItem().getFoodComponent();
                                    if (food != null && food.getHunger() <= 20 - hunger.getFoodLevel() && Energy.of(itemStack).use(30.0))
                                        player.eatFood(world, stack);
                                    if (!hungerManager.isNotFull()) break;
                                }
                            }
                            break;
                        case CHARGER:
                            IRPortableChargerItem.Companion.chargeItemsInInv(Energy.of(itemStack), player.inventory.main);
                            break;
                        case SOLAR_PANEL:
                            if (world.isDay() && world.isSkyVisible(player.getBlockPos().up())) {
                                for (ItemStack stackToCharge : inventory.armor) {
                                    if (Energy.valid(stackToCharge))
                                        Energy.of(stackToCharge).insert(75.0 * level);
                                }
                            }
                            break;
                        case PROTECTION:
                            if (ticks - 120 > lastDamageTick) {
                                ((IRModularArmor) itemStack.getItem()).regenShield(itemStack, level);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        for (ArmorModule module : effectsToRemove) {
            StatusEffectInstance effect = module.getApply().invoke(player, 1);
            if (effect != null)
                player.removeStatusEffect(effect.getEffectType());
        }
    }
}
