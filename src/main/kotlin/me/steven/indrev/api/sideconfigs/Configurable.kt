package me.steven.indrev.api.sideconfigs

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.netty.buffer.Unpooled
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.gui.widgets.machines.WMachineSideDisplay
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.function.Consumer

interface Configurable {
    fun isConfigurable(type: ConfigurationType): Boolean
    fun isFixed(type: ConfigurationType): Boolean
    fun getValidConfigurations(type: ConfigurationType): Array<TransferMode>
    fun getCurrentConfiguration(type: ConfigurationType): SideConfiguration
    fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>)

    fun getWrenchConfigurationPanel(world: World, pos: BlockPos, playerInventory: PlayerInventory, type: ConfigurationType): WWidget? {
        val root = WGridPanel()
        root.setSize(100, 128)

        val configuration = getCurrentConfiguration(type)

        if (configuration.type == ConfigurationType.ITEM) {
            val autoPushBtn = WToggleButton(TranslatableText("item.indrev.wrench.autopush"))
            autoPushBtn.toggle = configuration.autoPush
            autoPushBtn.onToggle = Consumer { v ->
                configuration.autoPush = v
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeByte(0)
                buf.writeBlockPos(pos)
                buf.writeBoolean(v)
                ClientPlayNetworking.send(UPDATE_AUTO_OPERATION_PACKET_ID, buf)
            }
            root.add(autoPushBtn, 0, 4)
            val autoPullBtn = WToggleButton(TranslatableText("item.indrev.wrench.autopull"))
            autoPullBtn.toggle = configuration.autoPull
            autoPullBtn.onToggle = Consumer { v ->
                configuration.autoPull = v
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeByte(1)
                buf.writeBlockPos(pos)
                buf.writeBoolean(v)
                ClientPlayNetworking.send(UPDATE_AUTO_OPERATION_PACKET_ID, buf)
            }
            root.add(autoPullBtn, 0, 5)
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
                widget.mode = widget.mode.next(getValidConfigurations(type))
                configuration[direction] = widget.mode
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeEnumConstant(type)
                buf.writeBlockPos(pos)
                buf.writeInt(direction.id)
                buf.writeInt(widget.mode.ordinal)
                ClientPlayNetworking.send(UPDATE_MACHINE_SIDE_PACKET_ID, buf)
            }
            machineVisualizerPanel.add(widget, (side.x) * 1.2, (side.y) * 1.2)
        }
        root.add(machineVisualizerPanel, 1.0, 0.5)
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
        val UPDATE_MACHINE_SIDE_PACKET_ID = identifier("update_machine_side")
        val UPDATE_AUTO_OPERATION_PACKET_ID = identifier("update_auto_pull_push")
    }

}