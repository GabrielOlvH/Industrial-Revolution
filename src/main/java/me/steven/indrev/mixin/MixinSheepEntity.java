package me.steven.indrev.mixin;

import me.steven.indrev.utils.FakePlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SheepEntity.class)
public abstract class MixinSheepEntity extends AnimalEntity {
    @Shadow public abstract void setSheared(boolean sheared);

    @Shadow @Final private static Map<DyeColor, ItemConvertible> DROPS;

    @Shadow public abstract DyeColor getColor();

    protected MixinSheepEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/SheepEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V"), method = "interactMob", cancellable = true)
    private void onShear(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof FakePlayerEntity) {
            world.playSoundFromEntity(null, this, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
            setSheared(true);
            int amount = 1 + random.nextInt(3);
            ItemStack stack = new ItemStack(DROPS.get(getColor()), amount);
            if (player.inventory.insertStack(stack))
                cir.setReturnValue(ActionResult.success(world.isClient));
        }
    }
}
