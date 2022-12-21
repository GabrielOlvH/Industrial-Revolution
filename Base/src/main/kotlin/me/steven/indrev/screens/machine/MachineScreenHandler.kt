package me.steven.indrev.screens.machine

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafting.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.farming.BaseFarmBlockEntity
import me.steven.indrev.components.*
import me.steven.indrev.items.RangeCardItem
import me.steven.indrev.packets.common.ToggleAutoInputOutputPacket
import me.steven.indrev.packets.common.UpdateMachineIOPacket
import me.steven.indrev.screens.MACHINE_SCREEN_HANDLER
import me.steven.indrev.screens.widgets.*
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.Direction
import kotlin.math.pow

open class MachineScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val blockEntity: MachineBlockEntity<*>) : ScreenHandler(MACHINE_SCREEN_HANDLER, syncId) {

    val properties = mutableListOf<SyncableObject>()
    val widgets: MutableList<Widget> = mutableListOf()

    init {
        blockEntity.properties.properties.forEach { property ->
            property.isDirty = true
            properties.add(property)
        }
    }

    private var animationState = AnimationState.CLOSED
    private var openIoConfigAnimationProgress = 0.0
    private val openIoConfigButton = WidgetButton(identifier("textures/gui/icons/open_io_screen_icon.png"))
    private val ioConfig: WidgetGroup
    private val ioButtonsWidgets = mutableListOf<Widget>()
    private val typeWidgets = mutableListOf<WidgetIOConfigMode>()
    private var config = blockEntity.inventory.sidedConfiguration
    private var selectedType = ConfigurationTypes.ITEM
    private var autoInputCheckbox = WidgetCheckbox()
    private var autoOutputCheckbox = WidgetCheckbox()

    init {
        val ioWidgets = mutableListOf<Widget>()
        ioWidgets.add(WidgetSprite(identifier("textures/gui/small_gui.png"), 88, 132))
        ioConfig = WidgetGroup(ioWidgets)

        openIoConfigButton.tooltipBuilder = { tooltip ->
            if (openIoConfigButton.enabled) {
                if (animationState == AnimationState.CLOSED) tooltip.add(Text.literal("Open I/O configuration"))
                else tooltip.add(Text.literal("Close I/O configuration"))
            } else {
                tooltip.add(Text.literal("Install Automated Item/Fluid Transfer Upgrade").styled { s -> s.withColor(0xFF0000) })
            }
        }
        openIoConfigButton.disabledIcon = identifier("textures/gui/icons/disabled_open_io_screen_icon.png")

        MachineSide.values().forEach { side ->
            val dir = side.direction
            val button = object : WidgetButton(identifier("textures/block/electric_furnace.png")) {
                override fun draw(matrices: MatrixStack, x: Int, y: Int) {
                    val (a, r, g, b) = argb(
                        when {
                            config.getMode(dir).allowInput -> INPUT_COLOR
                            config.getMode(dir).allowOutput -> OUTPUT_COLOR
                            else -> -1
                        }
                    )
                    RenderSystem.setShaderColor(r / 255f, g / 255f, b / 255f, a / 255f)
                    RenderSystem.setShaderTexture(0, icon)
                    drawTexturedQuad(matrices.peek().positionMatrix, x, x + width, y, y + height, 0, side.u1/16f, side.u2/16f, side.v1/16f, side.v2/16f)
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
                }
            }
            button.click = { _, _, _ ->
                val mode = config.getMode(dir).next()
                val buf = PacketByteBufs.create()
                buf.writeBlockPos(blockEntity.pos)
                buf.writeInt(selectedType.ordinal)
                buf.writeInt(dir.id)
                buf.writeInt(mode.id)
                config.setMode(dir, mode)
                UpdateMachineIOPacket.send(buf)
            }

            ioButtonsWidgets.add(button)
            ioWidgets.add(button)
            button.x = 34 + -dir.offsetX * button.width
            button.y = 54 + -dir.offsetY * button.height
            if (side == MachineSide.BACK) {
                button.x += 18
                button.y += 18
            }
        }

        val types = ConfigurationTypes.values().filter { it.enabled(blockEntity) }
        types.forEachIndexed { index, type ->
            val ioConfigMode = WidgetIOConfigMode(type, index == 0, false)
            ioConfigMode.x = 44 - (types.size * 18) / 2 + index * 18
            ioConfigMode.y = 12
            ioConfigMode.click = { _, _, _ ->
                typeWidgets.forEach { s -> s.selected = s.type == type }
                config = type.provider(blockEntity)
                selectedType = type
            }
            typeWidgets.add(ioConfigMode)
            ioWidgets.add(ioConfigMode)
        }

        selectedType = types[0]

        openIoConfigButton.click = { _, _, _ ->
            animationState = if (animationState == AnimationState.CLOSED) {
                typeWidgets.forEach { it.shown = true }
                typeWidgets[0].selected = true
                AnimationState.OPENED
            } else {
                typeWidgets.forEach { it.shown = false }
                AnimationState.CLOSED
            }
        }

        autoInputCheckbox.x = 8
        autoInputCheckbox.y = 99
        autoInputCheckbox.onChange = { value ->
            config.autoInput = value
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(blockEntity.pos)
            buf.writeBoolean(value)
            buf.writeBoolean(config.autoOutput)
            buf.writeEnumConstant(selectedType)
            ToggleAutoInputOutputPacket.send(buf)
        }
        val autoInputText = WidgetText { Text.literal("Auto input").styled { s -> s.withColor(0xECECEC) } }
        autoInputText.x = 20
        autoInputText.y = 100

        autoOutputCheckbox.x = 8
        autoOutputCheckbox.y = 114
        autoOutputCheckbox.onChange = { value ->
            config.autoOutput = value
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(blockEntity.pos)
            buf.writeBoolean(config.autoInput)
            buf.writeBoolean(value)
            buf.writeEnumConstant(selectedType)
            ToggleAutoInputOutputPacket.send(buf)
        }
        val autoOutputText = WidgetText { Text.literal("Auto output").styled { s -> s.withColor(0xECECEC) } }
        autoOutputText.x = 20
        autoOutputText.y = 115


        ioWidgets.add(autoInputCheckbox)
        ioWidgets.add(autoInputText)
        ioWidgets.add(autoOutputCheckbox)
        ioWidgets.add(autoOutputText)

        add(openIoConfigButton, -30, 0)
        add(ioConfig, 18, 22)
    }

