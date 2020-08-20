package me.steven.indrev.utils

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Point
import me.shedaniel.rei.api.widgets.Widgets
import me.shedaniel.rei.gui.widget.Widget
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.config.CableConfig
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.IConfig
import me.steven.indrev.gui.widgets.machines.WFluid
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

val EMPTY_INT_ARRAY = intArrayOf()

fun identifier(id: String) = Identifier(IndustrialRevolution.MOD_ID, id)

fun Identifier.block(block: Block): Identifier {
    Registry.register(Registry.BLOCK, this, block)
    return this
}

fun Identifier.fluid(fluid: Fluid): Identifier {
    Registry.register(Registry.FLUID, this, fluid)
    return this
}

fun Identifier.item(item: Item): Identifier {
    Registry.register(Registry.ITEM, this, item)
    return this
}

fun Identifier.blockEntityType(entityType: BlockEntityType<*>): Identifier {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, this, entityType)
    return this
}

fun Identifier.tierBasedItem(vararg tiers: Tier = Tier.VALUES, itemSupplier: (Tier) -> Item) {
    tiers.forEach { tier ->
        val item = itemSupplier(tier)
        identifier("${this.path}_${tier.toString().toLowerCase()}").item(item)
    }
}

fun <T : ScreenHandler> Identifier.registerScreenHandler(
    f: (Int, PlayerInventory, ScreenHandlerContext) -> T
): ExtendedScreenHandlerType<T> =
    ScreenHandlerRegistry.registerExtended(this) { syncId, inv, buf ->
        f(syncId, inv, ScreenHandlerContext.create(inv.player.world, buf.readBlockPos()))
    } as ExtendedScreenHandlerType<T>

fun Box.isSide(vec3d: Vec3d) =
    vec3d.x == minX || vec3d.x == maxX - 1 || vec3d.y == minY || vec3d.y == maxY - 1 || vec3d.z == minZ || vec3d.z == maxZ - 1

fun itemSettings(): Item.Settings = Item.Settings().group(IndustrialRevolution.MOD_GROUP)

fun IntRange.toIntArray(): IntArray = this.map { it }.toIntArray()

fun BlockPos.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun ChunkPos.asString() = "$x,$z"

fun getChunkPos(s: String): ChunkPos? {
    val split = s.split(",")
    val x = split[0].toIntOrNull() ?: return null
    val z = split[1].toIntOrNull() ?: return null
    return ChunkPos(x, z)
}

fun EnergySide.opposite(): EnergySide =
    when (this) {
        EnergySide.DOWN -> EnergySide.UP
        EnergySide.UP -> EnergySide.DOWN
        EnergySide.NORTH -> EnergySide.SOUTH
        EnergySide.SOUTH -> EnergySide.NORTH
        EnergySide.WEST -> EnergySide.EAST
        EnergySide.EAST -> EnergySide.WEST
        EnergySide.UNKNOWN -> EnergySide.UNKNOWN
    }

fun getShortEnergyDisplay(energy: Double): String =
    when {
        energy > 1000000 -> "${"%.1f".format(energy / 1000000)}M"
        energy > 1000 -> "${"%.1f".format(energy / 1000)}k"
        else -> "%.1f".format(energy)
    }

fun buildEnergyTooltip(stack: ItemStack?, tooltip: MutableList<Text>?) {
    val handler = Energy.of(stack)
    if (handler.energy > 0) {
        tooltip?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        tooltip?.add(LiteralText("${getShortEnergyDisplay(handler.energy)} LF").formatted(Formatting.GOLD))
    }
}

