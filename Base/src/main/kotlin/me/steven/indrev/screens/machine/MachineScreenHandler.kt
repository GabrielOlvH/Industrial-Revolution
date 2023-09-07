package me.steven.indrev.screens.machine

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafting.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.farming.BaseFarmBlockEntity
import me.steven.indrev.components.*
import me.steven.indrev.items.RangeCardItem
import me.steven.indrev.screens.MACHINE_SCREEN_HANDLER
import me.steven.indrev.screens.widgets.*
import me.steven.indrev.utils.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

class MachineScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val blockEntity: MachineBlockEntity<*>) : ScreenHandler(MACHINE_SCREEN_HANDLER, syncId) {

    val properties = mutableListOf<SyncableObject>()
    val background = mutableListOf<WidgetSprite>()
    val widgets: MutableList<Widget> = mutableListOf()
    var extended = false

    init {
        blockEntity.properties.properties.forEach { property ->
            property.isDirty = true
            properties.add(property)
        }
    }

    val ioHelper: IOTabHelper = IOTabHelper(blockEntity)

    init {
        ioHelper.addWidgets(this)
    }

    fun addEnergyBar(blockEntity: MachineBlockEntity<*>, highlight: () -> Boolean = { false }) {
        val w = WidgetBar.energyBar({ blockEntity.energy }, { blockEntity.capacity })
        w.highlight = highlight
        add(w, grid(8) + if (extended) 23*2 else 0, grid(0) - 4)
    }

    fun addProcessBar(machine: CraftingMachineBlockEntity, crafter: MachineRecipeCrafter, text: MutableText, x: Int, y: Int) {
        add(WidgetBar.processBar({ crafter.processTime.toInt() }, { crafter.totalProcessTime }, { crafter.currentRecipe }, crafter.troubleshooter, text, machine), x, y)
    }

    fun addUpgradeSlots(upgrades: MachineUpgrades, x: Int = (if (extended) 241 else 195) -12, y: Int = 0) {
        val widgets = mutableListOf<Widget>()
        widgets.add(WidgetSprite(identifier("textures/gui/upgrade_slots.png"), 37, 86))
        upgrades.inventory.forEachIndexed { index, _ ->
            val slotWidget = WidgetSlot(index, upgrades.inventory, -1, false)
            slotWidget.slotTexture = WidgetSlot.ITEM_SLOT_TEXTURE
            slotWidget.x = 12
            slotWidget.y = 7 + index*18
            slotWidget.width = 18
            slotWidget.height = 18
            slotWidget.overlay = identifier("textures/gui/icons/upgrade_icon.png")
            widgets.add(slotWidget)
        }
        add(WidgetGroup(widgets), x, y)
    }

    fun addRangeCardSlot(blockEntity: BaseFarmBlockEntity<*>, x: Int = 195-12, y: Int = 90) {
        val widgets = mutableListOf<Widget>()
        background.add(WidgetSprite(identifier("textures/gui/range_card_panel.png"), 37, 43).also {
            it.x = x
            it.y = y
        })
        val slotWidget = WidgetSlot(0, blockEntity.inventory, -1, false)
        slotWidget.highlight = { blockEntity.troubleshooter.contains(Troubleshooter.NO_RANGE) }
        slotWidget.filter = { stack -> stack.item is RangeCardItem }
        slotWidget.slotTexture = identifier("textures/gui/range_card_slot.png")
        slotWidget.x = 12
        slotWidget.y = 7
        slotWidget.width = 18
        slotWidget.height = 18
        widgets.add(slotWidget)
        val checkbox = WidgetCheckbox()
        checkbox.y = 27
        checkbox.x = 16
        checkbox.checked = blockEntity.renderWorkingArea
        checkbox.onChange = { c -> blockEntity.renderWorkingArea = c }
        checkbox.tooltipBuilder = { tooltip ->
            tooltip.add(Text.literal("Show range in world"))
        }
        widgets.add(checkbox)
        val group = WidgetGroup(widgets)
        group.width = 37
        group.height = 43
        add(group, x, y)
    }

    fun addTemperatureBar(temperatureController: MachineTemperatureController) {
        add(WidgetBar.temperatureBar({ temperatureController.temperature.toLong() }, { temperatureController.capacity.toLong() }, temperatureController.average, { temperatureController.heating }, { !temperatureController.coolerInventory[0].isEmpty() }), grid(0) + 1, grid(0) -4)
        val slot = WidgetSlot(0, temperatureController.coolerInventory)
        slot.slotTexture = WidgetSlot.ITEM_SLOT_TEXTURE
        slot.overlay = identifier("textures/gui/icons/vent_icon.png")
        slot.width = 18
        slot.height = 18
        add(slot, grid(0), grid(3) +3)
    }

    fun add(w: Widget, x: Int, y: Int): MachineScreenHandler {
        w.x = x
        w.y = y
        widgets.add(w)
        w.validate(this)
        return this
    }

    fun addPlayerInventorySlots() {
        for (i in 0 until 3) {
            for (j in 0 until 9) {
                addSlot(Slot(playerInventory, j + i * 9 + 9, 9 + j * 18 + if (extended) 23 else 0, 84 + i * 18 + 15))
            }
        }
        for (i in 0 until 9) {
            addSlot(Slot(playerInventory, i, 9 + i * 18 + if (extended) 23 else 0, 142 + 15))
        }
    }

    override fun quickMove(player: PlayerEntity, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (index < 9) {
                if (!insertItem(itemStack2, 9, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 9, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }
            slot.onTakeItem(player, itemStack2)
        }

        return itemStack
    }

    override fun canUse(player: PlayerEntity): Boolean = true

    fun addSlot0(slot: Slot) {
        addSlot(slot)
    }

    companion object {
        val DEFAULT_BG = identifier("textures/gui/background.png")
        val EXTENDED_BG = identifier("textures/gui/extended_background.png")
    }

    enum class AnimationState {
        OPENED, CLOSED
    }


    enum class MachineSide(val x: Int, val y: Int, val direction: Direction, val u1: Float, val v1: Float, val u2: Float, val v2: Float) {
        FRONT(1, 1, Direction.NORTH, 5.333f, 5.333f, 10.666f, 10.666f),
        LEFT(0, 1, Direction.EAST, 0.0f, 5.333f, 5.332f, 10.666f),
        BACK(2, 2, Direction.SOUTH, 10.667f, 10.667f, 16.0f, 16f),
        RIGHT(2, 1, Direction.WEST, 10.667f, 5.333f, 16.0f, 10.665f),
        TOP(1, 0, Direction.UP, 5.333f, 0.0f, 10.666f, 5.333f),
        BOTTOM(1, 2, Direction.DOWN, 5.333f, 10.667f, 10.666f, 15.998f)
    }
}

fun grid(i: Int) = 8 + 18 * i