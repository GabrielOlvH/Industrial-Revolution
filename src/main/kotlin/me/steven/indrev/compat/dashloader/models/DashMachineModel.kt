package me.steven.indrev.compat.dashloader.models

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import me.steven.indrev.blocks.models.MachineBakedModel
import me.steven.indrev.utils.blockSpriteId
import net.minecraft.client.render.model.BakedModel
import net.quantumfusion.dashloader.DashRegistry
import net.quantumfusion.dashloader.model.DashModel

class DashMachineModel : DashModel {


    var id: String @Serialize(order = 0) get
    var defaultSprite: Int @Serialize(order = 1) get
    var overlays: IntArray @Serialize(order = 2) get
    var workingOverlays: IntArray @Serialize(order = 3) get

    constructor(model: MachineBakedModel, registry: DashRegistry) {
        this.id = model.id
        this.defaultSprite = registry.createSpritePointer(model.baseSprite)
        this.overlays = model.overlays.map { registry.createSpritePointer(it) }.toIntArray()
        this.workingOverlays = model.workingOverlays.map { registry.createSpritePointer(it) }.toIntArray()
    }

    constructor(
        @Deserialize("id") id: String,
        @Deserialize("defaultSprite") defaultSprite: Int,
        @Deserialize("overlays") overlays: IntArray,
        @Deserialize("workingOverlays") workingOverlays: IntArray
    ) {
        this.id = id
        this.defaultSprite = defaultSprite
        this.overlays = overlays
        this.workingOverlays = workingOverlays
    }


    override fun toUndash(registry: DashRegistry): BakedModel {
        val model = MachineBakedModel(id)
        model.baseSprite = registry.getSprite(defaultSprite)
        overlays.indices.forEach { _ -> model.overlayIds.add(blockSpriteId("")) }
        workingOverlays.indices.forEach { _ -> model.workingOverlayIds.add(blockSpriteId("")) }
        model.overlays.indices.forEach { index ->
            val sprite = registry.getSprite(overlays[index])
            model.overlays[index] = sprite
            if (model.isEmissive(sprite)) model.emissives.add(sprite)
        }
        model.workingOverlays.indices.forEach { index ->
            val sprite = registry.getSprite(workingOverlays[index])
            model.workingOverlays[index] = sprite
            if (model.isEmissive(sprite)) model.emissives.add(sprite)
        }


        model.buildDefaultMesh()
        model.buildWorkingStateMesh()
        return model
    }

    override fun getStage(): Int = 0
}