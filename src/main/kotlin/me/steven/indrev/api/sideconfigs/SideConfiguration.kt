package me.steven.indrev.api.sideconfigs

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.netty.buffer.Unpooled
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.gui.widgets.machines.WMachineSideDisplay
import me.steven.indrev.packets.common.ConfigureIOPackets
import me.steven.indrev.utils.add
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import me.steven.indrev.utils.translatable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*
import java.util.function.Consumer

data class SideConfiguration(val type: ConfigurationType, private val transferConfig: EnumMap<Direction, TransferMode> = EnumMap(Direction::class.java))
    : MutableMap<Direction, TransferMode> by transferConfig {

    var autoPush = true
    var autoPull = true

    init {
        Direction.values().forEach { dir -> this[dir] = TransferMode.NONE }
    }

    fun canInput(direction: Direction): Boolean {
        return this[direction]?.input == true
    }

    fun canOutput(direction: Direction): Boolean {
        return this[direction]?.output == true
    }

    fun writeNbt(tag: NbtCompound?) {
        var transferConfigTag = tag?.getCompound("TransferConfig")
        if (tag?.contains("TransferConfig") == false) {
            transferConfigTag = NbtCompound()
            tag.put("TransferConfig", transferConfigTag)
        }
        val configTag = NbtCompound()
        forEach { (dir, mode) ->
            configTag.putString(dir.toString(), mode.toString())
        }
        transferConfigTag?.put(type.toString().lowercase(Locale.getDefault()), configTag)
        configTag.putBoolean("AutoPush", autoPush)
        configTag.putBoolean("AutoPull", autoPull)
    }

    fun readNbt(tag: NbtCompound?) {
        if (tag?.contains("TransferConfig") == true) {
            val transferConfigTag = tag.getCompound("TransferConfig")
            val configTag = transferConfigTag.getCompound(type.toString().lowercase(Locale.getDefault()))
            Direction.values().forEach { dir ->
                val value = configTag.getString(dir.toString()).uppercase(Locale.getDefault())
                if (value.isNotEmpty()) {
                    val mode = TransferMode.valueOf(value)
                    this[dir] = mode
                }
            }
            if (configTag.contains("AutoPush"))
                autoPush = configTag.getBoolean("AutoPush")
            if (configTag.contains("AutoPull"))
                autoPull = configTag.getBoolean("AutoPull")
        }
    }

    fun writeBuf(buf: PacketByteBuf) {
        buf.writeBoolean(autoPush)
        buf.writeBoolean(autoPull)
        forEach { dir, mode ->
            buf.writeByte(dir.ordinal)
            buf.writeByte(mode.ordinal)
        }
    }

    fun readBuf(buf: PacketByteBuf) {
        autoPush = buf.readBoolean()
        autoPull = buf.readBoolean()
        repeat(6) {
            val direction = Direction.values()[buf.readByte().toInt()]
            val mode = TransferMode.values()[buf.readByte().toInt()]
            this[direction] = mode
        }
    }

    fun getConfigurationPanel(world: World, pos: BlockPos, configurable: Configurable, playerInventory: PlayerInventory, type: ConfigurationType): WWidget {
        val root = WGridPanel()
        root.setSize(100, 128)

        val configuration = this

        if (configuration.type == ConfigurationType.ITEM) {
            val autoPushBtn = WToggleButton(translatable("item.indrev.wrench.autopush"))
            autoPushBtn.toggle = configuration.autoPush
            autoPushBtn.onToggle = Consumer { v ->
                configuration.autoPush = v
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeByte(0)
                buf.writeBlockPos(pos)
                buf.writeBoolean(v)
                ClientPlayNetworking.send(ConfigureIOPackets.UPDATE_AUTO_OPERATION_PACKET_ID, buf)
            }
            root.add(autoPushBtn, 0, 4)
            val autoPullBtn = WToggleButton(translatable("item.indrev.wrench.autopull"))
            autoPullBtn.toggle = configuration.autoPull
            autoPullBtn.onToggle = Consumer { v ->
                configuration.autoPull = v
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeByte(1)
                buf.writeBlockPos(pos)
                buf.writeBoolean(v)
                ClientPlayNetworking.send(ConfigureIOPackets.UPDATE_AUTO_OPERATION_PACKET_ID, buf)
            }
            root.add(autoPullBtn, 0.0, 4.8)
        }

        val blockState = world.getBlockState(pos)

        val machineVisualizerPanel = WGridPanel()
        MachineSide.values().forEach { side ->
            var facing =
                when {
                    blockState.contains(HorizontalFacingMachineBlock.HORIZONTAL_FACING) ->
                        blockState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
                    blockState.contains(FacingMachineBlock.FACING) ->
                        blockState[FacingMachineBlock.FACING]
                    else ->
                        Direction.UP
                }
            if (facing.axis.isVertical) {
                facing = playerInventory.player.horizontalFacing.opposite
            }
            val direction = offset(facing, side.direction)
            val mode = configuration[direction]!!
            val widget = WMachineSideDisplay(side, direction, mode, world, pos)
            widget.setOnClick {
                widget.mode = widget.mode.next(configurable.getValidConfigurations(type))
                configuration[direction] = widget.mode
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeBlockPos(pos)
                buf.writeInt(direction.id)
                buf.writeInt(widget.mode.ordinal)
                ClientPlayNetworking.send(ConfigureIOPackets.UPDATE_MACHINE_SIDE_PACKET_ID, buf)
            }
            machineVisualizerPanel.add(widget, (side.x) * 1.2, (side.y) * 1.2)
        }
        root.add(machineVisualizerPanel, 0.5, 0.0)
        return root
    }

    private fun offset(facing: Direction, side: Direction): Direction {
        return if (side.axis.isVertical) side
        else when (facing) {
            Direction.NORTH -> side
            Direction.SOUTH -> side.opposite
            Direction.WEST -> side.rotateYCounterclockwise()
            Direction.EAST -> side.rotateYClockwise()
            else -> side
        }
    }

    enum class MachineSide(val x: Int, val y: Int, val direction: Direction, val u1: Float, val v1: Float, val u2: Float, val v2: Float) {
        FRONT(1, 1, Direction.NORTH, 5.333f, 5.333f, 10.666f, 10.666f),
        LEFT(0, 1, Direction.EAST, 0.0f, 5.333f, 5.332f, 10.666f),
        BACK(2, 2, Direction.SOUTH, 10.667f, 10.667f, 16.0f, 16f),
        RIGHT(2, 1, Direction.WEST, 10.667f, 5.333f, 16.0f, 10.665f),
        TOP(1, 0, Direction.UP, 5.333f, 0.0f, 10.666f, 5.333f),
        BOTTOM(1, 2, Direction.DOWN, 5.333f, 10.667f, 10.666f, 15.998f)
    }

    companion object {
        val EMPTY_ITEM = SideConfiguration(ConfigurationType.ITEM)
        val EMPTY_FLUID = SideConfiguration(ConfigurationType.FLUID)
        val EMPTY_ENERGY = SideConfiguration(ConfigurationType.ENERGY)

    }
}