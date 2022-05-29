package me.steven.indrev.gui.screenhandlers.pipes

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.github.cottonmc.cotton.gui.widget.data.Insets
import me.steven.indrev.gui.screenhandlers.PIPE_FILTER_HANDLER
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.packets.common.ItemPipePackets
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundEvents
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*
import java.util.function.Consumer

class PipeFilterScreenHandler(
    syncId: Int, playerInventory: PlayerInventory,
    whitelist: Boolean = false,
    matchDurability: Boolean = false,
    matchTag: Boolean = false,
    var mode: EndpointData.Mode? = null,
    val type: EndpointData.Type? = null
) : SyncedGuiDescription(PIPE_FILTER_HANDLER, syncId, playerInventory) {

    val backingList = DefaultedList.ofSize(9, ItemStack.EMPTY)
    lateinit var direction: Direction
    lateinit var blockPos: BlockPos

    init {
        val root = WGridPanel()
        this.rootPanel = root
        root.insets = Insets.ROOT_PANEL

        (0 until backingList.size).forEach { index ->
            val slot = WFilterSlot(index)
            root.add(slot, 1 * index, 1)
        }

        val whitelistButton = object : WToggleButton(WHITELIST_ICON, BLACKLIST_ICON) {

            override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                WButton().also { it.setSize(20, 20) }.paint(matrices, x, y, mouseX, mouseY)
                super.paint(matrices, x + 1, y + 1, mouseX, mouseY)
            }

            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(translatable("gui.indrev.whitelist.$isOn"))
            }
        }
        whitelistButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(0)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(ItemPipePackets.CHANGE_FILTER_MODE_PACKET, buf)
        }
        whitelistButton.toggle = whitelist
        root.add(whitelistButton, 2, 2)
        whitelistButton.setLocation(2 * 18 + root.insets.left, 3 * 18)

        val matchDurabilityButton = object : WToggleButton(MATCH_DURABILITY_ICON, IGNORE_DURABILITY_ICON) {

            override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                WButton().also { it.setSize(20, 20) }.paint(matrices, x, y, mouseX, mouseY)
                super.paint(matrices, x + 1, y + 1, mouseX, mouseY)
            }

            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(translatable("gui.indrev.matchDurability.$isOn"))
            }
        }
        matchDurabilityButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(1)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(ItemPipePackets.CHANGE_FILTER_MODE_PACKET, buf)
        }
        matchDurabilityButton.toggle = matchDurability
        root.add(matchDurabilityButton, 4, 2)
        matchDurabilityButton.setLocation(4 * 18 + root.insets.left, 3 * 18)

        val matchTagButton = object : WToggleButton(MATCH_NBT_ICON, IGNORE_NBT_ICON) {

            override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                WButton().also { it.setSize(20, 20) }.paint(matrices, x, y, mouseX, mouseY)
                super.paint(matrices, x + 1, y + 1, mouseX, mouseY)
            }
            
            override fun addTooltip(tooltip: TooltipBuilder?) {
                tooltip?.add(translatable("gui.indrev.matchTag.$isOn"))
            }
        }
        matchTagButton.onToggle = Consumer { value ->
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeInt(2)
            buf.writeBoolean(value)
            ClientPlayNetworking.send(ItemPipePackets.CHANGE_FILTER_MODE_PACKET, buf)
        }
        matchTagButton.toggle = matchTag
        root.add(matchTagButton, 6, 3)
        matchTagButton.setLocation(6 * 18 + root.insets.left, 3 * 18)

        if (mode != null && type != null) {
            val modeWidget = WServoMode()
            root.add(modeWidget, 8, 0)
            modeWidget.setSize(18, 9)
        }

        val panel = createPlayerInventoryPanel()
        root.add(panel, 0, 4)

        root.validate(this)
    }

    private fun writeIdentifyingData(buf: PacketByteBuf) {
        buf.writeEnumConstant(direction)
        buf.writeBlockPos(blockPos)
    }

    inner class WFilterSlot(val index: Int) : WWidget() {

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            ScreenDrawing.drawBeveledPanel(matrices, x, y, height, width, -1207959552, 1275068416, -1191182337)
            MinecraftClient.getInstance().itemRenderer.renderInGui(backingList[index], x + 1, y + 1)
            MinecraftClient.getInstance().itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, backingList[index], x + 1, y + 1)
            if (mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height)
                DrawableHelper.fill(matrices, x + 1, y + 1, x + 17, y + 17, -2130706433)
        }

        override fun onClick(x: Int, y: Int, button: Int): InputResult {
            val buf = PacketByteBufs.create()
            buf.writeInt(index)
            writeIdentifyingData(buf)
            ClientPlayNetworking.send(ItemPipePackets.CLICK_FILTER_SLOT_PACKET, buf)

            return InputResult.PROCESSED
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            val itemStack = backingList[index]
            if (!itemStack.isEmpty)
                tooltip?.add(*itemStack.getTooltip(playerInventory.player) { MinecraftClient.getInstance().options.advancedItemTooltips }.toTypedArray())
        }
    }

    inner class WServoMode : WWidget() {

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            if (type == EndpointData.Type.OUTPUT)
                MinecraftClient.getInstance().itemRenderer.renderInGui(ItemStack(IRItemRegistry.SERVO_OUTPUT), x + 1, y - 2)
            else if (type == EndpointData.Type.RETRIEVER)
                MinecraftClient.getInstance().itemRenderer.renderInGui(ItemStack(IRItemRegistry.SERVO_RETRIEVER), x + 1, y - 2)
        }

        override fun onClick(x: Int, y: Int, button: Int): InputResult {
            mode = mode?.next() ?: return InputResult.IGNORED
            MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            val buf = PacketByteBufs.create()
            writeIdentifyingData(buf)
            buf.writeEnumConstant(mode)
            ClientPlayNetworking.send(ItemPipePackets.CHANGE_SERVO_MODE_PACKET, buf)
            return InputResult.PROCESSED
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            tooltip?.add(
                translatable("item.indrev.servo.mode")
                .append(translatable("item.indrev.servo.mode.${mode.toString().lowercase(Locale.getDefault())}").formatted(Formatting.BLUE)))
        }
    }

    companion object {

        val WHITELIST_ICON = identifier("textures/gui/filter_whitelist.png")
        val BLACKLIST_ICON = identifier("textures/gui/filter_blacklist.png")
        val IGNORE_DURABILITY_ICON = identifier("textures/gui/filter_ignore_durability.png")
        val MATCH_DURABILITY_ICON = identifier("textures/gui/filter_match_durability.png")
        val IGNORE_NBT_ICON = identifier("textures/gui/filter_ignore_nbt.png")
        val MATCH_NBT_ICON = identifier("textures/gui/filter_match_nbt.png")

        val SCREEN_ID = identifier("pipe_filter_screen")
    }
}