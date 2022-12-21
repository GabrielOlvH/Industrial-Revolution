package me.steven.indrev.events.client

import me.steven.indrev.blocks.MACHINES
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

object MachineModelLoader : ModelVariantProvider {

    private val unbakedModelsCache = mutableMapOf<Identifier, UnbakedModel?>()

    override fun loadModelVariant(modelId: ModelIdentifier, context: ModelProviderContext): UnbakedModel? {
        val id = Identifier(modelId.namespace,  modelId.path.replace(Regex("_(mk.*?|creative)$"), ""))
        return unbakedModelsCache.computeIfAbsent(id) { MACHINES[id]?.unbakedModelProvider?.invoke() }
    }
}