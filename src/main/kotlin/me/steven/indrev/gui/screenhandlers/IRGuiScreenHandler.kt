package me.steven.indrev.gui.screenhandlers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.components.GuiSyncableComponent
import me.steven.indrev.gui.properties.SyncableProperty
import me.steven.indrev.packets.client.GuiPropertySyncPacket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
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
            val blockEntity = world.getBlockEntity(pos) as? BaseBlockEntity ?: return@run
            component = blockEntity.guiSyncableComponent
        }
    }

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


    @Suppress("UNCHECKED_CAST")
    inline fun <T : BlockEntity> withBlockEntity(block: (T) -> Unit) {
        val be = ctx.get(BiFunction { world, pos ->
            world.getBlockEntity(pos) as? T ?: return@BiFunction null
        }).orElse(null)
        block(be ?: return)
    }

    @Suppress("UNCHECKED_CAST")
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

                    onSyncedProperty(i, props[i])
                }
            }
        }
    }

    open fun onSyncedProperty(index: Int, property: SyncableProperty<*>) {

    }
}