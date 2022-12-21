package me.steven.indrev.blocks

import com.mojang.datafixers.util.Pair
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.blockSpriteId
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

open class MachineBakedModel(val id: String, val hasOnModel: Boolean) : UnbakedModel, BakedModel, FabricBakedModel {

    var idleSpriteId = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/$id"))
    var idleSprite: Sprite? = null

    var onSpriteId = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/${id}_on"))
    var onSprite: Sprite? = null

    var overlayIds: MutableList<SpriteIdentifier> = mutableListOf()
    val overlays: Array<Sprite?> by lazy { arrayOfNulls(overlayIds.size) }

    val onOverlays: Array<Sprite?> by lazy { arrayOfNulls(onOverlayIds.size) }
    var onOverlayIds: MutableList<SpriteIdentifier> = mutableListOf()

    val emissives = hashSetOf<Sprite>()

    var idleMesh: Mesh? = null

    val onQuads = mutableListOf<BakedQuad>()

    fun factoryOverlay() {
        overlayIds.add(blockSpriteId("block/factory_overlay"))
    }

    fun tierOverlay(tier: Tier) {
        overlayIds.add(blockSpriteId("block/${tier.asString}_overlay"))
    }

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        idleSprite = textureGetter.apply(idleSpriteId)
        onSprite = if (hasOnModel) textureGetter.apply(onSpriteId) else idleSprite

        overlayIds.processSprites(overlays, textureGetter)
        onOverlayIds.processSprites(onOverlays, textureGetter)

        if (isEmissive(onSprite)) emissives.add(onSprite!!)
        if (isEmissive(idleSprite)) emissives.add(idleSprite!!)

        buildIdleMesh()
        buildOnQuads()

        return this
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        val list = mutableListOf(idleSpriteId)
        list.add(onSpriteId)
        list.addAll(overlayIds)
        list.addAll(onOverlayIds)
        return list
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = true

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite? = idleSprite

