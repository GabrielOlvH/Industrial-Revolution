package me.steven.indrev.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.steven.indrev.extensions.ServerWorldExtension;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import team.reborn.energy.api.EnergyStorage;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldExtension {

    private final Long2ObjectOpenHashMap<BlockApiCache<EnergyStorage, Direction>> indrev_energyIoCache = new Long2ObjectOpenHashMap<>();

    @NotNull
    @Override
    public Long2ObjectOpenHashMap<BlockApiCache<EnergyStorage, Direction>> indrev_getEnergyIoCache() {
        return indrev_energyIoCache;
    }
}
