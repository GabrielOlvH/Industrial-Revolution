package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3f
import kotlin.math.atan2

class WKnob(var angle: Float = 30.0f, val pos: BlockPos) : WWidget() {

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        matrices?.run {
            push()
            translate(x.toDouble() + width / 2.0, y.toDouble() + width / 2.0, 0.0)
            multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle))
            translate(-x.toDouble() - width / 2.0, -y.toDouble() - width / 2.0, 0.0)
            ScreenDrawing.texturedRect(matrices, x, y, width, height, KNOB_TEXTURE_ID, -1)
            pop()
        }

        val textRenderer = MinecraftClient.getInstance().textRenderer
        val text = String.format("%.1f", ((angle - 30) / 300f) * 100) + "%"
        ScreenDrawing.drawString(
            matrices,
            text,
            (x - textRenderer.getWidth(text) / 2) + width / 2,
            (y - textRenderer.fontHeight / 2) + height / 2,
            -1
        )
    }

    private fun calculateAngle(x: Float, y: Float): Float {
        val px = (x / width.toFloat()) - 0.5
        val py = (1 - y / height.toFloat()) - 0.5
        var angle = -(Math.toDegrees(atan2(py, px))).toFloat() + 180
        if (angle > 360) angle -= 360
        return angle.coerceIn(30f, 330f)
    }

    override fun onMouseDrag(x: Int, y: Int, button: Int, deltaX: Double, deltaY: Double): InputResult {
        angle = calculateAngle(x.toFloat(), y.toFloat())
        return InputResult.PROCESSED
    }

    override fun onMouseUp(x: Int, y: Int, button: Int): InputResult {
        val buf = PacketByteBufs.create()

        buf.writeBlockPos(pos)
        buf.writeFloat((angle - 30) / 300f)
        ClientPlayNetworking.send(UPDATE_EFFICIENCY_PACKET, buf)
        return InputResult.PROCESSED
    }

    companion object {
        val KNOB_TEXTURE_ID = identifier("textures/gui/knob.png")
        val UPDATE_EFFICIENCY_PACKET = identifier("update_steam_turbine_efficiency")
    }
}