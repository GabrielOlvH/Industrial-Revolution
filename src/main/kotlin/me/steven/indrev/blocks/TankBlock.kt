package me.steven.indrev.blocks

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.blockentities.storage.TankBlockEntity
import me.steven.indrev.mixin.AccessorBucketItem
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.BucketItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class TankBlock(private val tier: Tier, settings: Settings) : Block(settings), BlockEntityProvider, AttributeProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity? = TankBlockEntity(tier)

    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        val itemStack = player?.getStackInHand(hand)
        val item = itemStack?.item
        if (item is BucketItem) {
            val tankEntity = world?.getBlockEntity(pos) as? TankBlockEntity ?: return ActionResult.PASS
            val bucketFluid = (item as AccessorBucketItem).fluid
            val tank = tankEntity.fluidComponent.tanks[0]
            if (tank.volume.amount() >= FluidAmount.BUCKET && bucketFluid == Fluids.EMPTY) {
                val bucket = tank.volume.fluidKey.rawFluid?.bucketItem
                val extractable = tankEntity.fluidComponent.extractable
                val volume = tank.volume.fluidKey.withAmount(FluidAmount.BUCKET)
                if (bucket != null && !extractable.extract(volume.amount()).isEmpty) {
                    itemStack.decrement(1)
                    player.inventory?.insertStack(ItemStack(bucket))
                    return ActionResult.SUCCESS
                }
            } else if (bucketFluid != Fluids.EMPTY) {
                val volume = FluidKeys.get(bucketFluid).withAmount(FluidAmount.BUCKET)
                val insertable = tankEntity.fluidComponent.insertable
                if (insertable.insert(volume).isEmpty) {
                    itemStack.decrement(1)
                    player.inventory?.insertStack(ItemStack(Items.BUCKET))
                    return ActionResult.SUCCESS
                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun afterBreak(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        player?.incrementStat(Stats.MINED.getOrCreateStat(this))
        player?.addExhaustion(0.005f)
        toTagComponents(world, player, pos, state, blockEntity, toolStack)
    }

    fun toTagComponents(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        if (world is ServerWorld) {
            getDroppedStacks(state, world, pos, blockEntity, player, toolStack).forEach { stack ->
                if (blockEntity is TankBlockEntity) {
                    val tag = stack.orCreateTag
                    blockEntity.fluidComponent.toTag(tag)
                }
                dropStack(world, pos, stack)
            }
            state!!.onStacksDropped(world, pos, toolStack)
        }
    }

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        val tag = itemStack?.tag
        if (tag?.isEmpty == false) {
            val tankEntity = world?.getBlockEntity(pos) as? TankBlockEntity ?: return
            tankEntity.fluidComponent.fromTag(tag)
        }
    }

    override fun addAllAttributes(world: World?, pos: BlockPos?, state: BlockState?, to: AttributeList<*>?) {
        val tankEntity = world?.getBlockEntity(pos) as? TankBlockEntity ?: return
        when (to?.attribute) {
            FluidAttributes.EXTRACTABLE, FluidAttributes.INSERTABLE -> to?.offer(tankEntity.fluidComponent)
        }
    }
}