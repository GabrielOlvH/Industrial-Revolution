package me.steven.indrev.items.energy

import me.steven.indrev.tools.modular.GamerAxeModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.buildEnergyTooltip
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.ToolMaterial
import net.minecraft.tag.BlockTags
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.*

class IRGamerAxeItem(
    material: ToolMaterial,
    private val maxStored: Double,
    private val tier: Tier,
    attackDamage: Float,
    attackSpeed: Float,
    settings: Settings
) : AxeItem(material, attackDamage, attackSpeed, settings), EnergyHolder, IREnergyItem, IRModularItem {

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        Module.getInstalledTooltip(GamerAxeModule.getInstalled(stack), stack, tooltip)
        buildEnergyTooltip(stack, tooltip)
    }

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        if (world?.isClient == false) {
            val stack = user?.getStackInHand(hand)
            val tag = stack?.orCreateTag
            if (tag?.contains("Active") == false || tag?.contains("Progress") == false) {
                tag.putBoolean("Active", true)
                tag.putFloat("Progress", 0f)
            } else if (tag?.contains("Active") == true) {
                val active = !tag.getBoolean("Active")
                if (active && !Energy.of(stack).use(5.0))
                    return TypedActionResult.pass(stack)
                tag.putBoolean("Active", active)
            }
            return TypedActionResult.pass(stack)
        }
        return super.use(world, user, hand)
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        return ActionResult.PASS
    }

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val speedMultiplier = MiningToolModule.EFFICIENCY.getLevel(stack) + 1
        return if (!isActive(stack) || Energy.of(stack).energy <= 0) 0f
        else 8f * speedMultiplier
    }

    override fun hasGlint(stack: ItemStack?): Boolean {
        return stack?.tag?.getBoolean("Active") == true
    }

    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        if (!isActive(stack)) return false
        val energyHandler = Energy.of(stack)
        val canStart = energyHandler.use(1.0)
        if (canStart && !miner.isSneaking && state.block.isIn(BlockTags.LOGS)) {
            val scanned = mutableSetOf<BlockPos>()
            Direction.values().forEach { dir ->
                scanTree(scanned, world, energyHandler, pos.offset(dir))
            }
        }
        return canStart
    }

    fun scanTree(scanned: MutableSet<BlockPos>, world: World, energyHandler: EnergyHandler, pos: BlockPos) {
        if (!scanned.add(pos)) return
        val block = world.getBlockState(pos).block
        if (block.isIn(BlockTags.LOGS) || block.isIn(BlockTags.LEAVES)) {
            if (energyHandler.use(1.0)) {
                world.breakBlock(pos, true)
                if (scanned.size < 40)
                    Direction.values().forEach { dir ->
                        scanTree(scanned, world, energyHandler, pos.offset(dir))
                    }
            }
        }
    }

    override fun postHit(stack: ItemStack, target: LivingEntity?, attacker: LivingEntity?): Boolean {
        val level = GamerAxeModule.REACH.getLevel(stack)
        val handler = Energy.of(stack)
        if (attacker is PlayerEntity && isActive(stack) && level > 0) {
            target?.world?.getEntitiesByClass(LivingEntity::class.java, Box(target.blockPos).expand(level.toDouble())) { true }?.forEach { entity ->
                if (handler.use(1.0)) {
                    attacker.resetLastAttackedTicks()
                    attacker.attack(entity)
                }
            }
        }
        return handler.use(1.0)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getMaxStoredPower(): Double = maxStored

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getTier(): EnergyTier = EnergyTier.HIGH

    override fun getSlotLimit(): Int = -1

    override fun getCompatibleModules(itemStack: ItemStack): Array<Module> = GamerAxeModule.COMPATIBLE

    fun isActive(stack: ItemStack): Boolean {
        val tag = stack.orCreateTag ?: return false
        if (!tag.contains("Active")) return false
        return tag.getBoolean("Active")
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        val tag = stack?.orCreateTag ?: return
        if (!tag.contains("Active") || !tag.contains("Progress")) return
        val active = tag.getBoolean("Active")
        var progress = tag.getFloat("Progress")
        if (active && progress < 1) {
            progress += 0.12f
            if (progress >= 1)
                tag.putBoolean("Active", true)
        } else if (!active && progress > 0) {
            progress -= 0.12f
            if (progress <= 0)
                tag.putBoolean("Active", false)
        }
        tag.putFloat("Progress", progress.coerceIn(0f, 1f))

        val handler = Energy.of(stack)
        stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
    }
}