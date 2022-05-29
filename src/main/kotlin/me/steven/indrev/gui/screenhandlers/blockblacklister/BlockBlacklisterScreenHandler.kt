package me.steven.indrev.gui.screenhandlers.blockblacklister

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.github.cottonmc.cotton.gui.widget.data.Insets
import me.steven.indrev.packets.common.UpdateMiningDrillBlockBlacklistPacket
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.utils.literal
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import me.steven.indrev.utils.translatable
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.random.Random

class BlockBlacklisterScreenHandler : LightweightGuiDescription() {
    init {
        val panel = WGridPanel()
        this.rootPanel = panel
        panel.insets = Insets.ROOT_PANEL

        val stack = { MinecraftClient.getInstance().player!!.mainHandStack }
        val range = DrillModule.RANGE.getLevel(stack())
        if (range <= 0) {
            panel.add(WLabel(literal("Install at least one Range Module to unlock functionality.")), 0, 0)
        } else {
            for (x in -range..range) {
                for (y in -range..range) {
                    val pos = BlockPos(x, -y, 0)
                    panel.add(WToggleBlock(pos, stack), x + range, y + range)
                }
            }
        }

        val buttonFlipY = WButton(translatable("Mirror vertically"))
        buttonFlipY.onClick = Runnable {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.FLIP_Y.ordinal)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
        }
        panel.add(buttonFlipY, range * 2 + 2, 0)
        buttonFlipY.setSize(20, 20)

        val buttonFlipX = WButton(translatable("Mirror horizontally"))
        buttonFlipX.onClick = Runnable {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.FLIP_X.ordinal)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
        }
        panel.add(buttonFlipX, range * 2 + 2, 2)
        buttonFlipX.setSize(20, 20)

        val buttonRotX90 = WButton(translatable("Rotate 90º"))
        buttonRotX90.onClick = Runnable {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.ROT_X_90_CLOCKWISE.ordinal)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
        }
        panel.add(buttonRotX90, range * 2 + 2, 4)
        buttonRotX90.setSize(20, 20)

        val buttonRotX90CCW = WButton(translatable("Rotate 90º Counterclockwise"))
        buttonRotX90CCW.onClick = Runnable {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.ROT_X_90_COUNTERCLOCKWISE.ordinal)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
        }
        panel.add(buttonRotX90CCW, range * 2 + 2, 6)
        buttonRotX90CCW.setSize(20, 20)

        val buttonClear = WButton(translatable("Reset"))
        buttonClear.onClick = Runnable {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.CLEAR.ordinal)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
        }
        panel.add(buttonClear, range * 2 + 2, 8)
        buttonClear.setSize(20, 20)

        panel.validate(this)
    }

    override fun addPainters() {
        rootPanel.backgroundPainter = BackgroundPainter.createLightDarkVariants(
            BackgroundPainter.createNinePatch(Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_light.png")).setRightPadding(-35),
            BackgroundPainter.createNinePatch(Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_dark.png")).setRightPadding(-35)
        )
    }

    class WToggleBlock(private val pos: BlockPos, private val stack: () -> ItemStack) : WWidget() {

        private val isSelected: Boolean get() = !DrillModule.getBlacklistedPositions(stack()).contains(pos)
        
        private val texture = run {
            val r = Random.nextFloat()
            when {
                r < 0.0003 -> Identifier("textures/block/emerald_ore.png")
                r < 0.0008 -> Identifier("textures/block/diamond_ore.png")
                r < 0.025 -> Identifier("textures/block/gold_ore.png")
                r < 0.05 -> Identifier("textures/block/iron_ore.png")
                r < 0.1 -> Identifier("textures/block/coal_ore.png")
                r < 0.2 -> Identifier("textures/block/cobblestone.png")
                else -> Identifier("textures/block/stone.png")
            }
        }

        override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            ScreenDrawing.coloredRect(matrices, x - 1, y - 1, width + 2, height + 2, 0xFF000000.toInt())
            ScreenDrawing.texturedRect(matrices, x, y, width, height, texture, -1)
            if (!isSelected) {
                ScreenDrawing.coloredRect(matrices, x, y, width, height, 0xAA000000.toInt())
            }
        }


        override fun onClick(x: Int, y: Int, button: Int): InputResult {
            val buf = PacketByteBufs.create()
            buf.writeInt(UpdateMiningDrillBlockBlacklistPacket.Mode.SINGLE.ordinal)
            buf.writeBlockPos(pos)
            ClientPlayNetworking.send(UpdateMiningDrillBlockBlacklistPacket.UPDATE_BLACKLIST_PACKET, buf)
            return InputResult.PROCESSED
        }
    }
}