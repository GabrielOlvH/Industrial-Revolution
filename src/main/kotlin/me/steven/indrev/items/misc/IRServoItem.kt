package me.steven.indrev.items.misc

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.ServoNetworkState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.function.LongFunction

class IRServoItem(settings: Settings, val type: EndpointData.Type) : Item(settings) {

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (world?.isClient == true) return TypedActionResult.pass(user.getStackInHand(hand))
        val stack = user.getStackInHand(hand)
        val newMode = getMode(stack).next()
        stack.orCreateTag.putString("mode", newMode.toString())
        //TODO fix this
        user.sendMessage(LiteralText("set mode to $newMode"), true)
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
            val x = hit.x - pos!!.x
            val y = hit.y - pos.y
            val z = hit.z - pos.z
            val dir = when {
                y > 0.6625 -> Direction.UP
                y < 0.3375 -> Direction.DOWN
                x > 0.6793 -> Direction.EAST
                x < 0.3169 -> Direction.WEST
                z < 0.3169 -> Direction.NORTH
                z > 0.6625 -> Direction.SOUTH
                else -> null
            }
            if (dir != null && state[BasePipeBlock.getProperty(dir)]) {
                val network = block.type.getNetworkState(world) as? ServoNetworkState?
                network?.let { networkState ->
                    if (networkState[pos]?.containers?.containsKey(pos.offset(dir)) == true)
                        networkState.endpointData.let { modes ->
                            val data = EndpointData(type, getMode(stack))
                            modes.computeIfAbsent(pos.asLong(), LongFunction { Object2ObjectOpenHashMap() })[dir] = data
                            context.player?.sendMessage(LiteralText("Set $dir to $data"), true)
                        }
                    networkState.markDirty()
                    return ActionResult.CONSUME
                }
            }
        }
        return ActionResult.PASS
    }

    companion object {
        fun getMode(itemStack: ItemStack): EndpointData.Mode {
            val m = itemStack.orCreateTag.getString("mode")
            if (m.isNullOrEmpty()) return EndpointData.Mode.NEAREST_FIRST
            return EndpointData.Mode.valueOf(m.toUpperCase())
        }
    }
}