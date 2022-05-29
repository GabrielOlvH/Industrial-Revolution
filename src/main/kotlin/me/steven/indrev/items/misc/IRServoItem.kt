package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.cables.BasePipeBlockEntity
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.*
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.world.World

class IRServoItem(settings: Settings, val type: EndpointData.Type) : Item(settings) {

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(translatable("$translationKey.tooltip"))
        tooltip.add(EMPTY)
        val modeString = getMode(stack).toString().lowercase()
        tooltip.add(translatable("item.indrev.servo.mode")
            .append(translatable("item.indrev.servo.mode.$modeString").formatted(Formatting.BLUE)))
        tooltip.add(translatable("item.indrev.servo.mode.$modeString.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (world?.isClient == true) return TypedActionResult.pass(user.getStackInHand(hand))
        val stack = user.getStackInHand(hand)
        val newMode = getMode(stack).next()
        stack.orCreateNbt.putString("mode", newMode.toString())
        user.sendMessage(translatable("item.indrev.servo.mode")
            .append(translatable("item.indrev.servo.mode.${newMode.toString().lowercase()}").formatted(Formatting.BLUE)), true)
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
            val blockEntity = world.getBlockEntity(pos) as? BasePipeBlockEntity ?: return ActionResult.PASS
            if (dir != null && blockEntity.connections[dir]!!.isConnected()) {
                val network = block.type.getNetworkState(world) as? ServoNetworkState?
                network?.also { networkState ->
                    if (networkState.networksByPos.get(pos.asLong())?.containers?.containsKey(pos.offset(dir)) == true) {
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
                        val data = networkState.getEndpointData(pos, dir, true) ?: return@also context.player!!.sendMessage(literal("Failed to put servo"), true)
                        data.type = type
                        data.mode = getMode(stack)
                        networkState.version++
                        stack.decrement(1)

                        networkState.markDirty()
                        return ActionResult.SUCCESS
                    }
                }
            }
        }
        return ActionResult.PASS
    }

    companion object {
        fun getMode(itemStack: ItemStack): EndpointData.Mode {
            val m = itemStack.orCreateNbt.getString("mode")
            if (m.isNullOrEmpty()) return EndpointData.Mode.NEAREST_FIRST
            return EndpointData.Mode.valueOf(m.uppercase())
        }
    }
}