package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.HEAT_GENERATOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.misc.WStaticTooltip
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class HeatGeneratorScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiScreenHandler(
        HEAT_GENERATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.heat_generator", ctx, playerInventory, blockInventory, invPos = 4.25)

        val info = WStaticTooltip()
        root.add(info, 2.3, 0.9)
        info.setSize(90, 55)

        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? HeatGeneratorBlockEntity ?: return@run
            val generatingText = WText({
                val consumptionRate = (blockEntity.getConsumptionRate(component!![MachineBlockEntity.TEMPERATURE_ID]) / 810).toString()
                TranslatableText("gui.indrev.heatgen.title", LiteralText(consumptionRate).formatted(Formatting.DARK_RED)).formatted(Formatting.RED)
            }, HorizontalAlignment.LEFT)
            root.add(generatingText, 2.5, 1.0)
        }
        root.add(WText(TranslatableText("gui.indrev.heatgen.pertick").formatted(Formatting.RED), HorizontalAlignment.LEFT), 2.5, 1.6)

        val amount = WText({
            val ratio = component!!.get<Double>(HeatGeneratorBlockEntity.GENERATION_RATIO_ID)
            TranslatableText("gui.indrev.heatgen.generating", LiteralText(ratio.toString()).formatted(Formatting.WHITE)).formatted(Formatting.BLUE)
        }, HorizontalAlignment.LEFT)
        root.add(amount, 2.5, 2.6)

        root.add(WText(TranslatableText("gui.indrev.heatgen.pertick").formatted(Formatting.BLUE), HorizontalAlignment.LEFT), 2.5, 3.2)

        withBlockEntity<HeatGeneratorBlockEntity> { be ->
            val fluid = fluidTank(be, HeatGeneratorBlockEntity.TANK_ID)
            root.add(fluid, 8.0, 0.6)
        }

        root.validate(this)
    }

    override fun getEntry(): Identifier = identifier("machines/generators")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("heat_generator_screen")
    }
}