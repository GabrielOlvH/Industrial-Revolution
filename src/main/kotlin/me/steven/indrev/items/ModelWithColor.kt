package me.steven.indrev.items

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

class ModelWithColor(id: Identifier, val color: Int) {
    val itemBakedModel: BakedModel by lazy { MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(id, "inventory")) }
    val blockBakedModel: BakedModel by lazy { MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(id, "")) }
}