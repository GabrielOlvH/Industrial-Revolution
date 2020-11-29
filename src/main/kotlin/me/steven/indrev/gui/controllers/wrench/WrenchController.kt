package me.steven.indrev.gui.controllers.wrench

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.machines.WMachineSideDisplay
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.WrenchConfigurationType
import me.steven.indrev.utils.add
import me.steven.indrev.utils.addBookEntryShortcut
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class WrenchController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.WRENCH_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {

    private lateinit var currentType: WrenchConfigurationType
    private val displays = mutableMapOf<Direction, WMachineSideDisplay>()

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(96, 120)

        val titleText = WText(TranslatableText("item.indrev.wrench.title"), HorizontalAlignment.LEFT, 0x404040)
        root.add(titleText, 0.0, 0.2)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
            val blockState = world.getBlockState(pos)

            val availableTypes = WrenchConfigurationType.getTypes(blockEntity)
            currentType = availableTypes.first()
            val configTypeButton = WButton(currentType.title)
            configTypeButton.setOnClick {
                currentType = currentType.next(availableTypes)
                updateMachineDisplays(blockEntity)
                configTypeButton.label = currentType.title
            }
            if (availableTypes.size > 1)
                root.add(configTypeButton, 1.3, 0.9)
            configTypeButton.setSize(45, 20)

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
                val mode = currentType.getConfig(blockEntity)[direction]!!
                val widget = WMachineSideDisplay(side, direction, mode)
                widget.setOnClick {
                    widget.mode = widget.mode.next(currentType.validModes)
                    currentType.getConfig(blockEntity)[direction] = widget.mode
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeEnumConstant(currentType)
                    buf.writeBlockPos(pos)
                    buf.writeInt(direction.id)
                    buf.writeInt(widget.mode.ordinal)
                    ClientSidePacketRegistry.INSTANCE.sendToServer(SAVE_PACKET_ID, buf)
                }
                displays[direction] = widget
                root.add(widget, (side.x - 0.3) * 1.2, (side.y + 1.0) * 1.2)
            }
            addBookEntryShortcut(playerInventory, root, -1.4, -0.47)
        }
        root.validate(this)
    }

    private fun updateMachineDisplays(blockEntity: MachineBlockEntity<*>) {
        val config = currentType.getConfig(blockEntity)
        displays.forEach { (direction, display) ->
            display.mode = config[direction]!!
        }
    }

    override fun getEntry(): Identifier = identifier("tools/wrench")

    override fun getPage(): Int = 0

    override fun addPainters() {
        super.addPainters()
        rootPanel.backgroundPainter = BackgroundPainter.VANILLA
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
        FRONT(2, 2, Direction.NORTH, 5.333f, 5.333f, 10.666f, 10.666f),
        LEFT(1, 2, Direction.EAST, 0.0f, 5.333f, 5.332f, 10.666f),
        BACK(3, 3, Direction.SOUTH, 10.667f, 10.667f, 16.0f, 16f),
        RIGHT(3, 2, Direction.WEST, 10.667f, 5.333f, 16.0f, 10.665f),
        TOP(2, 1, Direction.UP, 5.333f, 0.0f, 10.666f, 5.333f),
        BOTTOM(2, 3, Direction.DOWN, 5.333f, 10.667f, 10.666f, 15.998f)
    }

    companion object {
        val SCREEN_ID = identifier("wrench_item_io_screen")
        val SAVE_PACKET_ID = identifier("save_packet_id")
    }
}