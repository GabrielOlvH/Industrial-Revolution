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
        Array(4) { i ->
            StoragePipeModel(
                mutableListOf(identifier("block/item_pipe_center_mk${i+1}"), identifier("block/item_pipe_side_mk${i+1}")),
                mutableListOf(blockSpriteId("block/item_pipe_mk${i + 1}"))
            )
        }
    }

    private val fluidPipeModel by lazy {
        Array(4) { i ->
            StoragePipeModel(
                mutableListOf(identifier("block/fluid_pipe_center_mk${i+1}"), identifier("block/fluid_pipe_side_mk${i+1}")),
                mutableListOf(blockSpriteId("block/fluid_pipe_mk${i + 1}"))
            )
        }
    }

    private val cableModel by lazy {
        Array(4) { i ->
            PipeModel(
                mutableListOf(identifier("block/cable_center_mk${i+1}"), identifier("block/cable_side_mk${i+1}")),
                mutableListOf(blockSpriteId("block/energy_cable_mk${i + 1}"))
            )
        }
    }

    override fun loadModelVariant(modelId: ModelIdentifier, context: ModelProviderContext): UnbakedModel? {
        return when {
            modelId.namespace != MOD_ID -> return null
            modelId.path.startsWith("item_pipe") -> itemPipeModel[modelId.path.last().digitToInt()-1]
            modelId.path.startsWith("cable") -> cableModel[modelId.path.last().digitToInt()-1]
            modelId.path.startsWith("fluid_pipe") -> fluidPipeModel[modelId.path.last().digitToInt()-1]
            else -> null
        }
    }
}