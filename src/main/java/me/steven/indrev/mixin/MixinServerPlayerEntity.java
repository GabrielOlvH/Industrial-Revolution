package me.steven.indrev.mixin;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.steven.indrev.IndustrialRevolution;
import me.steven.indrev.api.IRServerPlayerEntityExtension;
import me.steven.indrev.armor.IRArmorMaterial;
import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.items.energy.IRGamerAxeItem;
import me.steven.indrev.items.energy.IRPortableChargerItem;
import me.steven.indrev.tools.modular.ArmorModule;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
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

import java.util.List;
import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements IRServerPlayerEntityExtension {
    @Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);

    private int ticks = 0;
    private int lastDamageTick = 0;
    private double lastShield = 0.0;
    private final Object2IntMap<ArmorModule> oldAppliedModules = new Object2IntOpenHashMap<>();

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void indrev_applyEffects(CallbackInfo ci) {
        ticks++;

        if (ticks % 15 == 0) {
            setShieldDurability(Math.min(getShieldDurability(), getMaxShieldDurability()));
            applyArmorEffects();
            useActiveAxeEnergy();
        }
    }

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float indrev_absorbDamage(float amount, DamageSource source) {
        if (isInvulnerableTo(source)) return amount;
        lastDamageTick = ticks;
        if (shouldApplyToShield(source))
            return (float) applyDamageToShield(amount);
        else
            return amount;
    }

    private boolean shouldApplyToShield(DamageSource source) {
        return (source.equals(DamageSource.FALL) && isApplied(ArmorModule.FEATHER_FALLING))
                || (source.isFire() && isApplied(ArmorModule.FIRE_RESISTANCE))
                && !source.equals(DamageSource.STARVE) && !source.equals(DamageSource.DROWN);
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
        oldAppliedModules.clear();
        oldAppliedModules.putAll(getAppliedModules());
        getAppliedModules().clear();
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
                        case PIGLIN_TRICKER:
                        case FEATHER_FALLING:
                            if (Energy.of(itemStack).use(20.0))
                                applyModule(module, level);
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
                            if (ticks - 120 > lastDamageTick && getShieldDurability() < getMaxShieldDurability() && Energy.of(itemStack).use(30.0)) {
                                regenerateShield();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void regenerateShield() {
        setShieldDurability(Math.min(getShieldDurability() + 0.5, getMaxShieldDurability()));
    }

    @Override
    public double applyDamageToShield(double damage) {
        double absorbed = Math.min(damage, getShieldDurability());
        setShieldDurability(getShieldDurability() - absorbed);
        return damage - absorbed;
    }

    @Override
    public boolean shouldSync() {
        return !oldAppliedModules.equals(getAppliedModules()) || lastShield != getShieldDurability();
    }

    @Override
    public void sync() {
        lastShield = getShieldDurability();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Map<ArmorModule, Integer> appliedModules = getAppliedModules();
        buf.writeInt(appliedModules.size());
        appliedModules.forEach((module, level) -> {
            buf.writeInt(module.ordinal());
            buf.writeInt(level);
        });
        buf.writeDouble(getShieldDurability());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, IndustrialRevolution.INSTANCE.getSYNC_MODULE_PACKET(), buf);
    }
}