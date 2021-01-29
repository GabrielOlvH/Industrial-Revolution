package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.gui.controllers.machines.LaserController
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LaserBlock(registry: MachineRegistry, settings: Settings) : FacingMachineBlock(
    registry, settings, Tier.MK4, IndustrialRevolution.CONFIG.machines.laser, ::LaserController
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