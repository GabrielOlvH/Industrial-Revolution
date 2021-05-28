package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.machines.WEnergy
import me.steven.indrev.gui.widgets.machines.WProcessBar
import me.steven.indrev.gui.widgets.machines.WTemperature
import me.steven.indrev.gui.widgets.misc.WBookEntryShortcut
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.items.enhancer.IREnhancerItem
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import vazkii.patchouli.client.book.ClientBookRegistry
import java.util.function.Predicate

fun WGridPanel.add(w: WWidget, x: Double, y: Double, width: Double, height: Double) {
    this.add(w, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun WGridPanel.add(w: WWidget, x: Double, y: Double) {
    this.add(w, x.toInt(), y.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

val SPLIT_STACKS_PACKET = identifier("split_stacks_packet")
val SPLIT_ON_ICON = identifier("textures/gui/split_on.png")
val SPLIT_OFF_ICON = identifier("textures/gui/split_off.png")

fun SyncedGuiDescription.configure(
    titleId: String,
    ctx: ScreenHandlerContext,
    playerInventory: PlayerInventory,
    blockInventory: Inventory,
    panel: WGridPanel = rootPanel as WGridPanel
) {
    panel.setSize(150, 120)
    panel.add(createPlayerInventoryPanel(), 0, 5)
    val title = WText(TranslatableText(titleId), HorizontalAlignment.CENTER, 0x404040)
    var titlePos = 5.0

    val energyWidget = WEnergy()
    panel.add(energyWidget, 0.1, 0.0)

    val batterySlot = WTooltipedItemSlot.of(blockInventory, 0, TranslatableText("gui.indrev.battery_slot_type"))
    batterySlot.filter = Predicate { stack -> energyOf(stack) != null }

    ctx.run { world, blockPos ->
        if (world.isClient)
            batterySlot.backgroundPainter = getEnergySlotPainter(blockInventory, 0)
        panel.add(batterySlot, 0.0, 3.7)
        val blockEntity = world.getBlockEntity(blockPos)

        if (blockEntity is MachineBlockEntity<*> && blockEntity is EnhancerProvider) {
            addEnhancementSlots(blockEntity, blockInventory, world, panel)
        }

        if (blockEntity is MachineBlockEntity<*> && blockEntity.temperatureComponent != null) {
            titlePos += 0.5
            addTemperatureWidget(blockEntity, panel, blockInventory, world)
        }

        if (blockEntity is AOEMachineBlockEntity<*>) {
            addAOEWidgets(world, blockEntity, panel)
        }

        if (blockEntity is CraftingMachineBlockEntity<*> && blockEntity.craftingComponents.size > 1) {
            addSplitStackButton(blockEntity, blockPos, world, panel)
        }
    }
    if (this is PatchouliEntryShortcut) {
        addBookEntryShortcut(playerInventory, panel, -1.8, -0.47)
    }
    panel.add(title, titlePos, 0.0)
}

fun addSplitStackButton(blockEntity: CraftingMachineBlockEntity<*>, blockPos: BlockPos, world: World, panel: WGridPanel) {
    val buttonPanel = WGridPanel()
    val button = object : WButton() {
        init {
            if (world.isClient) {
                icon = Icon { matrices, x, y, size ->
                    val id = if (blockEntity.isSplitOn) SPLIT_ON_ICON else SPLIT_OFF_ICON
                    ScreenDrawing.texturedRect(matrices, x + 1, y + 1, size, size, id, -1)
                }
            }
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            tooltip?.add(TranslatableText("gui.indrev.button.auto_split"))
        }
    }
    button.setOnClick {
        blockEntity.isSplitOn = !blockEntity.isSplitOn
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBlockPos(blockPos)
        ClientPlayNetworking.send(SPLIT_STACKS_PACKET, buf)
    }
    if (world.isClient)
        buttonPanel.backgroundPainter = ENHANCER_SLOT_PANEL_PAINTER
    buttonPanel.add(button, 0, 0)
    panel.add(buttonPanel, 9.7, 4.2)
    button.setSize(20, 20)
}

fun addEnhancementSlots(blockEntity: MachineBlockEntity<*>, blockInventory: Inventory, world: World, panel: WGridPanel) {
    blockEntity as EnhancerProvider
    val slotPanel = WGridPanel()
    for ((i, slot) in blockEntity.enhancementsSlots.withIndex()) {
        val s =
            object : WTooltipedItemSlot(inventory = blockInventory, startIndex = slot, emptyTooltip = mutableListOf(TranslatableText("gui.indrev.enhancer_slot_type"))) {
                override fun createSlotPeer(inventory: Inventory?, index: Int, x: Int, y: Int): ValidatedSlot {
                    return object : ValidatedSlot(inventory, index, x, y) {
                        override fun getMaxItemCount(stack: ItemStack): Int {
                            val enhancer = (stack.item as? IREnhancerItem)?.enhancer ?: return 0
                            return blockEntity.getMaxEnhancer(enhancer)
                        }
                    }
                }
            }
        if (world.isClient)
            s.backgroundPainter = if (blockEntity.isLocked(slot, blockEntity.tier)) getLockedSlotPainter(
                blockInventory,
                slot
            ) else getEnhancementSlotPainter(blockInventory, slot)
        slotPanel.add(s, 0, i)
    }
    if (world.isClient)
        slotPanel.backgroundPainter = ENHANCER_SLOT_PANEL_PAINTER
    panel.add(slotPanel, 9.7, -0.25)
}

fun addTemperatureWidget(blockEntity: MachineBlockEntity<*>, panel: WGridPanel, blockInventory: Inventory, world: World) {
    val controller = blockEntity.temperatureComponent!!
    panel.add(WTemperature(controller), 1.1, 0.0)
    val coolerSlot =
        WTooltipedItemSlot.of(blockInventory, 1, TranslatableText("gui.indrev.cooler_slot_type"))
    if (world.isClient)
        coolerSlot.backgroundPainter = getCoolerSlotPainter(blockInventory, 1)
    panel.add(coolerSlot, 1.0, 3.7)
}

fun addAOEWidgets(world: World, blockEntity: AOEMachineBlockEntity<*>, panel: WGridPanel) {
    val buttonPanel = WGridPanel()
    val button = object : WButton() {
        override fun addTooltip(information: TooltipBuilder?) {
            information?.add(TranslatableText("block.indrev.aoe.toggle.${blockEntity.renderWorkingArea}"))
        }
    }
    button.setOnClick {
        blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
    }
    if (world.isClient) {
        button.icon = Icon { matrices, x, y, _ ->
            ScreenDrawing.texturedRect(matrices,x + 1, y + 1, 16, 16, identifier("textures/gui/range_icon.png"), -1)
        }
        buttonPanel.backgroundPainter = ENHANCER_SLOT_PANEL_PAINTER
    }
    buttonPanel.add(button, 0, 0)
    panel.add(buttonPanel, 9.7, 4.2)
    button.setSize(20, 20)
}

fun PatchouliEntryShortcut.addBookEntryShortcut(playerInventory: PlayerInventory, panel: WGridPanel, x: Double, y: Double): WButton {
    val containsBook = playerInventory.count(IRItemRegistry.GUIDE_BOOK) > 0
    val button = object : WBookEntryShortcut() {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            if (containsBook)
                tooltip?.add(
                    TranslatableText("gui.indrev.guide_book_shortcut.contains").formatted(
                        Formatting.BLUE,
                        Formatting.ITALIC
                    )
                )
            else
                tooltip?.add(
                    TranslatableText("gui.indrev.guide_book_shortcut.missing").formatted(
                        Formatting.RED,
                        Formatting.ITALIC
                    )
                )
        }

        override fun isWithinBounds(x: Int, y: Int): Boolean =
            x < this.width && y < this.height
    }
    if (containsBook) {
        button.setOnClick {
            ClientBookRegistry.INSTANCE.displayBookGui(
                Identifier("indrev:indrev"),
                this.getEntry(),
                this.getPage()
            )
        }
    }
    panel.add(button, x, y)
    button.setSize(24, 24)
    return button
}

fun WItemSlot.setPainterSafe(ctx: ScreenHandlerContext, painter: BackgroundPainter) {
    ctx.run { world, _ ->
        if (world.isClient) this.backgroundPainter = painter
    }
}

fun WItemSlot.setIcon(ctx: ScreenHandlerContext, inventory: Inventory, slot: Int, identifier: Identifier) {
    setPainterSafe(ctx) { left, top, widget ->
        BackgroundPainter.SLOT.paintBackground(left, top, widget)
        if (inventory.getStack(slot).isEmpty)
            ScreenDrawing.texturedRect(left + 1, top + 1, 16, 16, identifier, -1)
    }
}

val POWER_ICON_ID = identifier("textures/gui/power_icon.png")

fun getEnergySlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, POWER_ICON_ID, -1)
}

val VENT_ICON_ID = identifier("textures/gui/vent_icon.png")

fun getCoolerSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, VENT_ICON_ID, -1)
}

val ENHANCER_ICON_ID = identifier("textures/gui/enhancer_icon.png")

fun getEnhancementSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, ENHANCER_ICON_ID, -1)
}

val LOCKED_ICON_ID = identifier("textures/gui/locked_icon.png")

fun getLockedSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, LOCKED_ICON_ID, -1)
}

val PROCESS_EMPTY =
    identifier("textures/gui/widget_processing_empty.png")
val PROCESS_FULL =
    identifier("textures/gui/widget_processing_full.png")
val PROCESS_VERTICAL_EMPTY =
    identifier("textures/gui/widget_processing_empty_vertical.png")
val PROCESS_VERTICAL_FULL =
    identifier("textures/gui/widget_processing_full_vertical.png")

fun createProcessBar(direction: WBar.Direction = WBar.Direction.RIGHT, bg: Identifier = PROCESS_EMPTY, bar: Identifier = PROCESS_FULL, value: Int = 4, maxValue: Int = 5)
        = WProcessBar(direction, bg, bar, value, maxValue)