package me.steven.indrev.items.misc

import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.world.World

class IRServoItem(settings: Settings, val type: EndpointData.Type) : Item(settings) {

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(TranslatableText("$translationKey.tooltip"))
        tooltip.add(LiteralText.EMPTY)
        val modeString = getMode(stack).toString().lowercase()
        tooltip.add(TranslatableText("item.indrev.servo.mode")
            .append(TranslatableText("item.indrev.servo.mode.$modeString").formatted(Formatting.BLUE)))
        tooltip.add(TranslatableText("item.indrev.servo.mode.$modeString.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (world?.isClient == true) return TypedActionResult.pass(user.getStackInHand(hand))
        val stack = user.getStackInHand(hand)
        val newMode = getMode(stack).next()
        stack.orCreateTag.putString("mode", newMode.toString())
        user.sendMessage(TranslatableText("item.indrev.servo.mode")
            .append(TranslatableText("item.indrev.servo.mode.${newMode.toString().lowercase()}").formatted(Formatting.BLUE)), true)
        return TypedActionResult.consume(stack)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) return ActionResult.CONSUME
        val hand = context.hand
        val stack = context.stack
        val hit = context.hitPos
        val pos = context.blockPos

        val state = world.getBlockState(pos)
        val block = state.block
        if (block !is BasePipeBlock) return ActionResult.PASS

        if (world is ServerWorld && hand == Hand.MAIN_HAND) {
            val dir = BasePipeBlock.getSideFromHit(hit, pos!!)
            if (dir != null && state[BasePipeBlock.getProperty(dir)]) {
                val network = block.type.getNetworkState(world) as? ServoNetworkState?
                network?.also { networkState ->
                    if (block.type.networksByPos.get(pos.asLong())?.containers?.containsKey(pos.offset(dir)) == true) {
                        val (x, y, z) = hit
                        if (networkState.hasServo(pos, dir)) {
                            when (networkState.getEndpointData(pos, dir)?.type) {
                                EndpointData.Type.OUTPUT ->
                                    ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_OUTPUT))
                                EndpointData.Type.RETRIEVER ->
                                    ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_RETRIEVER))
                                else -> {}
                            }
                        }
                        val data = networkState.getEndpointData(pos, dir, true) ?: return@also context.player!!.sendMessage(LiteralText("Failed to put servo"), true)
                        data.type = type
                        data.mode = getMode(stack)
                        block.type.version++
                        stack.decrement(1)

                        networkState.markDirty()
                        return ActionResult.CONSUME
                    }
                }
            }
        }
        return ActionResult.PASS
    }

    companion object {
        fun getMode(itemStack: ItemStack): EndpointData.Mode {
            val m = itemStack.orCreateTag.getString("mode")
            if (m.isNullOrEmpty()) return EndpointData.Mode.NEAREST_FIRST
            return EndpointData.Mode.valueOf(m.uppercase())
        }
    }
}