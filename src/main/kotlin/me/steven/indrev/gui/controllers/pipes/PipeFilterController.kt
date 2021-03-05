package me.steven.indrev.gui.controllers.pipes

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WToggleButton
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.LiteralText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.function.Consumer

class PipeFilterController(
    syncId: Int, playerInventory: PlayerInventory,
    whitelist: Boolean = false,
    matchDurability: Boolean = false,
    matchTag: Boolean = false
) : SyncedGuiDescription(IndustrialRevolution.PIPE_FILTER_HANDLER, syncId, playerInventory) {

    val backingList = DefaultedList.ofSize(9, ItemStack.EMPTY)
    lateinit var direction: Direction
    lateinit var blockPos: BlockPos

    init {
        val root = WGridPanel()
        this.rootPanel = root

        (0 until backingList.size).forEach { index ->
            val slot = WFilterSlot(index)
            root.add(slot, 1 * index, 1)
        }

        val whitelistButton = object : WToggleButton() {
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(LiteralText(if (isOn) "Whitelist" else "Blacklist"))
            }
        }
        whitelistButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(0)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(CHANGE_FILTER_MODE_PACKET, buf)
        }
        whitelistButton.toggle = whitelist
        root.add(whitelistButton, 2, 2)

        val matchDurabilityButton = object : WToggleButton() {
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(LiteralText(if (isOn) "Match item damage" else "Ignore item damage"))
            }
        }
        matchDurabilityButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(1)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(CHANGE_FILTER_MODE_PACKET, buf)
        }
        matchDurabilityButton.toggle = matchDurability
        root.add(matchDurabilityButton, 4, 2)

        val matchTagButton = object : WToggleButton() {
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(LiteralText(if (isOn) "Match item NBT" else "Ignore item NBT"))
            }
        }
        matchTagButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(2)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(CHANGE_FILTER_MODE_PACKET, buf)
        }
        matchTagButton.toggle = matchTag
        root.add(matchTagButton, 6, 2)

        root.add(createPlayerInventoryPanel(), 0, 3)

        root.validate(this)
    }

    private fun writeIdentifyingData(buf: PacketByteBuf) {
        buf.writeEnumConstant(direction)
        buf.writeBlockPos(blockPos)
    }

    inner class WFilterSlot(val index: Int) : WWidget() {

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            ScreenDrawing.drawBeveledPanel(x, y, height, width, -1207959552, 1275068416, -1191182337)
            MinecraftClient.getInstance().itemRenderer.renderInGui(backingList[index], x + 1, y + 1)
            MinecraftClient.getInstance().itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, backingList[index], x + 1, y + 1)
        }

        override fun onClick(x: Int, y: Int, button: Int) {
            val buf = PacketByteBufs.create()
            buf.writeInt(index)
            writeIdentifyingData(buf)
            ClientPlayNetworking.send(CLICK_FILTER_SLOT_PACKET, buf)
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            val itemStack = backingList[index]
            if (!itemStack.isEmpty)
                tooltip?.add(*itemStack.getTooltip(playerInventory.player) { MinecraftClient.getInstance().options.advancedItemTooltips }.toTypedArray())
        }
    }

    companion object {
        val CLICK_FILTER_SLOT_PACKET = identifier("click_filter_slot")
        val UPDATE_FILTER_SLOT_S2C_PACKET = identifier("update_filter_s2c")
        val CHANGE_FILTER_MODE_PACKET = identifier("change_whitelist_mode")

        val SCREEN_ID = identifier("pipe_filter_screen")
    }
}