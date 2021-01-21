package me.steven.indrev.mixin.common;

import com.mojang.authlib.GameProfile;
import dev.technici4n.fasttransferlib.api.ContainerItemContext;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.steven.indrev.IndustrialRevolution;
import me.steven.indrev.api.IRServerPlayerEntityExtension;
import me.steven.indrev.items.armor.IRModularArmorItem;
import me.steven.indrev.items.energy.IRPortableChargerItem;
import me.steven.indrev.tools.modular.ArmorModule;
import me.steven.indrev.utils.EnergyApiUtilsKt;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
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
            applyArmorEffects();
        }
        setShieldDurability(Math.min(getShieldDurability(), getMaxShieldDurability()));
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
                && (!source.equals(DamageSource.STARVE) && !source.equals(DamageSource.DROWN));
    }

    private void applyArmorEffects() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.inventory;
        getAppliedModules().clear();
        for (ItemStack itemStack : inventory.armor) {
            if (itemStack.getItem() instanceof IRModularArmorItem) {
                List<ArmorModule> modules = ((IRModularArmorItem) itemStack.getItem()).getInstalled(itemStack);
                EnergyIo itemIo = EnergyApi.ITEM.get(ItemKey.of(itemStack), ContainerItemContext.ofStack(itemStack));
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
                            if (itemIo != null && itemIo.extract(20.0, Simulation.ACT) == 20.0)
                                applyModule(module, level);
                            break;
                        case AUTO_FEEDER:
                            HungerManager hunger = player.getHungerManager();
                            if (hunger.isNotFull()) {
                                for (int slot = 0; slot <= inventory.size(); slot++) {
                                    ItemStack stack = inventory.getStack(slot);
                                    FoodComponent food = stack.getItem().getFoodComponent();
                                    if (food != null && food.getHunger() <= 20 - hunger.getFoodLevel() && itemIo != null && itemIo.extract(30.0, Simulation.ACT) == 30.0)
                                        player.eatFood(world, stack);
                                    if (!hungerManager.isNotFull()) break;
                                }
                            }
                            break;
                        case CHARGER:
                            if (itemIo != null)
                                IRPortableChargerItem.Companion.chargeItemsInInv(itemIo, player.inventory.main);
                            break;
                        case SOLAR_PANEL:
                            if (world.isDay() && world.isSkyVisible(player.getBlockPos().up(2))) {
                                for (ItemStack stackToCharge : inventory.armor) {
                                    EnergyIo toCharge = EnergyApiUtilsKt.energyOf(stackToCharge);
                                    if (toCharge != null)
                                        toCharge.insert(75.0 * level, Simulation.ACT);
                                }
                            }
                            break;
                        case PROTECTION:
                            if (ticks - 120 > lastDamageTick && getShieldDurability() < getMaxShieldDurability() && itemIo != null && itemIo.extract(30.0, Simulation.ACT) == 30.0) {
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

    private void regenerateShield() {
        setShieldDurability(Math.min(getShieldDurability() + 0.5, getMaxShieldDurability()));
    }

    private double applyDamageToShield(double damage) {
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
        oldAppliedModules.clear();
        oldAppliedModules.putAll(getAppliedModules());
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Map<ArmorModule, Integer> appliedModules = getAppliedModules();
        buf.writeInt(appliedModules.size());
        appliedModules.forEach((module, level) -> {
            buf.writeInt(module.ordinal());
            buf.writeInt(level);
        });
        buf.writeDouble(getShieldDurability());
        buf.writeBoolean(ticks - 120 > lastDamageTick);
        ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, IndustrialRevolution.INSTANCE.getSYNC_MODULE_PACKET(), buf);
    }

    @Override
    public boolean isRegenerating() {
        return ticks - 120 > lastDamageTick;
    }
}