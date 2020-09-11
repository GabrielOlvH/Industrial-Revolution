package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.CableBlock
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.controllers.wrench.WrenchController
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Direction
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
        }
        return super.use(world, user, hand)
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        val world = context?.world
        val stack = context?.stack
        val pos = context?.blockPos
        val player = context?.player
        var state = world?.getBlockState(pos) ?: return ActionResult.FAIL
        val block = state.block
        val blockEntity = if (block.hasBlockEntity()) world.getBlockEntity(pos) else null
        when (getMode(stack)) {
            Mode.ROTATE -> {
                if (world.isClient) return super.useOnBlock(context)
                if (block is CableBlock && player?.isSneaking == false) {
                    val side = context.side
                    val property = CableBlock.getProperty(side)
                    state = state.with(property, !state[property])
                    world.setBlockState(pos, state)
                    stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
                    return ActionResult.SUCCESS
                } else if (block is MachineBlock) {
                    when {
                        player?.isSneaking == true -> {
                            block.toTagComponents(world, player, pos, state, blockEntity, stack)
                            world.breakBlock(pos, false, context.player)
                        }
                        state.contains(VERTICAL_FACING) -> {
                            val facing = state[VERTICAL_FACING]
                            val rotated = if (facing.ordinal + 1 >= ALL.size) 0 else facing.ordinal + 1
                            state = state.with(VERTICAL_FACING, ALL[rotated])
                            world.setBlockState(pos, state)
                        }
                        state.contains(HORIZONTAL_FACING) -> {
                            val facing = state[HORIZONTAL_FACING]
                            val rotated = if (facing.horizontal + 1 >= HORIZONTAL.size) 0 else facing.horizontal + 1
                            state = state.with(HORIZONTAL_FACING, HORIZONTAL[rotated])
                            world.setBlockState(pos, state)
                        }
                        else -> return super.useOnBlock(context)
                    }
                    stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
                    return ActionResult.SUCCESS
                }
            }
            Mode.CONFIGURE -> {
                if (blockEntity is MachineBlockEntity<*>) {
                    val inventoryComponent = blockEntity.inventoryComponent
                    if ((inventoryComponent != null
                            && (inventoryComponent.inventory.inputSlots.isNotEmpty() || inventoryComponent.inventory.outputSlots.isNotEmpty()))
                        || blockEntity.fluidComponent != null) {
                        player?.openHandledScreen(IRScreenHandlerFactory(::WrenchController, pos!!))
                    }
                }
            }
        }
        return super.useOnBlock(context)
    }

    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.wrench.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
        super.appendTooltip(stack, world, tooltip, context)
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
        private val ALL = Direction.values()
        private val HORIZONTAL = ALL
            .filter { direction -> direction.axis.isHorizontal }
            .sortedBy { dir -> dir.horizontal }
            .toTypedArray()
        private val VERTICAL_FACING = FacingMachineBlock.FACING
        private val HORIZONTAL_FACING = HorizontalFacingMachineBlock.HORIZONTAL_FACING

        private enum class Mode {
            CONFIGURE, ROTATE;

            fun next(): Mode = when (this) {
                CONFIGURE -> ROTATE
                ROTATE -> CONFIGURE
            }
        }
    }
}