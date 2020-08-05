package me.steven.indrev.world.chunkveins

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

class VeinTypeResourceListener : SimpleSynchronousResourceReloadListener {
    override fun apply(manager: ResourceManager?) {
        val jankson = Jankson.builder().registerTypeFactory(JsonArray::class.java) { JsonArray() }.build()
        val ids = manager?.findResources("veintypes") { r -> r.endsWith(".json") || r.endsWith(".json5") }
        ids?.forEach { id ->
            val json = jankson.load(manager.getResource(id).inputStream)
            val veinType = VeinType.fromJson(json)
            if (veinType.isEmpty()) {
                LOGGER.error("Unable to load vein type $id!")
                return@forEach
            }
            veinType.forEach { VeinType.REGISTERED[it.id] = it }
        }
        LOGGER.info("Loaded ${VeinType.REGISTERED.size} vein types!")
    }

    override fun getFabricId(): Identifier = identifier("veintypes")

    companion object {
        val LOGGER = LogManager.getLogger("Vein Type Resource Reloader")
    }
}