package me.steven.indrev.datagen

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.registry.WorldGeneration.configuredFeatures
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.data.report.DynamicRegistriesProvider
import net.minecraft.registry.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.world.gen.feature.*
import java.util.concurrent.CompletableFuture

class IndustrialRevolutionDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(datagen: FabricDataGenerator) {
        datagen.createPack().addProvider(IndustrialRevolutionDatagen::IRBlockTagProvider)
        datagen.createPack().addProvider { a, b -> object: FabricDynamicRegistryProvider(a,b) {
            override fun getName(): String {
                return "sf"
            }

            override fun configure(registries: RegistryWrapper.WrapperLookup?, entries: Entries) {
                for (feature in configuredFeatures) {
                    entries.add(feature.configuredFeatureKey, feature.configuredFeature)
                    entries.add(feature.placedFeatureKey, PlacedFeature(RegistryEntry.of(feature.configuredFeature), feature.modifiers))
                }


            }

        } }

    }

    override fun buildRegistry(registryBuilder: RegistryBuilder?) {
        registryBuilder?.addRegistry(RegistryKeys.CONFIGURED_FEATURE) { registerable ->
            for (feature in configuredFeatures) {
                registerable.register(
                    feature.configuredFeatureKey,
                    feature.configuredFeature
                )
            }
        }

        registryBuilder?.addRegistry(RegistryKeys.PLACED_FEATURE) { registerable ->
            for (feature in configuredFeatures) {
                registerable.register(
                    feature.placedFeatureKey,
                    PlacedFeature(RegistryEntry.of(feature.configuredFeature), feature.modifiers)
                )
            }
        }



    }

    class IRBlockTagProvider(datagen: FabricDataOutput, future: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricTagProvider.BlockTagProvider(datagen, future) {
        override fun configure(arg: RegistryWrapper.WrapperLookup) {
            Registries.BLOCK.ids.forEach { id ->
                if (id.namespace == IndustrialRevolution.MOD_ID) {
                  /*  val block = Registries.BLOCK.get(id)
                    if (block.defaultState.material == Material.METAL || block.defaultState.material == Material.STONE || block.defaultState.material == Material.GLASS) {
                        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(block)
                    }
                    if (block.defaultState.material == Material.WOOD) {
                        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(block)
                    }*/
                }
            }
        }
    }
}