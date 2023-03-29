package me.steven.indrev.blocks.models

import com.mojang.datafixers.util.Pair
import me.steven.indrev.blockentities.miningrig.MiningRigBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

class MinerBakedModel(id: String) : MachineBakedModel(id) {

    private val screenSpriteId = blockSpriteId("block/mining_rig_screen_emissive")
    var screenSprite: Sprite? = null

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        screenSprite = textureGetter.apply(screenSpriteId)
        emissives.add(screenSprite!!)
        return super.bake(loader, textureGetter, rotationContainer, modelId)
    }

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        val deps = super.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences)
        deps.add(screenSpriteId)
        return deps
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, ctx)
        val block = state.block as? MachineBlock ?: return
        val direction = block.getFacing(state)
        val blockEntity = blockView.getBlockEntity(pos) as? MiningRigBlockEntity ?: return

        if (blockEntity.workingState)
            ctx.emitter.draw(direction, screenSprite!!)
    }
}