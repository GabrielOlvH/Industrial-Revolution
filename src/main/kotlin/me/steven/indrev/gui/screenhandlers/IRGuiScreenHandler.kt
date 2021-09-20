package me.steven.indrev.gui.screenhandlers

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.netty.buffer.Unpooled
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import me.steven.indrev.components.GuiSyncableComponent
import me.steven.indrev.packets.client.GuiPropertySyncPacket
import me.steven.indrev.utils.properties
import me.steven.indrev.utils.trackedPropertyValues
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerSyncHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.BiFunction

open class IRGuiScreenHandler(
    type: ScreenHandlerType<*>?,
    syncId: Int,
    playerInventory: PlayerInventory,
    val ctx: ScreenHandlerContext
) : SyncedGuiDescription(type, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx)) {

    var component: GuiSyncableComponent? = null

    init {
        properties.clear()
        trackedPropertyValues.clear()

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? ComponentProvider ?: return@run
            component = ComponentKey.GUI_SYNCABLE.get(blockEntity)
        }
    }

    val player = playerInventory.player

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        val offset = 170 - rootPanel.width
        rootPanel.backgroundPainter =
            BackgroundPainter.createLightDarkVariants(
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8)
                    .setRightPadding(offset),
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8)
                    .setRightPadding(offset)
            )
    }

    override fun syncState() {
        super.syncState()

        // when someone opens the same screen, resync everything to everyone
        component?.properties?.forEach { p -> p.markDirty() }
    }


    inline fun <T : BlockEntity> withBlockEntity(block: (T) -> Unit) {
        val be = ctx.get(BiFunction { world, pos ->
            world.getBlockEntity(pos) as? T ?: return@BiFunction null
        }).orElse(null)
        block(be ?: return)
    }

    inline fun <T : BlockEntity, U> query(block: (T) -> U): U {
        val be = ctx.get(BiFunction { world, pos ->
            world.getBlockEntity(pos) as? T ?: return@BiFunction null
        }).orElse(null)
        return block(be ?: error("burh"))
    }

    fun syncProperties() {
        val props = component?.properties ?: return
        val player = playerInventory.player
        if (player is ServerPlayerEntity) {
            for (i in props.indices) {
                if (props[i].isDirty) {
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeInt(syncId)
                    buf.writeInt(i)
                    props[i].toPacket(buf)
                    ServerPlayNetworking.send(player, GuiPropertySyncPacket.SYNC_PROPERTY, buf)
                    props[i].isDirty = false
                }
            }
        }
    }
}