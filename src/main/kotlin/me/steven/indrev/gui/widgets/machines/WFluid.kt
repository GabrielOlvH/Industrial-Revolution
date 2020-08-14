package me.steven.indrev.gui.widgets.machines

import alexiil.mc.lib.attributes.fluid.render.DefaultFluidVolumeRenderer
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

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
                        fluid.getInvFluid(0)
                        val fluidRenderFace =
                            FluidRenderFace.createFlatFaceZ(
                                x.toDouble(),
                                y.toDouble() + height - barSize,
                                .0,
                                x.toDouble() + width,
                                y.toDouble() + height,
                                .0,
                                1.0,
                                true,
                                true
                            )
                        DefaultFluidVolumeRenderer.INSTANCE.render(
                            blockEntity.fluidComponent!!.volume,
                            listOf(fluidRenderFace),
                            FluidVolumeRenderer.VCPS,
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