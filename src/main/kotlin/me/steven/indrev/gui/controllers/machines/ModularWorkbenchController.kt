package me.steven.indrev.gui.controllers.machines

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.WCustomTabPanel
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.mixin.common.AccessorSyncedGuiDescription
import me.steven.indrev.recipes.machines.ModuleRecipe
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.util.TriState
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import kotlin.math.floor

// Careful, here be dragons.
class ModularWorkbenchController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.MODULAR_WORKBENCH_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    private val craftingInventory = CraftingInventory(this, 3, 3)
    private val outputInventory = CraftingResultInventory()
    private val slotLayout = hashMapOf<Int, Array<WToggleableItemSlot>>()
    private var slotsPanel = WPlainPanel()
    var selected: ModuleRecipe? = null

    init {

        slotLayout[1] = arrayOf(WToggleableItemSlot(0, 2 * 18, 0, false))
        slotLayout[2] = arrayOf(
            WToggleableItemSlot(0, 0,2 * 18,false),
            WToggleableItemSlot(1, 4 * 18, 2 * 18, false)
        )
        slotLayout[3] = arrayOf(
            WToggleableItemSlot(0, 2 * 18, 0, false),
            WToggleableItemSlot(1, 0, 4 * 17, false),
            WToggleableItemSlot(2, 4 * 18, 4 * 17, false)
        )
        slotLayout[4] = arrayOf(
            WToggleableItemSlot(0, 2 * 18, 0, false),
            WToggleableItemSlot(1, 0, 2 * 18, false),
            WToggleableItemSlot(2, 4 * 18, 2 * 18, false),
            WToggleableItemSlot(3, 2 * 18, 4 * 18, false)
        )
        slotLayout[5] = arrayOf(
            WToggleableItemSlot(0, 2 * 18, 0, false),
            WToggleableItemSlot(1, 0, 2 * 18, false),
            WToggleableItemSlot(2, 4 * 18, 2 * 18, false),
            WToggleableItemSlot(3, 1 * 14, 4 * 18, false),
            WToggleableItemSlot(4, 3 * 20, 4 * 18, false)
        )

        slotLayout.forEach { (_, slots) -> slots.forEach { slotsPanel.add(it, it.x, it.y) } }

        val root = WCustomTabPanel()
        setRootPanel(root)

        root.add(WCustomTabPanel.Tab(null, ItemIcon(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4).asItem()), buildInstallPanel(), {}))
        root.add(WCustomTabPanel.Tab(null, ItemIcon(IRItemRegistry.PROTECTION_MODULE_ITEM), buildCraftPanel(), {}))

        root.validate(this)
    }

    private fun buildCraftPanel(): WGridPanel {
        val panel = WGridPanel()

        val modules = WGridPanel()
        ctx.run { world, _ ->
            world.recipeManager.getAllOfType(ModuleRecipe.TYPE).entries.forEachIndexed { index, (id, recipe) ->
                val button = WModule(recipe.outputs.first().stack)
                button.clickAction = {
                    val buf = PacketByteBufs.create()
                    buf.writeIdentifier(id)
                    ClientPlayNetworking.send(MODULE_SELECT_PACKET, buf)
                    layoutSlots(recipe)
                    slotsPanel.addPainters()
                    selected = recipe
                }
                modules.add(button, index % 3, floor(index / 3.0).toInt())
            }
        }

        val outputSlot = WCraftingItemSlot(outputInventory, 0, true)
        slotsPanel.add(outputSlot, 2 * 18, 2 * 18)

        panel.add(slotsPanel, 4, 0)

        val scrollPanel = WScrollPanel(modules)
        scrollPanel.isScrollingVertically = TriState.TRUE
        scrollPanel.isScrollingHorizontally = TriState.FALSE
        panel.add(scrollPanel, 0, 0, 4, 5)
        scrollPanel.setSize(3 * 18 + 8, 5 * 18 - 4)

        panel.add(createPlayerInventoryPanel(), 0, 5)

        return panel
    }

    private fun updateItems() {
        val list = (0 until craftingInventory.size()).map { craftingInventory.getStack(it) }
        var stack = ItemStack.EMPTY
        if (selected?.matches(list, null) == true) {
            stack = selected!!.craft(null as Random?).first()
        }
        outputInventory.setStack(0, stack)
        (playerInventory.player as ServerPlayerEntity).networkHandler.sendPacket(ScreenHandlerSlotUpdateS2CPacket(syncId, 54, stack))
    }

    override fun onContentChanged(inventory: Inventory?) {
        if (!world.isClient)
           updateItems()
        super.onContentChanged(inventory)
    }

    // I told you.
    override fun onSlotClick(slotNumber: Int, button: Int, action: SlotActionType, player: PlayerEntity?): ItemStack? {
        return if (action == SlotActionType.QUICK_MOVE) {
            if (slotNumber !in 0 until slots.size) ItemStack.EMPTY
            else {
                val slot = slots[slotNumber]
                if (slot != null && slot.canTakeItems(player)) {
                    var remaining = ItemStack.EMPTY
                    if (slot.hasStack()) {
                        val toTransfer = slot.stack
                        remaining = toTransfer.copy()
                        if (blockInventory != null) {
                            if (slot.inventory != playerInventory) {
                                if (insertItem(toTransfer, playerInventory, true, player))
                                    slot.onTakeItem(player, remaining)
                                else
                                    ItemStack.EMPTY
                            } else if (
                                !insertItem(toTransfer, craftingInventory, false, player)
                                && !insertItem(toTransfer, blockInventory, false, player)
                            ) ItemStack.EMPTY
                        } else if (!swapHotbar(toTransfer, slotNumber, playerInventory, player))
                            ItemStack.EMPTY
                        if (toTransfer.isEmpty) slot.stack = ItemStack.EMPTY
                        else slot.markDirty()
                    }
                    remaining
                } else {
                    ItemStack.EMPTY
                }
            }
        } else {
            super.onSlotClick(slotNumber, button, action, player)
        }.also { onContentChanged(blockInventory) }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun insertItem(
        toInsert: ItemStack?,
        inventory: Inventory?,
        walkBackwards: Boolean,
        player: PlayerEntity?
    ): Boolean = (this as AccessorSyncedGuiDescription).indrev_callInsertItem(toInsert, inventory, walkBackwards, player)

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun swapHotbar(
        toInsert: ItemStack?,
        slotNumber: Int,
        inventory: Inventory?,
        player: PlayerEntity?
    ): Boolean = (this as AccessorSyncedGuiDescription).indrev_callSwapHotbar(toInsert, slotNumber, inventory, player)

    fun layoutSlots(recipe: ModuleRecipe) {
        val inputs = recipe.input
        slotLayout.forEach { (size, slots) ->
            slots.forEachIndexed { index, slot ->
                slot.hidden = size != inputs.size
                if (!slot.hidden && FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                    slot.preview = inputs[index].ingredient.matchingStacksClient[0]
            }
        }
        ctx.run { world, _ ->
            dropInventory(playerInventory.player, world, craftingInventory)
            if (!world.isClient) updateItems()
        }
    }

    private fun buildInstallPanel(): WGridPanel {
        val root = WGridPanel()
        configure("block.indrev.modular_workbench", ctx, playerInventory, blockInventory, root)

        val armorSlot = WTooltipedItemSlot.of(blockInventory, 2, TranslatableText("gui.indrev.modular_armor_slot_type"))
        root.add(armorSlot, 1.5, 3.5)

        val moduleSlot = WTooltipedItemSlot.of(blockInventory, 1, TranslatableText("gui.indrev.module_slot_type"))
        root.add(moduleSlot, 1.5, 1.0)

        val process = createProcessBar(WBar.Direction.DOWN, PROCESS_VERTICAL_EMPTY, PROCESS_VERTICAL_FULL, 2, 3)
        root.add(process, 1.5, 2.2)

        val info = WStaticTooltip()
        info.setSize(100, 60)
        root.add(info, 3, 1)

        addTextInfo(root)

        return root
    }

    private fun addTextInfo(panel: WGridPanel) {
        val armorInfoText = WText({
            val stack = blockInventory.getStack(2)
            if (!stack.isEmpty)
                TranslatableText(stack.item.translationKey).formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE)
            else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        val moduleToInstall = WText({
            val (stack, item) = blockInventory.getStack(1)
            if (!stack.isEmpty && item is IRModuleItem) {
                TranslatableText(item.translationKey).formatted(Formatting.GRAY, Formatting.ITALIC)
            } else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        val modulesInstalled = WText({
            val (stack, item) = blockInventory.getStack(2)
            if (!stack.isEmpty && item is IRModularItem<*>) {
                val modules = item.getCount(stack).toString()
                MODULE_COUNT().append(LiteralText(modules).formatted(Formatting.WHITE))
            } else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        val shield = WText({
            val (stack, item) = blockInventory.getStack(2)
            if (!stack.isEmpty && item is IRModularArmorItem) {
                val shield = item.getMaxShield(ArmorModule.PROTECTION.getLevel(stack)).toString()
                SHIELD_TEXT().append(LiteralText(shield).formatted(Formatting.WHITE))
            } else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        val installing = WText({
            val state = ModularWorkbenchBlockEntity.State.values()[propertyDelegate[4]]
            if (state == ModularWorkbenchBlockEntity.State.INSTALLING) {
                INSTALLING_TEXT()
            } else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        val progress = WText({
            val stack = blockInventory.getStack(1)
            val item = stack.item
            if (!stack.isEmpty && item is IRModuleItem) {
                val progress = propertyDelegate!![2]
                when (ModularWorkbenchBlockEntity.State.values()[propertyDelegate[4]]) {
                    ModularWorkbenchBlockEntity.State.INCOMPATIBLE -> INCOMPATIBLE_TEXT()
                    ModularWorkbenchBlockEntity.State.MAX_LEVEL -> MAX_LEVEL_TEXT()
                    else -> {
                        val percent = ((progress / 1200f) * 100).toInt()
                        PROGRESS_TEXT().append(LiteralText("$percent%"))
                    }
                }
            } else LiteralText.EMPTY
        }, HorizontalAlignment.LEFT)

        panel.add(installing, 3, 3)
        panel.add(progress, 3, 4)
        panel.add(armorInfoText, 3, 1)
        panel.add(modulesInstalled, 3.0, 1.5)
        panel.add(shield, 3, 2)
        panel.add(moduleToInstall, 3.0, 3.5)
    }

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        val offset = 178 - rootPanel.width
        (rootPanel as WCustomTabPanel).setForceBackgroundPainter(
            BackgroundPainter.createLightDarkVariants(
                NinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8).setLeftPadding(0)
                    .setRightPadding(offset).setTopPadding(-25),
                NinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8)
                    .setRightPadding(offset)
            ))
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        ctx.run { world, _ ->
            dropInventory(player, world, craftingInventory)
        }
    }

    inner class WModule(private val itemStack: ItemStack) : WWidget() {

        var clickAction: () -> Unit = {}

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            val hovered = mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height
            ScreenDrawing.drawBeveledPanel(x, y, width, height)
            if (hovered) ScreenDrawing.coloredRect(x, y, width, height, 0x887d88ff.toInt())
            MinecraftClient.getInstance().itemRenderer.renderInGui(itemStack, x + 1, y + 1)
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            tooltip?.add(itemStack.name)
        }

        override fun onClick(x: Int, y: Int, button: Int) {
            MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            clickAction()
        }
    }

    inner class WToggleableItemSlot(index: Int, x: Int, y: Int, big: Boolean)
        : WItemSlot(craftingInventory, index, 1, 1, big) {

        var preview: ItemStack? = null

        init {
            this.x = x
            this.y = y
        }

        var hidden = true

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            if (!hidden) {
                super.paint(matrices, x, y, mouseX, mouseY)
                if (preview != null) {
                    val renderer = MinecraftClient.getInstance().itemRenderer
                    renderer.renderInGui(preview, x + 1, y + 1)
                    RenderSystem.disableDepthTest()
                    ScreenDrawing.coloredRect(x + 1, y + 1, 16, 16, 0xb08b8b8b.toInt())
                }
            }
        }

        override fun createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot {
            return WToggleableSlot(inventory, index, x, y) { hidden }
        }
    }
    inner class WCraftingItemSlot(inventory: Inventory, index: Int, big: Boolean)
        : WItemSlot(inventory, index, 1, 1, big) {

        init {
            isInsertingAllowed = false
        }

        override fun createSlotPeer(inventory: Inventory?, index: Int, x: Int, y: Int): ValidatedSlot {
            return object : ValidatedSlot(inventory, index, x, y) {
                override fun onTakeItem(player: PlayerEntity, stack: ItemStack): ItemStack {
                    val remainders = selected!!.getRemainingStacks(craftingInventory)

                    for (slot in remainders.indices) {
                        var itemStack = craftingInventory.getStack(slot)
                        val rem = remainders[slot]
                        if (!itemStack.isEmpty) {
                            craftingInventory.removeStack(slot, 1)
                            itemStack = craftingInventory.getStack(slot)
                        }
                        if (!rem.isEmpty) {
                            when {
                                itemStack.isEmpty -> craftingInventory.setStack(slot, rem)
                                ItemStack.areItemsEqualIgnoreDamage(itemStack, rem)
                                        && ItemStack.areTagsEqual(itemStack, rem) -> {
                                    rem.increment(itemStack.count)
                                    craftingInventory.setStack(slot, rem)
                                }
                                !player.inventory.insertStack(rem) -> player.dropItem(rem, false)
                            }
                        }
                    }
                    markDirty()
                    return stack
                }
            }
        }
    }

    class WToggleableSlot(inventory: Inventory, index: Int, x: Int, y: Int, val hidden: () -> Boolean) : ValidatedSlot(inventory, index, x, y) {

        override fun canInsert(stack: ItemStack?): Boolean {
            return !hidden() && super.canInsert(stack)
        }

        @Environment(EnvType.CLIENT)
        override fun doDrawHoveringEffect(): Boolean {
            return !hidden() && super.doDrawHoveringEffect()
        }
    }

    companion object {
        val SCREEN_ID = identifier("modular_workbench_screen")
        val MODULE_SELECT_PACKET = identifier("module_select_packet")
        val SHIELD_TEXT = { TranslatableText("gui.indrev.shield").formatted(Formatting.BLUE) }
        val PROGRESS_TEXT = { TranslatableText("gui.indrev.progress").formatted(Formatting.BLUE) }
        val MODULE_COUNT = { TranslatableText("gui.indrev.modules_installed").formatted(Formatting.BLUE) }
        val INSTALLING_TEXT = { TranslatableText("gui.indrev.installing").formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE) }
        val INCOMPATIBLE_TEXT = { TranslatableText("gui.indrev.incompatible").formatted(Formatting.RED) }
        val MAX_LEVEL_TEXT = { TranslatableText("gui.indrev.max_level").formatted(Formatting.RED) }
    }

}