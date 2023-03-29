package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.github.cottonmc.cotton.gui.widget.data.Texture
import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.packets.common.FluidGuiHandInteractionPacket
import me.steven.indrev.utils.IRFluidTank
import me.steven.indrev.utils.getEnergyString
import me.steven.indrev.utils.getTooltip
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.PacketByteBuf
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper

open class WCustomBar(val bg: Texture, val bar: Texture, val value: () -> Int, val max: () -> Int, val direction: Direction) : WWidget() {

    override fun canResize(): Boolean = false

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(matrices, x, y, getWidth(), getHeight(), bg, -0x1)

        val maxVal: Int = max()
        var percent: Float = (value() / maxVal.toFloat()).coerceIn(0f, 1f)

        var barMax = getWidth()
        if (direction == Direction.DOWN || direction == Direction.UP) barMax = getHeight()
        percent = (percent * barMax).toInt() / barMax.toFloat() //Quantize to bar size


        val barSize = (barMax * percent).toInt()
        if (barSize <= 0) return

        when (direction) {
            Direction.UP -> {
                val left = x;
                var top = y + getHeight();
                top -= barSize;
                drawBar(matrices, left, top, getWidth(), barSize, bar.u1(), MathHelper.lerp(percent, bar.v2(), bar.v1()), bar.u2(), bar.v2());
            }
            Direction.RIGHT -> {
                drawBar(matrices, x, y, barSize, getHeight(),  bar.u1(), bar.v1(), MathHelper.lerp(percent, bar.u1(), bar.u2()), bar.v2())
            }
            Direction.DOWN -> {
                drawBar(matrices, x, y, getWidth(), barSize,bar.u1(), bar.v1(), bar.u2(), MathHelper.lerp(percent, bar.v1(), bar.v2()))
            }
            Direction.LEFT -> {
                var left = x + getWidth()
                left -= barSize
                drawBar(matrices, left, y, barSize, getHeight(), MathHelper.lerp(percent, bar.u2(), bar.u1()), bar.v1(), bar.u2(), bar.v2())
            }
        }
    }

    open fun drawBar(matrices: MatrixStack, left: Int, top: Int, width: Int, height: Int,  u1: Float, v1: Float, u2: Float, v2: Float) {
        ScreenDrawing.texturedRect(matrices, left, top, width, height, bar.image(), u1, v1, u2, v2, -1);
    }

    enum class Direction {
        UP, RIGHT, DOWN, LEFT
    }
}
private val LIT_TEXTURE_ID = Texture(identifier("textures/gui/widget_fuel_burning.png"))
private val UNLIT_TEXTURE_ID = Texture(identifier("textures/gui/widget_fuel_not_burning.png"))

fun fuelBar(blockEntity: BaseBlockEntity, valueIndex: Int = 4, maxIndex: Int = 5): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val fuel = WCustomBar(UNLIT_TEXTURE_ID, LIT_TEXTURE_ID, { properties[valueIndex] }, { properties[maxIndex] }, WCustomBar.Direction.UP)
    fuel.setSize(14, 14)
    return fuel
}

val TANK_BOTTOM = Texture(identifier("textures/gui/tank_bottom.png"))
val TANK_TOP = Texture(identifier("textures/gui/tank_top.png"))

fun fluidTank(blockEntity: BaseBlockEntity, index: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val tank = object : WCustomBar(TANK_BOTTOM, TANK_TOP, { 1 }, { 1 }, Direction.UP) {
        override fun drawBar(matrices: MatrixStack, left: Int, top: Int, width: Int, height: Int, u1: Float, v1: Float, u2: Float, v2: Float) {

            val maxVal: Long = properties.get<IRFluidTank>(index).capacity
            var percent: Float = (properties.get<IRFluidTank>(index).amount / maxVal.toFloat()).coerceIn(0f, 1f)

            val barMax = getHeight()
            percent = (percent * barMax).toInt() / barMax.toFloat() //Quantize to bar size


            val barSize = (barMax * percent).toInt()
            if (barSize > 0) {
                val tank = properties.get<IRFluidTank>(index)
                tank
                    .renderGuiRect(
                        matrices,
                        left + 1,
                        top + height - 1,
                        width - 2,
                        height - 2
                    )
            }
            ScreenDrawing.texturedRect(matrices, left, top, this.width, this.height, TANK_TOP, -1)
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            val tank = properties.get<IRFluidTank>(index)
            if (!tank.isEmpty)
                tooltip?.add(*getTooltip(tank.variant, tank.amount, tank.capacity).toTypedArray())
        }

        override fun onClick(x: Int, y: Int, button: Int): InputResult {
            val packet = PacketByteBuf(Unpooled.buffer())
            packet.writeInt(properties.get<IRFluidTank>(index).index)
            ClientPlayNetworking.send(FluidGuiHandInteractionPacket.FLUID_CLICK_PACKET, packet)
            return InputResult.PROCESSED
        }
    }
    tank.setSize(16, 43)
    return tank
}

val ENERGY_EMPTY = Texture(identifier("textures/gui/widget_energy_empty.png"))
val ENERGY_FULL = Texture(identifier("textures/gui/widget_energy_full.png"))

fun energyBar(blockEntity: BaseBlockEntity): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val energy = object : WCustomBar(ENERGY_EMPTY, ENERGY_FULL, { properties.get<Double>(0).toInt() }, { properties.get<Double>(1).toInt() }, Direction.UP) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            val energy = getEnergyString(properties[0])
            val maxEnergy = getEnergyString(properties[1])
            tooltip?.add(translatable("gui.widget.energy").formatted(Formatting.BLUE))
            tooltip?.add(literal("$energy / $maxEnergy LF"))
        }
    }
    energy.setSize(10, 64)
    return energy
}

