package me.steven.indrev.items

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

class ModelWithColor(val id: Identifier, val color: Int, type: MaterialBakedModel.TransformationType) {
    val bakedModel: BakedModel by lazy { MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(id, type.variant)) }
}