    override fun getTransformation(): ModelTransformation =
        me.steven.indrev.blocks.MachineBakedModel.Companion.TRANSFORM

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        ctx.pushTransform(rotateQuads(state[me.steven.indrev.blocks.MachineBlock.Companion.FACING]))
        ctx.meshConsumer().accept(idleMesh)
        ctx.popTransform()
    }

    override fun emitItemQuads(stack: ItemStack, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        ctx.meshConsumer().accept(idleMesh)
        val item = stack.item as? me.steven.indrev.blocks.MachineBlockItem ?: return
        val mesh = me.steven.indrev.blocks.MachineBlockEntityRenderer.Companion.TIER_MESHES[item.tier] ?: return
        ctx.meshConsumer().accept(mesh)
    }

    open fun buildIdleMesh() {
        val renderer = RendererAccess.INSTANCE.renderer!!
        val builder = renderer.meshBuilder()
        val emitter = builder.emitter

        for (direction in Direction.values()) {
            emitter.drawSide(direction, idleSprite!!, -1)
            emitter.emit()
            overlays.forEach { overlay ->
                emitter.drawSide(direction, overlay!!, -1)
                emitter.emit()
            }
        }
        idleMesh = builder.build()
    }

    private fun buildOnQuads() {
        val renderer = RendererAccess.INSTANCE.renderer!!
        val builder = renderer.meshBuilder()
        val emitter = builder.emitter

        for (direction in Direction.values()) {
            emitter.drawSide(direction, onSprite!!, -1, -2e-4f)
            onQuads.add(emitter.toBakedQuad(0, onSprite, false))
            onOverlays.forEach { overlay ->
                emitter.drawSide(direction, overlay!!, -1, -2e-4f)
                onQuads.add(emitter.toBakedQuad(0, overlay, false))
            }
            overlays.forEach { overlay ->
                emitter.drawSide(direction, overlay!!, -1, -2e-4f)
                onQuads.add(emitter.toBakedQuad(0, overlay, false))
            }
        }
    }

    private fun List<SpriteIdentifier>.processSprites(arr: Array<Sprite?>, textureGetter: Function<SpriteIdentifier, Sprite>) {
        forEachIndexed { index, id ->
            val sprite = textureGetter.apply(id)
            if (sprite.id != MissingSprite.getMissingSpriteId()) {
                arr[index] = sprite
                if (isEmissive(sprite))
                    emissives.add(sprite)
            }
        }
    }

    private fun isEmissive(sprite: Sprite?) = sprite?.id?.toString()?.contains("emissive") == true

    protected fun QuadEmitter.drawSide(
        side: Direction,
        sprite: Sprite,
        color: Int = -1,
        depth: Float = 0f
    ) {
        square(side, 0f, 0f, 1f, 1f, depth)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, color, color, color, color)
        if (emissives.contains(sprite))
            material(me.steven.indrev.blocks.MachineBakedModel.Companion.MATERIAL)
        val uv =
            if (sprite.width == 16 && sprite.height == 16) me.steven.indrev.blocks.MachineBakedModel.MachineTextureUV.FULL
            else me.steven.indrev.blocks.MachineBakedModel.MachineTextureUV.Companion.BY_DIRECTION[side]!!
        sprite(0, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
        sprite(1, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(2, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(3, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
    }

    /**
     * Original code belongs to Haven-King.
     * Source: https://github.com/Haven-King/Automotion
     */
    private fun rotateQuads(direction: Direction): RenderContext.QuadTransform = RenderContext.QuadTransform { q ->
        val rotate = Vec3f.POSITIVE_Y.getDegreesQuaternion(
            when (direction) {
                Direction.NORTH -> 0f
                Direction.EAST -> 270f
                Direction.SOUTH -> 180f
                Direction.WEST -> 90f
                else -> 0f
            }
        )

        val tmp = Vec3f()
        for (i in 0..3) {
            q.copyPos(i, tmp)
            tmp.add(-0.5f, -0.5f, -0.5f)
            tmp.rotate(rotate)
            tmp.add(0.5f, 0.5f, 0.5f)
            q.pos(i, tmp)

            if (q.hasNormal(i)) {
                q.copyNormal(i, tmp)
                tmp.rotate(rotate)
                q.normal(i, tmp)
            }
        }
        q.cullFace(null)
        q.nominalFace(direction)
        true
    }

    enum class MachineTextureUV(val direction: Direction?, val u1: Float, val v1: Float, val u2: Float, val v2: Float) {
        FRONT(Direction.NORTH, 16f / 48f * 16f, 16f / 48f * 16f, 32f / 48f * 16f, 32f / 48f * 16f),
        LEFT(Direction.EAST, 0.0f, 16f / 48f * 16f, 16f / 48f * 16f, 32f / 48f * 16f),
        BACK(Direction.SOUTH, 32f / 48f * 16f, 32f / 48f * 16f, 16.0f, 16f),
        RIGHT(Direction.WEST, 32f / 48f * 16f, 16f / 48f * 16f, 16.0f, 32f / 48f * 16f),
        TOP(Direction.UP, 16f / 48f * 16f, 0.0f, 32f / 48f * 16f, 16f / 48f * 16f),
        BOTTOM(Direction.DOWN, 16f / 48f * 16f, 32f / 48f * 16f, 32f / 48f * 16f, 16.0f),
        FULL(null, 0f, 0f, 16f, 16f);

        companion object {
            val BY_DIRECTION = values().associateBy { it.direction }
        }
    }

    companion object {
        val MATERIAL by lazy {
            RendererAccess.INSTANCE.renderer?.materialFinder()!!.clear()
                .spriteDepth(1)
                .blendMode(0, BlendMode.CUTOUT)
                .disableAo(0, true)
                .disableDiffuse(0, true)
                .emissive(0, true)
                ?.find()
        }
        val TRANSFORM: ModelTransformation by lazy {
            MinecraftClient.getInstance().bakedModelManager.getModel(
                ModelIdentifier(Identifier("stone"), "")
            ).transformation
        }
    }
}