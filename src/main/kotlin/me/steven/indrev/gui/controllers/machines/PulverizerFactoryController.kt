package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WBar
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.crafters.PulverizerFactoryBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class PulverizerFactoryController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiController(
        IndustrialRevolution.PULVERIZER_FACTORY_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.pulverizer", ctx, playerInventory, blockInventory, propertyDelegate)
        root.add(WText(TranslatableText("block.indrev.factory"), HorizontalAlignment.CENTER, 0x404040), 4.3, 0.5)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? PulverizerFactoryBlockEntity ?: return@run
            val slotsAmount = (blockEntity.tier.ordinal + 2).coerceAtMost(5)
            val offset = (10 - slotsAmount) / 2.0

            for ((index, slot) in blockEntity.inventoryComponent!!.inventory.inputSlots.withIndex()) {
                val inputSlot = WItemSlot.of(blockInventory, slot)
                root.add(inputSlot, offset + index, 1.2)
            }

            for (i in 0 until slotsAmount) {
                val processWidget = createProcessBar(WBar.Direction.DOWN, PROCESS_VERTICAL_EMPTY, PROCESS_VERTICAL_FULL, 3 + (i * 2), 4 + (i * 2))
                root.add(processWidget, offset + i, 2.3)
            }

            for ((index, slot) in blockEntity.inventoryComponent!!.inventory.outputSlots.withIndex()) {
                val outputSlot = WItemSlot.of(blockInventory, slot)
                root.add(outputSlot, offset + index, 3.4)
                outputSlot.addChangeListener { _, _, _, _ ->
                    val player = playerInventory.player
                    if (!player.world.isClient) {
                        blockEntity.dropExperience(player)
                    }
                }
                outputSlot.isInsertingAllowed = false
            }
            val button = WButton(LiteralText("S"))
            button.setOnClick {
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeBlockPos(pos)
                ClientSidePacketRegistry.INSTANCE.sendToServer(SPLIT_STACKS_PACKET, buf)
            }
            root.add(button, 8, 4)
        }
        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 1

    companion object {
        val SCREEN_ID = identifier("pulverizer_factory_screen")
        val SPLIT_STACKS_PACKET = identifier("split_stacks_packet")
    }
}