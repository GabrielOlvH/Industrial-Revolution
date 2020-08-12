package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.FakePlayerEntity
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.toIntArray
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class RancherBlockEntity(tier: Tier) : AOEMachineBlockEntity(tier, MachineRegistry.RANCHER_REGISTRY), UpgradeProvider {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(19, (2..5).toIntArray(), (6 until 15).toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot in 2 until 15 -> true
                    else -> false
                }
            }
        }
    }

    var cooldown = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cooldown += Upgrade.SPEED(this)
        if (cooldown < getConfig().processSpeed) return
        val input = inventory.getInputInventory()
        val animals = world?.getEntitiesByClass(AnimalEntity::class.java, getWorkingArea()) { true }?.toMutableList()
            ?: mutableListOf()
        if (animals.isEmpty() || !Energy.of(this).simulate().use(Upgrade.ENERGY(this))) {
            setWorkingState(false)
            return
        } else setWorkingState(true)
        val swordStack = (0 until input.size()).map { input.getStack(it) }.firstOrNull { it.item is SwordItem }
        val fakePlayer = FakePlayerEntity(world!!, pos)
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            val kill = filterAnimalsToKill(animals)
            if (kill.isNotEmpty()) Energy.of(this).use(Upgrade.ENERGY(this))
            kill.forEach { animal ->
                swordStack.damage(1, world?.random, null)
                val lootTable = (world as ServerWorld).server.lootManager.getTable(animal.lootTable)
                animal.damage(DamageSource.player(fakePlayer), swordItem.attackDamage)
                if (animal.isDead) {
                    animals.remove(animal)
                    val lootContext = LootContext.Builder(world as ServerWorld)
                        .random(world?.random)
                        .parameter(LootContextParameters.ORIGIN, animal.pos)
                        .parameter(LootContextParameters.DAMAGE_SOURCE, DamageSource.player(fakePlayer))
                        .parameter(LootContextParameters.THIS_ENTITY, animal)
                        .build(LootContextTypes.ENTITY)
                    lootTable.generateLoot(lootContext).forEach { inventory.addStack(it) }
                }
            }
        }
        for (animal in animals) {
            (0 until input.size()).forEach { slot ->
                val stack = inventory.getStack(slot).copy()
                fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                if (animal.interactMob(fakePlayer, Hand.MAIN_HAND).isAccepted)
                    Energy.of(this).use(Upgrade.ENERGY(this))
                val res = inventory.addStack(fakePlayer.inventory.getStack(1))
                if (res.isEmpty)
                    inventory.setStack(slot, stack)
                fakePlayer.inventory.setStack(1, ItemStack.EMPTY)
            }
        }
        cooldown = 0.0
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

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    private fun getRange() =
        when (tier) {
            Tier.MK1 -> Vec3d(3.0, 4.0, 3.0)
            Tier.MK2 -> Vec3d(4.0, 4.0, 4.0)
            Tier.MK3 -> Vec3d(5.0, 4.0, 5.0)
            Tier.MK4, Tier.CREATIVE -> Vec3d(6.0, 4.0, 6.0)
        }

    override fun getUpgradeSlots(): IntArray = intArrayOf(15, 16, 17, 18)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double =
        when (upgrade) {
            Upgrade.ENERGY -> getConfig().energyCost * Upgrade.SPEED(this)
            Upgrade.SPEED -> 1.0
            Upgrade.BUFFER -> getBaseBuffer()
        }

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    fun getConfig() = IndustrialRevolution.CONFIG.machines.rancher
}