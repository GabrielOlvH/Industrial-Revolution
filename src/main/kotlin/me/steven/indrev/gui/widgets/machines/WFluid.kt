package me.steven.indrev.gui.widgets.machines

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.netty.buffer.Unpooled
import me.steven.indrev.packets.common.FluidGuiHandInteractionPacket
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.util.registry.Registry

class WFluid(val ctx: ScreenHandlerContext, val delegate: PropertyDelegate, val tankId: Int, val tankSizeId: Int, val amountId: Int, val fluidId: Int) : WWidget() {
    init {
        this.setSize(16, 43)
    }

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(matrices, x, y, width, height, TANK_BOTTOM, -1)

        val fluid = Registry.FLUID.get(delegate[fluidId])
        val amount = delegate[amountId]
        val maxAmount = delegate[tankSizeId]
        if (amount > 0) {
            var percent = amount.toFloat() / maxAmount.toFloat()
            percent = (percent * height).toInt() / height.toFloat()
            val barSize = (height * percent).toInt()
            if (barSize > 0) {
                val offset = 1.0
                val vol = FluidKeys.get(fluid).withAmount(FluidAmount.of(amount.toLong(), 1000))
                vol.renderGuiRect(
                    x + offset,
                    y.toDouble() + height - barSize + offset,
                    x.toDouble() + width - offset,
                    y.toDouble() + height - offset
                )
            }
        }
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        ScreenDrawing.texturedRect(matrices, x, y, width, height, TANK_TOP, -1)
    }

    override fun addTooltip(information: TooltipBuilder?) {
        val energy = delegate[amountId]
        val maxEnergy = delegate[tankSizeId]
        val fluid = Registry.FLUID.get(delegate[fluidId])
        information?.add(*FluidKeys.get(fluid).fullTooltip.toTypedArray())
        information?.add(LiteralText("$energy / $maxEnergy mB"))
    }

    override fun onClick(x: Int, y: Int, button: Int): InputResult {
        super.onClick(x, y, button)
        val packet = PacketByteBuf(Unpooled.buffer())
        ctx.run { _, pos -> packet.writeBlockPos(pos) }
        packet.writeInt(tankId)
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