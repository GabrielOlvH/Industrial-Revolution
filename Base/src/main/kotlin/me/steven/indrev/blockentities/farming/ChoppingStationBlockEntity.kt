package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.CHOPPING_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.AxeItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

class ChoppingStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(CHOPPING_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(9, inSlots(1, 2, 3) { v -> v.item is AxeItem }, outSlots(4, 5, 6, 7, 8), onChange = ::inventoryUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        if (troubleshooter.contains(Troubleshooter.NO_SPACE)) {
            return
        }
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 5
            var actions = getActionCount()
            var axe = findAxeStack() ?: return

            if (!queue.hasNext()) {
                val list = mutableListOf<BlockPos>()
                getArea().forEach { pos ->
                    collectBlocks(pos.toImmutable(), list, getArea().expand(6.0, 0.0, 6.0))
                }
                queue = list.sortedByDescending { it.y }.iterator()
            }
            val stack = axe.resource.toStack(axe.amount.toInt())
            while (queue.hasNext() && actions > 0) {
                val pos = queue.next()
                val state = world!!.getBlockState(pos)
                if (state.registryEntry.isIn(BlockTags.LOGS) && useEnergy(2, troubleshooter)) {
                    stack.damage(1, world!!.random, null)
                    if (stack.maxDamage > 0 && stack.damage >= stack.maxDamage) {
                        stack.decrement(1)
                        axe.set(stack)
                        axe = findAxeStack() ?: break
                    }
                } else if (!state.registryEntry.isIn(BlockTags.LEAVES) || !useEnergy(1, troubleshooter)) continue
                world!!.removeBlock(pos, false)

                insertLoot(state, pos, stack, OUTPUT_SLOTS)

                actions--
            }
            axe.set(stack)
        }
    }

    private fun collectBlocks(pos: BlockPos, blocks: MutableList<BlockPos>, limit: Box) {
        if (!blocks.contains(pos) && limit.contains(pos.x.toDouble(), limit.minY, pos.z.toDouble())) {
            val registryEntry = world!!.getBlockState(pos).registryEntry
            if ((registryEntry.isIn(BlockTags.LOGS) || registryEntry.isIn(BlockTags.LEAVES))) {
                blocks.add(pos)
                DIRECTIONS.forEach { dir ->
                    collectBlocks(pos.offset(dir), blocks, limit)
                }
            }
        }
    }

    private fun findAxeStack(): MachineItemInventory.ItemSlot?  {
        val axeStack = inventory.parts.firstOrNull { !it.isResourceBlank && it.resource.item is AxeItem }
        troubleshooter.test(Troubleshooter.NO_TOOL, axeStack != null)
        return axeStack
    }

    override fun getRenderColor(): Int {
        return 0xFF0000
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
       // handler.addDefaultBackground()
        handler.addEnergyBar(this) { troubleshooter.contains(Troubleshooter.NO_ENERGY) }
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)

        repeat(3) { i ->
            val axeSlot = WidgetSlot(i + 1, inventory, INPUT_COLOR)
            axeSlot.overlay = identifier("textures/gui/icons/axe_icon.png")
            axeSlot.highlight = { troubleshooter.contains(Troubleshooter.NO_TOOL) }
            handler.add(axeSlot, grid(2) + 14 + 22*(i), grid(0) + 9)
        }

        repeat(5) { i ->
            val outSlot = WidgetSlot(4 + i, inventory, OUTPUT_COLOR)
            outSlot.highlight = { troubleshooter.contains(Troubleshooter.NO_SPACE) }
            handler.add(outSlot, grid(1) + 10 + i * 22, grid(3))
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Chopping Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val OUTPUT_SLOTS = intArrayOf(4, 5, 6, 7, 8)
        private val EMPTY_ITERATOR = listOf<BlockPos>().iterator()
        private val DIRECTIONS = Direction.values()
    }
}