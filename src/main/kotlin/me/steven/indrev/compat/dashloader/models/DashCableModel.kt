package me.steven.indrev.compat.dashloader.models

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.models.pipes.CableModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel



@DashObject(CableModel::class) class DashCableModel : DashModel {

    var tier: Int @Serialize(order = 0) get
    var models: IntArray @Serialize(order = 1) get
    var sprites: IntArray @Serialize(order = 2) get

    constructor(model: CableModel, registry: DashRegistry) {
        this.tier = model.tier.ordinal
        this.models = model.modelArray.map { m -> registry.createModelPointer(m) }.toIntArray()
        this.sprites = model.spriteArray.map { s -> registry.createSpritePointer(s) }.toIntArray()
    }

    constructor(
        @Deserialize("tier") tier: Int,
        @Deserialize("models") models: IntArray,
        @Deserialize("sprites") sprites: IntArray
    ) {
        this.tier = tier
        this.models = models
        this.sprites = sprites
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val model = CableModel(Tier.ALL_VALUES[tier])
        this.models.forEachIndexed { index, pointer -> model.modelArray[index] = registry.getModel(pointer) }
        this.sprites.forEachIndexed { index, pointer -> model.spriteArray[index] = registry.getSprite(pointer) }
        model.transform = model.modelArray[0]!!.transformation
        model.buildMeshes()
        return model
    }

    override fun getStage(): Int = 3
}