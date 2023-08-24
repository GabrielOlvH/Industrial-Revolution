package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.SLAUGHTER_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.extensions.redirectDrops
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.SwordItem
import net.minecraft.registry.tag.ItemTags
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class SlaughterStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(SLAUGHTER_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(9, inSlots(1, 2, 3) { v -> v.item is SwordItem }, outSlots(4, 5, 6, 7, 8), onChange = ::inventoryUpdate)
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
            cooldown = 20
            var actions = getActionCount()

            var sword = findSwordStack() ?: return

            if (!queue.hasNext()) {
                queue = world!!.getEntitiesByClass(AnimalEntity::class.java, getArea()) { e -> e.isAlive }.map { it.id }.iterator()
            }
            val swordStack = sword.resource.toStack(sword.amount.toInt())
            while (queue.hasNext()) {
                val id = queue.next()
                val entity = world!!.getEntityById(id)
                if (entity is AnimalEntity && entity.maxHealth < 40 && entity.isAlive) {
                    if (!useEnergy(5)) break
                    swordStack.damage(1, world!!.random, null)
                    if (swordStack.maxDamage > 0 && swordStack.damage >= swordStack.maxDamage) {
                        swordStack.decrement(1)
                        sword.set(swordStack)
                        sword = findSwordStack() ?: break
                    }
                    actions--

                    entity.redirectDrops({ entity.kill() })
                    { stack ->
                        tx { tx ->
                            val inserted =
                                inventory.insert(OUTPUT_SLOTS, ItemVariant.of(stack), stack.count.toLong(), tx)
                            if (inserted < stack.count) {
                                val overflowed = inventory.insertOverflow(OUTPUT_SLOTS, ItemVariant.of(stack), stack.count.toLong() - inserted, tx)
                                if (overflowed)
                                    troubleshooter.offer(Troubleshooter.NO_SPACE)
                            }
                            tx.commit()
                            stack.count = 0
                        }
                    }

                    if (sword.isEmpty()) sword = findSwordStack() ?: break
                    else if (actions <= 0) break
                }
            }
            sword.set(swordStack)
        }
    }

    private fun findSwordStack() = inventory.parts.firstOrNull { !it.isResourceBlank && it.resource.item.registryEntry.isIn(ItemTags.SWORDS) }

    override fun getRenderColor(): Int {
        return 0xFF00FF
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
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

    override fun getDisplayName(): Text = Text.literal("Slaughter Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val OUTPUT_SLOTS = intArrayOf(4, 5, 6, 7, 8)
        private val EMPTY_ITERATOR = listOf<Int>().iterator()
    }
}