package me.steven.indrev.gui.widgets.machines

import alexiil.mc.lib.attributes.fluid.render.DefaultFluidVolumeRenderer
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

class WFluid(private val ctx: ScreenHandlerContext) : WWidget() {
    init {
        this.setSize(16, 64)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, ENERGY_EMPTY, -1)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is MachineBlockEntity) {
                val fluid = blockEntity.fluidComponent ?: return@run
                val energy = fluid.volume.amount().whole
                val maxEnergy = fluid.limit.whole
                if (energy > 0) {
                    var percent = energy.toFloat() / maxEnergy.toFloat()
                    percent = (percent * height).toInt() / height.toFloat()
                    val barSize = (height * percent).toInt()
                    if (barSize > 0) {
                        val fluidRenderFace =
                            FluidRenderFace.createFlatFace(
                                x.toDouble(),
                                y.toDouble() + height - barSize,
                                .0,
                                x.toDouble() + width,
                                y.toDouble() + height,
                                .0,
                                1.0,
                                Direction.DOWN,
                                true
                            )
                        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
                        DefaultFluidVolumeRenderer.INSTANCE.render(
                            blockEntity.fluidComponent!!.volume,
                            listOf(fluidRenderFace),
                            immediate,
                            matrices
                        )
                    }
                }
            }
        }
    }

    override fun addTooltip(information: TooltipBuilder?) {
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is MachineBlockEntity) {
                val fluid = blockEntity.fluidComponent ?: return@run
                val energy = fluid.volume.amount_F.whole
                val maxEnergy = fluid.limit.whole
                information?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
                information?.add(LiteralText("$energy / $maxEnergy LF"))
                super.addTooltip(information)
            }
        }
    }

    override fun canResize(): Boolean = false

    companion object {
        private val ENERGY_EMPTY =
            identifier("textures/gui/widget_energy_empty.png")
    }
}