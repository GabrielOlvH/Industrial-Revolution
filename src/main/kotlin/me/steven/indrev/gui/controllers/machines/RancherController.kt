package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.WCustomTabPanel
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class RancherController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.RANCHER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {

    var value = -1

    var feedBabies: Boolean = false
    var mateAdults: Boolean = false
    val matingLimitText = WTextField()
    var matingLimit: Int = 0
    val killAfterText = WTextField()
    var killAfter: Int = 0

    init {
        val root = WCustomTabPanel()

        setRootPanel(root)

        feedBabies = propertyDelegate[4] == 1
        mateAdults = propertyDelegate[5] == 1
        matingLimit = propertyDelegate[6]
        killAfter = propertyDelegate[7]

        root.add(WCustomTabPanel.Tab(null, ItemIcon(RANCHER_MK4.asItem()), buildMainPanel(), {}))
        root.add(WCustomTabPanel.Tab(null, ItemIcon(IRRegistry.WRENCH), buildConfigPanel(), {}))

        root.validate(this)
    }

    private fun buildMainPanel(): WWidget {
        val mainPanel = WGridPanel()
        configure("block.indrev.rancher", ctx, playerInventory, blockInventory, mainPanel)

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
        mainPanel.add(slider, 1.6, 4.0)
        slider.setSize(50, 20)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@run
            slider.value = blockEntity.range
        }
        slider.setValueChangeListener { newValue -> this.value = newValue }

        val text = WText({
            TranslatableText("block.indrev.aoe.range", slider.value)
        }, HorizontalAlignment.LEFT)
        mainPanel.add(text, 1.8, 3.7)

        return mainPanel
    }

    private fun buildConfigPanel(): WWidget {
        val configPanel = WGridPanel()
        val feedBabyInput = WGridPanel()
        feedBabyInput.add(WLabel("Feed babies"), 0.0, 0.25)
        val feedBabyText = WToggleButton()
        feedBabyText.toggle = feedBabies
        feedBabyText.setOnToggle { v -> feedBabies = v }
        feedBabyInput.add(feedBabyText, 4.4, 0.0)
        configPanel.add(feedBabyInput, 1, 1)

        val mateAdultsInput = WGridPanel()
        mateAdultsInput.add(WLabel("Mate adults"), 0.0, 0.25)
        val mateAdultsText = WToggleButton()
        mateAdultsText.toggle = mateAdults
        mateAdultsText.setOnToggle { v -> mateAdults = v }
        mateAdultsInput.add(mateAdultsText, 4.4, 0.0)
        configPanel.add(mateAdultsInput, 1, 3)

        val matingLimitInput = WGridPanel()
        matingLimitInput.add(WLabel("Mating limit"), 0.0, 0.25)
        matingLimitText.text = matingLimit.toString()
        matingLimitInput.add(matingLimitText, 4, 0)
        matingLimitText.setSize(34, 18)
        configPanel.add(matingLimitInput, 1, 5)

        val killAfterInput = WGridPanel()
        killAfterInput.add(WLabel("Kill after"), 0.0, 0.25)
        killAfterText.text = killAfter.toString()
        killAfterInput.add(killAfterText, 4, 0)
        killAfterText.setSize(34, 18)
        configPanel.add(killAfterInput, 1, 7)
        
        return configPanel
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
        AOEMachineBlockEntity.sendValueUpdatePacket(value, ctx)

        ctx.run { world, pos ->
            if (world.isClient) {
                val buf = PacketByteBuf(Unpooled.buffer())
                buf.writeBlockPos(pos)
                buf.writeBoolean(feedBabies)
                buf.writeBoolean(mateAdults)
                buf.writeInt(matingLimitText.text.toIntOrNull() ?: matingLimit)
                buf.writeInt(killAfterText.text.toIntOrNull() ?: killAfter)
                ClientSidePacketRegistry.INSTANCE.sendToServer(SYNC_RANCHER_CONFIG, buf)
            }
        }
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/rancher")

    override fun getPage(): Int = 0

    companion object {
        val SCREEN_ID = identifier("rancher_screen")
        val SYNC_RANCHER_CONFIG = identifier("rancher_sync_config")
        val RANCHER_MK4 by lazy { MachineRegistry.RANCHER_REGISTRY.block(Tier.MK4) }
    }
}