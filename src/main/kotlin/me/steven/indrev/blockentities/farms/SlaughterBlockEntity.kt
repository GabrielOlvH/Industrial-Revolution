package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.redirectDrops
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.use

import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.SwordItem
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import team.reborn.energy.api.base.SimpleBatteryItem

class SlaughterBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.SLAUGHTER_REGISTRY, pos, state) {

    init {
        this.enhancerComponent = EnhancerComponent(
            intArrayOf(11, 12, 13, 14),
            arrayOf(Enhancer.SPEED, Enhancer.BUFFER, Enhancer.DAMAGE),
            this::getMaxCount
        )
        this.inventoryComponent = inventory(this) {
            input { slot = 1 }
            output { slots = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10) }
        }
    }

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    var cooldown = 0.0
    override var range = 5
    private val fakePlayer by lazy { IndustrialRevolution.FAKE_PLAYER_BUILDER.create(world!!.server, world as ServerWorld, "slaughter") }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val enhancers = enhancerComponent!!.enhancers
        cooldown += getProcessingSpeed()
        if (cooldown < config.processSpeed) return
        val source = DamageSource.player(fakePlayer)
        val mobs = world?.getEntitiesByClass(LivingEntity::class.java, getWorkingArea()) { e -> e !is PlayerEntity && e !is ArmorStandEntity && !e.isDead && !e.isInvulnerableTo(source) && (e !is WitherEntity || e.invulnerableTimer <= 0) } ?: emptyList()
        if (mobs.isEmpty() || !canUse(getEnergyCost())) {
            workingState = false
            return
        } else workingState = true
        val swordStack = inventory.inputSlots.map { inventory.getStack(it) }.firstOrNull { it.item is SwordItem }
        fakePlayer.inventory.selectedSlot = 0
        if (swordStack != null && !swordStack.isEmpty && swordStack.item is SwordItem) {
            val swordItem = swordStack.item as SwordItem
            use(getEnergyCost())
            mobs.forEach { mob ->
                if (mob.isAlive && damageToolOrUseEnergy(swordStack)) {
                    mob.redirectDrops(inventory) {
                        mob.damage(source, (swordItem.attackDamage * Enhancer.getDamageMultiplier(enhancers)).toFloat())
                    }
                }
            }
        }
        fakePlayer.inventory.clear()
        cooldown = 0.0
    }
    
    private fun damageToolOrUseEnergy(swordItem : ItemStack) : Boolean {
        if (swordItem.item is SimpleBatteryItem){
            return (swordItem.item as SimpleBatteryItem).tryUseEnergy(swordItem,1);
        }
        else if (energyOf(swordItem) != null){
            return energyOf(swordItem)!!.use(1)
        }
        else {
            if (!swordItem.isDamageable) return false
            swordItem.damage(1, world?.random, null)
            if (swordItem.damage >= swordItem.maxDamage) swordItem.decrement(1)
            return true
        }
    }
    
    override fun getEnergyCost(): Long {
        val speedEnhancers = (enhancerComponent!!.getCount(Enhancer.SPEED) * 2).coerceAtLeast(1)
        val dmgEnhancers = (enhancerComponent!!.getCount(Enhancer.DAMAGE) * 8).coerceAtLeast(1)
        return config.energyCost * speedEnhancers * dmgEnhancers
    }

    fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED, Enhancer.DAMAGE -> return 1
            Enhancer.BUFFER -> 4
            else -> 1
        }
    }
}
