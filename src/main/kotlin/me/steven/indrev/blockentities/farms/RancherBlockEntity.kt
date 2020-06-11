package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.FakePlayerEntity
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.toIntArray
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.SwordItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class RancherBlockEntity(tier: Tier) : AOEMachineBlockEntity(tier, MachineRegistry.RANCHER_REGISTRY), UpgradeProvider {

    init {
        this.inventoryController = InventoryController({ this }) {
            DefaultSidedInventory(19, (2..5).toIntArray(), (6 until 15).toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is UpgradeItem -> getUpgradeSlots().contains(slot)
                    item is RechargeableItem && item.canOutput -> slot == 0
                    item is CoolerItem -> slot == 1
                    slot in 2 until 15 -> true
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController({ this }, 0.02, 350..430, 600.0)
    }

    var cooldown = 10

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = inventoryController?.getInventory() ?: return
        if (cooldown > 0) {
            cooldown--
            return
        }

        val input = inventory.getInputInventory()
        val stacks = (0 until input.size()).map { input.getStack(it) }
        val animals = world?.getEntities(AnimalEntity::class.java, getWorkingArea()) { true }?.toMutableList()
            ?: mutableListOf()
        if (animals.isEmpty() || !takeEnergy(Upgrade.ENERGY.apply(this, inventory)))
            return
        val swordStack = stacks.firstOrNull { it.item is SwordItem }
        val fakePlayer = FakePlayerEntity(world!!, pos)
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            val kill = filterAnimalsToKill(animals)
            kill.forEach { animal ->
                swordStack.damage(1, world?.random, null)
                val lootTable = (world as ServerWorld).server.lootManager.getTable(animal.lootTable)
                animal.damage(DamageSource.player(fakePlayer), swordItem.attackDamage)
                if (animal.isDead) {
                    animals.remove(animal)
                    val lootContext = LootContext.Builder(world as ServerWorld)
                        .random(world?.random)
                        .parameter(LootContextParameters.POSITION, BlockPos(animal.pos))
                        .parameter(LootContextParameters.DAMAGE_SOURCE, DamageSource.player(fakePlayer))
                        .parameter(LootContextParameters.THIS_ENTITY, animal)
                        .build(LootContextTypes.ENTITY)
                    lootTable.generateLoot(lootContext).forEach { inventory.addStack(it) }
                }
            }
        }
        for (animal in animals) {
            stacks.forEach { stack ->
                fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                animal.interactMob(fakePlayer, Hand.MAIN_HAND)
            }
        }
        cooldown += 6 - (Upgrade.SPEED.apply(this, inventory).toInt() / 4)
    }

    private fun filterAnimalsToKill(entities: List<AnimalEntity>): List<AnimalEntity> {
        val adults = entities.filter { !it.isBaby }
        val types = adults.map { it.type }.associateWith { mutableListOf<AnimalEntity>() }
        adults.forEach { types[it.type]?.add(it) }
        return types.values.let { values -> values.map { animals -> animals.filterIndexed { index, _ -> index > 7 } } }.flatten()
    }

    override fun getWorkingArea(): Box {
        val box = Box(pos)
        if (this.hasWorld()) {
            val range = getRange()
            return box.expand(range.x, 0.0, range.z).stretch(0.0, range.y, 0.0)
        }
        return box
    }

    private fun getRange() =
        when (tier) {
            Tier.MK1 -> Vec3d(3.0, 6.0, 3.0)
            Tier.MK2 -> Vec3d(4.0, 7.0, 4.0)
            Tier.MK3 -> Vec3d(5.0, 8.0, 5.0)
            Tier.MK4 -> Vec3d(6.0, 9.0, 6.0)
        }

    override fun getUpgradeSlots(): IntArray = intArrayOf(15, 16, 17, 18)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double =
        when (upgrade) {
            Upgrade.ENERGY -> 2.0 * Upgrade.SPEED.apply(this, inventoryController!!.getInventory())
            Upgrade.SPEED -> if (temperatureController!!.temperature.toInt() in temperatureController!!.optimalRange) 4.0 else 3.0
            Upgrade.BUFFER -> baseBuffer
        }
}