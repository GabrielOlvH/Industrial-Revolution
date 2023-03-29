package me.steven.indrev.gui.screenhandlers.wrench

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.Insets
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SCREWDRIVER_HANDLER
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.utils.add
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import me.steven.indrev.utils.translatable
import java.util.*

class ScrewdriverScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext, val configs: EnumMap<ConfigurationType, SideConfiguration>) :
    IRGuiScreenHandler(
        SCREWDRIVER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    private lateinit var currentType: ConfigurationType

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(100, 128)
        root.insets = Insets.ROOT_PANEL

        val titleText = WText(translatable("item.indrev.wrench.title"), HorizontalAlignment.LEFT, 0x404040)
        root.add(titleText, 0.3, 0.4)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run

            val availableTypes = ConfigurationType.getTypes(blockEntity)
            currentType = availableTypes.first()
            var widget = configs[currentType]?.getConfigurationPanel(world, pos, blockEntity, playerInventory, currentType) ?: return@run
            val configY = if (availableTypes.size > 1) 2 else 1
            root.add(widget, 0, configY)
            val configTypeButton = WButton(currentType.title)
            configTypeButton.setOnClick {
                currentType = currentType.next(availableTypes)
                configTypeButton.label = currentType.title
                root.remove(widget)
                widget = configs[currentType]?.getConfigurationPanel(world, pos, blockEntity, playerInventory, currentType) ?: return@setOnClick
                root.add(widget, 0, configY)
                root.validate(this)
            }
            if (availableTypes.size > 1)
                root.add(configTypeButton, 1.6, 1.0)
            configTypeButton.setSize(45, 20)

        }
        root.validate(this)
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        if (player is ServerPlayerEntity) {
            ctx.run { world, pos ->
                val blockEntity = world.getBlockEntity(pos) as? LazuliFluxContainerBlockEntity ?: return@run
                blockEntity.sync()
                world.updateNeighbors(pos, blockEntity.cachedState.block)
            }
        }
    }

    override fun addPainters() {
        super.addPainters()
        rootPanel.backgroundPainter = BackgroundPainter.VANILLA
    }

    companion object {
        val SCREEN_ID = identifier("screwdriver_config_screen")
    }
}