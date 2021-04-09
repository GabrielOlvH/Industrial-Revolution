package me.steven.indrev.blockentities.laser

import me.steven.indrev.IndustrialRevolution
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
import me.steven.indrev.utils.toVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.math.Vector3f
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

class LaserBlockEntity : MachineBlockEntity<MachineConfig>(Tier.MK4, MachineRegistry.LASER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    private var ticksUntilExplode = 100

    override fun machineTick() {
        if (!cachedState[LaserBlock.POWERED]) {
            ticksUntilExplode = 100
            return
        }
        val facing = cachedState[FacingMachineBlock.FACING]
        val containerPos = pos.offset(facing, 4)

        val container = world?.getBlockEntity(containerPos) as? CapsuleBlockEntity

        if (container == null) {
            ticksUntilExplode--
            if (ticksUntilExplode < 0) explode()
            return
        } else {
            ticksUntilExplode = 100
            BlockPos.iterate(pos.offset(facing), pos.offset(facing, 3)).forEach {
                world?.breakBlock(it, true)
            }
        }

        val stack = container.inventory[0]
        if ((stack.item != IRItemRegistry.MODULAR_CORE && stack.item != IRItemRegistry.MODULAR_CORE_ACTIVATED)) {
            world?.breakBlock(containerPos, false)
            return
        }

        if (!use(config.energyCost)) {
            world?.setBlockState(pos, cachedState.with(LaserBlock.POWERED, false))
            return
        }

        val (x, y, z) = scale(facing.vector3f(), 3.0f)
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
            world?.updateNeighbors(containerPos, container.cachedState.block)
        } else
            tag.putDouble("Progress", progress + config.energyCost)
    }

    @Environment(EnvType.CLIENT)
    override fun machineClientTick() {
        if (cachedState[LaserBlock.POWERED]) {
            if (!isEmittingLaser()) {
                if (world!!.random.nextDouble() > 0.7) {
                    spawnParticles(world!!, pos, isFire = true)
                    world!!.playSound(
                        pos.x.toDouble() + 0.5,
                        pos.y.toDouble() + 0.5,
                        pos.z.toDouble() + 0.5,
                        SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        SoundCategory.BLOCKS,
                        0.6f,
                        0.8f,
                        false
                    )
                }
                return
            }
            spawnParticles(world!!, pos, 0.4)
        }
    }

    private fun spawnParticles(world: World, pos: BlockPos, width: Double = 0.5625, isFire: Boolean = false) {
        val random = world.random
        val facing = cachedState[FacingMachineBlock.FACING]
        repeat(6) {
            val axis = facing.axis
            val x = if (axis != Direction.Axis.X) 0.7 - random.nextDouble() * width else 0.0
            val y = if (axis != Direction.Axis.Y) 0.7 - random.nextDouble() * width else 0.0
            val z = if (axis != Direction.Axis.Z) 0.7 - random.nextDouble() * width else 0.0
            val source = Vec3d(pos.x + x, pos.y + y, pos.z + z).let {
                val (x, y, z) = facing.unitVector
                if (x > 0 || y > 0 || z > 0)
                    it.add(Vec3d(facing.unitVector))
                else it
            }
            val velocity = pos.offset(facing, 4).toVec3d().add(x, y, z).subtract(source).normalize().multiply(0.18)
            world.addParticle(
                if (isFire) ParticleTypes.SMOKE else IndustrialRevolution.LASER_PARTICLE,
                true,
                source.x,
                source.y,
                source.z,
                if (isFire) 0.0 else velocity.x,
                if (isFire) 0.05 else velocity.y,
                if (isFire) 0.0 else velocity.z
            )
        }
    }

    fun isEmittingLaser(): Boolean {
        val facing = cachedState[FacingMachineBlock.FACING]
        val containerPos = pos.offset(facing, 4)
        return cachedState[LaserBlock.POWERED] && world?.getBlockEntity(containerPos) as? CapsuleBlockEntity != null
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
        private fun Direction.vector3f() = Vector3f(offsetX.toFloat(), offsetY.toFloat(), offsetZ.toFloat())

        private fun scale(v: Vector3f, scale: Float): Vector3f {
            return Vector3f(v.x * scale, v.y * scale, v.z * scale)
        }
    }
}