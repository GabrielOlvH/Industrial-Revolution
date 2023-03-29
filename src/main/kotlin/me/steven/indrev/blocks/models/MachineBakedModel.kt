package me.steven.indrev.blocks.models

import com.mojang.datafixers.util.Pair
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.utils.blockSpriteId
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
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
import java.util.Locale
import java.util.function.Function
import java.util.function.Supplier


open class MachineBakedModel(val id: String) : UnbakedModel, BakedModel, FabricBakedModel {

    var baseSpriteId = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/${id.replace(Regex("_mk[0-4]"), "")}"))

    var overlayIds: MutableList<SpriteIdentifier> = mutableListOf()
    var workingOverlayIds: MutableList<SpriteIdentifier> = mutableListOf()

    var baseSprite: Sprite? = null
    val overlays: Array<Sprite?> by lazy { arrayOfNulls(overlayIds.size) }
    val workingOverlays: Array<Sprite?> by lazy { arrayOfNulls(workingOverlayIds.size) }

    val emissives = hashSetOf<Sprite>()

    var defaultMesh: Mesh? = null
    var workingStateMesh: Mesh? = null

    fun factoryOverlay() {
        overlayIds.add(blockSpriteId("block/factory_overlay"))
    }

    fun tierOverlay(tier: Tier) {
        overlayIds.add(blockSpriteId("block/${tier.toString().lowercase(Locale.getDefault())}_overlay"))
    }

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        baseSprite = textureGetter.apply(baseSpriteId)
        overlayIds.processSprites(overlays, textureGetter)
        workingOverlayIds.processSprites(workingOverlays, textureGetter)
        if (isEmissive(baseSprite)) emissives.add(baseSprite!!)

        buildDefaultMesh()
        buildWorkingStateMesh()

        return this
    }

    open fun buildDefaultMesh() {
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        val builder: MeshBuilder = renderer.meshBuilder()
        val emitter = builder.emitter

        for (direction in Direction.values()) {
            emitter.draw(direction, baseSprite!!, -1)
            overlays.forEach { overlay -> emitter.draw(direction, overlay!!, -1) }
        }
        defaultMesh = builder.build()
    }

     fun buildWorkingStateMesh() {
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        val builder: MeshBuilder = renderer.meshBuilder()
        val emitter = builder.emitter

        for (direction in Direction.values()) {
            emitter.draw(direction, baseSprite!!, -1)
            workingOverlays.forEach { overlay -> emitter.draw(direction, overlay!!, -1) }
            overlays.forEach { overlay -> emitter.draw(direction, overlay!!, -1) }
        }
        workingStateMesh = builder.build()
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

    //don't judge me
    fun isEmissive(sprite: Sprite?) = sprite?.id?.toString()?.contains("emissive") == true

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        val list = mutableListOf(baseSpriteId)
        list.addAll(overlayIds)
        list.addAll(workingOverlayIds)
        return list
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = true

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite? = baseSprite

    override fun getTransformation(): ModelTransformation = TRANSFORM

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        val block = state.block as? MachineBlock ?: return
        val direction = block.getFacing(state)
        val blockEntity = blockView.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
        ctx.pushTransform(rotateQuads(direction))
        val m = if (blockEntity.workingState) workingStateMesh else defaultMesh
        ctx.meshConsumer().accept(m)
        ctx.popTransform()
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        ctx.meshConsumer().accept(defaultMesh)
    }

    protected fun QuadEmitter.draw(
        side: Direction,
        sprite: Sprite,
        color: Int = -1
    ) {
        square(side, 0f, 0f, 1f, 1f, 0f)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, color, color, color, color)
        if (emissives.contains(sprite))
            material(MATERIAL)
        val uv =
            if (sprite.width == 16 && sprite.height == 16) MachineTextureUV.FULL
            else MachineTextureUV.BY_DIRECTION[side]!!
        sprite(0, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
        sprite(1, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(2, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(3, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
        emit()
    }

    /**
     * Original code belongs to Haven-King.
     * Source: https://github.com/Haven-King/Automotion
     */
    fun rotateQuads(direction: Direction): RenderContext.QuadTransform = RenderContext.QuadTransform { q ->
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