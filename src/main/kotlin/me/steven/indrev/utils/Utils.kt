package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import me.shedaniel.math.Point
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.IREntityExtension
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.inventories.IRInventory
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.OrderedText
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World


val EMPTY_INT_ARRAY = intArrayOf()

fun identifier(id: String) = Identifier(IndustrialRevolution.MOD_ID, id)

fun blockSpriteId(id: String) = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier(id))

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

fun itemSettings(): FabricItemSettings = FabricItemSettings().group(IndustrialRevolution.MOD_GROUP)

fun <T : ScreenHandler> Identifier.registerScreenHandler(
    f: (Int, PlayerInventory, ScreenHandlerContext) -> T
): ExtendedScreenHandlerType<T> =
    ScreenHandlerRegistry.registerExtended(this) { syncId, inv, buf ->
        f(syncId, inv, ScreenHandlerContext.create(inv.player.world, buf.readBlockPos()))
    } as ExtendedScreenHandlerType<T>

fun BlockPos.toVec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun ChunkPos.toNbt() = NbtCompound().also {
    it.putInt("x", x)
    it.putInt("z", z)
}

fun getChunkPos(nbt: NbtCompound) = ChunkPos(nbt.getInt("x"), nbt.getInt("z"))

fun getFluidFromJson(json: JsonObject): FluidVolume {
    val fluidId = json.get("fluid").asString
    val fluidKey = FluidKeys.get(Registry.FLUID.get(Identifier(fluidId)))
    val amount = JsonHelper.getLong(json, "count", 1)
    val fluidAmount = when (val type = json.get("type").asString) {
        "nugget" -> NUGGET_AMOUNT
        "ingot" -> INGOT_AMOUNT
        "block" -> BLOCK_AMOUNT
        "bucket" -> FluidAmount.BUCKET
        "scrap" -> SCRAP_AMOUNT
        "bottle" -> FluidAmount.BOTTLE
        else -> throw IllegalArgumentException("unknown amount type $type")
    }.mul(amount)
    return fluidKey.withAmount(fluidAmount)
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

fun World.setBlockState(pos: BlockPos, state: BlockState, condition: (BlockState) -> Boolean) {
    val blockState = getBlockState(pos)
    if (condition(blockState)) setBlockState(pos, state)
}
fun World.isLoaded(pos: BlockPos): Boolean {
    return chunkManager.isChunkLoaded(pos.x shr 4, pos.z shr 4)
}

fun <E> WeightedList<E>.pickRandom(): E {
    return this.shuffle().entries.first().element
}

fun pack(dirs: Collection<Direction>): Byte {
    var i = 0
    dirs.forEach { dir -> i = i or (1 shl dir.id) }
    return i.toByte()
}

fun unpack(byte: Byte): List<Direction> {
    val i = byte.toInt()
    return DIRECTIONS.filter { dir -> i and (1 shl dir.id) != 0 }
}

val DIRECTIONS = Direction.values()

inline fun Entity.redirectDrops(inv: IRInventory, run: () -> Unit) {
    this as IREntityExtension
    this.machineInv = inv
    run()
    this.machineInv = null
}