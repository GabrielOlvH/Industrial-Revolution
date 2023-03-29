package me.steven.indrev.gui.screenhandlers.modular

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import io.netty.buffer.Unpooled
import me.steven.indrev.WCustomTabPanel
import me.steven.indrev.gui.widgets.misc.WPlayerRender
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.items.energy.IRGamerAxeItem
import me.steven.indrev.packets.common.UpdateModularToolLevelPacket
import me.steven.indrev.tools.modular.IRModularItem
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.network.PacketByteBuf
import me.steven.indrev.utils.literal
import me.steven.indrev.utils.translatable
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class ModularItemConfigurationScreenHandler(playerInventory: PlayerInventory) : LightweightGuiDescription() {

    val iconProvider: (Item) -> Icon? = { item ->
        val id = Registry.ITEM.getId(item)
        val textureId = Identifier(id.namespace, "textures/item/${id.path}.png")
        Icon { _, x, y, size ->
            if (item is IRGamerAxeItem)
                ScreenDrawing.texturedRect(MatrixStack(), x, y, size, size, textureId, 0f, 0f, 1f, 0.14285714f, -1)
            else
                ScreenDrawing.texturedRect(MatrixStack(), x, y, size, size, textureId, -1)
        }
    }

    init {
        val root = WCustomTabPanel()
        this.rootPanel = root

        (playerInventory.size() - 1 downTo 0)
            .associateWith { slot -> playerInventory.getStack(slot) }
            .filter { (_, stack) -> stack.item is IRModularItem<*> }
            .forEach { (slot, stack) ->
                val tabPanel = WBox(Axis.HORIZONTAL)
                tabPanel.spacing = 10
                val item = stack.item as IRModularItem<*>
                item.getInstalled(stack).forEach { module ->
                    val moduleItem = module.item.asItem()
                    val icon = iconProvider(moduleItem!!)
                    val moduleBox = WBox(Axis.VERTICAL)
                    moduleBox.add(object : WWidget() {
                        override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
                            icon?.paint(matrices, x, y, width)
                        }

                        override fun addTooltip(tooltip: TooltipBuilder?) {
                            tooltip?.add(translatable(moduleItem.translationKey))
                        }
                    })
                    val maxLevel = module.getMaxInstalledLevel(stack)
                    val slider = object : WSlider(0, maxLevel, Axis.HORIZONTAL) {
                        override fun addTooltip(tooltip: TooltipBuilder?) {
                            tooltip?.add(literal("${value * 100 / maxLevel}%"))
                        }
                    }
                    slider.value = module.getLevel(stack)
                    slider.setDraggingFinishedListener { value ->
                        val buf = PacketByteBuf(Unpooled.buffer())
                        buf.writeString(module.key)
                        buf.writeInt(value)
                        buf.writeInt(slot)
                        ClientPlayNetworking.send(UpdateModularToolLevelPacket.UPDATE_MODULAR_TOOL_LEVEL, buf)
                    }
                    moduleBox.add(slider)
                    tabPanel.add(moduleBox)
                }

                tabPanel.setSize(tabPanel.width + 65, tabPanel.height)

                root.add(
                    WCustomTabPanel.Tab(
                        null,
                        iconProvider(stack.item),
                        tabPanel,
                        { it.add(translatable(stack.item.translationKey)) })
                )
            }

        val playerBg = WStaticTooltip()
        root.add(playerBg, -50, 4)
        playerBg.setSize(40, 65)

        val playerWidget = WPlayerRender()
        root.add(playerWidget, -30, 64)

        root.validate(this)
    }
}