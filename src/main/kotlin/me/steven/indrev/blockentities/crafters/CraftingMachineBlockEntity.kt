package me.steven.indrev.blockentities.crafters

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.getRecipes
import net.minecraft.block.BlockState
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.function.IntBinaryOperator
import kotlin.math.floor

abstract class CraftingMachineBlockEntity<T : IRRecipe>(tier: Tier, registry: MachineRegistry, pos: BlockPos, state: BlockState) :
    MachineBlockEntity<BasicMachineConfig>(tier, registry, pos, state) {

    override val maxOutput: Long = 0

    private var currentRecipe: T? = null
    val usedRecipes = Object2IntOpenHashMap<Identifier>()
    abstract val type: IRecipeGetter<T>
    var craftingComponents = Array(1) { CraftingComponent(0, this) }
    var isSplitOn = false

    override fun machineTick() {
        ticks++
        craftingComponents.forEach { it.tick() }
        workingState = craftingComponents.any { it.isCrafting }
        if (ticks % 20 == 0 && isSplitOn) { splitStacks() }
    }

    override fun getCapacity(): Long {
        return Enhancer.getBuffer(enhancerComponent)
    }

    override fun getEnergyCost(): Long {
        val speedEnhancers = (enhancerComponent!!.getCount(Enhancer.SPEED) * 2).coerceAtLeast(1)
        return (if (temperatureComponent?.isFullEfficiency() == true) config.energyCost * 1.5
        else config.energyCost).toLong() * speedEnhancers
    }

    open fun getBaseValue(enhancer: Enhancer): Double {
        val isFullEfficiency = temperatureComponent?.isFullEfficiency() == true
        return when (enhancer) {
            Enhancer.SPEED ->
                if (isFullEfficiency)
                    ((config as? HeatMachineConfig?)?.processTemperatureBoost ?: 1.0) * config.processSpeed
                else
                    config.processSpeed
            Enhancer.BUFFER -> config.maxEnergyStored.toDouble()
            else -> 0.0
        }
    }

    open fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED -> return 1
            Enhancer.BUFFER -> 4
            else -> 1
        }
    }

    open fun splitStacks() {
        if (craftingComponents.size <= 1) return
        val inventory = inventoryComponent!!.inventory
        splitStacks(inventory.inputSlots)
    }

    fun splitStacks(inputSlots: IntArray) {
        if (craftingComponents.size <= 1) return
        val inventory = inventoryComponent!!.inventory
        val (item, sum) = inputSlots.associateStacks { inventory.getStack(it) }.maxByOrNull { it.value } ?: return
        if (sum <= 0) return

        val freeSlots = inputSlots.filter { inventory.fits(item, it) }
        var remaining = sum
        val slotsUsed = freeSlots.size.coerceAtMost(sum)
        val rem = sum % slotsUsed
        val isBelowLimit = slotsUsed > freeSlots.size
        val baseAmount = Math.floorDiv(sum, slotsUsed)
        freeSlots.forEachIndexed { index, slot ->
            if (isBelowLimit && index + 1 > slotsUsed) {
                inventory.setStack(slot, ItemStack.EMPTY)
                return@forEachIndexed
            }
            var set = baseAmount
            if (rem != 0 && rem > index && remaining > 0)
                set++
            if (index == slotsUsed - 1)
                set += remaining
            remaining -= set
            if (remaining < 0) set += remaining
            inventory.setStack(slot, ItemStack(item, set))
        }
    }

    private inline fun IntArray.associateStacks(transform: (Int) -> ItemStack): Map<Item, Int> {
        return associateToStacks(Object2IntArrayMap(5), transform)
    }

    private inline fun <M : Object2IntArrayMap<Item>> IntArray.associateToStacks(destination: M, transform: (Int) -> ItemStack): M {
        for (element in this) {
            val stack = transform(element)
            if (!stack.isEmpty && stack.nbt?.isEmpty != false)
                destination.mergeInt(stack.item, stack.count, IntBinaryOperator { old, new -> old + new })
        }
        return destination
    }

    override fun fromTag(tag: NbtCompound) {
        val craftTags = tag.getList("craftingComponents", 10)
        craftTags?.forEach { craftTag ->
            val index = (craftTag as NbtCompound).getInt("index")
            craftingComponents[index].readNbt(craftTag)
        }
        isSplitOn = tag.getBoolean("split")
        super.fromTag(tag)
    }

    override fun toTag(tag: NbtCompound) {
        val craftTags = NbtList()
        craftingComponents.forEachIndexed { index, crafting ->
            val craftTag = NbtCompound()
            craftTags.add(crafting.writeNbt(craftTag))
            craftTag.putInt("index", index)
        }
        tag.put("craftingComponents", craftTags)
        tag.putBoolean("split", isSplitOn)
        return super.toTag(tag)
    }

    @Suppress("UNCHECKED_CAST")
    fun dropExperience(player: PlayerEntity) {
        val list = mutableListOf<T>()

        usedRecipes.forEach { (id, amount) ->
            for (recipes in world!!.recipeManager.getRecipes().values) {
                val recipe = recipes[id] ?: continue
                list.add(recipe as? T ?: continue)
                if (recipe is SmeltingRecipe)
                    spawnOrbs(world!!, player.pos, amount, recipe.experience)
                break
            }
        }

        player.unlockRecipes(list.toList())
        usedRecipes.clear()
    }

    private fun spawnOrbs(world: World, pos: Vec3d, amount: Int, experience: Float) {
        val xp = amount.toFloat() * experience
        var n = floor(xp).toInt()
        val decimal = xp % 1
        if (decimal != 0.0f && Math.random() < decimal.toDouble()) {
            ++n
        }
        ExperienceOrbEntity.spawn(world as ServerWorld, pos, n)
    }
}