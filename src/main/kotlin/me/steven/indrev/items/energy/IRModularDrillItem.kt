package me.steven.indrev.items.energy

import draylar.magna.api.BlockFinder
import draylar.magna.api.BlockProcessor
import draylar.magna.api.MagnaTool
import draylar.magna.api.reach.ReachDistanceHelper
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.CustomEnchantmentProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.gui.screenhandlers.blockblacklister.BlockBlacklisterScreenHandler
import me.steven.indrev.gui.tooltip.modular.ModularTooltipData
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.ToolMaterial
import me.steven.indrev.utils.literal
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import java.util.*

class IRModularDrillItem(
    toolMaterial: ToolMaterial,
    tier: Tier,
    maxStored: Double,
    baseMiningSpeed: Float,
    settings: Settings
) : IRMiningDrillItem(toolMaterial, tier, maxStored, baseMiningSpeed, settings), MagnaTool, IRModularItem<Module>, CustomEnchantmentProvider {

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val material = state?.material
        val hasEnergy = (energyOf(stack)?.amount ?: 0) > 0
        val level = MiningToolModule.EFFICIENCY.getLevel(stack)
        var speedMultiplier = (level + 1) * 2
        if (level == 5) speedMultiplier *= 50
        return when {
            SUPPORTED_MATERIALS.contains(material) && hasEnergy -> baseMiningSpeed + speedMultiplier.toFloat()
            !hasEnergy -> 0F
            else -> super.getMiningSpeedMultiplier(stack, state)
        }
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        if (Screen.hasShiftDown())
            getInstalledTooltip(getInstalled(stack), stack, tooltip)
        tooltip?.add(
            translatable("item.indrev.modular_item.tooltip", literal("").append(
                IndustrialRevolutionClient.MODULAR_CONTROLLER_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(
                Formatting.GRAY))
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world!!.isClient && (DrillModule.CONTROLLED_DESTRUCTION.getLevel(stack) > 0 || DrillModule.MATTER_PROJECTOR.getLevel(stack) > 0)) {
            MinecraftClient.getInstance().setScreen(object : CottonClientScreen(BlockBlacklisterScreenHandler()) {
                override fun shouldPause(): Boolean = false
            })
            return TypedActionResult.success(stack)
        }
        return TypedActionResult.pass(stack)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (DrillModule.MATTER_PROJECTOR.getLevel(context.stack) <= 0) return ActionResult.PASS

        val world = context.world
        val player = context.player!!
        val blockState = world.getBlockState(context.blockPos)
        blockFinder.findPositions(world, context.player, getRadius(context.stack)).forEach { pos ->

            val offset = pos.offset(context.side)
            if (world.getBlockState(offset).material.isReplaceable) {
                val stackToRemove = ItemStack(blockState.block)
                val slot = player.inventory.getSlotWithStack(stackToRemove)
                if (slot >= 0) {
                    val removed = player.inventory.removeStack(slot, 1)
                    if (removed.item == stackToRemove.item && removed.count == 1) {
                        world.setBlockState(offset, blockState)
                    }
                } else if (player.isCreative) {
                    world.setBlockState(offset, blockState)
                }
            }
        }
        return ActionResult.success(world.isClient)
    }

    override fun getBlockFinder(): BlockFinder {
        return object : BlockFinder {
            override fun findPositions(
                world: World,
                playerEntity: PlayerEntity,
                radius: Int,
                depth: Int
            ): MutableList<BlockPos> {
                val cameraPos = playerEntity.getCameraPosVec(1f)
                val rotation = playerEntity.getRotationVec(1f)
                val reachDistance = ReachDistanceHelper.getReachDistance(playerEntity)
                val combined = cameraPos.add(rotation.x * reachDistance, rotation.y * reachDistance, rotation.z * reachDistance)
                val blockHitResult = world.raycast(RaycastContext(cameraPos, combined, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, playerEntity))

                val handStack = playerEntity.getStackInHand(Hand.MAIN_HAND)

                val blocks = super.findPositions(world, playerEntity, radius, depth)

                val center = getCenterPosition(world, playerEntity, blockHitResult, handStack)

                filterBlacklistedBlocks(center, blockHitResult, playerEntity, handStack, blocks)

                return blocks
            }
        }
    }

    override fun getCompatibleModules(itemStack: ItemStack): Array<Module> = DrillModule.COMPATIBLE

    override fun getLevel(enchantment: Enchantment, itemStack: ItemStack): Int {
        val module =
            when {
                Enchantments.FORTUNE == enchantment -> DrillModule.FORTUNE
                Enchantments.SILK_TOUCH == enchantment -> DrillModule.SILK_TOUCH
                else -> return -1
            }
        return module.getLevel(itemStack)
    }

    override fun getRadius(stack: ItemStack): Int = DrillModule.RANGE.getLevel(stack)

    override fun playBreakEffects(): Boolean = false

    override fun getCenterPosition(
        world: World,
        player: PlayerEntity,
        blockHitResult: BlockHitResult,
        toolStack: ItemStack
    ): BlockPos {
        val pos = blockHitResult.blockPos
        val radius = getRadius(toolStack)
        return if (blockHitResult.side.axis == Direction.Axis.Y || radius < 1) pos
        else pos.up(radius - 1)
    }

    override fun attemptBreak(
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity,
        breakRadius: Int,
        processor: BlockProcessor?
    ): Boolean {
        val mainHandStack = player.mainHandStack
        return if (getRadius(mainHandStack) > 0)
            super.attemptBreak(world, pos, player, breakRadius, processor)
        else false
    }

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        val modules = getInstalled(stack)
        return Optional.of(ModularTooltipData(handler.amount, handler.capacity, modules) { it.getLevel(stack) })
    }

    companion object {
        fun filterBlacklistedBlocks(center: BlockPos, blockHitResult: BlockHitResult, playerEntity: PlayerEntity, stack: ItemStack, blocks: MutableList<BlockPos>) {
            val blacklistedPositions = DrillModule.getBlacklistedPositions(stack)
            blocks.removeIf { pos ->

                var offset = pos.subtract(center)
                if (blockHitResult.side.axis.isVertical) {
                    offset = BlockPos(offset.x, offset.z, offset.y)
                    if (playerEntity.horizontalFacing.axis == Direction.Axis.Z) {
                        offset = BlockPos(offset.x * -playerEntity.horizontalFacing.offsetZ, offset.y * playerEntity.horizontalFacing.offsetZ, offset.z)
                    }
                    else if (playerEntity.horizontalFacing.axis == Direction.Axis.X) {
                        offset = BlockPos(offset.y * playerEntity.horizontalFacing.offsetX, offset.x * playerEntity.horizontalFacing.offsetX, offset.z)
                    }
                } else if (blockHitResult.side.axis == Direction.Axis.X) {
                    offset = BlockPos(offset.z * -blockHitResult.side.offsetX, offset.y, offset.x)
                } else if (blockHitResult.side.axis == Direction.Axis.Z) {
                    offset = BlockPos(offset.x * blockHitResult.side.offsetZ, offset.y, offset.z)
                }

                blacklistedPositions.contains(offset)
            }

        }
    }
}