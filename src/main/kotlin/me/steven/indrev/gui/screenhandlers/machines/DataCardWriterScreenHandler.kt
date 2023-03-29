package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WDynamicLabel
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.miningrig.DataCardWriterBlockEntity
import me.steven.indrev.gui.screenhandlers.DATA_CARD_WRITER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.packets.common.DataCardWriteStartPacket
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.literal

class DataCardWriterScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        DATA_CARD_WRITER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val startButton = WButton(literal("Write"))
        val root = object : WGridPanel() {
            override fun tick() {
                super.tick()
                startButton.isEnabled = component!!.get<Int>(DataCardWriterBlockEntity.TOTAL_PROCESS_ID) <= 0
            }
        }
        setRootPanel(root)
        configure("block.indrev.data_card_writer", ctx, playerInventory, blockInventory, invPos = 5.7, widgetPos = 0.85)

        val cardSlot = WItemSlot.of(blockInventory, 0)
        root.add(cardSlot, 4.0, 1.0)

        val inputOres = WItemSlot.of(blockInventory, 1, 6, 2)
        root.add(inputOres, 1.5, 3.5)

        val modifierSlots = WItemSlot.of(blockInventory, 13, 1, 3)
        root.add(modifierSlots, 8.0, 0.5)

        startButton.onClick = Runnable {

            val buf = PacketByteBufs.create()
            ctx.run { _, pos -> buf.writeBlockPos(pos) }
            ClientPlayNetworking.send(DataCardWriteStartPacket.START_PACKET, buf)
        }
        root.add(startButton, 7.65, 4.5)
        startButton.setSize(28, 20)

        val dataLabel = WLabel(literal("Data"))
        root.add(dataLabel, 1.5, 3.0)

        val timeLabel = WDynamicLabel {
            val processTime = component!!.get<Int>(DataCardWriterBlockEntity.PROCESS_ID)
            val totalProcessTime = component!!.get<Int>(DataCardWriterBlockEntity.TOTAL_PROCESS_ID)
            val remaining = (totalProcessTime - processTime) / 20
            if (remaining > 0) "${remaining}s" else ""
        }
        timeLabel.setAlignment(HorizontalAlignment.CENTER)
        root.add(timeLabel, 4.1, 2.1)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("data_card_writer_screen")
    }
}