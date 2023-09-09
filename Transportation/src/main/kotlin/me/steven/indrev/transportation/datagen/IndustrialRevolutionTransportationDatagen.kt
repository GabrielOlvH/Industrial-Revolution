package me.steven.indrev.transportation.datagen

import me.steven.indrev.transportation.MOD_ID
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.Registries

class IndustrialRevolutionTransportationDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider { output, _ -> DefaultLangGenerator(output) }
    }

    class DefaultLangGenerator(output: FabricDataOutput) : FabricLanguageProvider(output) {
        override fun generateTranslations(translationBuilder: TranslationBuilder) {
            Registries.BLOCK.entrySet.forEach { (key, item) ->
                if (key.value.namespace == MOD_ID) {
                    val name = key.value.path.split("_")
                        .joinToString(" ") { c -> c.substring(0, 1).uppercase() + c.substring(1) }
                    translationBuilder.add(item, name)
                }
            }
        }
    }
}