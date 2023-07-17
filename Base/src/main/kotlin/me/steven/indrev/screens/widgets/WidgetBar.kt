package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.blockentities.crafting.CraftingMachineBlockEntity
import me.steven.indrev.components.MachineFluidInventory
import me.steven.indrev.packets.common.ClickFluidWidgetPacket
import me.steven.indrev.recipes.MachineRecipe
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

open class WidgetBar(
    val background: Identifier,
    val bar: Identifier,
    override var width: Int,
    override var height: Int,
    val direction: Direction,
    val current: () -> Long,
    val max: () -> Long
    ) : Widget() {
        var prev = 0f
    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        RenderSystem.enableBlend()

        ctx.drawTexture(background, x, y, 0f, 0f, width, height, width, height)

        drawBar(ctx, x, y)

    }

    open fun drawBar(ctx: DrawContext, x: Int, y: Int) {
        if (current() <= 0) return
        val value = MathHelper.lerp(MinecraftClient.getInstance().tickDelta, prev, (current() / max().toFloat()).coerceIn(0f, 1f))
        prev = value
        when (direction) {
            Direction.UP -> {
                val p = ((value * height) / height.toFloat())
                val size = (height * p).toInt()
                ctx.drawTexture(bar, x, y + height - size, width, size, 0f, height - size.toFloat(), width, size, width, height)
            }

            Direction.DOWN -> TODO()
            Direction.LEFT -> TODO()
            Direction.RIGHT -> {
                val p = ((value * width) / width.toFloat())
                val size = (width * p).toInt()
                ctx.drawTexture(bar, x, y, width - size, height, 0f, 0f, width - size, height, width, height)
            }
        }
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    companion object {
        fun energyBar(current: () -> Long, max: () -> Long): WidgetBar {
            return WidgetBar(
                identifier("textures/gui/widgets/widget_energy_empty.png"),
                identifier("textures/gui/widgets/widget_energy_full.png"),
                21,
                75,
                Direction.UP,
                current,
                max
            ).setTooltip { tooltip ->
                tooltip.add(Text.literal("Energy").styled { s -> s.withColor(0x3c00f0) })
                tooltip.add(Text.literal("${current()} / ${max()} LF"))
            } as WidgetBar
        }

        fun temperatureBar(
            current: () -> Long,
            max: () -> Long,
            average: Int,
            heating: () -> Boolean,
            hasCooler: () -> Boolean
        ): WidgetBar {
            return WidgetBar(
                identifier("textures/gui/widgets/widget_temperature_empty.png"),
                identifier("textures/gui/widgets/widget_temperature_full.png"),
                16,
                59,
                Direction.UP,
                current,
                max
            ).setTooltip { tooltip ->
                tooltip.add(Text.literal("Temperature").styled { s -> s.withColor(0x3c00f0) })
                tooltip.add(
                    Text.literal("${current()} / ${max()} ºC").append(
                        Text.literal(if (heating()) " \u2197" else " \u2198")
                            .styled { s -> s.withColor(0x3c00f0).withBold(true) })
                )
                if (current() in average - 250..average + 250) {
                    if (heating() || hasCooler()) {
                        tooltip.add(Text.literal("50% boost").styled { s -> s.withColor(0x3cf000).withItalic(true) })
                    }

                    if (!heating() && !hasCooler()) {
                        tooltip.add(
                            Text.literal("Insert cooler to boost while cooling down")
                                .formatted(Formatting.BLUE, Formatting.ITALIC)
                        )
                    }
                }
            } as WidgetBar
        }

        fun burning(current: () -> Int, max: () -> Int): WidgetBar {
            return WidgetBar(
                identifier("textures/gui/widgets/widget_fuel_not_burning.png"),
                identifier("textures/gui/widgets/widget_fuel_burning.png"),
                26,
                9,
                Direction.UP,
                { current().toLong() },
                { max().toLong() }
            ).setTooltip { tooltip ->
                if (current() > 0) {
                    tooltip.add(Text.literal("Burning").styled { s -> s.withColor(0xf03c00) })
                    tooltip.add(Text.literal("${current()} ticks remaining"))
                }
            } as WidgetBar
        }

        fun fluidTank(fluidInv: MachineFluidInventory, slot: Int, pos: BlockPos): WidgetBar {
            return object : WidgetBar(
                identifier("textures/gui/widgets/widget_tank_bottom.png"),
                identifier("textures/gui/widgets/widget_tank_top.png"),
                22,
                72,
                Direction.UP,
                { fluidInv[slot].capacity },
                { fluidInv[slot].capacity }
            ) {
                override fun drawBar(ctx: DrawContext, x: Int, y: Int) {
                    val fluidSlot = fluidInv[slot]
                    if (!fluidSlot.isEmpty())
                        renderInGui(
                            ctx.matrices,
                            fluidSlot.resource,
                            fluidSlot.amount,
                            fluidSlot.capacity,
                            x + 1,
                            y - 1 + height,
                            width - 2,
                            height - 2
                        )
                    super.drawBar(ctx, x, y)
                }

                override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
                    val buf = PacketByteBufs.create()
                    buf.writeInt(slot)
                    buf.writeBlockPos(pos)
                    ClickFluidWidgetPacket.send(buf)
                }

            }.setTooltip { tooltip ->
                val fluidSlot = fluidInv[slot]
                if (!fluidSlot.isEmpty()) {
                    tooltip.addAll(getFluidTooltip(fluidSlot.variant, fluidSlot.amount, fluidSlot.capacity))
                }
            } as WidgetBar
        }


        fun processBar(
            current: () -> Int,
            max: () -> Int,
            recipeProvider: () -> MachineRecipe?,
            troubleshooter: Troubleshooter,
            text: MutableText = Text.literal("Processing"),
            machine: CraftingMachineBlockEntity
        ): WidgetBar {
            return object : WidgetBar(
                identifier("textures/gui/widgets/widget_processing_empty.png"),
                identifier("textures/gui/widgets/widget_processing_full.png"),
                18,
                18,
                Direction.RIGHT,
                { current().toLong() },
                { max().toLong() }
            ) {
                override fun drawBar(ctx: DrawContext, x: Int, y: Int) {
                    if (ProcessingBoost.getActiveBoosts(machine).isNotEmpty()) {
                        val (alpha, red, green, blue) = argb(0xFFFFA8A8.toInt())
                        ctx.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
                    }
                    super.drawBar(ctx, x, y)

                    if (!troubleshooter.isEmpty()) {
                        ctx.setShaderColor(1f, 1f, 1f, 1f)
                        ctx.drawTexture(identifier("textures/gui/widgets/widget_warning.png"), x, y, 0f,0f, width, height, width, height)
                    }
                }
            }.setTooltip { tooltip ->
                if (!troubleshooter.isEmpty()) {
                    troubleshooter.appendMessages(tooltip)
                } else {

                    if (current() > 0) {
                        val percentage = current() / max().toDouble() * 100
                        tooltip.add(text.copy().append(Text.literal(": ")).styled { s -> s.withColor(0x3c00f0) }
                            .append(Text.literal("${100 - percentage.toInt()}%").formatted(Formatting.WHITE)))
                    }
                    val recipe = recipeProvider()
                    if (recipe != null) {
                        val boosts = ProcessingBoost.getActiveBoosts(machine)
                        tooltip.add(ProcessingBoost.appendTickTime(boosts, recipe, machine.config?.processingSpeed ?: 1.0))
                        boosts.forEach { it.append(tooltip) }
                    }
                }
            } as WidgetBar
        }
    }
}