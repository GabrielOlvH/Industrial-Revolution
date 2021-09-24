package me.steven.indrev;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;


/**
 * Copied from SimpleSidedEnergyContainer and modified to fit IR's needs
 */
public abstract class IREnergyStorage extends SnapshotParticipant<Long> {
    private final SideStorage[] sideStorages = new SideStorage[7];

    public IREnergyStorage() {
        for (int i = 0; i < 7; ++i) {
            sideStorages[i] = new SideStorage(i == 6 ? null : Direction.byId(i));
        }
    }

    /**
     * @return The current capacity of this storage.
     */
    public abstract long getCapacity();

    /**
     * @return The maximum amount of energy that can be inserted in a single operation from the passed side.
     */
    public abstract long getMaxInsert(@Nullable Direction side);

    /**
     * @return The maximum amount of energy that can be extracted in a single operation from the passed side.
     */
    public abstract long getMaxExtract(@Nullable Direction side);

    /**
     * @return An {@link EnergyStorage} implementation for the passed side.
     */
    public EnergyStorage getSideStorage(@Nullable Direction side) {
        return sideStorages[side == null ? 6 : side.getId()];
    }

    public long getAmount() {
        return 0;
    }

    public void setAmount(long v) {

    }

    @Override
    protected Long createSnapshot() {
        return getAmount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        setAmount(snapshot);
    }

    private class SideStorage implements EnergyStorage {
        private final Direction side;

        private SideStorage(Direction side) {
            this.side = side;
        }

        @Override
        public boolean supportsInsertion() {
            return getMaxInsert(side) > 0;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            long inserted = Math.min(getMaxInsert(side), Math.min(maxAmount, getCapacity() - getAmount()));

            if (inserted > 0) {
                updateSnapshots(transaction);

                setAmount(getAmount() + inserted);
                return inserted;
            }

            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return getMaxExtract(side) > 0;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);

            long extracted = Math.min(getMaxExtract(side), Math.min(maxAmount, getAmount()));

            if (extracted > 0) {
                updateSnapshots(transaction);
                setAmount(getAmount() - extracted);
                return extracted;
            }

            return 0;
        }

        @Override
        public long getAmount() {
            return IREnergyStorage.this.getAmount();
        }

        @Override
        public long getCapacity() {
            return IREnergyStorage.this.getCapacity();
        }
    }
}

