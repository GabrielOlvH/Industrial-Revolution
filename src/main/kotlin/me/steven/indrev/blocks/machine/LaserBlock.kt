package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.LaserEmitterScreenHandler
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class LaserBlock(registry: MachineRegistry, settings: Settings) : FacingMachineBlock(
    registry, settings, Tier.MK4, IRConfig.machines.laser, ::LaserEmitterScreenHandler
) {

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return super.getPlacementState(ctx)?.with(POWERED, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(POWERED)
    }

    @Suppress("DEPRECATION")
    override fun neighborUpdate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        block: Block?,
        fromPos: BlockPos?,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify)

        world?.setBlockState(pos, state?.with(POWERED, world.isReceivingRedstonePower(pos)))
    }

    override fun randomDisplayTick(state: BlockState?, world: World, pos: BlockPos, random: Random?) {
        if (state!![POWERED] && random!!.nextDouble() > 0.9) {
            val (x, y, z) = pos
            world.playSound(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5,
                IndustrialRevolution.LASER_SOUND_EVENT, SoundCategory.BLOCKS, 0.4f, 1f, false
            )
        }
    }


    companion object {
        val POWERED: BooleanProperty = Properties.POWERED
        val LASER_DAMAGE_SOURCE = object : DamageSource("laser") {
            init {
                setFire()
                setBypassesArmor()
            }
        }
    }
}