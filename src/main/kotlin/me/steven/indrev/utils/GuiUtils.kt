package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.BookShortcutWidget
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.gui.widgets.TemperatureWidget
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import vazkii.patchouli.client.book.ClientBookRegistry

fun WGridPanel.add(w: WWidget, x: Double, y: Double, width: Double, height: Double) {
    this.add(w, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun WGridPanel.add(w: WWidget, x: Double, y: Double) {
    this.add(w, x.toInt(), y.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun SyncedGuiDescription.configure(
    titleId: String,
    screenHandlerContext: ScreenHandlerContext,
    playerInventory: PlayerInventory,
    blockInventory: Inventory,
    propertyDelegate: PropertyDelegate
) {
    (rootPanel as WGridPanel).also {
        it.setSize(150, 120)
        it.add(createPlayerInventoryPanel(), 0, 5)
        it.add(StringWidget(TranslatableText(titleId), HorizontalAlignment.CENTER, 0x404040), 4, 0)

        val energyWidget = EnergyWidget(screenHandlerContext)
        it.add(energyWidget, 0, 0, 16, 64)

        val batterySlot = WItemSlot.of(blockInventory, 0)
        it.add(batterySlot, 0.0, 3.7)

        screenHandlerContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is UpgradeProvider) {
                for ((i, slot) in blockEntity.getUpgradeSlots().withIndex()) {
                    val s = WItemSlot.of(blockInventory, slot)
                    it.add(s, 8, i)
                }
            }
            if (blockEntity is MachineBlockEntity && blockEntity.temperatureComponent != null) {
                val controller = blockEntity.temperatureComponent!!
                it.add(TemperatureWidget(propertyDelegate, controller), 1, 0, 16, 64)
                val coolerSlot = WItemSlot.of(blockInventory, 1)
                it.add(coolerSlot, 1.0, 3.7)
            }
            if (blockEntity is AOEMachineBlockEntity) {
                val button = object : WButton(TranslatableText("block.indrev.aoe.toggle.btn")) {
                    override fun addTooltip(information: MutableList<StringRenderable>?) {
                        information?.add(TranslatableText("block.indrev.aoe.toggle.${blockEntity.renderWorkingArea}"))
                    }
                }
                button.setOnClick {
                    blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
                }
                it.add(button, 8.0, 4.0)
            }
        }
        if (this is PatchouliEntryShortcut) {
            val containsBook =
                playerInventory.contains(ItemStack(Registry.ITEM[identifier("patchouli:guide_book")]).also { stack ->
                    stack.tag = CompoundTag().also { it.putString("patchouli:book", "indrev:indrev") }
                })
            val button = object : BookShortcutWidget() {
                override fun addTooltip(tooltip: MutableList<StringRenderable>?) {
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
            it.add(button, 7.5, 0.0)
            button.setSize(8, 8)
        }
    }
}