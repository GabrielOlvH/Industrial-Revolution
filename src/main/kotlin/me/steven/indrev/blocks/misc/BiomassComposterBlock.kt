package me.steven.indrev.blocks.misc

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import alexiil.mc.lib.attributes.item.ItemAttributes
import me.steven.indrev.blockentities.farms.BiomassComposterBlockEntity
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BiomassComposterBlock : Block(FabricBlockSettings.copyOf(Blocks.COMPOSTER).breakByTool(FabricToolTags.AXES, 1).requiresTool()), AttributeProvider, BlockEntityProvider {

    init {
        this.defaultState = stateManager.defaultState.with(CLOSED, false)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = BiomassComposterBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else BlockEntityTicker { _, _, state, blockEntity ->
            BiomassComposterBlockEntity.tick(
                state, blockEntity as? BiomassComposterBlockEntity ?: return@BlockEntityTicker
            )
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(CLOSED)
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        val blockEntity = world.getBlockEntity(pos) as? BiomassComposterBlockEntity ?: return
        if (to.attribute == ItemAttributes.INSERTABLE || to.attribute == ItemAttributes.EXTRACTABLE) {
            to.offer(blockEntity)
        } else if (
            to.attribute == FluidAttributes.INSERTABLE
            || to.attribute == FluidAttributes.EXTRACTABLE
            || to.attribute == FluidAttributes.GROUPED_INV
            || to.attribute == FluidAttributes.FIXED_INV
        ) {
            to.offer(blockEntity.fluidInv)
        }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult?
    ): ActionResult {
        val stack = player.getStackInHand(hand)
        val blockEntity = world.getBlockEntity(pos) as? BiomassComposterBlockEntity ?: return ActionResult.PASS
        val result = FluidInvUtil.interactHandWithTank(blockEntity.fluidInv.transferable, player, hand)
        if (result.didMoveAny()) {
            blockEntity.markDirty()
            if (!world.isClient)
                blockEntity.sync()
            return result.asActionResult()
        } else if (ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.contains(stack.item) || stack.isEmpty && blockEntity.isReady()) {
            compost(stack, player, blockEntity, world)
        } else if (state[CLOSED]) {
            world.setBlockState(pos, state.with(CLOSED, false))
            if (!player.isCreative)
                player.inventory.offerOrDrop(ItemStack(IRBlockRegistry.PLANKS))
        } else if (stack.isOf(IRBlockRegistry.PLANKS.asItem())) {
            world.setBlockState(pos, state.with(CLOSED, true))
            if (!player.isCreative)
                stack.decrement(1)
        } else return ActionResult.FAIL

        return ActionResult.success(world.isClient)
    }

    private fun compost(stack: ItemStack, player: PlayerEntity, blockEntity: BiomassComposterBlockEntity, world: World) {
        if (world.isClient) return
        if (blockEntity.level < 7 && !stack.isEmpty) {
            val chance = ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.getValue(stack.item)
            if (!player.isCreative)
                stack.decrement(1)
            var leveledUp = false
            if (world.random.nextInt() < chance) {
                blockEntity.level++
                blockEntity.markDirty()
                blockEntity.sync()
                leveledUp = true
            }
            world.syncWorldEvent(1500, blockEntity.pos, if (leveledUp) 1 else 0)
        } else if (blockEntity.ticks >= 20) {
            player.inventory.offerOrDrop(ItemStack(IRItemRegistry.BIOMASS))
            blockEntity.reset()
        }
    }

    companion object {
        val CLOSED: BooleanProperty = BooleanProperty.of("closed")
    }
}