package me.steven.indrev.transportation.client

import me.steven.indrev.transportation.MOD_ID
import me.steven.indrev.transportation.client.models.PipeModel
import me.steven.indrev.transportation.client.models.StoragePipeModel
import me.steven.indrev.transportation.utils.blockSpriteId
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

object PipeModelProvider : ModelVariantProvider {

    private val itemPipeModel by lazy {
        StoragePipeModel(
            mutableListOf(identifier("block/item_pipe_center"), identifier("block/item_pipe_side")),
            mutableListOf(blockSpriteId("block/item_pipe_mk1"))
        )
    }

    private val cableModel by lazy {
        PipeModel(
            mutableListOf(identifier("block/cable_center"), identifier("block/cable_side")),
            mutableListOf(blockSpriteId("block/energy_cable_mk1"))
        )
    }

    override fun loadModelVariant(modelId: ModelIdentifier, context: ModelProviderContext): UnbakedModel? {
        if (modelId.namespace != MOD_ID) return null
        return if (modelId.path == "item_pipe") itemPipeModel else if (modelId.path == "cable") cableModel else null
    }
}