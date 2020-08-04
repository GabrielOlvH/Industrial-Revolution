package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.*
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

        val batterySlot = IRTooltipedItemSlot.of(
            blockInventory,
            0,
            mutableListOf(
                TranslatableText("gui.indrev.battery_slot_type").formatted(
                    Formatting.BLUE,
                    Formatting.ITALIC
                )
            )
        )
        it.add(batterySlot, 0.0, 3.7)

        screenHandlerContext.run { world, blockPos ->
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is UpgradeProvider) {
                for ((i, slot) in blockEntity.getUpgradeSlots().withIndex()) {
                    val s = IRTooltipedItemSlot.of(
                        blockInventory,
                        slot,
                        mutableListOf(
                            TranslatableText("gui.indrev.upgrade_slot_type").formatted(
                                Formatting.BLUE,
                                Formatting.ITALIC
                            )
                        )
                    )
                    it.add(s, 8, i)
                }
            }
            if (blockEntity is MachineBlockEntity && blockEntity.temperatureComponent != null) {
                val controller = blockEntity.temperatureComponent!!
                it.add(TemperatureWidget(propertyDelegate, controller), 1, 0, 16, 64)
                val coolerSlot = IRTooltipedItemSlot.of(
                    blockInventory,
                    1,
                    mutableListOf(
                        TranslatableText("gui.indrev.cooler_slot_type").formatted(
                            Formatting.BLUE,
                            Formatting.ITALIC
                        )
                    )
                )
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
            addBookEntryShortcut(playerInventory, it, 7, 0)
        }
    }
}

fun PatchouliEntryShortcut.addBookEntryShortcut(playerInventory: PlayerInventory, panel: WGridPanel, x: Int, y: Int) {
    val containsBook =
        playerInventory.contains(ItemStack(Registry.ITEM[Identifier("patchouli:guide_book")]).also { stack ->
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
    panel.add(button, x, y)
    button.setSize(16, 16)
}