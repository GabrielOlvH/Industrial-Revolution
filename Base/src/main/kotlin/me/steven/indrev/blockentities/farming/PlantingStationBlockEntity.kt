package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.PLANTING_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.registry.tag.ItemTags
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class PlantingStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(PLANTING_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(10, inSlots(1, 2, 3, 4, 5, 6, 7, 8, 9) { v -> v.item.registryEntry.isIn(ItemTags.SAPLINGS)}, onChange = ::markForUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 5
            var actions = getActionCount()

            var saplings = findSaplingStack() ?: return
            val saplingBlockItem = saplings.resource.item as? BlockItem ?: return
            val state = saplingBlockItem.block.defaultState

            if (!queue.hasNext()) {
                queue = getArea().toList().iterator()
            }

            while (queue.hasNext()) {
                val pos = queue.next()
                if (world!!.getBlockState(pos).isReplaceable && state.canPlaceAt(world, pos)) {
                    if (!useEnergy(1)) break
                    world?.setBlockState(pos, state)
                    saplings.decrement(1)
                    actions--

                    if (saplings.isEmpty()) saplings = findSaplingStack() ?: break
                    else if (actions <= 0) break
                }
            }
        }
    }

    private fun findSaplingStack() = inventory.parts.firstOrNull { !it.isResourceBlank && it.resource.item.registryEntry.isIn(ItemTags.SAPLINGS) }

    override fun getRenderColor(): Int {
        return 0xFF00FF
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)

        repeat(9) { index ->
            val saplingSlot = WidgetSlot(index + 1, inventory, INPUT_COLOR)
            saplingSlot.overlay = identifier("textures/gui/icons/sapling_icon.png")
            handler.add(saplingSlot, grid(2) + 14 + (index % 3) * 22, grid(0) + 8 + (index / 3) * 22)
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Planting Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val EMPTY_ITERATOR = listOf<BlockPos>().iterator()
    }
}