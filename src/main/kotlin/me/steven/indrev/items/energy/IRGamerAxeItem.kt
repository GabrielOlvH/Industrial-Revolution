package me.steven.indrev.items.energy

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.AttributeModifierProvider
import me.steven.indrev.api.CustomEnchantmentProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.tools.modular.GamerAxeModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.use
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
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
import java.util.*

class IRGamerAxeItem(
    material: ToolMaterial,
    maxStored: Double,
    tier: Tier,
    attackDamage: Float,
    attackSpeed: Float,
    settings: Settings
) : AxeItem(material, attackDamage, attackSpeed, settings), IREnergyItem, IRModularItem<Module>, AttributeModifierProvider, CustomEnchantmentProvider {

    init {
        EnergyApi.ITEM.registerForItems(SimpleItemEnergyIo.getProvider(maxStored, tier.io, tier.io), this)
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        getInstalledTooltip(getInstalled(stack), stack, tooltip)
        buildEnergyTooltip(stack, tooltip)
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun isEnchantable(stack: ItemStack?): Boolean = false

    override fun use(world: World?, user: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        if (world?.isClient == false) {
            val stack = user?.getStackInHand(hand)
            val tag = stack?.orCreateTag
            if (tag?.contains("Active") == false || tag?.contains("Progress") == false) {
                tag.putBoolean("Active", true)
                tag.putFloat("Progress", 0f)
            } else if (tag?.contains("Active") == true) {
                val active = !tag.getBoolean("Active")
                if (active && energyOf(stack)?.use(5.0) == false)
                    return TypedActionResult.pass(stack)
                stack.orCreateTag.putBoolean("Active", active)
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
        return if (!isActive(stack) || (energyOf(stack)?.energy ?: 0.0) <= 0) 0f
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
        val energyHandler = energyOf(stack) ?: return false
        val canStart = energyHandler.use(1.0)
        if (canStart && !miner.isSneaking && state.isIn(BlockTags.LOGS)) {
            val scanned = mutableSetOf<BlockPos>()
            Direction.values().forEach { dir ->
                scanTree(scanned, world, energyHandler, pos.offset(dir))
            }
        }
        return canStart
    }

    fun scanTree(scanned: MutableSet<BlockPos>, world: World, energyHandler: EnergyIo, pos: BlockPos) {
        if (!scanned.add(pos)) return
        val state = world.getBlockState(pos)
        if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES)) {
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
        val handler =energyOf(stack) ?: return false
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

    override fun getCompatibleModules(itemStack: ItemStack): Array<Module> = GamerAxeModule.COMPATIBLE

    fun isActive(stack: ItemStack): Boolean {
        val tag = stack.orCreateTag ?: return false
        return tag.contains("Active") && tag.getBoolean("Active")
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        val tag = stack?.orCreateTag ?: return

        tickAnimations(stack)

        val itemIo = energyOf(stack)
        if (isActive(stack) && itemIo?.extract(5.0, Simulation.ACT) != 5.0) {
            tag.putBoolean("Active", false)
        }


    }

    private fun tickAnimations(stack: ItemStack) {
        val tag = stack.orCreateTag
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
    }

    override fun getAttributeModifiers(
        itemStack: ItemStack,
        equipmentSlot: EquipmentSlot
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val itemIo = energyOf(itemStack)
        if (!isActive(itemStack) || itemIo == null || itemIo.energy <= 0)
            return ImmutableMultimap.of()
        else if (equipmentSlot == EquipmentSlot.MAINHAND) {
            val builder = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>()
            builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                EntityAttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_ID,
                    "Tool modifier",
                    (itemStack.item as IRGamerAxeItem).attackDamage * (GamerAxeModule.SHARPNESS.getLevel(itemStack) / 2.0 + 1),
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
            builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                EntityAttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "Tool modifier",
                    1.0,
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
            return builder.build()
        }
        return getAttributeModifiers(equipmentSlot)
    }

    override fun getLevel(enchantment: Enchantment, itemStack: ItemStack): Int {
        val module =
            when {
                Enchantments.LOOTING == enchantment -> GamerAxeModule.LOOTING
                Enchantments.FIRE_ASPECT == enchantment -> GamerAxeModule.FIRE_ASPECT
                else -> return 0
            }
        return module.getLevel(itemStack)
    }

    companion object {
        private val ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF")
        private val ATTACK_SPEED_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3")
    }

}