package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.ExperienceRewardRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.associateStacks
import net.minecraft.block.BlockState
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import team.reborn.energy.EnergySide
import kotlin.math.floor

abstract class CraftingMachineBlockEntity<T : IRRecipe>(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity<BasicMachineConfig>(tier, registry), Tickable, UpgradeProvider {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(6)
    }

    private var currentRecipe: T? = null
    val usedRecipes = mutableMapOf<Identifier, Int>()
    abstract val type: IRecipeGetter<T>
    var craftingComponents = Array(1) { CraftingComponent(0, this) }
    var isSplitOn = false

    override fun machineTick() {
        ticks++
        craftingComponents.forEach { it.tick() }
        workingState = craftingComponents.any { it.isCrafting }
        if (ticks % 20 == 0 && isSplitOn) { splitStacks() }
    }

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> config.energyCost
        Upgrade.SPEED ->
            if (temperatureComponent?.isFullEfficiency() == true)
                ((config as? HeatMachineConfig?)?.processTemperatureBoost ?: 1.0) * config.processSpeed
            else
                config.processSpeed
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
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

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        val craftTags = tag?.getList("craftingComponents", 10)
        craftTags?.forEach { craftTag ->
            val index = (craftTag as CompoundTag).getInt("index")
            craftingComponents[index].fromTag(craftTag)
        }
        isSplitOn = tag?.getBoolean("split") ?: isSplitOn
        super.fromTag(state, tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        val craftTags = ListTag()
        craftingComponents.forEachIndexed { index, crafting ->
            val craftTag = CompoundTag()
            craftTags.add(crafting.toTag(craftTag))
            craftTag.putInt("index", index)
        }
        tag?.put("craftingComponents", craftTags)
        tag?.putBoolean("split", isSplitOn)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        val craftTags = tag?.getList("craftingComponents", 10)
        craftTags?.forEach { craftTag ->
            val index = (craftTag as CompoundTag).getInt("index")
            craftingComponents[index].fromTag(craftTag)
        }
        isSplitOn = tag?.getBoolean("split") ?: isSplitOn
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        val craftTags = ListTag()
        craftingComponents.forEachIndexed { index, crafting ->
            val craftTag = CompoundTag()
            craftTags.add(crafting.toTag(craftTag))
            craftTag.putInt("index", index)
        }
        tag?.putBoolean("split", isSplitOn)
        return super.toClientTag(tag)
    }

    fun dropExperience(player: PlayerEntity) {
        val list = mutableListOf<T>()
        usedRecipes.forEach { (id, amount) ->
            world!!.recipeManager[id].ifPresent { recipe ->
                list.add(recipe as? T ?: return@ifPresent)
                spawnOrbs(world!!, player.pos, amount, ((recipe as? ExperienceRewardRecipe)?.amount ?: (recipe as? SmeltingRecipe)?.experience ?: return@ifPresent))
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
        while (n > 0) {
            val size = ExperienceOrbEntity.roundToOrbSize(n)
            n -= size
            world.spawnEntity(ExperienceOrbEntity(world, pos.x, pos.y, pos.z, size))
        }
    }

    open fun onCraft() {}
}