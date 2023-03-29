package me.steven.indrev.items.energy

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.api.AttributeModifierProvider
import me.steven.indrev.api.CustomEnchantmentProvider
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.gui.tooltip.modular.ModularTooltipData
import me.steven.indrev.tools.modular.GamerAxeModule
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.tools.modular.Module
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
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
import me.steven.indrev.utils.literal
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleBatteryItem
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl
import java.util.*

class IRGamerAxeItem(
    material: ToolMaterial,
    maxStored: Long,
    tier: Tier,
    attackDamage: Float,
    attackSpeed: Float,
    settings: Settings
) : AxeItem(material, attackDamage, attackSpeed, settings), IREnergyItem, IRModularItem<Module>, AttributeModifierProvider, CustomEnchantmentProvider {

    init {
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, maxStored, tier.io, tier.io) }, this)
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        if (Screen.hasShiftDown())
            getInstalledTooltip(getInstalled(stack), stack, tooltip)
        val active = stack.nbt?.getBoolean("Active") ?: false
        tooltip.add(translatable("item.indrev.gamer_axe.tooltip.$active", literal("").append(IndustrialRevolutionClient.GAMER_AXE_TOGGLE_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(Formatting.GRAY))
        tooltip.add(translatable("item.indrev.modular_item.tooltip", literal("").append(IndustrialRevolutionClient.MODULAR_CONTROLLER_KEYBINDING.boundKeyLocalizedText).formatted(Formatting.AQUA)).formatted(Formatting.GRAY))
    }

    override fun getItemBarColor(stack: ItemStack?): Int = getDurabilityBarColor(stack)

    override fun isItemBarVisible(stack: ItemStack?): Boolean = hasDurabilityBar(stack)

    override fun getItemBarStep(stack: ItemStack?): Int = getDurabilityBarProgress(stack)

    override fun isEnchantable(stack: ItemStack?): Boolean = false

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        val modules = getInstalled(stack)
        return Optional.of(ModularTooltipData(handler.amount, handler.capacity, modules) { it.getLevel(stack) })
    }

    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        return ActionResult.PASS
    }

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState?): Float {
        val speedMultiplier = MiningToolModule.EFFICIENCY.getLevel(stack) + 1
        return if (!isActive(stack) || (energyOf(stack)?.amount ?: 0) <= 0) 0f
        else 8f * speedMultiplier
    }

    override fun hasGlint(stack: ItemStack?): Boolean {
        return stack?.nbt?.getBoolean("Active") == true
    }

    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        if (!isActive(stack) || miner !is PlayerEntity) return false
        val canStart = unsafeUse(stack, 1)
        if (canStart && !miner.isSneaking && state.isIn(BlockTags.LOGS)) {
            val scanned = mutableSetOf<BlockPos>()
            Direction.values().forEach { dir ->
                scanTree(scanned, world, stack, pos.offset(dir))
            }
        }
        return canStart
    }

    fun scanTree(scanned: MutableSet<BlockPos>, world: World, stack: ItemStack, pos: BlockPos) {
        if (!scanned.add(pos)) return
        val state = world.getBlockState(pos)
        if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES)) {
            if (unsafeUse(stack, 1)) {
                world.breakBlock(pos, true)
                if (scanned.size < 40)
                    Direction.values().forEach { dir ->
                        scanTree(scanned, world, stack, pos.offset(dir))
                    }
            }
        }
    }

    override fun postHit(stack: ItemStack, target: LivingEntity?, attacker: LivingEntity): Boolean {
        if (attacker !is PlayerEntity) return false
        val level = GamerAxeModule.REACH.getLevel(stack)
        if (isActive(stack) && level > 0) {
            target?.world?.getEntitiesByClass(LivingEntity::class.java, Box(target.blockPos).expand(level.toDouble())) { true }?.forEach { entity ->
                if (unsafeUse(stack, 1)) {
                    attacker.resetLastAttackedTicks()
                    attacker.attack(entity)
                }
            }
        }
        return unsafeUse(stack, 1)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getCompatibleModules(itemStack: ItemStack): Array<Module> = GamerAxeModule.COMPATIBLE

    fun isActive(stack: ItemStack): Boolean {
        val tag = stack.orCreateNbt ?: return false
        return tag.contains("Active") && tag.getBoolean("Active")
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        val tag = stack?.orCreateNbt ?: return

        tickAnimations(stack)

        if (isActive(stack) && !unsafeUse(stack, 5)) {
            tag.putBoolean("Active", false)
        }
    }

    fun unsafeUse(stack: ItemStack, amount: Long): Boolean {
        val itemIo = energyOf(stack)!!
        if (itemIo.amount > amount) {
            SimpleBatteryItem.setStoredEnergyUnchecked(stack, itemIo.amount - amount)
            return true
        }
        return false
    }

    private fun tickAnimations(stack: ItemStack) {
        val tag = stack.orCreateNbt
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
        if (!isActive(itemStack) || itemIo == null || itemIo.amount <= 0)
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