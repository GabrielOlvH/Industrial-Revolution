package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.FERTILIZING_STATION
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.components.MachineTemperatureController
import me.steven.indrev.components.MachineUpgrades
import me.steven.indrev.components.inSlots
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.INPUT_COLOR
import me.steven.indrev.utils.Upgrade
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.toList
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BoneMealItem
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class FertilizingStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(FERTILIZING_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(10, inSlots(1, 2, 3, 4, 5, 6, 7, 8, 9) { v -> v.item is BoneMealItem }, onChange = ::markForUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 5
            var actions = getActionCount()

            var boneMeal = findBoneMealStack() ?: return

            if (!queue.hasNext()) {
                queue = getArea().toList().iterator()
            }

            while (queue.hasNext()) {
                val pos = queue.next()
                val state = world!!.getBlockState(pos)
                val block = state.block
                if (block is Fertilizable && block.canGrow(world, world!!.random, pos, state)) {
                    if (!useEnergy(1, troubleshooter)) break
                    block.grow(world as ServerWorld, world!!.random, pos, state)
                    boneMeal.decrement(1)
                    actions--

                    if (boneMeal.isEmpty()) boneMeal = findBoneMealStack() ?: break
                    else if (actions <= 0) break
                }
            }
        }
    }

    private fun findBoneMealStack() = inventory.parts.firstOrNull { !it.isResourceBlank && it.resource.item is BoneMealItem }

    override fun getRenderColor(): Int {
        return 0x00FF00
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addDefaultBackground()
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)

        repeat(9) { index ->
            val bonemealSlot = WidgetSlot(index + 1, inventory, INPUT_COLOR)
            bonemealSlot.overlay = identifier("textures/gui/icons/bone_meal_icon.png")
            handler.add(bonemealSlot, grid(2) + 14 + (index % 3) * 22, grid(0) + 8 + (index / 3) * 22)
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Fertilizing Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val EMPTY_ITERATOR = listOf<BlockPos>().iterator()
    }
}