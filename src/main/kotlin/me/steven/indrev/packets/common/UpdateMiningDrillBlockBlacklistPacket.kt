package me.steven.indrev.packets.common

import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

object UpdateMiningDrillBlockBlacklistPacket {

    val UPDATE_BLACKLIST_PACKET = identifier("update_drill_blacklist")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_BLACKLIST_PACKET) { server, player, _, buf, _ ->
            val mode = Mode.values()[buf.readInt()]
            mode.process(buf, server, player)
        }
    }

    enum class Mode(val process: (PacketByteBuf, MinecraftServer, ServerPlayerEntity) -> Unit) {
        SINGLE({ buf, server, player ->
            val pos = buf.readBlockPos()
            server.execute {
                val stack = player.mainHandStack
                val nbt = stack.orCreateNbt.getList("BlacklistedPositions", 10)

                val posNbt = NbtHelper.fromBlockPos(pos)

                if (nbt.contains(posNbt)) nbt.remove(posNbt)
                else nbt.add(posNbt)

                stack.nbt!!.put("BlacklistedPositions", nbt)
            }
        }),
        FLIP_Y({ _, server, player ->
            server.execute {
                val stack = player.mainHandStack
                val flipped = DrillModule.getBlacklistedPositions(stack).map { BlockPos(it.x, -it.y, it.z) }
                update(stack, flipped)
            }
        }),
        FLIP_X({ _, server, player ->
            server.execute {
                val stack = player.mainHandStack
                val flipped = DrillModule.getBlacklistedPositions(stack).map { BlockPos(-it.x, it.y, it.z) }
                update(stack, flipped)
            }
        }),
        ROT_X_90_CLOCKWISE({ _, server, player ->
            server.execute {
                val stack = player.mainHandStack
                val flipped = DrillModule.getBlacklistedPositions(stack).map { BlockPos(it.y, -it.x, it.z) }
                update(stack, flipped)
            }
        }),
        ROT_X_90_COUNTERCLOCKWISE({ _, server, player ->
            server.execute {
                val stack = player.mainHandStack
                val flipped = DrillModule.getBlacklistedPositions(stack).map { BlockPos(-it.y, it.x, it.z) }
                update(stack, flipped)
            }
        }),
        CLEAR({ _, server, player ->
            server.execute {
                val stack = player.mainHandStack
                update(stack, emptyList())
            }
        }),
    }

    private fun update(stack: ItemStack, blacklist: List<BlockPos>) {
        if (blacklist.isEmpty()) {
            stack.removeSubNbt("BlacklistedPositions")
            return
        }

        val tagList = NbtList()
        blacklist.map { pos -> NbtHelper.fromBlockPos(pos) }.forEach { tagList.add(it) }

        stack.nbt!!.put("BlacklistedPositions", tagList)
    }
}