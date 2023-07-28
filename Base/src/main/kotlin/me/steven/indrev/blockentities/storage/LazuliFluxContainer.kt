package me.steven.indrev.blockentities.storage

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.config.machinesConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.INPUT_COLOR
import me.steven.indrev.utils.OUTPUT_COLOR
import me.steven.indrev.utils.SidedConfiguration
import me.steven.indrev.utils.Troubleshooter
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class LazuliFluxContainer(pos: BlockPos, state: BlockState) : MachineBlockEntity<MachineConfig>(TODO(), pos, state) {

    override val config: MachineConfig get() = machinesConfig.lazuliFluxContainer.get(tier)

    val sideConfig: SidedConfiguration = SidedConfiguration()

    init {
        sideConfig.forceDefault = false
    }

    override fun getMaxInput(dir: Direction): Long {
        return if (sideConfig.getMode(dir).allowInput) maxInput else 0
    }

    override fun getMaxOutput(dir: Direction): Long {
        return if (sideConfig.getMode(dir).allowOutput) maxOutput else 0
    }
    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity?): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this)

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Lazuli Flux Container")


}