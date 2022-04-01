package me.steven.indrev.datagen

import me.steven.indrev.IndustrialRevolution
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Material
import net.minecraft.tag.BlockTags
import net.minecraft.util.registry.Registry

class IndustrialRevolutionDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(datagen: FabricDataGenerator) {
        datagen.addProvider(IndustrialRevolutionDatagen::IRBlockTagProvider)
    }

    class IRBlockTagProvider(datagen: FabricDataGenerator) : FabricTagProvider.BlockTagProvider(datagen) {
        override fun generateTags() {
            Registry.BLOCK.ids.forEach { id ->
                if (id.namespace == IndustrialRevolution.MOD_ID) {
                    val block = Registry.BLOCK.get(id)
                    if (block.defaultState.material == Material.METAL || block.defaultState.material == Material.STONE) {
                        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(block)
                    }
                    if (block.defaultState.material == Material.WOOD) {
                        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(block)
                    }
                }
            }
        }

    }
}