package me.steven.indrev.screens.machine

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainer
import me.steven.indrev.packets.common.ToggleAutoInputOutputPacket
import me.steven.indrev.packets.common.UpdateMachineIOPacket
import me.steven.indrev.screens.widgets.*
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import kotlin.math.pow

class IOTabHelper(val blockEntity: MachineBlockEntity<*>) {
    private val openIoConfigButton = WidgetButton(identifier("textures/gui/icons/open_io_screen_icon.png"))
    private var autoInputCheckbox = WidgetCheckbox()
    private var autoOutputCheckbox = WidgetCheckbox()

    private val ioButtonsWidgets = mutableListOf<Widget>()
    private val typeWidgets = mutableListOf<WidgetIOConfigMode>()

    private var config = blockEntity.inventory.sidedConfiguration
    private var selectedType = ConfigurationTypes.ITEM

    private var animationState = MachineScreenHandler.AnimationState.CLOSED
    var openIoConfigAnimationProgress = 0.0

    lateinit var ioConfig: WidgetGroup


    fun addWidgets(handler: MachineScreenHandler) {
        openIoConfigButton.tooltipBuilder = { tooltip ->
            if (openIoConfigButton.enabled) {
                if (animationState == MachineScreenHandler.AnimationState.CLOSED) tooltip.add(Text.literal("Open I/O configuration"))
                else tooltip.add(Text.literal("Close I/O configuration"))
            } else {
                tooltip.add(
                    Text.literal("Install Automated Item/Fluid Transfer Upgrade").styled { s -> s.withColor(0xFF0000) })
            }
        }
        openIoConfigButton.disabledIcon = identifier("textures/gui/icons/disabled_open_io_screen_icon.png")
        val ioWidgets = mutableListOf<Widget>()
        ioWidgets.add(WidgetSprite(identifier("textures/gui/small_gui.png"), 88, 132))
        MachineScreenHandler.MachineSide.values().forEach { side ->
            val dir = side.direction
            val button = object : WidgetButton(identifier("textures/block/electric_furnace.png")) {
                override fun draw(ctx: DrawContext, x: Int, y: Int) {
                    val (a, r, g, b) = argb(
                        when {
                            config.getMode(dir).allowInput -> INPUT_COLOR
                            config.getMode(dir).allowOutput -> OUTPUT_COLOR
                            else -> -1
                        }
                    )
                    RenderSystem.setShaderColor(r / 255f, g / 255f, b / 255f, a / 255f)
                    RenderSystem.setShaderTexture(0, icon)
                    drawTexturedQuad(
                        ctx.matrices.peek().positionMatrix,
                        x,
                        x + width,
                        y,
                        y + height,
                        0,
                        side.u1 / 16f,
                        side.u2 / 16f,
                        side.v1 / 16f,
                        side.v2 / 16f
                    )
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                }
            }
            button.click = { _, _, _ ->
                val mode = config.getMode(dir).next()
                val buf = PacketByteBufs.create()
                buf.writeBlockPos(blockEntity.pos)
                buf.writeInt(selectedType.ordinal)
                buf.writeInt(dir.id)
                buf.writeInt(mode.id)
                config.setMode(dir, mode)
                UpdateMachineIOPacket.send(buf)
            }

            ioButtonsWidgets.add(button)
            ioWidgets.add(button)
            button.x = 34 + -dir.offsetX * button.width
            button.y = 54 + -dir.offsetY * button.height
            if (side == MachineScreenHandler.MachineSide.BACK) {
                button.x += 18
                button.y += 18
            }
        }

        val types = ConfigurationTypes.values().filter { it.enabled(blockEntity) }
        types.forEachIndexed { index, type ->
            val ioConfigMode = WidgetIOConfigMode(type, index == 0, false)
            ioConfigMode.x = 44 - (types.size * 18) / 2 + index * 18
            ioConfigMode.y = 12
            ioConfigMode.click = { _, _, _ ->
                typeWidgets.forEach { s -> s.selected = s.type == type }
                config = type.provider(blockEntity)
                selectedType = type
            }
            typeWidgets.add(ioConfigMode)
            ioWidgets.add(ioConfigMode)
        }

        selectedType = types[0]
        config = selectedType.provider(blockEntity)

        openIoConfigButton.click = { _, _, _ ->
            animationState = if (animationState == MachineScreenHandler.AnimationState.CLOSED) {
                typeWidgets.forEach { it.shown = true }
                typeWidgets[0].selected = true
                MachineScreenHandler.AnimationState.OPENED
            } else {
                typeWidgets.forEach { it.shown = false }
                MachineScreenHandler.AnimationState.CLOSED
            }
        }

        autoInputCheckbox.x = 8
        autoInputCheckbox.y = 99
        autoInputCheckbox.onChange = { value ->
            config.autoInput = value
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(blockEntity.pos)
            buf.writeBoolean(value)
            buf.writeBoolean(config.autoOutput)
            buf.writeEnumConstant(selectedType)
            ToggleAutoInputOutputPacket.send(buf)
        }
        val autoInputText = WidgetText { Text.literal("Auto input").styled { s -> s.withColor(0xECECEC) } }
        autoInputText.x = 20
        autoInputText.y = 100

        autoOutputCheckbox.x = 8
        autoOutputCheckbox.y = 114
        autoOutputCheckbox.onChange = { value ->
            config.autoOutput = value
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(blockEntity.pos)
            buf.writeBoolean(config.autoInput)
            buf.writeBoolean(value)
            buf.writeEnumConstant(selectedType)
            ToggleAutoInputOutputPacket.send(buf)
        }
        val autoOutputText = WidgetText { Text.literal("Auto output").styled { s -> s.withColor(0xECECEC) } }
        autoOutputText.x = 20
        autoOutputText.y = 115


        ioWidgets.add(autoInputCheckbox)
        ioWidgets.add(autoInputText)
        ioWidgets.add(autoOutputCheckbox)
        ioWidgets.add(autoOutputText)

        ioConfig = object : WidgetGroup(ioWidgets) {
            override fun draw(ctx: DrawContext, x: Int, y: Int) {
                ctx.enableScissor(0, 0, x - this.x - 4, 1000)
                super.draw(ctx, x, y)
                ctx.disableScissor()
            }
        }
        handler.add(openIoConfigButton, -30, 0)
        handler.add(ioConfig, 18, 22)
    }