fun buildMachineTooltip(config: Any, tooltip: MutableList<Text>?) {
    if (Screen.hasShiftDown()) {
        when (config) {
            is IConfig -> {
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxInput").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.maxInput
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxEnergyStored").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lf",
                                getShortEnergyDisplay(config.maxEnergyStored)
                            ).formatted(
                                Formatting.GOLD
                            )
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.energyCost").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lf",
                                config.energyCost
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.processSpeed").formatted(Formatting.BLUE)
                        .append(LiteralText(config.processSpeed.toString()).formatted(Formatting.GOLD))
                )
            }
            is HeatMachineConfig -> {
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxInput").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.maxInput
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxEnergyStored").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lf",
                                getShortEnergyDisplay(config.maxEnergyStored)
                            ).formatted(
                                Formatting.GOLD
                            )
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.energyCost").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lf",
                                config.energyCost
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.processSpeed").formatted(Formatting.BLUE)
                        .append(LiteralText(config.processSpeed.toString()).formatted(Formatting.GOLD))
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.processSpeed").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.processTemperatureBoost
                            ).formatted(Formatting.GOLD)
                        )
                )
            }
            is GeneratorConfig -> {
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxOutput").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.maxOutput
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxEnergyStored").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lf",
                                getShortEnergyDisplay(config.maxEnergyStored)
                            ).formatted(
                                Formatting.GOLD
                            )
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.ratio").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.ratio
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.processSpeed").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText("gui.indrev.tooltip.lftick", config.temperatureBoost).formatted(
                                Formatting.GOLD
                            )
                        )
                )
            }
            is CableConfig -> {
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxInput").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.maxInput
                            ).formatted(Formatting.GOLD)
                        )
                )
                tooltip?.add(
                    TranslatableText("gui.indrev.tooltip.maxOutput").formatted(Formatting.BLUE)
                        .append(
                            TranslatableText(
                                "gui.indrev.tooltip.lftick",
                                config.maxOutput
                            ).formatted(Formatting.GOLD)
                        )
                )
            }
        }
    } else {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.press_shift").formatted(
                Formatting.BLUE,
                Formatting.ITALIC
            )
        )
    }
}

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

fun <T> getFirstMatch(identifier: Array<Identifier>, registry: Registry<T>): T =
    registry[identifier.first { registry.getOrEmpty(it).isPresent }]!!

fun getFluidFromJson(json: JsonObject): FluidVolume {
    val fluidId = json.get("fluid").asString
    val fluidKey = FluidKeys.get(Registry.FLUID.get(Identifier(fluidId)))
    val amount = JsonHelper.getLong(json, "count", 1)
    val fluidAmount = when (val type = json.get("type").asString) {
        "nugget" -> NUGGET_AMOUNT
        "ingot" -> INGOT_AMOUNT
        "block" -> BLOCK_AMOUNT
        "bucket" -> FluidAmount.BUCKET
        else -> throw IllegalArgumentException("unknown amount type $type")
    }.mul(amount)
    return fluidKey.withAmount(fluidAmount)
}

fun getItemStackFromJson(json: JsonObject): ItemStack {
    val itemPath = json.get("item").asString
    if (itemPath == "empty") return ItemStack.EMPTY
    val item =
        if (itemPath.contains(":")) Registry.ITEM.get(Identifier(itemPath))
        else
            getFirstMatch(
                arrayOf(
                    Identifier(IndustrialRevolution.CONFIG.compatibility.targetModId, itemPath),
                    identifier(itemPath)
                ), Registry.ITEM
            )
    val output = ItemStack { item }
    output.count = JsonHelper.getInt(json, "count", 1)
    return output
}

inline fun Box.any(f: (Int, Int, Int) -> Boolean): Boolean {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return true
    return false
}

inline fun Box.forEach(f: (Int, Int, Int) -> Unit) {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                f(x, y, z)
}


inline fun Box.firstOrNull(f: (Int, Int, Int) -> Boolean): BlockPos? {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return BlockPos(x, y, z)
    return null
}

fun offerDefaultAttributes(fluidComponent: FluidComponent, to: AttributeList<*>) {
    val opposite = to.searchDirection?.opposite
    if (to.attribute == FluidAttributes.INSERTABLE && fluidComponent.transferConfig[opposite]?.input == true)
        to.offer(fluidComponent)
    else if (to.attribute == FluidAttributes.EXTRACTABLE && fluidComponent.transferConfig[opposite]?.output == true)
        to.offer(fluidComponent)
}

fun createREIFluidWidget(widgets: MutableList<Widget>, startPoint: Point, fluid: FluidVolume) {
    widgets.add(Widgets.createTexturedWidget(WFluid.ENERGY_EMPTY, startPoint.x, startPoint.y, 0f, 0f, 16, 52, 16, 52))
    widgets.add(Widgets.createDrawableWidget { _, matrices, mouseX, mouseY, _ ->
        fluid.renderGuiRect(startPoint.x + 2.0, startPoint.y.toDouble() + 1.5, startPoint.x.toDouble() + 14, startPoint.y.toDouble() + 50)
        if (mouseX > startPoint.x && mouseX < startPoint.x + 16 && mouseY > startPoint.y && mouseY < startPoint.y + 52) {
            val information = mutableListOf<OrderedText>()
            information.addAll(fluid.fluidKey.fullTooltip.map { it.asOrderedText() })
            information.add(LiteralText("${(fluid.amount().asInexactDouble() * 1000).toInt()} mB").asOrderedText())
            MinecraftClient.getInstance().currentScreen?.renderOrderedTooltip(matrices, information, mouseX, mouseY)
        }
    })
}