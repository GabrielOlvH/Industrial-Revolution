package me.steven.indrev.world.chunkveins

import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import blue.endless.jankson.JsonPrimitive
import net.minecraft.block.Block
import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome
import java.lang.IndexOutOfBoundsException

data class VeinType(val id: Identifier, val outputs: WeightedList<Block>, val infiniteOutputs: WeightedList<Block>, val sizeRange: IntRange) {
    companion object {
        val REGISTERED = hashMapOf<Identifier, VeinType>()
        val BIOME_VEINS = hashMapOf<RegistryKey<Biome>, WeightedList<Identifier>>()
        fun fromJson(json: JsonObject): Array<VeinType>? {
            if (json.getBoolean("containsMultiple", false))
                return json.get(JsonArray::class.java, "veins")!!.flatMap { fromJson(it as JsonObject)!!.toList() }.toTypedArray()
            val id = Identifier(json.get(String::class.java, "id"))
            val array = json.get(JsonArray::class.java, "output")
            val optional = json.getBoolean("optional", false)
            val weightedList = WeightedList<Block>()
            val infiniteWeightedList = WeightedList<Block>()
            array?.forEach { element ->
                element as JsonObject
                val blockId = Identifier(element.get(String::class.java, "id"))
                val weight = element.getInt("weight", 1)
                val infiniteWeight = element.getInt("infiniteWeight", 1)
                if (!Registry.BLOCK.getOrEmpty(blockId).isPresent) {
                    if (optional) {
                        VeinTypeResourceListener.LOGGER.debug("Not loading optional vein type $id because $blockId is missing")
                        return emptyArray()
                    }
                    VeinTypeResourceListener.LOGGER.error("Expected block but received unknown string $blockId when loading vein type $id")
                }
                weightedList.add(Registry.BLOCK.get(blockId), weight)
                infiniteWeightedList.add(Registry.BLOCK.get(blockId), infiniteWeight)
            }
            val range = json.get(JsonArray::class.java, "sizeRange")
            if (range == null) {
                VeinTypeResourceListener.LOGGER.error("Missing range entry when loading vein type $id")
                return null
            }
            val min = range.map { (it as JsonPrimitive).asInt(1) }.minOrNull()!!
            val max = range.map { (it as JsonPrimitive).asInt(1) }.maxOrNull()!!
            val biomeArray = json.get(JsonArray::class.java, "biomes")
            val biomes = mutableMapOf<RegistryKey<Biome>, Pair<Int, Int>>()
            biomeArray?.forEach { element ->
                element as JsonObject
                val weight = element.getInt("weight", 1)
                val infiniteWeight = element.getInt("infiniteWeight", 1)
                if (element.containsKey("category")) {
                    element.get(JsonArray::class.java, "category")?.forEach { e ->
                        val cat = Biome.Category.valueOf((e as JsonPrimitive).asString().uppercase())
                        BuiltinRegistries.BIOME.ids
                            .filter {
                                BuiltinRegistries.BIOME[it]?.category == cat
                            }.forEach {
                                biomes[RegistryKey.of(Registry.BIOME_KEY, it)] = Pair(weight, infiniteWeight)
                            }
                    }
                } else {
                    element.get(JsonArray::class.java, "ids")?.forEach { s ->
                        val biomeId = Identifier(s.toString())
                        if (!BuiltinRegistries.BIOME.containsId(biomeId)) {
                            VeinTypeResourceListener.LOGGER.error("Expected biome but received unkonwn string $biomeId when loading vein type $id")
                        }
                        biomes[RegistryKey.of(Registry.BIOME_KEY, biomeId)] = Pair(weight, infiniteWeight)
                    }
                }
            }
            biomes.forEach { (biome, weights) -> BIOME_VEINS.computeIfAbsent(biome) { WeightedList() }.add(id, weights.first) }
            return arrayOf(VeinType(id, weightedList, infiniteWeightedList, min..max))
        }
    }
}