package me.steven.indrev.gui.controllers.wrench

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.addBookEntryShortcut
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class WrenchController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.WRENCH_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {

    private lateinit var currentType: ConfigurationType

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(100, 128)

        val titleText = WText(TranslatableText("item.indrev.wrench.title"), HorizontalAlignment.LEFT, 0x404040)
        root.add(titleText, 0.2, 0.2)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run

            val availableTypes = ConfigurationType.getTypes(blockEntity)
            currentType = availableTypes.first()
            var widget = blockEntity.getWrenchConfigurationPanel(world, pos, playerInventory, currentType) ?: return@run
            root.add(widget, 0, 2)
            val configTypeButton = WButton(currentType.title)
            configTypeButton.setOnClick {
                currentType = currentType.next(availableTypes)
                configTypeButton.label = currentType.title
                root.remove(widget)
                widget = blockEntity.getWrenchConfigurationPanel(world, pos, playerInventory, currentType) ?: return@setOnClick
                root.add(widget, 0, 2)
                root.validate(this)
            }
            if (availableTypes.size > 1)
                root.add(configTypeButton, 1.6, 1.0)
            configTypeButton.setSize(45, 20)
            
            addBookEntryShortcut(playerInventory, root, -1.8, -0.47)
        }
        root.validate(this)
    }

    override fun getEntry(): Identifier = identifier("tools/wrench")

    override fun getPage(): Int = 0

    override fun close(player: PlayerEntity?) {
        super.close(player)
        if (player is ServerPlayerEntity) {
            val buf = PacketByteBuf(Unpooled.buffer())
            ctx.run { _, pos -> buf.writeBlockPos(pos) }
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerInventory.player, IndustrialRevolution.RERENDER_CHUNK_PACKET, buf)
        }
    }

    override fun addPainters() {
        super.addPainters()
        rootPanel.backgroundPainter = BackgroundPainter.VANILLA
    }

    companion object {
        val SCREEN_ID = identifier("wrench_item_io_screen")
    }
}