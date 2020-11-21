package me.steven.indrev.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnimalEntity.class)
public interface AccessorAnimalEntity {
    @Invoker
    void callEat(PlayerEntity player, ItemStack stack);
}
