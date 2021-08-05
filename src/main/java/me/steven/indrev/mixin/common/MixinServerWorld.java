package me.steven.indrev.mixin.common;

import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.steven.indrev.api.ServerWorldExtension;
import me.steven.indrev.networks.Network;
import me.steven.indrev.networks.NetworkState;
import me.steven.indrev.networks.ServoNetworkState;
import me.steven.indrev.networks.energy.EnergyNetwork;
import me.steven.indrev.networks.energy.EnergyNetworkState;
import me.steven.indrev.networks.fluid.FluidNetworkState;
import me.steven.indrev.networks.item.ItemNetworkState;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements ServerWorldExtension {

    private final Long2ObjectOpenHashMap<BlockApiCache<EnergyIo, Direction>> indrev_energyIoCache = new Long2ObjectOpenHashMap<>();

    private ItemNetworkState indrev_itemNetworkState = null;
    private FluidNetworkState indrev_fluidNetworkState = null;
    private EnergyNetworkState indrev_energyNetworkState = null;

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @NotNull
    @Override
    public Long2ObjectOpenHashMap<BlockApiCache<EnergyIo, Direction>> indrev_getEnergyCache() {
        return indrev_energyIoCache;
    }

    @NotNull
    @Override
    public EnergyNetworkState indrev_getEnergyNetworkState() {
        ServerWorld world = (ServerWorld) (Object) this;
        if (indrev_energyNetworkState == null) {
            indrev_energyNetworkState = new EnergyNetworkState(world);
        }
        return indrev_energyNetworkState;
    }

    @NotNull
    @Override
    public FluidNetworkState indrev_getFluidNetworkState() {
        ServerWorld world = (ServerWorld) (Object) this;
        if (indrev_fluidNetworkState == null) {
            indrev_fluidNetworkState = getPersistentStateManager()
                    .getOrCreate(
                            nbt -> ServoNetworkState.Companion.readNbt(nbt, () -> new FluidNetworkState(world)),
                            () -> new FluidNetworkState(world),
                            Network.Type.Companion.getFLUID().getKey());
        }
        return indrev_fluidNetworkState;
    }

    @NotNull
    @Override
    public ItemNetworkState indrev_getItemNetworkState() {
        ServerWorld world = (ServerWorld) (Object) this;
        if (indrev_itemNetworkState == null) {
            indrev_itemNetworkState = getPersistentStateManager()
                    .getOrCreate(
                            nbt -> ItemNetworkState.Companion.readNbt(nbt, () -> new ItemNetworkState(world)),
                            () -> new ItemNetworkState(world),
                            Network.Type.Companion.getITEM().getKey());
        }
        return indrev_itemNetworkState;
    }
}
