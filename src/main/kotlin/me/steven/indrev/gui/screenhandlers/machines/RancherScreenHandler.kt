package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import io.netty.buffer.Unpooled
import me.steven.indrev.WCustomTabPanel
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.RancherBlockEntity
import me.steven.indrev.gui.properties.SyncableProperty
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.RANCHER_HANDLER
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.packets.common.UpdateRancherConfigPacket
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class RancherScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        RANCHER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    var value = -1

    val feedBabyText = WToggleButton()
    var feedBabies: Boolean = false

    val mateAdultsText = WToggleButton()
    var mateAdults: Boolean = false

    val matingLimitText = WTextField()
    var matingLimit: Int = 0

    val killAfterText = WTextField()
    var killAfter: Int = 0

    init {
        val root = WCustomTabPanel()

        setRootPanel(root)

        feedBabies = component!![RancherBlockEntity.FEED_BABIES_ID]
        mateAdults = component!![RancherBlockEntity.MATE_ADULTS]

        root.add(buildMainPanel()) { it.icon(ItemIcon(RANCHER_MK4.asItem())) }
        root.add(buildConfigPanel()) { it.icon(ItemIcon(IRItemRegistry.WRENCH)) }

        root.validate(this)
    }

    private fun buildMainPanel(): WWidget {
        val mainPanel = WGridPanel()
        configure("block.indrev.rancher", ctx, playerInventory, blockInventory, mainPanel, invPos = 4.45)

        val inputFrame = WSprite(identifier("textures/gui/input_frame.png"))
        mainPanel.add(inputFrame, 1.9, 0.7)
        inputFrame.setSize(40, 44)
        val outputFrame = WSprite(identifier("textures/gui/output_frame.png"))
        mainPanel.add(outputFrame, 5.1, 0.7)
        outputFrame.setSize(58, 62)

        mainPanel.add(
            WItemSlot.of(blockInventory, 6, 3, 3),
            5.2,
            1.0
        )
        mainPanel.add(
            WItemSlot.of(blockInventory, 2, 2, 2),
            2.0,
            1.0
        )

        val slider = WSlider(1, 9, Axis.HORIZONTAL)
        mainPanel.add(slider, 1.6, 3.6)
        slider.setSize(50, 20)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@run
            slider.value = blockEntity.range
        }
        slider.setValueChangeListener { newValue -> this.value = newValue }

        val text = WText({
            translatable("block.indrev.aoe.range", slider.value)
        }, HorizontalAlignment.LEFT)
        mainPanel.add(text, 1.8, 3.3)

        return mainPanel
    }

    private fun buildConfigPanel(): WWidget {
        val configPanel = WGridPanel()
        val feedBabyInput = WGridPanel()
        feedBabyInput.add(WLabel(literal("Feed babies")), 0.0, 0.25)

        feedBabyText.toggle = feedBabies
        feedBabyText.setOnToggle { v -> feedBabies = v }
        feedBabyInput.add(feedBabyText, 4.4, 0.0)
        configPanel.add(feedBabyInput, 1, 1)

        val mateAdultsInput = WGridPanel()
        mateAdultsInput.add(WLabel(literal("Mate adults")), 0.0, 0.25)

        mateAdultsText.toggle = mateAdults
        mateAdultsText.setOnToggle { v -> mateAdults = v }
        mateAdultsInput.add(mateAdultsText, 4.4, 0.0)
        configPanel.add(mateAdultsInput, 1, 3)

        val matingLimitInput = WGridPanel()
        matingLimitInput.add(WLabel(literal("Mating limit")), 0.0, 0.25)

        matingLimitInput.add(matingLimitText, 4, 0)
        matingLimitText.setSize(34, 18)
        configPanel.add(matingLimitInput, 1, 5)

        val killAfterInput = WGridPanel()
        killAfterInput.add(WLabel(literal("Kill after")), 0.0, 0.25)

        killAfterInput.add(killAfterText, 4, 0)
        killAfterText.setSize(34, 18)
        configPanel.add(killAfterInput, 1, 7)
        
        return configPanel
    }

    override fun onSyncedProperty(index: Int, property: SyncableProperty<*>) {
        when (index) {
            RancherBlockEntity.KILL_AFTER -> {
                killAfter = property.value as Int
                killAfterText.text = killAfter.toString()
            }
            RancherBlockEntity.MATING_LIMIT -> {
                matingLimit = property.value as Int
                matingLimitText.text = matingLimit.toString()
            }
            RancherBlockEntity.MATE_ADULTS -> {
                mateAdults = property.value as Boolean
                mateAdultsText.toggle = mateAdults
            }
            RancherBlockEntity.FEED_BABIES_ID -> {
                feedBabies = property.value as Boolean
                feedBabyText.toggle = feedBabies
            }
        }
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

    override fun close(player: PlayerEntity?) {
        super.close(player)
        AOEMachineBlockEntity.sendValueUpdatePacket(value, ctx)

        ctx.run { world, pos ->
            if (world.isClient) {
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeBlockPos(pos)
                buf.writeBoolean(feedBabies)
                buf.writeBoolean(mateAdults)
                buf.writeInt(matingLimitText.text.trim().toIntOrNull() ?: matingLimit)
                buf.writeInt(killAfterText.text.trim().toIntOrNull() ?: killAfter)
                ClientPlayNetworking.send(UpdateRancherConfigPacket.SYNC_RANCHER_CONFIG, buf)
            }
        }
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("rancher_screen")
        val RANCHER_MK4 by lazy { MachineRegistry.RANCHER_REGISTRY.block(Tier.MK4) }
    }
}