package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.FakePlayerEntity
import me.steven.indrev.utils.redirectDrops
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.SwordItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class SlaughterBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.SLAUGHTER_REGISTRY, pos, state), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancerSlots: IntArray = intArrayOf(11, 12, 13, 14)
    override val availableEnhancers: Array<Enhancer> = arrayOf(Enhancer.SPEED, Enhancer.BUFFER, Enhancer.DAMAGE)

    init {
        this.inventoryComponent = inventory(this) {
            input { slot = 1 }
            output { slots = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10) }
        }
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    var cooldown = 0.0
    override var range = 5
    private val fakePlayer by lazy { FakePlayerEntity(world as ServerWorld, pos) }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val enhancers = getEnhancers()
        cooldown += Enhancer.getSpeed(enhancers, this)
        if (cooldown < config.processSpeed) return
        val source = DamageSource.player(fakePlayer)
        val mobs = world?.getEntitiesByClass(LivingEntity::class.java, getWorkingArea()) { e -> e !is PlayerEntity && e !is ArmorStandEntity && !e.isDead && !e.isInvulnerableTo(source) && (e !is WitherEntity || e.invulnerableTimer <= 0) }
            ?: emptyList()
        if (mobs.isEmpty() || !canUse(getEnergyCost())) {
            workingState = false
            return
        } else workingState = true
        val swordStack = inventory.inputSlots.map { inventory.getStack(it) }.firstOrNull { it.item is SwordItem }
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.damage < swordStack.maxDamage) {
            val swordItem = swordStack.item as SwordItem
            use(getEnergyCost())
            mobs.forEach { mob ->
                swordStack.damage(1, world?.random, null)
                if (swordStack.damage >= swordStack.maxDamage) swordStack.decrement(1)

                mob.redirectDrops(inventory) {
                    mob.damage(source, (swordItem.attackDamage * Enhancer.getDamageMultiplier(enhancers, this)).toFloat())
                }
            }
        }
        fakePlayer.inventory.clear()
        cooldown = 0.0
    }

    override fun getEnergyCost(): Double {
        val speedEnhancers = (getEnhancers().getInt(Enhancer.SPEED) * 2).coerceAtLeast(1)
        val dmgEnhancers = (getEnhancers().getInt(Enhancer.DAMAGE) * 8).coerceAtLeast(1)
        return config.energyCost * speedEnhancers * dmgEnhancers
    }

    override fun getBaseValue(enhancer: Enhancer): Double =
        when (enhancer) {
            Enhancer.SPEED -> 1.0
            Enhancer.BUFFER -> config.maxEnergyStored
            else -> 0.0
        }

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED || enhancer == Enhancer.DAMAGE) return 1 else super.getMaxCount(enhancer)
    }

    override fun getEnergyCapacity(): Double = Enhancer.getBuffer(this)
}