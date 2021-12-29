package me.steven.indrev.compat.dashloader.models

import io.activej.serializer.annotations.Deserialize
import io.activej.serializer.annotations.Serialize
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.models.pipes.ItemPipeModel
import net.minecraft.client.render.model.BakedModel
import net.oskarstrom.dashloader.DashRegistry
import net.oskarstrom.dashloader.api.annotation.DashObject
import net.oskarstrom.dashloader.model.DashModel


@DashObject(ItemPipeModel::class) class DashItemPipeModel: DashModel {

    var tier: Int @Serialize(order = 0) get
    var models: IntArray @Serialize(order = 1) get
    var sprites: IntArray @Serialize(order = 2) get
    var retrieverServos: IntArray @Serialize(order = 3) get
    var outputServos: IntArray @Serialize(order = 4) get

    constructor(model: ItemPipeModel, registry: DashRegistry) {
        this.tier = model.tier.ordinal
        this.models = model.modelArray.map { m -> registry.createModelPointer(m) }.toIntArray()
        this.sprites = model.spriteArray.map { s -> registry.createSpritePointer(s) }.toIntArray()
        this.retrieverServos = model.retrieverServoModels.map { m -> registry.createModelPointer(m) }.toIntArray()
        this.outputServos = model.outputServoModels.map { m -> registry.createModelPointer(m) }.toIntArray()
    }

    constructor(
        @Deserialize("tier") tier: Int,
        @Deserialize("models") models: IntArray,
        @Deserialize("sprites") sprites: IntArray,
        @Deserialize("retrieverServos") retrieverServos: IntArray,
        @Deserialize("outputServos") outputServos: IntArray
    ) {
        this.tier = tier
        this.models = models
        this.sprites = sprites
        this.retrieverServos = retrieverServos
        this.outputServos = outputServos
    }

    override fun toUndash(registry: DashRegistry): BakedModel {
        val model = ItemPipeModel(Tier.ALL_VALUES[tier])
        this.models.forEachIndexed { index, pointer -> model.modelArray[index] = registry.getModel(pointer) }
        this.sprites.forEachIndexed { index, pointer -> model.spriteArray[index] = registry.getSprite(pointer) }
        this.retrieverServos.forEachIndexed { index, pointer -> model.retrieverServoModels[index] = registry.getModel(pointer) }
        this.outputServos.forEachIndexed { index, pointer -> model.outputServoModels[index] = registry.getModel(pointer) }
        model.transform = model.modelArray[0]!!.transformation
        model.buildMeshes()
        return model
    }

    override fun getStage(): Int = 3
}