    fun addDefaultBackground() = add(WidgetSprite(DEFAULT_BG, 194, 201), -8, -11)

    fun addEnergyBar(blockEntity: MachineBlockEntity<*>) {
        add(WidgetBar.energyBar({ blockEntity.energy }, { blockEntity.capacity }), grid(8), grid(0) - 4)
    }

    fun addProcessBar(machine: CraftingMachineBlockEntity, crafter: MachineRecipeCrafter, text: MutableText, x: Int, y: Int) {
        add(WidgetBar.processBar({ crafter.processTime.toInt() }, { crafter.totalProcessTime }, { crafter.currentRecipe }, text, machine), x, y)
    }

    fun addUpgradeSlots(upgrades: MachineUpgrades, x: Int = 195-12, y: Int = 0) {
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
        widgets.add(WidgetSprite(identifier("textures/gui/range_card_slot.png"), 37, 43))

        val slotWidget = WidgetSlot(0, blockEntity.inventory, -1, false)
        slotWidget.filter = { stack -> stack.item is RangeCardItem }
        slotWidget.slotTexture = null
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
        add(WidgetGroup(widgets), x, y)
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
                addSlot(Slot(playerInventory, j + i * 9 + 9, 9 + j * 18, 84 + i * 18 + 15))
            }
        }
        for (i in 0 until 9) {
            addSlot(Slot(playerInventory, i, 9 + i * 18, 142 + 15))
        }
    }

    private fun tickAnimation() {
        if (animationState == AnimationState.OPENED && openIoConfigAnimationProgress < 1.0) {
            openIoConfigAnimationProgress += 0.1
            if (openIoConfigAnimationProgress >= 1.0) {
                openIoConfigAnimationProgress = 1.0
            }
        } else if (animationState == AnimationState.CLOSED && openIoConfigAnimationProgress > 0.0) {
            openIoConfigAnimationProgress -= 0.1
            if (openIoConfigAnimationProgress <= 0.0) {
                openIoConfigAnimationProgress = 0.0
            }
        } else return

        val c1 = 1.70158
        val c3 = c1 + 1
        ioConfig.x = 18+(-118 * (1 + c3 * (openIoConfigAnimationProgress-1).pow(3.0) + c1 * (openIoConfigAnimationProgress-1).pow(2))).toInt()
    }

    fun tick() {
        tickAnimation()

        openIoConfigButton.enabled = blockEntity.upgrades.contains(Upgrade.AUTOMATED_FLUID_TRANSFER) || blockEntity.upgrades.contains(Upgrade.AUTOMATED_ITEM_TRANSFER)
        if (!openIoConfigButton.enabled) {
            animationState = AnimationState.CLOSED
            typeWidgets.forEach { it.shown = false }
        }
        typeWidgets.forEach { type ->
            type.enabled = type.type.canModify(blockEntity)
            if (!type.enabled && type.selected) {
                type.selected = false
                val w = typeWidgets.firstOrNull { it.enabled }
                if (w != null) {
                    w.selected = true
                    selectedType = w.type
                    config = w.type.provider(blockEntity)
                }
            }
        }
        autoInputCheckbox.checked = config.autoInput
        autoOutputCheckbox.checked = config.autoOutput
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
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