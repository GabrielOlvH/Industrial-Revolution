package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.generators.SolarGeneratorBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.misc.WDynamicSprite
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class SolarGeneratorScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        IndustrialRevolution.SOLAR_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.solar_generator", ctx, playerInventory, blockInventory)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? SolarGeneratorBlockEntity ?: return@run
            val sprite = WDynamicSprite { if (blockEntity.shouldGenerate()) GENERATING_ICON else NOT_GENERATING_ICON }
            root.add(sprite, 4.5, 2.0)
            sprite.setSize(19, 10)

            val text = WText({
                if (blockEntity.shouldGenerate()) TranslatableText("gui.indrev.solar.on")
                else TranslatableText("gui.indrev.heatgen.idle")
            }, HorizontalAlignment.CENTER, 0x404040)
            root.add(text, 4.5, 2.7)
        }

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/generators")

    override fun getPage(): Int = 2

    companion object {
        val SCREEN_ID = identifier("solar_generator")
        private val GENERATING_ICON = identifier("textures/gui/sun_icon.png")
        private val NOT_GENERATING_ICON = identifier("textures/gui/sun_unlit_icon.png")
    }
}