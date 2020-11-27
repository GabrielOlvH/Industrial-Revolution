package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class RancherBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.RANCHER_REGISTRY), UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(8)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(2, 3, 4, 5) }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    var cooldown = 0.0
    override var range = 5
    private val fakePlayer by lazy { FakePlayerEntity(world as ServerWorld, pos) }
    var feedBabies: Boolean by BooleanProperty(4, true)
    var mateAdults: Boolean by BooleanProperty(5, true)
    var matingLimit: Int by Property(6, 16) { i -> i.coerceAtLeast(0) }
    var killAfter: Int by Property(7, 8) { i -> i.coerceAtLeast(0) }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val upgrades = getUpgrades(inventory)
        cooldown += Upgrade.getSpeed(upgrades, this)
        if (cooldown < config.processSpeed) return
        val input = inventory.getInputInventory()
        val animals = world?.getEntitiesByClass(AnimalEntity::class.java, getWorkingArea()) { true }?.toMutableList()
            ?: mutableListOf()
        if (animals.isEmpty() || !Energy.of(this).simulate().use(Upgrade.getEnergyCost(upgrades, this))) {
            workingState = false
            return
        } else workingState = true
        val swordStack = (0 until input.size()).map { input.getStack(it) }.firstOrNull { it.item is SwordItem }
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            val kill = filterAnimalsToKill(animals)
            if (kill.isNotEmpty()) Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))
            kill.forEach { animal ->
                swordStack.damage(1, world?.random, null)
                if (swordStack.damage >= swordStack.maxDamage) swordStack.decrement(1)
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
                    lootTable.generateLoot(lootContext).forEach { inventory.output(it) }
                }
            }
        }
        for (animal in animals) {
            inventory.inputSlots.forEach { slot ->
                val stack = inventory.getStack(slot).copy()
                if (tryFeed(animals.size, animal, inventory.getStack(slot)).isAccepted) return@forEach
                fakePlayer.inventory.selectedSlot = 8
                fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                if (animal.interactMob(fakePlayer, Hand.MAIN_HAND).isAccepted)
                    Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))
                val inserted = inventory.output(fakePlayer.inventory.getStack(0))
                val handStack = fakePlayer.getStackInHand(Hand.MAIN_HAND)
                if (!handStack.isEmpty && handStack.item != stack.item) {
                    inventory.output(handStack)
                    fakePlayer.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY)
                }
                if (inserted)
                    inventory.setStack(slot, stack)
                fakePlayer.inventory.clear()
            }
        }
        fakePlayer.inventory.clear()
        cooldown = 0.0
    }

    private fun tryFeed(size: Int, animalEntity: AnimalEntity, stack: ItemStack): ActionResult {
        if (animalEntity.isBreedingItem(stack)) {
            val breedingAge: Int = animalEntity.breedingAge
            if (!world!!.isClient && breedingAge == 0 && animalEntity.canEat() && size <= matingLimit && mateAdults) {
                animalEntity.eat(fakePlayer, stack)
                animalEntity.lovePlayer(fakePlayer)
            }
            if (animalEntity.isBaby && feedBabies) {
                animalEntity.eat(fakePlayer, stack)
                animalEntity.growUp(((-breedingAge / 20f) * 0.1f).toInt(), true)
            }
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    private fun filterAnimalsToKill(entities: List<AnimalEntity>): List<AnimalEntity> {
        val adults = entities.filter { !it.isBaby }
        val types = adults.map { it.type }.associateWith { mutableListOf<AnimalEntity>() }
        adults.forEach { types[it.type]?.add(it) }
        return types.values.let { values ->
            values.map { animals -> animals.dropLast((animals.size - killAfter).coerceAtLeast(killAfter)) }
        }.flatten()
    }

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getUpgradeSlots(): IntArray = intArrayOf(15, 16, 17, 18)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double =
        when (upgrade) {
            Upgrade.ENERGY -> config.energyCost
            Upgrade.SPEED -> 1.0
            Upgrade.BUFFER -> getBaseBuffer()
            else -> 0.0
        }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        super.toTag(tag)
        tag?.putBoolean("feedBabies", feedBabies)
        tag?.putBoolean("mateAdults", mateAdults)
        tag?.putInt("matingLimit", matingLimit)
        tag?.putInt("killAfter", killAfter)
        return tag!!
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        feedBabies = tag?.getBoolean("feedBabies") ?: feedBabies
        mateAdults = tag?.getBoolean("mateAdults") ?: mateAdults
        matingLimit = tag?.getInt("matingLimit") ?: matingLimit
        killAfter = tag?.getInt("killAfter") ?: killAfter
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        super.toClientTag(tag)
        tag?.putBoolean("feedBabies", feedBabies)
        tag?.putBoolean("mateAdults", mateAdults)
        tag?.putInt("matingLimit", matingLimit)
        tag?.putInt("killAfter", killAfter)
        return tag!!
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        feedBabies = tag?.getBoolean("feedBabies") ?: feedBabies
        mateAdults = tag?.getBoolean("mateAdults") ?: mateAdults
        matingLimit = tag?.getInt("matingLimit") ?: matingLimit
        killAfter = tag?.getInt("killAfter") ?: killAfter
    }

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)
}