package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.HARVESTING_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.block.PlantBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class HarvestingStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(HARVESTING_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(10, outSlots(1, 2, 3, 4, 5, 6, 7, 8, 9), onChange = ::markForUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 5

            var actions = getActionCount()

            if (!queue.hasNext()) {
                queue = getArea().toList().iterator()
            }

            while (queue.hasNext() && actions > 0) {
                val pos = queue.next()
                val state = world!!.getBlockState(pos)
                if (state.block is PlantBlock && useEnergy(2, troubleshooter)) {
                    world!!.removeBlock(pos, false)
                    insertLoot(state, pos, ItemStack.EMPTY, OUTPUT_SLOTS)
                }

                actions--
            }
        }
    }

    override fun getRenderColor(): Int {
        return 0xFF0000
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addDefaultBackground()
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)
        repeat(9) { i ->
            handler.add(WidgetSlot(1 + i, inventory, OUTPUT_COLOR), grid(2) + 14 + (i % 3) * 22, grid(0) + 8 + (i / 3) * 22)
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Harvesting Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val OUTPUT_SLOTS = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        private val EMPTY_ITERATOR = listOf<BlockPos>().iterator()
    }
}