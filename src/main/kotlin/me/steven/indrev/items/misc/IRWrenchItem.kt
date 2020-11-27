package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.controllers.wrench.WrenchController
import me.steven.indrev.utils.WrenchConfigurationType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class IRWrenchItem(settings: Settings) : Item(settings) {

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        if (user?.isSneaking == true && world?.isClient == false) {
            val stack = user.getStackInHand(hand)
            if (stack.item != this) return TypedActionResult.pass(stack)
            val mode = getMode(stack).next()
            val tag = stack.tag ?: CompoundTag()
            tag.putString("TransferMode", mode.toString())
            stack.tag = tag
            user.sendMessage(TranslatableText("item.indrev.wrench.switch_mode", mode), true)
            return TypedActionResult.success(stack, world.isClient)
        }
        return TypedActionResult.pass(user?.getStackInHand(hand))
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val stack = context.stack
        val pos = context.blockPos
        val player = context.player
        val state = world?.getBlockState(pos) ?: return ActionResult.FAIL
        val block = state.block
        val blockEntity = if (block.hasBlockEntity()) world.getBlockEntity(pos) else null
        return getMode(stack).useOnBlock(world, pos, state, blockEntity, player, stack)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText(
                "item.indrev.wrench.tooltip1",
                TranslatableText("item.indrev.wrench.tooltip1.${getMode(stack).toString().toLowerCase()}").formatted(
                    Formatting.WHITE
                )
            ).formatted(Formatting.GOLD)
        )
        tooltip?.add(TranslatableText("item.indrev.wrench.tooltip").formatted(Formatting.DARK_GRAY))
    }

    private fun getMode(stack: ItemStack?): Mode {
        val tag = stack?.tag
        if (tag != null && tag.contains("TransferMode")) {
            val s = tag.getString("TransferMode").toUpperCase()
            return Mode.valueOf(s)
        }
        return Mode.ROTATE
    }

    companion object {
        private enum class Mode {
            CONFIGURE {
                override fun useOnBlock(
                    world: World,
                    pos: BlockPos,
                    blockState: BlockState,
                    blockEntity: BlockEntity?,
                    player: PlayerEntity?,
                    stack: ItemStack
                ): ActionResult {
                    if (blockEntity is MachineBlockEntity<*>) {
                        if (WrenchConfigurationType.getTypes(blockEntity).isNotEmpty()) {
                            player?.openHandledScreen(IRScreenHandlerFactory(::WrenchController, pos))
                            return ActionResult.success(world.isClient)
                        }
                    }
                    return ActionResult.PASS
                }
            },
            ROTATE {
                override fun useOnBlock(
                    world: World,
                    pos: BlockPos,
                    blockState: BlockState,
                    blockEntity: BlockEntity?,
                    player: PlayerEntity?,
                    stack: ItemStack
                ): ActionResult {
                    val block = blockState.block
                    if (player?.isSneaking == true && block is MachineBlock) {
                        block.toTagComponents(world, player, pos, blockState, blockEntity, stack)
                        world.breakBlock(pos, false, player)
                    } else {
                        val rotated = blockState.rotate(BlockRotation.CLOCKWISE_90)
                        if (rotated == blockState) return ActionResult.PASS
                        world.setBlockState(pos, rotated)
                    }
                    return ActionResult.success(world.isClient)
                }
            };

            abstract fun useOnBlock(
                world: World,
                pos: BlockPos,
                blockState: BlockState,
                blockEntity: BlockEntity?,
                player: PlayerEntity?,
                stack: ItemStack
            ): ActionResult

            fun next(): Mode = when (this) {
                CONFIGURE -> ROTATE
                ROTATE -> CONFIGURE
            }
        }
    }
}