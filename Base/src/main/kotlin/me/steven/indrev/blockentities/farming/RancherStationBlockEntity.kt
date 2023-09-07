package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.RANCHER_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class RancherStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(RANCHER_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(7, inSlots( 1, 2, 3, 4, 5, 6), onChange = ::inventoryUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 20
            var actions = getActionCount()

            if (!queue.hasNext()) {
                queue = world!!.getEntitiesByClass(AnimalEntity::class.java, getArea()) { e -> e.isAlive }.map { it.id }.iterator()
            }
            while (queue.hasNext()) {
                val id = queue.next()
                val entity = world!!.getEntityById(id)
                if (entity is AnimalEntity && entity.isAlive) {
                    val breedingSlot = findBreedingItem(entity)
                    if (breedingSlot != null && !entity.isInLove) {
                        if (!useEnergy(1)) break
                        actions--
                        breedingSlot.amount--
                        entity.lovePlayer(null)
                        if (actions <= 0) break
                    }
                }
            }
        }
    }

    private fun findBreedingItem(animalEntity: AnimalEntity) = inventory.parts.firstOrNull { !it.isResourceBlank && animalEntity.isBreedingItem(it.resource.toStack()) }

    override fun getRenderColor(): Int {
        return 0xFF00FF
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addEnergyBar(this) { troubleshooter.contains(Troubleshooter.NO_ENERGY) }
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)

        INPUT_SLOTS.forEach { i ->
            val inputSlot = WidgetSlot(i, inventory, INPUT_COLOR)
            inputSlot.highlight = { troubleshooter.contains(Troubleshooter.NO_TOOL) }
            handler.add(inputSlot, grid(2) + 14 + 22*((i-1)%3), grid(0) + 9 + 22*((i-1)/3))
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Rancher Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val INPUT_SLOTS = intArrayOf(1, 2, 3, 4, 5, 6)
        private val EMPTY_ITERATOR = listOf<Int>().iterator()
    }
}