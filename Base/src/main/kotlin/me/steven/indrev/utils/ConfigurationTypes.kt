package me.steven.indrev.utils

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.blocks.ELECTRIC_FURNACE
import net.minecraft.item.ItemConvertible
import net.minecraft.item.Items
import net.minecraft.text.Text

enum class ConfigurationTypes(val icon: ItemConvertible, val text: Text, val upgradeText: Text, val enabled: (MachineBlockEntity<*>) -> Boolean, val provider: (MachineBlockEntity<*>) -> SidedConfiguration, val canModify: (MachineBlockEntity<*>) -> Boolean) {
    ITEM(
        Items.CHEST,
        Text.literal("Item I/O Configuration"),
        Text.literal("Requires Automated Item Transfer Upgrade").styled { s -> s.withColor(0xFF0000) },
        { it.inventory.exists() },
        { it.inventory.sidedConfiguration },
        { it.upgrades.contains(Upgrade.AUTOMATED_ITEM_TRANSFER) }
    ),
    FLUID(
        Items.BUCKET,
        Text.literal("Fluid I/O Configuration"),
        Text.literal("Requires Automated Fluid Transfer Upgrade").styled { s -> s.withColor(0xFF0000) },
        { it.fluidInventory.exists() },
        { it.fluidInventory.sidedConfiguration },
        { it.upgrades.contains(Upgrade.AUTOMATED_FLUID_TRANSFER) }
    ),
    ENERGY(
        ELECTRIC_FURNACE.block.asItem(),
        Text.literal("Energy I/O Configuration"),
        Text.empty(),
        { it is LazuliFluxContainerBlockEntity },
        { (it as LazuliFluxContainerBlockEntity).sideConfig },
        { true }
    )
}