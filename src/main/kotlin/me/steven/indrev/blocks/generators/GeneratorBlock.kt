package me.steven.indrev.blocks.generators

import me.steven.indrev.blocks.ElectricBlock
import me.steven.indrev.identifier
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Identifier

class GeneratorBlock(settings: Settings, screenId: Identifier,  maxBuffer: Double, test: (BlockEntity?) -> Boolean, blockEntityProvider: () -> GeneratorBlockEntity) :
        ElectricBlock(settings, screenId, maxBuffer, test, blockEntityProvider) {

    companion object {
        val COAL_GENERATOR_SCREEN_ID: Identifier = identifier("coal_generator_screen")
    }
}