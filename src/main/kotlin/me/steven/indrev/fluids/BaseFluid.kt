package me.steven.indrev.fluids

import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.StateManager
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class BaseFluid(
    val identifier: Identifier,
    val block: () -> FluidBlock,
    private val bucketItem: () -> BucketItem,
    private val color: Int
) : FlowableFluid() {
    override fun toBlockState(state: FluidState?): BlockState? = block().defaultState.with(
        FluidBlock.LEVEL, method_15741(state)
    )

    override fun getBucketItem(): Item = bucketItem()

    override fun getLevelDecreasePerBlock(world: WorldView?): Int = 1

    override fun getTickRate(world: WorldView?): Int = 5

    override fun isInfinite(): Boolean = false

    override fun getFlowSpeed(world: WorldView?): Int = 2

    override fun canBeReplacedWith(
        state: FluidState?,
        world: BlockView?,
        pos: BlockPos?,
        fluid: Fluid,
        direction: Direction
    ): Boolean = false

    override fun getBlastResistance(): Float = 100F

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos?, state: BlockState) {
        val blockEntity = if (state.block.hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world, pos, blockEntity)
    }

    override fun matchesType(fluid: Fluid?): Boolean = fluid == flowing || fluid == still

    fun registerRender(fluidType: FluidType) {
        val stillSpriteId = fluidType.stillId
        val flowSpriteId = fluidType.flowId
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            .register(ClientSpriteRegistryCallback { _, registry ->
                registry.register(stillSpriteId)
                registry.register(flowSpriteId)
            })
        val moltenFluidSprites = arrayOfNulls<Sprite>(2)
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun apply(manager: ResourceManager?) {
                    val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                    moltenFluidSprites[0] = atlas.apply(stillSpriteId)
                    moltenFluidSprites[1] = atlas.apply(flowSpriteId)
                }

                override fun getFabricId(): Identifier = identifier("${identifier.path}_reload_listener")
            })
        val moltenOreFluidRender = object : FluidRenderHandler {
            override fun getFluidSprites(view: BlockRenderView, pos: BlockPos, state: FluidState): Array<Sprite?> =
                moltenFluidSprites

            override fun getFluidColor(view: BlockRenderView, pos: BlockPos, state: FluidState): Int = color
        }
        FluidRenderHandlerRegistry.INSTANCE.register(still, moltenOreFluidRender)
        FluidRenderHandlerRegistry.INSTANCE.register(flowing, moltenOreFluidRender)
        BlockRenderLayerMap.INSTANCE.putFluids(
            RenderLayer.getTranslucent(),
            still,
            flowing
        )
    }


    class Flowing(
        identifier: Identifier,
        block: () -> FluidBlock,
        bucketItem: () -> BucketItem,
        color: Int,
        val still: () -> Still
    ) : BaseFluid(identifier, block, bucketItem, color) {
        override fun appendProperties(builder: StateManager.Builder<Fluid, FluidState>?) {
            super.appendProperties(builder)
            builder?.add(LEVEL)
        }

        override fun getFlowing(): Fluid = this

        override fun getStill(): Fluid = still()

        override fun getLevel(state: FluidState): Int = state[LEVEL]

        override fun isStill(state: FluidState?): Boolean = false
    }

    class Still(
        identifier: Identifier,
        block: () -> FluidBlock,
        bucketItem: () -> BucketItem,
        color: Int,
        val flowing: () -> Flowing
    ) : BaseFluid(identifier, block, bucketItem, color) {
        override fun getLevel(state: FluidState?): Int = 8

        override fun getFlowing(): Fluid = flowing()

        override fun getStill(): Fluid = this

        override fun isStill(state: FluidState?): Boolean = true

    }
}