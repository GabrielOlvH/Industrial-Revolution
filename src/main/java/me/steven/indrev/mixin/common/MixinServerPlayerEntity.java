package me.steven.indrev.mixin.common;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.steven.indrev.api.IRServerPlayerEntityExtension;
import me.steven.indrev.items.armor.IRModularArmorItem;
import me.steven.indrev.items.energy.IRPortableChargerItem;
import me.steven.indrev.packets.client.SyncAppliedModulesPacket;
import me.steven.indrev.tools.modular.ArmorModule;
import me.steven.indrev.utils.AccessorextensionsKt;
import me.steven.indrev.utils.EnergyutilsKt;
import me.steven.indrev.utils.HelperextensionsKt;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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

    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    private int ticks = 0;
    private int lastDamageTick = 0;
    private float lastDmg = 0f;
    private double lastShield = 0.0;
    private final Object2IntMap<ArmorModule> oldAppliedModules = new Object2IntOpenHashMap<>();

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void indrev_applyEffects(CallbackInfo ci) {
        ticks++;

        if (ticks % 15 == 0) {
            applyArmorEffects();
        }
        indrev_tickMagnet();
        setShieldDurability(Math.min(getShieldDurability(), getMaxShieldDurability()));
    }

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float indrev_absorbDamage(float amount, DamageSource source) {
        final float initial = amount;
        if (isInvulnerableTo(source)) return amount;
        if (lastDamageTick + 10 > ticks) {
            if (amount <= lastDmg)
                return 0f;
            amount = amount - lastDmg;
        }
        lastDamageTick = ticks;
        lastDmg = initial;
        if (shouldApplyToShield(source)) {
            float leftover = (float) applyDamageToShield(amount);
            if (amount > leftover)
                world.playSoundFromEntity(null, this, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS, 1f, 0.0001f);
            return leftover;
        } else
            return amount;
    }

    @Inject(method = "worldChanged", at = @At("TAIL"))
    private void indrev_syncOnDimChange(ServerWorld origin, CallbackInfo ci) {
        indrev_sync();
        AccessorextensionsKt.getFluidNetworkState(origin).onDimChange(this);
        AccessorextensionsKt.getItemNetworkState(origin).onDimChange(this);
    }

    private boolean shouldApplyToShield(DamageSource source) {
        if (source.equals(DamageSource.FALL)) return isApplied(ArmorModule.FEATHER_FALLING);
        else if (source.isFire()) return isApplied(ArmorModule.FIRE_RESISTANCE);
        else return !source.equals(DamageSource.STARVE) && !source.equals(DamageSource.DROWN);
    }

    private void applyArmorEffects() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.getInventory();
        getAppliedModules().clear();

        for (int i = 0; i < inventory.armor.size(); i++) {
            int cSlot = 36+i;
            ItemStack itemStack = inventory.getStack(cSlot);
            if (itemStack.getItem() instanceof IRModularArmorItem) {
                List<ArmorModule> modules = ((IRModularArmorItem) itemStack.getItem()).getInstalled(itemStack);
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
                        case WATER_AFFINITY:
                            if (EnergyutilsKt.extract(inventory, cSlot, 20))
                                applyModule(module, level);
                            break;
                        case AUTO_FEEDER:
                            HungerManager hunger = player.getHungerManager();
                            if (hunger.isNotFull()) {
                                for (int slot = 0; slot <= inventory.size(); slot++) {
                                    ItemStack stack = inventory.getStack(slot);
                                    FoodComponent food = stack.getItem().getFoodComponent();
                                    if (food != null && !food.isAlwaysEdible() && !HelperextensionsKt.hasNegativeEffects(food) && food.getHunger() <= 20 - hunger.getFoodLevel() && EnergyutilsKt.extract(inventory, cSlot, 30)) {
                                        stack.finishUsing(world, player);
                                        player.eatFood(world, stack);
                                    }
                                    if (!hungerManager.isNotFull()) break;
                                }
                            }
                            break;
                        case CHARGER:
                                IRPortableChargerItem.Companion.chargeItemsInInv(cSlot, player.getInventory());
                            break;
                        case SOLAR_PANEL:
                            if (world.isDay() && world.isSkyVisible(player.getBlockPos().up(2))) {
                                for (int x = 0; x < 4; x++) {
                                    EnergyutilsKt.insert(inventory, cSlot - x, 75L * level);
                                }
                            }
                            break;
                        case PROTECTION:
                            if (ticks - 120 > lastDamageTick && getShieldDurability() < getMaxShieldDurability() && EnergyutilsKt.extract(inventory, cSlot, 30)) {
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

    private void indrev_tickMagnet() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerInventory inventory = player.getInventory();
        for (ItemStack itemStack : inventory.armor) {
            if (itemStack.getItem() instanceof IRModularArmorItem) {
                int level = ArmorModule.MAGNET.getLevel(itemStack);
                if (level > 0) {
                    Vec3i offset = new Vec3i(8, 8, 8);
                    Box area = new Box(getBlockPos().subtract(offset), getBlockPos().add(offset));
                    Vec3d blockCenter = HelperextensionsKt.toVec3d(getBlockPos()).add(0.5, 0.5, 0.5);
                    world.getOtherEntities(this, area, (entity) -> entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity).forEach(entity -> {
                        if ((entity instanceof ItemEntity itemEntity && !itemEntity.cannotPickup())
                                || (entity instanceof ExperienceOrbEntity xpEntity && xpEntity.age > 40)) {
                            Vec3d v = entity.getPos().relativize(blockCenter).normalize().multiply(0.2);
                            entity.addVelocity(v.x, v.y, v.z);
                            //applyModule(ArmorModule.MAGNET, 1);
                        }
                    });
                    return;
                }
            }
        }
        //getAppliedModules().remove(ArmorModule.MAGNET);
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
    public boolean indrev_shouldSync() {
        return !oldAppliedModules.equals(getAppliedModules()) || lastShield != getShieldDurability();
    }

    @Override
    public void indrev_sync() {
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
        ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, SyncAppliedModulesPacket.INSTANCE.getSYNC_MODULE_PACKET(), buf);
    }

    @Override
    public boolean isRegenerating() {
        return ticks - 120 > lastDamageTick;
    }
}