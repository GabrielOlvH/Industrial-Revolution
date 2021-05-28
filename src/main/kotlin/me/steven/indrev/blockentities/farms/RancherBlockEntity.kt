package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.properties.BooleanProperty
import me.steven.indrev.api.machines.properties.Property
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.FakePlayerEntity
import me.steven.indrev.utils.eat
import me.steven.indrev.utils.redirectDrops
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class RancherBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.RANCHER_REGISTRY), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancementsSlots: IntArray = intArrayOf(15, 16, 17, 18)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.propertyDelegate = ArrayPropertyDelegate(8)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(2, 3, 4, 5) }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

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
        val enhancements = getEnhancers(inventory)
        cooldown += Enhancer.getSpeed(enhancements, this)
        if (cooldown < config.processSpeed) return
        val animals = world?.getEntitiesByClass(AnimalEntity::class.java, getWorkingArea()) { true }?.toMutableList()
            ?: mutableListOf()
        val energyCost = Enhancer.getEnergyCost(enhancements, this)
        if (animals.isEmpty() || !canUse(energyCost)) {
            workingState = false
            return
        } else workingState = true
        val swordStack = inventory.inputSlots.map { inventory.getStack(it) }.firstOrNull { it.item is SwordItem }
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            val kill = filterAnimalsToKill(animals)
            if (kill.isNotEmpty()) use(energyCost)
            kill.forEach { animal ->
                swordStack.damage(1, world?.random, null)
                if (swordStack.damage >= swordStack.maxDamage) swordStack.decrement(1)

                animal.redirectDrops(inventory) {
                    animal.damage(DamageSource.player(fakePlayer), swordItem.attackDamage)
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
                        use(energyCost)
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

    override fun getBaseValue(enhancer: Enhancer): Double =
        when (enhancer) {
            Enhancer.ENERGY -> config.energyCost
            Enhancer.SPEED -> 1.0
            Enhancer.BUFFER -> config.maxEnergyStored
            else -> 0.0
        }

    override fun getMaxEnhancer(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 1 else super.getMaxEnhancer(enhancer)
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

    override fun getEnergyCapacity(): Double = Enhancer.getBuffer(this)
}