    fun tick() {
        openIoConfigButton.enabled =
            blockEntity.upgrades.contains(Upgrade.AUTOMATED_FLUID_TRANSFER) || blockEntity.upgrades.contains(Upgrade.AUTOMATED_ITEM_TRANSFER) || blockEntity is LazuliFluxContainer
        if (!openIoConfigButton.enabled) {
            animationState = MachineScreenHandler.AnimationState.CLOSED
            typeWidgets.forEach { it.shown = false }
        }
        typeWidgets.forEach { type ->
            type.enabled = type.type.canModify(blockEntity)
            if (!type.enabled && type.selected) {
                type.selected = false
                val w = typeWidgets.firstOrNull { it.enabled }
                if (w != null) {
                    w.selected = true
                    selectedType = w.type
                    config = w.type.provider(blockEntity)
                }
            }
        }
        autoInputCheckbox.checked = config.autoInput
        autoOutputCheckbox.checked = config.autoOutput

        if (animationState == MachineScreenHandler.AnimationState.OPENED && openIoConfigAnimationProgress < 1.0) {
            openIoConfigAnimationProgress += 0.1
            if (openIoConfigAnimationProgress >= 1.0) {
                openIoConfigAnimationProgress = 1.0
            }
        } else if (animationState == MachineScreenHandler.AnimationState.CLOSED && openIoConfigAnimationProgress > 0.0) {
            openIoConfigAnimationProgress -= 0.1
            if (openIoConfigAnimationProgress <= 0.0) {
                openIoConfigAnimationProgress = 0.0
            }
        } else return

        val c1 = 1.70158
        val c3 = c1 + 1
        ioConfig.x = 18 + (-118 * (1 + c3 * (openIoConfigAnimationProgress - 1).pow(3.0) + c1 * (openIoConfigAnimationProgress - 1).pow(2))).toInt()
    }

}