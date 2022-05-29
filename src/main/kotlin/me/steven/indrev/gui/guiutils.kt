package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.widgets.machines.energyBar
import me.steven.indrev.gui.widgets.machines.temperatureBar
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.items.upgrade.IREnhancerItem
import me.steven.indrev.packets.common.ToggleFactoryStackSplittingPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import me.steven.indrev.utils.translatable
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun WGridPanel.add(w: WWidget, x: Double, y: Double, width: Double, height: Double) {
    this.add(w, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun WGridPanel.add(w: WWidget, x: Double, y: Double) {
    this.add(w, x.toInt(), y.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

val SPLIT_ON_ICON = identifier("textures/gui/split_on.png")
val SPLIT_OFF_ICON = identifier("textures/gui/split_off.png")

fun SyncedGuiDescription.configure(
    titleId: String,
    ctx: ScreenHandlerContext,
    playerInventory: PlayerInventory,
    blockInventory: Inventory,
    panel: WGridPanel = rootPanel as WGridPanel,
    invPos: Double = 4.0,
    widgetPos: Double = 0.15
) {
    panel.setSize(150, 120)
    panel.add(createPlayerInventoryPanel(), 0.0, invPos)
    val title = WText(translatable(titleId), HorizontalAlignment.CENTER, 0x404040)
    var titlePos = 4.7

    ctx.run { world, blockPos ->
        val blockEntity = world.getBlockEntity(blockPos) as BaseBlockEntity

        val energyWidget = energyBar(blockEntity)
        panel.add(energyWidget, 0.1, widgetPos)

        if (blockEntity is MachineBlockEntity<*> && blockEntity.enhancerComponent != null) {
            addUpgradeSlots(blockEntity, blockInventory, world, panel)
        }

        if (blockEntity is MachineBlockEntity<*> && blockEntity.temperatureComponent != null) {
            titlePos += 0.5
            addTemperatureWidget(blockEntity, panel, blockInventory, world, widgetPos)
        }

        if (blockEntity is AOEMachineBlockEntity<*>) {
            addAOEWidgets(world, blockEntity, panel)
        }

        if (blockEntity is CraftingMachineBlockEntity<*> && blockEntity.craftingComponents.size > 1) {
            addSplitStackButton(blockEntity, blockPos, world, panel)
        }
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
            tooltip?.add(translatable("gui.indrev.button.auto_split"))
        }
    }
    button.setOnClick {
        blockEntity.isSplitOn = !blockEntity.isSplitOn
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBlockPos(blockPos)
        ClientPlayNetworking.send(ToggleFactoryStackSplittingPacket.SPLIT_STACKS_PACKET, buf)
    }
    if (world.isClient)
        buttonPanel.backgroundPainter = UPGRADE_SLOT_PANEL_PAINTER
    buttonPanel.add(button, 0, 0)
    panel.add(buttonPanel, 9.7, 4.2)
    button.setSize(20, 20)
}

fun addUpgradeSlots(blockEntity: MachineBlockEntity<*>, blockInventory: Inventory, world: World, panel: WGridPanel) {
    val enhancerComponent = blockEntity.enhancerComponent!!
    val slotPanel = WGridPanel()
    for ((i, slot) in enhancerComponent.slots.withIndex()) {
        val s =
            object : WTooltipedItemSlot(inventory = blockInventory, startIndex = slot, emptyTooltip = mutableListOf(translatable("gui.indrev.upgrade_slot_type"))) {
                override fun createSlotPeer(inventory: Inventory?, index: Int, x: Int, y: Int): ValidatedSlot {
                    return object : ValidatedSlot(inventory, index, x, y) {
                        override fun getMaxItemCount(stack: ItemStack): Int {
                            val upgrade = (stack.item as? IREnhancerItem)?.enhancer ?: return 0
                            return enhancerComponent.maxSlotCount(upgrade)
                        }
                    }
                }
            }
        if (world.isClient)
            s.backgroundPainter = if (enhancerComponent.isLocked(slot, blockEntity.tier)) getLockedSlotPainter(
                blockInventory,
                slot
            ) else getUpgradeSlotPainter(blockInventory, slot)
        slotPanel.add(s, 0, i)
    }
    if (world.isClient)
        slotPanel.backgroundPainter = UPGRADE_SLOT_PANEL_PAINTER
    panel.add(slotPanel, 9.7, -0.25)
}

fun addTemperatureWidget(blockEntity: MachineBlockEntity<*>, panel: WGridPanel, blockInventory: Inventory, world: World, widgetPos: Double) {
    panel.add(temperatureBar(blockEntity), 0.95, widgetPos)
    val coolerSlot =
        WTooltipedItemSlot.of(blockInventory, blockEntity.inventoryComponent!!.inventory.coolerSlot!!, translatable("gui.indrev.cooler_slot_type"))
    if (world.isClient)
        coolerSlot.backgroundPainter = getCoolerSlotPainter(blockInventory, 1)
    panel.add(coolerSlot, 0.75, widgetPos + 2.6)
}

fun addAOEWidgets(world: World, blockEntity: AOEMachineBlockEntity<*>, panel: WGridPanel) {
    val buttonPanel = WGridPanel()
    val button = object : WButton() {
        override fun addTooltip(information: TooltipBuilder?) {
            information?.add(translatable("block.indrev.aoe.toggle.${blockEntity.renderWorkingArea}"))
        }
    }
    button.setOnClick {
        blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
    }
    if (world.isClient) {
        button.icon = Icon { matrices, x, y, _ ->
            ScreenDrawing.texturedRect(matrices,x + 1, y + 1, 16, 16, identifier("textures/gui/range_icon.png"), -1)
        }
        buttonPanel.backgroundPainter = UPGRADE_SLOT_PANEL_PAINTER
    }
    buttonPanel.add(button, 0, 0)
    panel.add(buttonPanel, 9.7, 4.2)
    button.setSize(20, 20)
}

fun WItemSlot.setPainterSafe(ctx: ScreenHandlerContext, painter: () -> BackgroundPainter) {
    ctx.run { world, _ ->
        if (world.isClient) this.backgroundPainter = painter()
    }
}

fun WItemSlot.setIcon(ctx: ScreenHandlerContext, inventory: Inventory, slot: Int, identifier: Identifier) {
    setPainterSafe(ctx) {
        BackgroundPainter { matrices, left, top, widget ->
            BackgroundPainter.SLOT.paintBackground(matrices, left, top, widget)
            if (inventory.getStack(slot).isEmpty)
                ScreenDrawing.texturedRect(matrices, left + 1, top + 1, 16, 16, identifier, -1)
        }
    }
}

val POWER_ICON_ID = identifier("textures/gui/power_icon.png")

fun getEnergySlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { matrices, left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(matrices, left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(matrices, left, top, 18, 18, POWER_ICON_ID, -1)
}

val VENT_ICON_ID = identifier("textures/gui/vent_icon.png")

fun getCoolerSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { matrices, left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(matrices, left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(matrices, left, top, 18, 18, VENT_ICON_ID, -1)
}

val UPGRADE_ICON_ID = identifier("textures/gui/upgrade_icon.png")

fun getUpgradeSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { matrices, left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(matrices, left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(matrices, left, top, 18, 18, UPGRADE_ICON_ID, -1)
}

val LOCKED_ICON_ID = identifier("textures/gui/locked_icon.png")

fun getLockedSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { matrices, left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(matrices, left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(matrices, left, top, 18, 18, LOCKED_ICON_ID, -1)
}
