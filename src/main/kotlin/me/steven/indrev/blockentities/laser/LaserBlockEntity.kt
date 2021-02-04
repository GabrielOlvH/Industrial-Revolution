package me.steven.indrev.blockentities.laser

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.blocks.machine.LaserBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.particle.DustParticleEffect
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class LaserBlockEntity : MachineBlockEntity<MachineConfig>(Tier.MK4, MachineRegistry.LASER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    override fun machineTick() {
        if (!cachedState[LaserBlock.POWERED]) return
        val facing = cachedState[FacingMachineBlock.FACING]
        val containerPos = pos.offset(facing, 4)
        BlockPos.iterate(pos.offset(facing), pos.offset(facing, 3)).forEach {
            world?.breakBlock(it, true)
        }
        val container = world?.getBlockEntity(containerPos) as? CapsuleBlockEntity ?: return explode()
        val stack = container.inventory[0]
        if ((stack.item != IRItemRegistry.MODULAR_CORE && stack.item != IRItemRegistry.MODULAR_CORE_ACTIVATED)
            || !use(config.energyCost))
            return explode()

        val (x, y, z) = facing.unitVector.apply { scale(3.0f) }
        val damageArea = Box(pos).stretch(x.toDouble(), y.toDouble(), z.toDouble()).let {
            when {
                facing.axis.isVertical ->
                    it.shrink(0.3, 0.0, 0.3)
                facing.axis == Direction.Axis.X ->
                    it.shrink(0.0, 0.3, 0.3)
                else ->
                    it.shrink(0.3, 0.3, 0.0)
            }
        }
        world?.getEntitiesByClass(Entity::class.java, damageArea, null)?.forEach {
            it.setOnFireFor(1000)
            it.damage(LaserBlock.LASER_DAMAGE_SOURCE, 2f)
        }

        val tag = stack.orCreateTag
        val progress = tag.getDouble("Progress")
        if (progress >= 200000000) {
            container.inventory[0] = ItemStack(IRItemRegistry.MODULAR_CORE_ACTIVATED)
            container.markDirty()
            container.sync()
        } else
            tag.putDouble("Progress", progress + config.energyCost)
    }

    @Environment(EnvType.CLIENT)
    override fun machineClientTick() {
        if (!cachedState[LaserBlock.POWERED]) return
        val facing = cachedState[FacingMachineBlock.FACING]
        val containerPos = pos.offset(facing, 4)
        if (world!!.random.nextDouble() > 0.7)
            spawnParticles(world!!, containerPos)
        BlockPos.iterate(pos.offset(facing), pos.offset(facing, 3)).forEach {
            if (world!!.random.nextDouble() > 0.95)
                spawnParticles(world!!, it, 0.05)
        }
    }

    private fun spawnParticles(world: World, pos: BlockPos, width: Double = 0.5625) {
        val random = world.random
        Direction.values().forEach { direction ->
            val axis = direction.axis
            val e = if (axis == Direction.Axis.X) 0.5 + width * direction.offsetX
                .toDouble() else random.nextFloat().toDouble()
            val f = if (axis == Direction.Axis.Y) 0.5 + width * direction.offsetY
                .toDouble() else random.nextFloat().toDouble()
            val g = if (axis == Direction.Axis.Z) 0.5 + width * direction.offsetZ
                .toDouble() else random.nextFloat().toDouble()
            world.addParticle(
                if (random.nextDouble() > 0.5) LIGHTER else DARKER,
                pos.x.toDouble() + e,
                pos.y.toDouble() + f,
                pos.z.toDouble() + g,
                0.0,
                0.0,
                0.0
            )
        }
    }

    private fun explode() {
        world?.createExplosion(
            null,
            pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
            3f,
            true,
            Explosion.DestructionType.DESTROY
        )
    }

    companion object {
        private val LIGHTER = DustParticleEffect(115 / 255f, 239 / 255f, 232 / 255f, 1.0f)
        private val DARKER = DustParticleEffect(89 / 255f, 205 / 255f, 223 / 255f, 1.0f)
    }
}