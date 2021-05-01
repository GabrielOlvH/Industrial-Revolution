package me.steven.indrev.utils

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.LFCConfig
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.options.KeyBinding
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

val UPGRADE_SLOT_PANEL_PAINTER: BackgroundPainter = BackgroundPainter.createLightDarkVariants(
    BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png"), 4),
    BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png"), 4)
)

fun draw2Colors(matrices: MatrixStack, x1: Int, y1: Int, x2: Int, y2: Int, color1: Long, color2: Long) {
    val matrix = matrices.peek().model

    var j: Int
    var xx1 = x1.toFloat()
    var xx2 = x2.toFloat()
    var yy1 = x1.toFloat()
    var yy2 = x2.toFloat()

    if (x1 < x2) {
        j = x1
        xx1 = x2.toFloat()
        xx2 = j.toFloat()
    }

    if (y1 < y2) {
        j = y1
        yy1 = y2.toFloat()
        yy2 = j.toFloat()
    }

    val f1 = (color1 shr 24 and 255) / 255.0f
    val g1 = (color1 shr 16 and 255) / 255.0f
    val h1 = (color1 shr 8 and 255) / 255.0f
    val k1 = (color1 and 255) / 255.0f

    val f2 = (color2 shr 24 and 255) / 255.0f
    val g2 = (color2 shr 16 and 255) / 255.0f
    val h2 = (color2 shr 8 and 255) / 255.0f
    val k2 = (color2 and 255) / 255.0f

    RenderSystem.enableBlend()
    RenderSystem.disableTexture()
    RenderSystem.defaultBlendFunc()
    Tessellator.getInstance().buffer.run {
        begin(7, VertexFormats.POSITION_COLOR)
        vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).next()
        vertex(matrix, xx1, yy2, 0.0f).color(g1, h1, k1, f1).next()
        vertex(matrix, xx2, yy2, 0.0f).color(g1, h1, k1, f1).next()
        vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).next()
        end()
        BufferRenderer.draw(this)
        begin(7, VertexFormats.POSITION_COLOR)
        vertex(matrix, xx1, yy1, 0.0f).color(g2, h2, k2, f2).next()
        vertex(matrix, xx2, yy2, 0.0f).color(g2, h2, k2, f2).next()
        vertex(matrix, xx2, yy1, 0.0f).color(g2, h2, k2, f2).next()
        vertex(matrix, xx1, yy1, 0.0f).color(g2, h2, k2, f2).next()
        end()
        BufferRenderer.draw(this)
    }
    RenderSystem.enableTexture()
    RenderSystem.disableBlend()
}

fun buildEnergyTooltip(stack: ItemStack?, tooltip: MutableList<Text>?) {
    val handler = energyOf(stack) ?: return
    if (handler.energy > 0) {
        val percentage = handler.energy * 100 / handler.energyCapacity
        tooltip?.add(LiteralText("${getEnergyString(handler.energy)} LF (${percentage.toInt()}%)").formatted(Formatting.GRAY))
    }
}

fun buildMachineTooltip(config: Any, tooltip: MutableList<Text>?) {
    if (Screen.hasShiftDown()) {
        when (config) {
            is HeatMachineConfig -> {
                tooltip?.add(configText("maxInput", "lftick", config.maxInput))
                tooltip?.add(configText("maxEnergyStored", "lf", getEnergyString(config.maxEnergyStored)))
                tooltip?.add(configText("energyCost", "lftick", config.energyCost))
                val speed = config.processSpeed * 100
                if (speed >= 1000)
                    tooltip?.add(configText("processSpeed", "seconds", config.processSpeed / 20))
                else
                    tooltip?.add(configText("processSpeed", "${speed.toInt()}%"))
                tooltip?.add(configText("temperatureBoost", "${config.processTemperatureBoost * config.processSpeed * 100}%"))
            }
            is BasicMachineConfig -> {
                tooltip?.add(configText("maxInput", "lftick", config.maxInput))
                tooltip?.add(configText("maxEnergyStored", "lf", getEnergyString(config.maxEnergyStored)))
                tooltip?.add(configText("energyCost", "lftick", config.energyCost))
                val speed = config.processSpeed * 100
                if (speed >= 1000)
                    tooltip?.add(configText("processSpeed", "seconds", config.processSpeed / 20))
                else
                    tooltip?.add(configText("processSpeed", "${speed.toInt()}%"))
            }
            is GeneratorConfig -> {
                tooltip?.add(configText("maxOutput", "lftick", config.maxOutput))
                tooltip?.add(configText("maxEnergyStored", "lf", getEnergyString(config.maxEnergyStored)))
                tooltip?.add(configText("ratio", "lftick", config.ratio))
                if (config.temperatureBoost > 0)
                    tooltip?.add(configText("temperatureBoost", "lftick", config.temperatureBoost * config.ratio))
            }
            is LFCConfig -> {
                tooltip?.add(configText("maxInput", "lftick", config.maxInput))
                tooltip?.add(configText("maxOutput", "lftick", config.maxOutput))
                tooltip?.add(configText("maxEnergyStored", "lf", getEnergyString(config.maxEnergyStored)))
            }
        }
    } else {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.press_shift", LiteralText("").append(KeyBinding.getLocalizedName("key.keyboard.left.shift").get()).formatted(Formatting.AQUA)).formatted(Formatting.GRAY)
        )
    }
}

private fun configText(key: String, value: String): Text {
    return TranslatableText("gui.indrev.tooltip.$key").formatted(Formatting.AQUA)
        .append(LiteralText(value).formatted(Formatting.GRAY))
}

private fun configText(key: String, unit: String, value: Any): Text {
    return TranslatableText("gui.indrev.tooltip.$key").formatted(Formatting.AQUA)
        .append(TranslatableText("gui.indrev.tooltip.$unit", value).formatted(Formatting.GRAY))
}

fun getEnergyString(energy: Double): String =
    when {
        energy >= 1000000000000 -> "${"%.1f".format(energy / 1000000000000)}T"
        energy >= 1000000000 -> "${"%.1f".format(energy / 1000000000)}B"
        energy >= 1000000 -> "${"%.1f".format(energy / 1000000)}M"
        energy >= 1000 -> "${"%.1f".format(energy / 1000)}k"
        else -> "%.1f".format(energy)
    }