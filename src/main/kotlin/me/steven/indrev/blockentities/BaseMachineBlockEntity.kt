package me.steven.indrev.blockentities

import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.components.ComponentProvider
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import team.reborn.energy.api.EnergyStorage

//i didn't like having all of that in the MachineBlockEntity class so yea i made this, shut
abstract class BaseMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState)
    : SyncableBlockEntity(type, pos, state), InventoryProvider, Configurable, ComponentProvider