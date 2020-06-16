package me.steven.indrev.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.reborn.energy.EnergyHandler;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;

@Mixin(EnergyHandler.class)
public interface AccessorEnergyHandler {
    @Accessor
    EnergyStorage getHolder();

    @Accessor
    EnergySide getSide();
}
