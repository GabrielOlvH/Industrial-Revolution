package me.steven.indrev.blocks.furnace

import me.steven.indrev.blocks.ElectricBlock
import me.steven.indrev.identifier
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Identifier

class ElectricCraftingBlock(
        settings: Settings, screenId: Identifier, maxBuffer: Double, test: (BlockEntity?) -> Boolean, blockEntityProvider: () -> ElectricCraftingBlockEntity<*>
) : ElectricBlock(settings, screenId, maxBuffer, test, blockEntityProvider) {

    companion object {
        val ELECTRIC_FURNACE_SCREEN_ID = identifier("electric_furnace_screen")
    }
}