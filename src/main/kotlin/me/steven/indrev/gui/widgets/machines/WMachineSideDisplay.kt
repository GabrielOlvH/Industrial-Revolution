package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WButton
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.utils.draw2Colors
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*

class WMachineSideDisplay(
    private val side: SideConfiguration.MachineSide,
    private val direction: Direction,
    var mode: TransferMode,
    private val world: World,
    private val blockPos: BlockPos
) : WButton() {
    init {
        this.setSize(16, 16)
    }

    override fun setSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(matrices, x, y, width, height, TEXTURE_ID, side.u1 / 16f, side.v1 / 16f, side.u2 / 16f, side.v2 / 16f, -1)
        if (mode == TransferMode.INPUT_OUTPUT)
            draw2Colors(matrices, x, y, x + width, y + height, TransferMode.INPUT.rgb, TransferMode.OUTPUT.rgb)
        else if (mode != TransferMode.NONE)
            DrawableHelper.fill(matrices, x, y, x + width, y + height, mode.rgb.toInt())
        if (isWithinBounds(mouseX, mouseY))
            DrawableHelper.fill(matrices, x, y, x + width, y + height, -2130706433)
    }

    override fun addTooltip(tooltip: TooltipBuilder?) {
        val modeText = translatable("item.indrev.wrench.mode",
            translatable("item.indrev.wrench.${mode.toString().lowercase(Locale.getDefault())}").formatted(Formatting.WHITE)
        ).formatted(Formatting.BLUE)
        val side = translatable("item.indrev.wrench.side.${side.toString().lowercase(Locale.getDefault())}")
            .append(literal(" (")
                .append(translatable("item.indrev.wrench.side.${direction.toString().lowercase(Locale.getDefault())}"))
                .append(literal(")"))).formatted(Formatting.WHITE)
        tooltip?.add(modeText, side)
        val blockState = world.getBlockState(blockPos.offset(direction))
        if (!blockState.isAir) {
            val neighbor = translatable("item.indrev.wrench.connected", blockState.block.name)
            tooltip?.add(neighbor)
        }
    }

    companion object {
        val TEXTURE_ID = identifier("textures/block/machine_block.png")
    }
}