package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText

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
                val energy = fluid.volume.amount().asInexactDouble() * 1000
                val maxEnergy = fluid.limit.asInexactDouble() * 1000
                if (energy > 0) {
                    var percent = energy.toFloat() / maxEnergy.toFloat()
                    percent = (percent * height).toInt() / height.toFloat()
                    val barSize = (height * percent).toInt()
                    if (barSize > 0) {
                        val offset = 2.0
                        blockEntity.fluidComponent!!.volume.renderGuiRect(
                            x + offset,
                            y.toDouble() + height - barSize + offset,
                            x.toDouble() + width - offset,
                            y.toDouble() + height - offset
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
                val energy = (fluid.volume.amount_F.asInexactDouble() * 1000).toInt()
                val maxEnergy = (fluid.limit.asInexactDouble() * 1000).toInt()
                information?.add(*fluid.volume.fluidKey.fullTooltip.toTypedArray())
                information?.add(LiteralText("$energy / $maxEnergy mB"))
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