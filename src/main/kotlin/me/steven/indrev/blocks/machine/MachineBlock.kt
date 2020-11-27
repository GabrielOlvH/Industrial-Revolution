package me.steven.indrev.blocks.machine

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IConfig
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.items.misc.IRMachineUpgradeItem
import me.steven.indrev.items.misc.IRWrenchItem
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.TransferMode
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import team.reborn.energy.Energy
import java.util.*

open class MachineBlock(
    settings: Settings,
    val tier: Tier,
    val config: IConfig?,
    private val screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
    private val blockEntityProvider: () -> MachineBlockEntity<*>
) : Block(settings), BlockEntityProvider, InventoryProvider, AttributeProvider {

    init {
        if (this.defaultState.contains(WORKING_PROPERTY))
            this.defaultState = stateManager.defaultState.with(WORKING_PROPERTY, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(WORKING_PROPERTY)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return defaultState.with(WORKING_PROPERTY, false)
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult? {
        if (world.isClient) return ActionResult.CONSUME
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return ActionResult.FAIL
        if (blockEntity.fluidComponent != null) {
            val result = FluidInvUtil.interactHandWithTank(blockEntity.fluidComponent, player as ServerPlayerEntity, hand)
            if (result.asActionResult().isAccepted) return result.asActionResult()
        }
        val stack = player?.mainHandStack
        val item = stack?.item
        if (item is IRWrenchItem || item is IRMachineUpgradeItem) return ActionResult.PASS
        else if (blockEntity.multiblockComponent != null
            && !blockEntity.multiblockComponent!!.isBuilt(world, pos!!, blockEntity.cachedState)) {
            player?.sendMessage(TranslatableText("text.multiblock.not_built"), true)
            blockEntity.multiblockComponent?.toggleRender()
            blockEntity.markDirty()
            blockEntity.sync()
        } else if (screenHandler != null && blockEntity.inventoryComponent != null) {
            player?.openHandledScreen(IRScreenHandlerFactory(screenHandler, pos!!))
        }
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*>
        super.onStateReplaced(state, world, pos, newState, moved)
        if (!state.isOf(newState.block) && !world.isClient) {
            if (blockEntity?.inventoryComponent != null) {
                ItemScatterer.spawn(world, pos, blockEntity.inventoryComponent!!.inventory)
                world.updateComparators(pos, this)
            }
        }
    }

    override fun afterBreak(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        player?.incrementStat(Stats.MINED.getOrCreateStat(this))
        player?.addExhaustion(0.005f)
        toTagComponents(world, player, pos, state, blockEntity, toolStack)
    }

    fun toTagComponents(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        if (world is ServerWorld) {
            getDroppedStacks(state, world, pos, blockEntity, player, toolStack).forEach { stack ->
                val item = stack.item
                if (blockEntity is MachineBlockEntity<*> && item is BlockItem && item.block is MachineBlock) {
                    if (Energy.valid(stack))
                        Energy.of(stack).set(blockEntity.energy)
                    val tag = stack.getOrCreateSubTag("MachineInfo")
                    val temperatureController = blockEntity.temperatureComponent
                    if (temperatureController != null)
                        tag.putDouble("Temperature", temperatureController.temperature)
                }
                dropStack(world, pos, stack)
            }
            state!!.onStacksDropped(world, pos, toolStack)
        }
    }

    override fun onPlaced(world: World?, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack?) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world?.isClient == true) return
        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is MachineBlockEntity<*>) {
            val tag = itemStack?.getSubTag("MachineInfo")
            val temperatureController = blockEntity.temperatureComponent
            if (Energy.valid(itemStack))
                Energy.of(blockEntity).set(Energy.of(itemStack).energy)
            if (temperatureController != null) {
                val temperature = tag?.getDouble("Temperature")
                if (temperature != null) temperatureController.temperature = temperature
            }
            val invComponent = blockEntity.inventoryComponent
            val fluidComponent = blockEntity.fluidComponent
            if (invComponent != null)
                applyInitialItemConfiguration(state, invComponent.itemConfig)
            if (fluidComponent != null)
                applyInitialFluidConfiguration(state, fluidComponent.transferConfig)
        }
    }

    open fun applyInitialItemConfiguration(state: BlockState, itemConfig: MutableMap<Direction, TransferMode>) {
    }

    open fun applyInitialFluidConfiguration(state: BlockState, fluidConfig: MutableMap<Direction, TransferMode>) {
    }

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory? {
        val blockEntity = world?.getBlockEntity(pos) as? InventoryProvider
            ?: throw IllegalArgumentException("tried to retrieve an inventory from an invalid block entity")
        return blockEntity.getInventory(state, world, pos)
    }

    @Environment(EnvType.CLIENT)
    override fun randomDisplayTick(state: BlockState?, world: World, pos: BlockPos, random: Random?) {
        if (state?.contains(WORKING_PROPERTY) == true && state[WORKING_PROPERTY]) {
            val d = pos.x.toDouble() + 0.5
            val e = pos.y.toDouble() + 1.0
            val f = pos.z.toDouble() + 0.5
            world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0)
        }
    }

    override fun addAllAttributes(world: World?, pos: BlockPos?, blockState: BlockState?, to: AttributeList<*>) {
        val blockEntity = world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
        val fluidComponent = blockEntity.fluidComponent ?: return
        val opposite = to.searchDirection?.opposite
        if (to.attribute == FluidAttributes.INSERTABLE && fluidComponent.transferConfig[opposite]?.input == true)
            to.offer(fluidComponent)
        else if (to.attribute == FluidAttributes.EXTRACTABLE && fluidComponent.transferConfig[opposite]?.output == true)
            to.offer(fluidComponent)
    }

    companion object {
        val WORKING_PROPERTY: BooleanProperty = BooleanProperty.of("working")
    }
}