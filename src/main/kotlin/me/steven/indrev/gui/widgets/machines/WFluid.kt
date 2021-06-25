package me.steven.indrev.gui.widgets.machines

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.netty.buffer.Unpooled
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.packets.common.FluidGuiHandInteractionPacket
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText

class WFluid(private val ctx: ScreenHandlerContext, val tank: Int) : WWidget() {
    init {
        this.setSize(16, 43)
    }

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(matrices, x, y, width, height, TANK_BOTTOM, -1)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) ?: return@run
            val fluid = ComponentKey.FLUID.get(blockEntity) ?: return@run
            val energy = fluid.tanks[tank].amount().asInexactDouble() * 1000
            val maxEnergy = fluid.limit.asInexactDouble() * 1000
            if (energy > 0) {
                var percent = energy.toFloat() / maxEnergy.toFloat()
                percent = (percent * height).toInt() / height.toFloat()
                val barSize = (height * percent).toInt()
                if (barSize > 0) {
                    val offset = 1.0
                    fluid.tanks[tank].renderGuiRect(
                        x + offset,
                        y.toDouble() + height - barSize + offset,
                        x.toDouble() + width - offset,
                        y.toDouble() + height - offset
                    )
                }
            }
        }
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        ScreenDrawing.texturedRect(matrices, x, y, width, height, TANK_TOP,-1)
    }

    override fun addTooltip(information: TooltipBuilder?) {
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) ?: return@run
            val fluid = ComponentKey.FLUID.get(blockEntity) ?: return@run
            val tank = fluid.tanks[tank]
            val energy = tank.amount_F.asInt(1000)
            val maxEnergy = fluid.limit.asInt(1000)
            information?.add(*tank.fluidKey.fullTooltip.toTypedArray())
            information?.add(LiteralText("$energy / $maxEnergy mB"))
            super.addTooltip(information)
        }
    }

    override fun onClick(x: Int, y: Int, button: Int): InputResult {
        super.onClick(x, y, button)
        val packet = PacketByteBuf(Unpooled.buffer())
        ctx.run { _, pos -> packet.writeBlockPos(pos) }
        packet.writeInt(tank)
        ClientPlayNetworking.send(FluidGuiHandInteractionPacket.FLUID_CLICK_PACKET, packet)
        return InputResult.PROCESSED
    }

    override fun canResize(): Boolean = false

    companion object {
        val TANK_BOTTOM =
            identifier("textures/gui/tank_bottom.png")
        val TANK_TOP =
            identifier("textures/gui/tank_top.png")
    }
}