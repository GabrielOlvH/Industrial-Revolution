package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.getShortEnergyDisplay
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class EnergyWidget(private val ctx: ScreenHandlerContext) : WWidget() {
    init {
        this.setSize(16, 64)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, ENERGY_EMPTY, -1)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is MachineBlockEntity) {
                val energy = blockEntity.energy
                val maxEnergy = blockEntity.maxStoredPower
                if (energy > 0) {
                    var percent = energy.toFloat() / maxEnergy.toFloat()
                    percent = (percent * height).toInt() / height.toFloat()
                    val barSize = (height * percent).toInt()
                    if (barSize > 0)
                        ScreenDrawing.texturedRect(
                            x, y + getHeight() - barSize, width, barSize,
                            ENERGY_FULL, 0f, 1 - percent, 1f, 1f, -1
                        )
                }
            }
        }
    }

    override fun addTooltip(information: MutableList<StringRenderable>?) {
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is MachineBlockEntity) {
                val energy = getShortEnergyDisplay(blockEntity.energy)
                val maxEnergy = getShortEnergyDisplay(blockEntity.maxStoredPower)
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
        private val ENERGY_FULL =
            identifier("textures/gui/widget_energy_full.png")
    }
}