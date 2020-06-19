package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

open class MachineBlock(
    settings: Settings,
    private val tier: Tier,
    val screenHandlerType: ExtendedScreenHandlerType<*>?,
    private val blockEntityProvider: () -> MachineBlockEntity
) : Block(settings), BlockEntityProvider, InventoryProvider {

    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun buildTooltip(
        stack: ItemStack?,
        view: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(TranslatableText("block.machines.tooltip.io", Formatting.BLUE, Formatting.WHITE, tier.io))
    }

    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult? {
        if (world.isClient) return ActionResult.SUCCESS
        val blockEntity = world.getBlockEntity(pos)
        if (screenHandlerType != null && blockEntity is MachineBlockEntity && blockEntity.inventoryController != null) {
            player?.openHandledScreen(IRScreenHandlerFactory(screenHandlerType, pos!!))?.ifPresent { syncId ->
                blockEntity.viewers[player.uuid] = syncId
            }
        }
        return ActionResult.SUCCESS
    }

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory {
        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity !is InventoryProvider) throw IllegalArgumentException("tried to retrieve an inventory from an invalid block entity")
        return blockEntity.getInventory(state, world, pos)
    }
}