package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.client.node.ClientServoNodeInfo
import me.steven.indrev.networks.client.node.to
import me.steven.indrev.utils.blockSpriteId
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.ModelRotation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

class FluidPipeModel(tier: Tier) : BasePipeModel(tier, "fluid_pipe") {
    override val spriteIdCollection: MutableList<SpriteIdentifier> = mutableListOf(
        blockSpriteId("block/fluid_pipe_center_${tier.toString().lowercase()}"),
        blockSpriteId("block/fluid_pipe_side_${tier.toString().lowercase()}"),
        blockSpriteId("block/servo_retriever"),
        blockSpriteId("block/servo_output")
    )


    val retrieverServoModels = arrayOfNulls<BakedModel>(6)
    val outputServoModels = arrayOfNulls<BakedModel>(6)

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        super.bake(loader, textureGetter, rotationContainer, modelId)

        val retrieverModel = loader.getOrLoadModel( identifier("block/servo_retriever"))
        retrieverServoModels[0] = retrieverModel.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId) // NORTH
        retrieverServoModels[1] = retrieverModel.bake(loader, textureGetter, ModelRotation.X270_Y90, modelId) // EAST
        retrieverServoModels[2] = retrieverModel.bake(loader, textureGetter, ModelRotation.X270_Y180, modelId) // SOUTH
        retrieverServoModels[3] = retrieverModel.bake(loader, textureGetter, ModelRotation.X270_Y270, modelId) // WEST
        retrieverServoModels[4] = retrieverModel.bake(loader, textureGetter, ModelRotation.X180_Y0, modelId) // UP
        retrieverServoModels[5] = retrieverModel.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId) // DOWN

        val outputModel = loader.getOrLoadModel( identifier("block/servo_output"))
        outputServoModels[0] = outputModel.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId) // NORTH
        outputServoModels[1] = outputModel.bake(loader, textureGetter, ModelRotation.X270_Y90, modelId) // EAST
        outputServoModels[2] = outputModel.bake(loader, textureGetter, ModelRotation.X270_Y180, modelId) // SOUTH
        outputServoModels[3] = outputModel.bake(loader, textureGetter, ModelRotation.X270_Y270, modelId) // WEST
        outputServoModels[4] = outputModel.bake(loader, textureGetter, ModelRotation.X180_Y0, modelId) // UP
        outputServoModels[5] = outputModel.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId) // DOWN

        return this
    }

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        super.emitBlockQuads(world, state, pos, randSupplier, context)
        IndustrialRevolutionClient.CLIENT_NETWORK_STATE[Network.Type.FLUID]?.get(pos)?.to<ClientServoNodeInfo>()?.servos?.forEach { (dir, type) ->
            val index = when (dir!!) {
                Direction.DOWN -> 5
                Direction.UP -> 4
                Direction.NORTH -> 0
                Direction.SOUTH -> 2
                Direction.WEST -> 3
                Direction.EAST -> 1
            }

            val model = when (type) {
                EndpointData.Type.RETRIEVER -> retrieverServoModels
                EndpointData.Type.OUTPUT -> outputServoModels
                else -> return
            }[index]
            context.fallbackConsumer().accept(model)
        }
    }

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        super.emitItemQuads(stack, p1, context)

        context.meshConsumer().accept(meshArray[1])
        context.meshConsumer().accept(meshArray[3])
    }
}