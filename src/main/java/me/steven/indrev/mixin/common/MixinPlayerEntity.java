package me.steven.indrev.mixin.common;

import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.steven.indrev.api.IRPlayerEntityExtension;
import me.steven.indrev.items.energy.IREnergyItem;
import me.steven.indrev.items.energy.IRGamerAxeItem;
import me.steven.indrev.tools.modular.ArmorModule;
import me.steven.indrev.utils.EnergyutilsKt;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IRPlayerEntityExtension {

    private boolean indrev_regenerating = false;
    private double indrev_shield = 0.0;
    private final Object2IntMap<ArmorModule> appliedEffects = new Object2IntOpenHashMap<>();

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void indrev_checkEnergyTool(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = inventory.main.get(inventory.selectedSlot);
        Item item = itemStack.getItem();
        EnergyIo itemIo = EnergyutilsKt.energyOf(itemStack);
        if (itemIo != null && item instanceof IREnergyItem) {
            if (item instanceof IRGamerAxeItem) {
                NbtCompound tag = itemStack.getOrCreateTag();
                if (tag.contains("Active") && !tag.getBoolean("Active")) {
                    cir.setReturnValue(0.2F);
                    return;
                }
            }
            if (itemIo.getEnergy() < 1) cir.setReturnValue(0.2F);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void indrev_writeShieldToPlayerTag(NbtCompound tag, CallbackInfo ci) {
        tag.putDouble("indrev:shield", indrev_shield);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void indrev_readShieldToPlayerTag(NbtCompound tag, CallbackInfo ci) {
        indrev_shield = tag.getDouble("indrev:shield");
    }

    @Override
    public double getShieldDurability() {
        return indrev_shield;
    }

    @Override
    public void setShieldDurability(double shieldDurability) {
        this.indrev_shield = shieldDurability;
    }

    @Override
    public double getMaxShieldDurability() {
        Iterator<ItemStack> iterator = getArmorItems().iterator();
        double shield = 0.0;
        while (iterator.hasNext()) {
            ItemStack next = iterator.next();
            shield += ArmorModule.PROTECTION.getLevel(next) * 25;
        }
        return shield;
    }


    @Override
    public @NotNull Map<ArmorModule, Integer> getAppliedModules() {
        return appliedEffects;
    }

    @Override
    public boolean isApplied(@NotNull ArmorModule module) {
        return appliedEffects.containsKey(module);
    }

    @Override
    public void applyModule(@NotNull ArmorModule module, int level) {
        appliedEffects.put(module, level);
    }

    @Override
    public int getAppliedLevel(@NotNull ArmorModule module) {
        return appliedEffects.getOrDefault(module, 0);
    }

    @Override
    public void setRegenerating(boolean isRegenerating) {
        this.indrev_regenerating = isRegenerating;
    }

    @Override
    public boolean isRegenerating() {
        return indrev_regenerating;
    }
}
