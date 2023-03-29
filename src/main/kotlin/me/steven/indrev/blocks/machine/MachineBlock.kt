package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.config.IConfig
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.items.misc.IRMachineUpgradeItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.screwdriver
import me.steven.indrev.utils.wrench
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import me.steven.indrev.utils.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.util.EnumMap

open class MachineBlock(
    val registry: MachineRegistry,
    settings: Settings,
    val tier: Tier,
    val config: IConfig?,
    private val screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
) : Block(settings), BlockEntityProvider, InventoryProvider{

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = registry.blockEntityType(tier).instantiate(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient)
            BlockEntityTicker { _, _, _, blockEntity -> (blockEntity as? MachineBlockEntity<*>)?.machineClientTick() }
        else
            BlockEntityTicker { _, _, _, blockEntity -> (blockEntity as? MachineBlockEntity<*>)?.tick() }
    }

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
            val result = StorageUtil.move(ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM), blockEntity.fluidComponent, { true }, Long.MAX_VALUE, null)
            if (result > 0) return ActionResult.SUCCESS
        }
        val stack = player?.mainHandStack!!
        val item = stack.item
        if (item is IRMachineUpgradeItem) {
            return ActionResult.PASS
        } else if (stack.isIn(IndustrialRevolution.WRENCH_TAG)) {
            return wrench(world, pos!!, state!!, blockEntity, player, stack)
        } else if (stack.isIn(IndustrialRevolution.SCREWDRIVER_TAG)) {
            return screwdriver(world, pos!!, state!!, blockEntity, player, stack)
        } else if (blockEntity.multiblockComponent != null
            && !blockEntity.multiblockComponent!!.isBuilt(world, pos!!, blockEntity.cachedState, true)) {
            player.sendMessage(translatable("text.multiblock.not_built"), true)
            blockEntity.multiblockComponent?.toggleRender(player.isSneaking)
            blockEntity.markDirty()
            blockEntity.sync()
        } else if (screenHandler != null) {
            player.openHandledScreen(IRScreenHandlerFactory(screenHandler, pos!!))
        } else return ActionResult.PASS
        return ActionResult.SUCCESS
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        val oldBlockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*>
        super.onStateReplaced(state, world, pos, newState, moved)
        if (world.isClient) return

        if (newState.isOf(this)) {
            val oldFacing = getFacing(state)
            val newFacing = getFacing(newState)
            if (oldFacing == newFacing) return

            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
            val rotation = offset(oldFacing, newFacing)
            blockEntity.inventoryComponent?.run {
                update(EnumMap(itemConfig).clone(), itemConfig, rotation)
            }
            blockEntity.fluidComponent?.run {
                update(EnumMap(transferConfig).clone(), transferConfig, rotation)
            }
            if (blockEntity is LazuliFluxContainerBlockEntity) {
                update(EnumMap(blockEntity.transferConfig).clone(), blockEntity.transferConfig, rotation)
            }
            blockEntity.markDirty()
        } else if (oldBlockEntity?.inventoryComponent != null) {
            ItemScatterer.spawn(world, pos, oldBlockEntity.inventoryComponent!!.inventory)
            world.updateComparators(pos, this)
        }
    }

    private fun update(original: EnumMap<Direction, TransferMode>, config: SideConfiguration, rotation: BlockRotation) {
        Direction.values().forEach { side ->
            config[side] = original[rotation.rotate(side)]!!
        }
    }

    private fun offset(old: Direction, new: Direction): BlockRotation {
        return when (old) {
            new.rotateYClockwise() -> BlockRotation.CLOCKWISE_90
            new.rotateYCounterclockwise() -> BlockRotation.COUNTERCLOCKWISE_90
            new.opposite -> BlockRotation.CLOCKWISE_180
            else -> BlockRotation.NONE
        }
    }

    override fun afterBreak(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        player?.incrementStat(Stats.MINED.getOrCreateStat(this))
        player?.addExhaustion(0.005f)
        writeNbtComponents(world, player, pos, state, blockEntity, toolStack)
    }

    fun writeNbtComponents(world: World?, player: PlayerEntity?, pos: BlockPos?, state: BlockState?, blockEntity: BlockEntity?, toolStack: ItemStack?) {
        if (world is ServerWorld) {
            getDroppedStacks(state, world, pos, blockEntity, player, toolStack).forEach { stack ->
                val item = stack.item
                if (blockEntity is MachineBlockEntity<*> && item is BlockItem && item.block is MachineBlock) {
                    val itemIo = energyOf(stack)
                    if (itemIo != null) {
                        stack.orCreateNbt.putLong("energy", blockEntity.energy)
                    }
                    val tag = stack.getOrCreateSubNbt("MachineInfo")
                    val temperatureController = blockEntity.temperatureComponent
                    if (temperatureController != null)
                        tag.putDouble("Temperature", temperatureController.temperature)
                }
                dropStack(world, pos, stack)
            }
            state!!.onStacksDropped(world, pos, toolStack, true)
        }
    }

    override fun onPlaced(world: World?, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack?) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world?.isClient == true) return
        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is MachineBlockEntity<*>) {
            val tag = itemStack?.getSubNbt("MachineInfo")
            val temperatureController = blockEntity.temperatureComponent
            val itemIo = energyOf(itemStack)
            if (itemIo != null) {
                blockEntity.energy = itemIo.amount
            }
            if (temperatureController != null) {
                val temperature = tag?.getDouble("Temperature")
                if (temperature != null) temperatureController.temperature = temperature
            }
            ConfigurationType.values().forEach { type ->
                if (blockEntity.isConfigurable(type))
                    blockEntity.applyDefault(state, type, blockEntity.getCurrentConfiguration(type))
            }
            world.updateNeighbors(pos, this)
            blockEntity.markDirty()
        }
    }

    override fun neighborUpdate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        block: Block?,
        fromPos: BlockPos?,
        notify: Boolean
    ) {
        val blockEntity = world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return

        blockEntity.validConnections.clear()
        blockEntity.validConnections.addAll(Direction.values())
    }

    open fun getFacing(state: BlockState): Direction = Direction.UP

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory? {
        val blockEntity = world?.getBlockEntity(pos) as? InventoryProvider ?: return null
        return blockEntity.getInventory(state, world, pos)
    }

    @Environment(EnvType.CLIENT)
    override fun randomDisplayTick(state: BlockState?, world: World, pos: BlockPos, random: Random?) {
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
        if (blockEntity.workingState) {
            val d = pos.x.toDouble() + 0.5
            val e = pos.y.toDouble() + 1.0
            val f = pos.z.toDouble() + 0.5
            world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0)
        }
    }
}
