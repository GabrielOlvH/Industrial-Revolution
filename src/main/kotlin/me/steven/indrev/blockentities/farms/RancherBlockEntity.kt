package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.autosync
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.eat
import me.steven.indrev.utils.redirectDrops
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos

class RancherBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.RANCHER_REGISTRY, pos, state) {

    init {
        this.enhancerComponent = EnhancerComponent(intArrayOf(15, 16, 17, 18), Enhancer.DEFAULT, this::getBaseValue, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(2, 3, 4, 5) }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    var cooldown = 0.0
    override var range = 5
    private val fakePlayer by lazy { IndustrialRevolution.FAKE_PLAYER_BUILDER.create(world!!.server, world as ServerWorld, "rancher") }
    var feedBabies: Boolean by autosync(FEED_BABIES_ID, true)
    var mateAdults: Boolean by autosync(MATE_ADULTS, true)
    var matingLimit: Int by autosync(MATING_LIMIT, 16)
    var killAfter: Int by autosync(KILL_AFTER, 8)

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val upgrades = enhancerComponent!!.enhancers
        cooldown += Enhancer.getSpeed(upgrades, enhancerComponent!!)
        if (cooldown < config.processSpeed) return
        val animals = world?.getEntitiesByClass(AnimalEntity::class.java, getWorkingArea()) { true } ?: emptyList()
        if (animals.isEmpty() || !canUse(getEnergyCost())) {
            workingState = false
            return
        } else workingState = true
        val swordStack = inventory.inputSlots.map { inventory.getStack(it) }.firstOrNull { it.item is SwordItem }
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            val kill = filterAnimalsToKill(animals)
            if (kill.isNotEmpty()) use(getEnergyCost())
            kill.forEach { animal ->
                animal.redirectDrops(inventory) {
                    if (!animal.isAlive || !animal.damage(DamageSource.player(fakePlayer), swordItem.attackDamage)) return@forEach
                    swordStack.damage(1, world?.random, null)
                    if (swordStack.damage >= swordStack.maxDamage) swordStack.decrement(1)
                }
            }
        }
        for (animal in animals) {
            inventory.inputSlots.forEach { slot ->
                val stack = inventory.getStack(slot).copy()
                animal.redirectDrops(inventory) {
                    if (tryFeed(animals.size, animal, inventory.getStack(slot)).isAccepted) return@forEach
                    fakePlayer.inventory.selectedSlot = 8
                    fakePlayer.setStackInHand(Hand.MAIN_HAND, stack)
                    if (animal.interactMob(fakePlayer, Hand.MAIN_HAND).isAccepted)
                        use(getEnergyCost())
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
        }
        fakePlayer.inventory.clear()
        cooldown = 0.0
    }

    private fun tryFeed(size: Int, animalEntity: AnimalEntity, stack: ItemStack): ActionResult {
        if (animalEntity.isBreedingItem(stack)) {
            val breedingAge: Int = animalEntity.breedingAge
            if (!world!!.isClient && breedingAge == 0 && animalEntity.canEat() && size <= matingLimit && mateAdults) {
                animalEntity.eat(fakePlayer, Hand.MAIN_HAND, stack)
                animalEntity.lovePlayer(fakePlayer)
            }
            if (animalEntity.isBaby && feedBabies) {
                animalEntity.eat(fakePlayer, Hand.MAIN_HAND, stack)
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

    override fun getEnergyCost(): Long {
        val speedEnhancers = (enhancerComponent!!.getCount(Enhancer.SPEED) * 2).coerceAtLeast(1)
        return config.energyCost * speedEnhancers
    }

    fun getBaseValue(enhancer: Enhancer): Double =
        when (enhancer) {
            Enhancer.SPEED -> 1.0
            Enhancer.BUFFER -> config.maxEnergyStored.toDouble()
            else -> 0.0
        }

    fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED -> return 1 
            Enhancer.BUFFER -> 4
            else -> 1
        }
    }

    override fun toTag(tag: NbtCompound) {
        super.toTag(tag)
        tag.putBoolean("feedBabies", feedBabies)
        tag.putBoolean("mateAdults", mateAdults)
        tag.putInt("matingLimit", matingLimit)
        tag.putInt("killAfter", killAfter)
    }

    override fun fromTag(tag: NbtCompound) {
        super.fromTag(tag)
        feedBabies = tag.getBoolean("feedBabies")
        mateAdults = tag.getBoolean("mateAdults")
        matingLimit = tag.getInt("matingLimit")
        killAfter = tag.getInt("killAfter")
    }

    override fun getCapacity(): Long = Enhancer.getBuffer(enhancerComponent!!)

    companion object {
        const val FEED_BABIES_ID = 2
        const val MATE_ADULTS = 3
        const val MATING_LIMIT = 4
        const val KILL_AFTER = 5
    }
}