package me.steven.indrev.gui.screenhandlers.machines

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import me.steven.indrev.WCustomTabPanel
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.MODULAR_WORKBENCH_HANDLER
import me.steven.indrev.gui.widgets.machines.WCustomBar
import me.steven.indrev.gui.widgets.machines.upProcessBar
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.packets.common.SelectModuleOnWorkbenchPacket
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
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.sound.SoundEvents
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import kotlin.math.floor
import kotlin.math.sin

// Careful, here be dragons.
// All the `index + 3` are because the slot numbers changed and it was easier to do this, if you don't like it don't read it :)
class ModularWorkbenchScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        MODULAR_WORKBENCH_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    private val slotLayout = hashMapOf<Int, Array<WToggleableItemSlot>>()
    private var slotsPanel = WPlainPanel()

    private val selected: ModuleRecipe?
        get() {
            var r: ModuleRecipe? = null
            ctx.run { world, pos ->
                val blockEntity = world.getBlockEntity(pos) as? ModularWorkbenchBlockEntity
                r = blockEntity?.recipe
            }
            return r
        }

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
        slotLayout[6] = arrayOf(
            WToggleableItemSlot(0, 2 * 18, 0, false),
            WToggleableItemSlot(1, 0 * 18, 1 * 18, false),
            WToggleableItemSlot(2, 4 * 18, 1 * 18, false),
            WToggleableItemSlot(3, 0 * 14, 3 * 18, false),
            WToggleableItemSlot(4, 4 * 18, 3 * 18, false),
            WToggleableItemSlot(5, 2 * 18, 4 * 18, false)
        )

        slotLayout.forEach { (_, slots) -> slots.forEach { slotsPanel.add(it, it.x, it.y) } }

        if (selected != null)
            layoutSlots(selected!!)

        val root = WCustomTabPanel()
        setRootPanel(root)

        root.add(buildInstallPanel()) { it.icon(ItemIcon(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4).asItem())) }
        root.add(buildCraftPanel()) { it.icon(ItemIcon(IRItemRegistry.PROTECTION_MODULE_ITEM)) }
        root.validate(this)
    }

    private fun buildCraftPanel(): WGridPanel {
        val panel = WGridPanel()

        val modules = WGridPanel()
        ctx.run { world, pos ->
            world.recipeManager.getRecipes(ModuleRecipe.TYPE).entries.forEachIndexed { index, (id, recipe) ->
                val button = WModule(recipe.outputs.first().stack)
                button.clickAction = {
                    val buf = PacketByteBufs.create()
                    buf.writeInt(syncId)
                    buf.writeIdentifier(id)
                    buf.writeBlockPos(pos)
                    ClientPlayNetworking.send(SelectModuleOnWorkbenchPacket.MODULE_SELECT_PACKET, buf)
                    layoutSlots(recipe)
                    slotsPanel.addPainters()
                }
                modules.add(button, index % 3, floor(index / 3.0).toInt())
            }
        }

        val outputSlot = WCraftingItemSlot(blockInventory, 15, true)
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

    fun layoutSlots(recipe: ModuleRecipe) {
        val inputs = recipe.input
        slotLayout.forEach { (size, slots) ->
            slots.forEachIndexed { index, slot ->
                slot.hidden = size != inputs.size
                if (!slot.hidden && FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                    slot.preview = inputs[index].ingredient.matchingStacks[0]
            }
        }
    }

    private fun buildInstallPanel(): WGridPanel {
        val root = WGridPanel()
        configure("block.indrev.modular_workbench", ctx, playerInventory, blockInventory, root, invPos = 5.0, widgetPos = 0.9)

        val armorSlot = WTooltipedItemSlot.of(blockInventory, 2, translatable("gui.indrev.modular_armor_slot_type"))
        root.add(armorSlot, 1.5, 3.5)

        val moduleSlot = WTooltipedItemSlot.of(blockInventory, 1, translatable("gui.indrev.module_slot_type"))
        root.add(moduleSlot, 1.5, 1.0)

        val process = query<ModularWorkbenchBlockEntity, WCustomBar> { upProcessBar(it, ModularWorkbenchBlockEntity.INSTALL_TIME_ID, ModularWorkbenchBlockEntity.MAX_INSTALL_TIME_ID) }
        root.add(process, 1.5, 2.2)

        val info = WStaticTooltip()
        info.setSize(100, 60)
        root.add(info, 3, 1)

        addTextInfo(root)

        return root
    }

    // Are you seriously still going?
    private fun addTextInfo(panel: WGridPanel) {
        val armorInfoText = WText({
            val stack = blockInventory.getStack(2)
            if (!stack.isEmpty)
                translatable(stack.item.translationKey).formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE)
            else EMPTY
        }, HorizontalAlignment.LEFT)

        val moduleToInstall = WText({
            val (stack, item) = blockInventory.getStack(1)
            if (!stack.isEmpty && item is IRModuleItem) {
                translatable(item.translationKey).formatted(Formatting.GRAY, Formatting.ITALIC)
            } else EMPTY
        }, HorizontalAlignment.LEFT)

        val modulesInstalled = WText({
            val (stack, item) = blockInventory.getStack(2)
            if (!stack.isEmpty && item is IRModularItem<*>) {
                val modules = item.getCount(stack).toString()
                MODULE_COUNT().append(literal(modules).formatted(Formatting.WHITE))
            } else EMPTY
        }, HorizontalAlignment.LEFT)

        val shield = WText({
            val (stack, item) = blockInventory.getStack(2)
            if (!stack.isEmpty && item is IRModularArmorItem) {
                val shield = item.getMaxShield(ArmorModule.PROTECTION.getLevel(stack)).toString()
                SHIELD_TEXT().append(literal(shield).formatted(Formatting.WHITE))
            } else EMPTY
        }, HorizontalAlignment.LEFT)

        val installing = WText({
            val state = component!!.get<ModularWorkbenchBlockEntity.State>(ModularWorkbenchBlockEntity.STATE_ID)
            if (state == ModularWorkbenchBlockEntity.State.INSTALLING) {
                INSTALLING_TEXT()
            } else EMPTY
        }, HorizontalAlignment.LEFT)

        val progress = WText({
            val stack = blockInventory.getStack(1)
            val item = stack.item
            if (!stack.isEmpty && item is IRModuleItem) {
                val progress = component!!.get<Int>(ModularWorkbenchBlockEntity.INSTALL_TIME_ID)
                when (component!!.get<ModularWorkbenchBlockEntity.State>(ModularWorkbenchBlockEntity.STATE_ID)) {
                    ModularWorkbenchBlockEntity.State.INCOMPATIBLE -> INCOMPATIBLE_TEXT()
                    ModularWorkbenchBlockEntity.State.MAX_LEVEL -> MAX_LEVEL_TEXT()
                    else -> {
                        val percent = ((progress / component!!.get<Int>(ModularWorkbenchBlockEntity.MAX_INSTALL_TIME_ID).toDouble().coerceAtLeast(1.0)) * 100).toInt()
                        PROGRESS_TEXT().append(literal("$percent%"))
                    }
                }
            } else EMPTY
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
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8).setLeftPadding(0)
                    .setRightPadding(offset).setTopPadding(-25),
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8)
                    .setRightPadding(offset)
            ))
    }

    inner class WModule(private val itemStack: ItemStack) : WWidget() {

        var clickAction: () -> Unit = {}

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            val hovered = mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height
            ScreenDrawing.drawBeveledPanel(matrices, x, y, width, height)
            if (hovered) ScreenDrawing.coloredRect(matrices, x, y, width, height, 0x887d88ff.toInt())
            MinecraftClient.getInstance().itemRenderer.renderInGui(itemStack, x + 1, y + 1)
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            val texts = itemStack.getTooltip(MinecraftClient.getInstance().player)
            { MinecraftClient.getInstance().options.advancedItemTooltips }
            tooltip?.add(*texts.toTypedArray())
        }

        override fun onClick(x: Int, y: Int, button: Int): InputResult {
            MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            clickAction()
            return InputResult.PROCESSED
        }
    }

    inner class WToggleableItemSlot(private val index: Int, x: Int, y: Int, big: Boolean)
        : WItemSlot(blockInventory, index + 3, 1, 1, big) {

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
                    ScreenDrawing.coloredRect(matrices, x + 1, y + 1, 16, 16, 0xb08b8b8b.toInt())
                }
            }
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            if (!blockInventory.getStack(index + 3).isEmpty) return
            val texts = preview?.getTooltip(MinecraftClient.getInstance().player)
            { MinecraftClient.getInstance().options.advancedItemTooltips } ?: return
            tooltip?.add(*texts.toTypedArray())
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

        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
            super.paint(matrices, x, y, mouseX, mouseY)
            if (selected != null) {
                val cur = component!!.get<Int>(ModularWorkbenchBlockEntity.PROCESS_TIME_ID)
                val max = component!!.get<Int>(ModularWorkbenchBlockEntity.MAX_PROCESS_TIME_ID)
                val renderer = MinecraftClient.getInstance().itemRenderer
                renderer.renderInGui(selected!!.outputs[0].stack, x + 1, y + 1)
                RenderSystem.disableDepthTest()
                val a = 255 - ((cur / max.toDouble()) * 255).toInt()
                ScreenDrawing.coloredRect(matrices, x + 1, y + 1, 16, 16, a shl 24 or 0x8b8b8b)
                RenderSystem.enableDepthTest()

                if (cur > 0 && max > 0 && cur != max) {
                    val txt = "${(cur / max.toDouble() * 100).toInt() }%"
                    val width = MinecraftClient.getInstance().textRenderer.getWidth(txt)
                    MinecraftClient.getInstance().textRenderer.draw(matrices, txt, x.toFloat() + 10 - (width / 2), y.toFloat() + 25, 0x404040)

                    val s = sin(world.time.toDouble() / 5) * 10
                    ScreenDrawing.coloredRect(matrices, x - 3, (y + s).toInt() + 8, this.width + 6, 2, 0x99578bfa.toInt())
                }
            }
        }

        override fun addTooltip(tooltip: TooltipBuilder?) {
            val texts = selected?.outputs?.get(0)?.stack?.getTooltip(MinecraftClient.getInstance().player)
            { MinecraftClient.getInstance().options.advancedItemTooltips } ?: return

            val cur = component!!.get<Int>(ModularWorkbenchBlockEntity.PROCESS_TIME_ID)
            val max = component!!.get<Int>(ModularWorkbenchBlockEntity.MAX_PROCESS_TIME_ID)
            if (max > 0 && cur != max) {
                tooltip?.add(literal("Crafting: "))
                tooltip?.add( EMPTY)
            }
            tooltip?.add(*texts.toTypedArray())
        }
    }

    inner class WToggleableSlot(inventory: Inventory, index: Int, x: Int, y: Int, val hidden: () -> Boolean) : ValidatedSlot(inventory, index, x, y) {

        override fun getMaxItemCount(): Int {
            return selected?.input?.get(index - 3)?.count ?: 0
        }

        override fun canInsert(stack: ItemStack?): Boolean {
            return !hidden() && super.canInsert(stack)
        }

        override fun isEnabled(): Boolean {
            return !hidden() && super.isEnabled()
        }
    }

    companion object {
        val SCREEN_ID = identifier("modular_workbench_screen")
        val SHIELD_TEXT = { translatable("gui.indrev.shield").formatted(Formatting.BLUE) }
        val PROGRESS_TEXT = { translatable("gui.indrev.progress").formatted(Formatting.BLUE) }
        val MODULE_COUNT = { translatable("gui.indrev.modules_installed").formatted(Formatting.BLUE) }
        val INSTALLING_TEXT = { translatable("gui.indrev.installing").formatted(Formatting.DARK_PURPLE, Formatting.UNDERLINE) }
        val INCOMPATIBLE_TEXT = { translatable("gui.indrev.incompatible").formatted(Formatting.RED) }
        val MAX_LEVEL_TEXT = { translatable("gui.indrev.max_level").formatted(Formatting.RED) }
    }

}