package me.steven.indrev.blocks.generators

import me.steven.indrev.blocks.ElectricBlock
import me.steven.indrev.identifier
import net.minecraft.util.Identifier

class GeneratorBlock(settings: Settings, screenId: Identifier,  maxBuffer: Double, blockEntityProvider: () -> GeneratorBlockEntity) :
        ElectricBlock(settings, screenId, maxBuffer, { blockEntity -> blockEntity is GeneratorBlockEntity }, blockEntityProvider) {

    companion object {
        val COAL_GENERATOR_SCREEN_ID: Identifier = identifier("coal_generator_screen")
    }
}