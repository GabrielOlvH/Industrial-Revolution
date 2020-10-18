package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.CableBlock
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
import net.minecraft.util.*
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
            return TypedActionResult.method_29237(stack, world.isClient)
        }
        return TypedActionResult.pass(user?.getStackInHand(hand))
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
                if (block is CableBlock && player?.isSneaking == false) {
                    val side = context.side
                    val property = CableBlock.getProperty(side)
                    state = state.with(property, !state[property])
                    world.setBlockState(pos, state)
                    stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
                } else if (player?.isSneaking == true && block is MachineBlock) {
                    block.toTagComponents(world, player, pos, state, blockEntity, stack)
                    world.breakBlock(pos, false, context.player)
                } else {
                    val rotated = state.rotate(BlockRotation.CLOCKWISE_90)
                    if (rotated == state) return ActionResult.PASS
                    world.setBlockState(pos, rotated)
                }
                stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
                return ActionResult.success(world.isClient)
            }
            Mode.CONFIGURE -> {
                if (blockEntity is MachineBlockEntity<*>) {
                    val inventoryComponent = blockEntity.inventoryComponent
                    if ((inventoryComponent != null
                            && (inventoryComponent.inventory.inputSlots.isNotEmpty() || inventoryComponent.inventory.outputSlots.isNotEmpty()))
                        || blockEntity.fluidComponent != null) {
                        player?.openHandledScreen(IRScreenHandlerFactory(::WrenchController, pos!!))
                        return ActionResult.success(world.isClient)
                    }
                }
            }
        }
        return ActionResult.PASS
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

        private enum class Mode {
            CONFIGURE, ROTATE;

            fun next(): Mode = when (this) {
                CONFIGURE -> ROTATE
                ROTATE -> CONFIGURE
            }
        }
    }
}