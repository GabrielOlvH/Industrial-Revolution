package me.steven.indrev.items.misc

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.HeliostatBlock
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.wrench.WrenchScreenHandler
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class IRWrenchItem(settings: Settings) : Item(settings) {

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        if (user?.isSneaking == true && world?.isClient == false) {
            val stack = user.getStackInHand(hand)
            if (stack.item != this) return TypedActionResult.pass(stack)
            val mode = getMode(stack).next()
            val tag = stack.tag ?: NbtCompound()
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
        val blockEntity = if (state.hasBlockEntity()) world.getBlockEntity(pos) else null
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
                TranslatableText("item.indrev.wrench.tooltip1.${getMode(stack).toString().lowercase(Locale.getDefault())}").formatted(
                    Formatting.WHITE
                )
            ).formatted(Formatting.GOLD)
        )
        tooltip?.add(TranslatableText("item.indrev.wrench.tooltip").formatted(Formatting.DARK_GRAY))
    }

    private fun getMode(stack: ItemStack?): Mode {
        val tag = stack?.tag
        if (tag != null && tag.contains("TransferMode")) {
            val s = tag.getString("TransferMode").uppercase(Locale.getDefault())
            return Mode.valueOf(s)
        }
        return Mode.ROTATE
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity?, slot: Int, selected: Boolean) {
        if (!selected) stack?.orCreateTag?.remove("SelectedHeliostats")
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
                    if (blockState.isOf(IRBlockRegistry.HELIOSTAT_BLOCK)) {
                        val positions = LongOpenHashSet()
                        positions.add(pos.asLong())
                        HeliostatBlock.findConnectingHeliostats(pos, world, LongOpenHashSet(), positions)
                        val tagList = stack.orCreateTag.getList("SelectedHeliostats", 4)
                        positions.forEach { long -> tagList.add(LongTag.of(long)) }
                        stack.orCreateTag.put("SelectedHeliostats", tagList)
                        player?.sendMessage(LiteralText("Click on Solar Power Plant Tower to link the Heliostats."), true)
                    } else if (blockEntity is MachineBlockEntity<*>) {
                        if (ConfigurationType.getTypes(blockEntity).isNotEmpty()) {
                            player?.openHandledScreen(IRScreenHandlerFactory(::WrenchScreenHandler, pos))
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
                        block.writeNbtComponents(world, player, pos, blockState, blockEntity, stack)
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