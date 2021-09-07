package me.steven.indrev.blockentities

import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.components.ComponentProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.math.BlockPos

//i didn't like having all of that in the MachineBlockEntity class so yea i made this, shut
abstract class BaseMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState)
    : SyncableBlockEntity(type, pos, state), InventoryProvider, EnergyIo, Configurable, ComponentProvider