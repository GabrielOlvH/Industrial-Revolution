package me.steven.indrev.blockentities.farming

import me.steven.indrev.blocks.CHOPPING_STATION
import me.steven.indrev.components.*
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.BlockTags
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class ChoppingStationBlockEntity(pos: BlockPos, state: BlockState) : BaseFarmBlockEntity<MachineConfig>(CHOPPING_STATION.type, pos, state) {

    override val inventory: MachineItemInventory = MachineItemInventory(9, inSlots(1, 2, 3) { v -> v.item is AxeItem }, outSlots(4, 5, 6, 7, 8), onChange = ::markForUpdate)
    override val temperatureController: MachineTemperatureController = properties.sync(MachineTemperatureController(TEMPERATURE_ID, 1500, ::markForUpdate))
    override val upgrades: MachineUpgrades = MachineUpgrades(UPGRADES, ::updateUpgrades)

    private var cooldown = 0
    private var queue = EMPTY_ITERATOR

    override fun machineTick() {
        cooldown--
        if (cooldown <= 0 && getRange() > 0) {
            cooldown = 5
            var actions = getActionCount()
            var axe = findAxeStack() ?: return

            if (!queue.hasNext()) {
                queue = getArea().toList().iterator()
            }
            val stack = axe.resource.toStack(axe.amount.toInt())
            while (queue.hasNext()) {
                val pos = queue.next()
                val state = world!!.getBlockState(pos)
                if (state.registryEntry.isIn(BlockTags.LOGS) && useEnergy(2)) {
                    stack.damage(1, world!!.random, null)
                    if (stack.damage >= stack.maxDamage) {
                        stack.decrement(1)
                        axe.set(stack)
                        axe = findAxeStack() ?: break
                    }
                } else if (!state.registryEntry.isIn(BlockTags.LEAVES) || !useEnergy(1)) continue
                world!!.removeBlock(pos, false)

                insertLoot(state, pos, stack)

                actions--
                if (actions <= 0)
                    break
            }
            axe.set(stack)
        }
    }

    private fun findAxeStack() = inventory.parts.firstOrNull { !it.isResourceBlank && it.resource.item is AxeItem }

    override fun getRenderColor(): Int {
        return 0xFF0000
    }

    override fun getArea(): Box {
        return super.getArea().stretch(0.0, getRange() * 2.0, 0.0)
    }

    private fun insertLoot(state: BlockState, pos: BlockPos, stack: ItemStack) {
        val ctx = LootContext.Builder(world as ServerWorld)
            .parameter(LootContextParameters.BLOCK_STATE, state)
            .parameter(LootContextParameters.ORIGIN, Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
            .parameter(LootContextParameters.TOOL, stack)
            .build(LootContextTypes.BLOCK)
        tx { tx ->
            world!!.server!!.lootManager.getTable(state.block.lootTableId).generateLoot(ctx) { stack ->
                val inserted = inventory.insert(OUTPUT_SLOTS, ItemVariant.of(stack), stack.count.toLong(), tx)
                if (inserted < stack.count) {
                    stack.decrement(inserted.toInt())
                    ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
                }
            }
            tx.commit()
        }
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = MachineScreenHandler(syncId, inv, this)
        handler.addDefaultBackground()
        handler.addEnergyBar(this)
        handler.addTemperatureBar(temperatureController)
        handler.addUpgradeSlots(upgrades)

        handler.addRangeCardSlot(this)

        repeat(3) { i ->
            val axeSlot = WidgetSlot(i + 1, inventory, INPUT_COLOR)
            axeSlot.overlay = identifier("textures/gui/icons/axe_icon.png")
            handler.add(axeSlot, grid(2) + 14 + 22*(i), grid(0) + 9)
        }

        repeat(5) { i ->
            handler.add(WidgetSlot(4 + i, inventory, OUTPUT_COLOR), grid(1) + 10 + i * 22, grid(3))
        }

        handler.addPlayerInventorySlots()
        return handler
    }

    override fun getDisplayName(): Text = Text.literal("Chopping Station")

    companion object {
        val UPGRADES = arrayOf(Upgrade.OVERCLOCKER, Upgrade.OVERCLOCKER_2X, Upgrade.OVERCLOCKER_4X, Upgrade.OVERCLOCKER_8X, Upgrade.AUTOMATED_ITEM_TRANSFER)
        private val OUTPUT_SLOTS = intArrayOf(4, 5, 6, 7, 8)
        private val EMPTY_ITERATOR = listOf<BlockPos>().iterator()
    }
}