private val EMPTY_HEAT = Texture(identifier("textures/gui/widget_temperature_empty.png"))
private val FULL_HEAT = Texture(identifier("textures/gui/widget_temperature_full.png"))

fun temperatureBar(blockEntity: BaseBlockEntity): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val temp = object : WCustomBar(EMPTY_HEAT, FULL_HEAT, { properties.get<Double>(2).toInt() }, { properties.get<Double>(3).toInt() }, Direction.UP) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            val temperature = properties.get<Double>(2).toInt()
            val maxTemperature = properties.get<Double>(3).toInt()

            //TODO
            /*
            val info = when {
                temperature > temperatureComponent.optimalRange.last ->
                    translatable("gui.widget.temperature_info.high").formatted(Formatting.DARK_RED, Formatting.ITALIC)
                temperature in temperatureComponent.optimalRange ->
                    translatable("gui.widget.temperature_info.medium").formatted(Formatting.YELLOW, Formatting.ITALIC)
                else ->
                    translatable("gui.widget.temperature_info.low").formatted(Formatting.GREEN, Formatting.ITALIC)
            }
            tooltip?.add(translatable("gui.widget.temperature").formatted(Formatting.BLUE))*/
            tooltip?.add(literal("$temperature / $maxTemperature °C"))
            //tooltip?.add(info)
        }
    }
    temp.setSize(10, 43)
    return temp
}

val RIGHT_PROCESS_EMPTY = Texture(identifier("textures/gui/widget_processing_empty.png"))
val RIGHT_PROCESS_FULL = Texture(identifier("textures/gui/widget_processing_full.png"))

val LEFT_PROCESS_EMPTY = Texture(identifier("textures/gui/widget_processing_empty_left.png"))
val LEFT_PROCESS_FULL = Texture(identifier("textures/gui/widget_processing_full_left.png"))

val UP_PROCESS_EMPTY = Texture(identifier("textures/gui/widget_processing_empty_vertical.png"))
val UP_PROCESS_FULL = Texture(identifier("textures/gui/widget_processing_full_vertical.png"))

fun processBar(blockEntity: BaseBlockEntity, index: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val process = object : WCustomBar(RIGHT_PROCESS_EMPTY, RIGHT_PROCESS_FULL, { properties.get<CraftingComponent<*>>(index).processTime }, { properties.get<CraftingComponent<*>>(index).totalProcessTime }, Direction.RIGHT) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            val progress = properties.get<CraftingComponent<*>>(index).processTime
            val max = properties.get<CraftingComponent<*>>(index).totalProcessTime
            if (max <= 0) return
            val percentage = progress * 100 / max
            tooltip?.add(translatable("gui.widget.process", percentage).append(literal("%")))
        }
    }
    return process
}

fun processBar(blockEntity: BaseBlockEntity, progress: Int, max: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val process = object : WCustomBar(RIGHT_PROCESS_EMPTY, RIGHT_PROCESS_FULL,  { properties[progress] }, { properties[max] }, Direction.RIGHT) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            if (properties.get<Int>(max) <= 0) return
            val percentage = properties.get<Int>(progress) * 100 / properties.get<Int>(max)
            tooltip?.add(translatable("gui.widget.process", percentage).append(literal("%")))
        }
    }
    return process
}

fun upProcessBar(blockEntity: BaseBlockEntity, index: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val process = object : WCustomBar(UP_PROCESS_EMPTY, UP_PROCESS_FULL, { properties.get<CraftingComponent<*>>(index).processTime }, { properties.get<CraftingComponent<*>>(index).totalProcessTime }, Direction.DOWN) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            val progress = properties.get<CraftingComponent<*>>(index).processTime
            val max = properties.get<CraftingComponent<*>>(index).totalProcessTime
            if (max <= 0) return
            val percentage = progress * 100 / max
            tooltip?.add(translatable("gui.widget.process", percentage).append(literal("%")))
        }
    }
    return process

}

fun upProcessBar(blockEntity: BaseBlockEntity, progress: Int, max: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val process = object : WCustomBar(UP_PROCESS_EMPTY, UP_PROCESS_FULL, { properties[progress] }, { properties[max] }, Direction.DOWN) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            if (properties.get<Int>(max) <= 0) return
            val percentage = properties.get<Int>(progress) * 100 / properties.get<Int>(max)
            tooltip?.add(translatable("gui.widget.process", percentage).append(literal("%")))
        }
    }
    return process

}

fun leftProcessBar(blockEntity: BaseBlockEntity, index: Int): WCustomBar {
    val properties = blockEntity.guiSyncableComponent ?: error("$blockEntity does not provide gui_syncable component")
    val process = object : WCustomBar(LEFT_PROCESS_EMPTY, LEFT_PROCESS_FULL, { properties.get<CraftingComponent<*>>(index).processTime }, { properties.get<CraftingComponent<*>>(index).totalProcessTime }, Direction.LEFT) {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            val progress = properties.get<CraftingComponent<*>>(index).processTime
            val max = properties.get<CraftingComponent<*>>(index).totalProcessTime
            if (max <= 0) return
            val percentage = progress * 100 / max
            tooltip?.add(translatable("gui.widget.process", percentage).append(literal("%")))
        }
    }
    return process
}