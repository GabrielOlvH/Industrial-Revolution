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
        this.propertyDelegate = ArrayPropertyDelegate(5)
    }

    private var currentRecipe: T? = null
    val usedRecipes = mutableMapOf<Identifier, Int>()
    abstract val type: IRecipeGetter<T>
    var craftingComponents = Array(1) { CraftingComponent(0, this) }

    override fun machineTick() {
       craftingComponents.forEach { it.tick() }
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

    fun splitStacks() {
        if (craftingComponents.size <= 1) return
        val inventory = inventoryComponent!!.inventory
        val stacks = craftingComponents.flatMap { component -> component.inputSlots!!.map { inventory.getStack(it) }.filter { s -> !s.isEmpty } }
        val itemStack = stacks.maxByOrNull { stacks.filter { s -> s.item == it.item }.sumBy { s -> s.count } } ?: return
        val sum = stacks.filter { s -> s.item == itemStack.item }.sumBy { s -> s.count }
        val freeSlots = craftingComponents.flatMap { component -> component.inputSlots!!.filter { component.fits(ItemStack(itemStack.item), it) } }
        var remaining = sum
        freeSlots.forEachIndexed { index, slot ->
            val slotsUsed = freeSlots.size.coerceAtMost(sum)
            if (slotsUsed > freeSlots.size && index + 1 > slotsUsed) {
                inventory.setStack(slot, ItemStack.EMPTY)
                return@forEachIndexed
            }
            var set = floor(sum.toDouble() / slotsUsed.toDouble()).toInt()
            if (sum % slotsUsed != 0 && sum % slotsUsed > index && remaining > 0)
                set++
            if (index == slotsUsed - 1)
                set += remaining
            remaining -= set
            if (remaining < 0) set += remaining
            inventory.setStack(slot, ItemStack(itemStack.item, set))
        }
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        val craftTags = tag?.getList("craftingComponents", 10)
        craftTags?.forEach { craftTag ->
            val index = (craftTag as CompoundTag).getInt("index")
            craftingComponents[index].fromTag(craftTag)
        }
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
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        val craftTags = tag?.getList("craftingComponents", 10)
        craftTags?.forEach { craftTag ->
            val index = (craftTag as CompoundTag).getInt("index")
            craftingComponents[index].fromTag(craftTag)
        }
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        val craftTags = ListTag()
        craftingComponents.forEachIndexed { index, crafting ->
            val craftTag = CompoundTag()
            craftTags.add(crafting.toTag(craftTag))
            craftTag.putInt("index", index)
        }
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