package me.steven.indrev.compat.dashloader.models

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import me.steven.indrev.blocks.models.LazuliFluxContainerBakedModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel


@DashObject(LazuliFluxContainerBakedModel::class) class DashLazuliFluxContainerModel : DashModel {
    var id: String @Serialize(order = 0) get
    var defaultSprite: Int @Serialize(order = 1) get
    var overlays: IntArray @Serialize(order = 2) get

    constructor(model: LazuliFluxContainerBakedModel, registry: DashRegistry) {
        this.id = model.id
        this.defaultSprite = registry.createSpritePointer(model.baseSprite)
        this.overlays = model.overlays.map { registry.createSpritePointer(it) }.toIntArray()
    }

    constructor(
        @Deserialize("id") id: String,
        @Deserialize("defaultSprite") defaultSprite: Int,
        @Deserialize("overlays") overlays: IntArray
    ) {
        this.id = id
        this.defaultSprite = defaultSprite
        this.overlays = overlays
    }


    override fun toUndash(registry: DashRegistry): BakedModel {
        val model = LazuliFluxContainerBakedModel(id)
        model.baseSprite = registry.getSprite(defaultSprite)
        model.overlays.indices.forEach { index ->
            val sprite = registry.getSprite(overlays[index])
            model.overlays[index] = sprite
            if (model.isEmissive(sprite)) model.emissives.add(sprite)
        }

        model.buildDefaultMesh()
        return model
    }

    override fun getStage(): Int = 0
}