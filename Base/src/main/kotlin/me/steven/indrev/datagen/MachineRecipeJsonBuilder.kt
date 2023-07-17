package me.steven.indrev.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.steven.indrev.recipes.ALLOY_SMELTER_RECIPE_TYPE
import me.steven.indrev.recipes.COMPRESSOR_RECIPE_TYPE
import me.steven.indrev.recipes.MachineRecipe
import me.steven.indrev.recipes.PULVERIZER_RECIPE_TYPE
import me.steven.indrev.utils.identifier
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.CriterionMerger
import net.minecraft.advancement.criterion.CriterionConditions
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.RecipeProvider
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.function.Consumer

class MachineRecipeJsonBuilder(
    private val id: Identifier,
    val itemInput: Array<MachineRecipe.RecipeItemInput>,
    val itemOutput: Array<MachineRecipe.RecipeItemOutput>,
    val fluidInput: Array<MachineRecipe.RecipeFluidInput>,
    val fluidOutput: Array<MachineRecipe.RecipeFluidOutput>,
    val ticks: Int,
    val cost: Long,
    private val serializer: RecipeSerializer<MachineRecipe>,
) : CraftingRecipeJsonBuilder {

    private val advancementBuilder: Advancement.Builder = Advancement.Builder.create()
    var group: String? = null

    override fun criterion(string: String?, criterionConditions: CriterionConditions?): MachineRecipeJsonBuilder {
        advancementBuilder.criterion(string, criterionConditions)
        return this
    }

    override fun group(string: String?): MachineRecipeJsonBuilder {
        group = string
        return this
    }

    override fun getOutputItem(): Item {
        return itemOutput[0].item
    }
    private fun validate(recipeId: Identifier) {
        check(advancementBuilder.criteria.isNotEmpty()) { "No way of obtaining recipe $recipeId" }
    }
    override fun offerTo(exporter: Consumer<RecipeJsonProvider?>, recipeId: Identifier) {
     //   this.validate(recipeId)
        advancementBuilder.parent(CraftingRecipeJsonBuilder.ROOT)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(
                AdvancementRewards.Builder.recipe(recipeId)
            ).criteriaMerger(CriterionMerger.OR)
        exporter.accept(
            MachineRecipeJsonProvider(
                id,
                itemInput,
                itemOutput,
                fluidInput,
                fluidOutput,
                ticks,
                cost,
                advancementBuilder,
                recipeId.withPrefixedPath("recipes/misc/"),
                serializer,
                group
            )
        )
    }

    companion object {
        fun compress(exporter: Consumer<RecipeJsonProvider>, input: Item, output: Item) {
            val inputId = Registries.ITEM.getId(input).path
            val outputId = Registries.ITEM.getId(output).path
            MachineRecipeJsonBuilder(
                identifier(outputId + "_from_compressing_" + inputId),
                arrayOf(MachineRecipe.RecipeItemInput(Ingredient.ofItems(input), 1, 1.0)),
                arrayOf(MachineRecipe.RecipeItemOutput(output, 1, 1.0)),
                emptyArray(),
                emptyArray(),
                200,
                1,
                Registries.RECIPE_SERIALIZER.get(Registries.RECIPE_TYPE.getId(COMPRESSOR_RECIPE_TYPE)) as RecipeSerializer<MachineRecipe>
            ).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, outputId + "_from_compressing_" + inputId)
        }
        fun pulverize(exporter: Consumer<RecipeJsonProvider>, input: Item, output: Item) {
            val inputId = Registries.ITEM.getId(input).path
            val outputId = Registries.ITEM.getId(output).path
            MachineRecipeJsonBuilder(
                identifier(outputId + "_from_pulverizing_" + inputId),
                arrayOf(MachineRecipe.RecipeItemInput(Ingredient.ofItems(input), 1, 1.0)),
                arrayOf(MachineRecipe.RecipeItemOutput(output, 1, 1.0)),
                emptyArray(),
                emptyArray(),
                200,
                1,
                Registries.RECIPE_SERIALIZER.get(Registries.RECIPE_TYPE.getId(PULVERIZER_RECIPE_TYPE)) as RecipeSerializer<MachineRecipe>
            ).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, outputId + "_from_pulverizing_" + inputId)
        }

        fun dustIngotConversions(exporter: Consumer<RecipeJsonProvider>, input: Item, output: Item) {
            val outputId = Registries.ITEM.getId(output).path
            pulverize(exporter, input, output)
            RecipeProvider.offerSmelting(exporter, listOf(output), RecipeCategory.MISC, input, 0f, 200, outputId)
            RecipeProvider.offerBlasting(exporter, listOf(output), RecipeCategory.MISC, input, 0f, 100, outputId)
        }

        fun alloy(exporter: Consumer<RecipeJsonProvider>, left: Item, right: Item, output: Item) {
            return alloy(exporter, left, 1, right, 1, output, 1)
        }
        fun alloy(exporter: Consumer<RecipeJsonProvider>, left: Item, leftCount: Int, right: Item, rightCount: Int, output: Item, outCount: Int) {
            alloy(exporter, Ingredient.ofItems(left), leftCount, Ingredient.ofItems(right), rightCount, output, outCount)
        }

        fun alloy(exporter: Consumer<RecipeJsonProvider>, left: TagKey<Item>, leftCount: Int, right: TagKey<Item>, rightCount: Int, output: Item, outCount: Int) {
            alloy(exporter, Ingredient.fromTag(left), leftCount, Ingredient.fromTag(right), rightCount, output, outCount)
        }


        fun alloy(exporter: Consumer<RecipeJsonProvider>, left: Ingredient, leftCount: Int, right: Ingredient, rightCount: Int, output: Item, outCount: Int) {
            val outputId = Registries.ITEM.getId(output).path
            MachineRecipeJsonBuilder(
                identifier(outputId + "_from_alloy"),
                arrayOf(MachineRecipe.RecipeItemInput(left, leftCount, 1.0), MachineRecipe.RecipeItemInput(right, rightCount, 1.0)),
                arrayOf(MachineRecipe.RecipeItemOutput(output, outCount, 1.0)),
                emptyArray(),
                emptyArray(),
                200,
                1,
                Registries.RECIPE_SERIALIZER.get(Registries.RECIPE_TYPE.getId(ALLOY_SMELTER_RECIPE_TYPE)) as RecipeSerializer<MachineRecipe>
            )
              //  .criterion(hasItem(left.matchingStacks[0].item), conditionsFromItem(left.matchingStacks[0].item))
              //  .criterion(hasItem(right.matchingStacks[0].item), conditionsFromItem(right.matchingStacks[0].item))
                .offerTo(exporter, outputId + "_from_alloy")
        }


        private fun hasItem(item: ItemConvertible?): String {
            return "has_" + RecipeProvider.getItemPath(item)
        }
        private fun conditionsFromItem(item: ItemConvertible): InventoryChangedCriterion.Conditions? {
            return RecipeProvider.conditionsFromItemPredicates(
                ItemPredicate.Builder.create().items(*arrayOf(item)).build()
            )
        }
    }

    class MachineRecipeJsonProvider(
        private val id: Identifier,
        val itemInput: Array<MachineRecipe.RecipeItemInput>,
        val itemOutput: Array<MachineRecipe.RecipeItemOutput>,
        val fluidInput: Array<MachineRecipe.RecipeFluidInput>,
        val fluidOutput: Array<MachineRecipe.RecipeFluidOutput>,
        val ticks: Int,
        val cost: Long,
        private val advancementBuilder: Advancement.Builder,
        private val advancementId: Identifier?,
        private val serializer: RecipeSerializer<MachineRecipe>,
        val group: String?
        ) : RecipeJsonProvider {

        override fun serialize(json: JsonObject) {
            if (!group.isNullOrEmpty()) {
                json.addProperty("group", group)
            }

          //  json.addProperty("category", category.asString())
            val inputArray = JsonArray()
            this.itemInput.forEach { i ->
                val itemObj = i.ingredient.toJson().asJsonObject
                itemObj.addProperty("count", i.count)
                itemObj.addProperty("chance", i.chance)
                inputArray.add(itemObj)

            }
            if (!inputArray.isEmpty) json.add("input", inputArray)


            val fluidInputArray = JsonArray()
            this.fluidInput.forEach { i ->
                val fluidObj = JsonObject()
                fluidObj.addProperty("fluid", Registries.FLUID.getId(i.fluid).toString())
                fluidObj.addProperty("amount", i.amount)
                fluidObj.addProperty("chance", i.chance)
                fluidInputArray.add(fluidObj)

            }
            if (!fluidInputArray.isEmpty) json.add("fluidInput", fluidInputArray)

            val outputArray = JsonArray()
            this.itemOutput.forEach { i ->
                val itemObj = JsonObject()
                itemObj.addProperty("item", Registries.ITEM.getId(i.item).toString())
                itemObj.addProperty("count", i.count)
                itemObj.addProperty("chance", i.chance)
                outputArray.add(itemObj)

            }
            if (!outputArray.isEmpty) json.add("output", outputArray)

            val fluidOutputArray = JsonArray()
            this.fluidOutput.forEach { i ->
                val fluidObj = JsonObject()
                fluidObj.addProperty("fluid", Registries.FLUID.getId(i.fluid).toString())
                fluidObj.addProperty("amount", i.amount)
                fluidObj.addProperty("chance", i.chance)
                fluidOutputArray.add(fluidObj)

            }
            if (!fluidOutputArray.isEmpty) json.add("outputFluid", fluidOutputArray)


            json.addProperty("ticks", this.ticks)
            json.addProperty("cost", this.cost)
        }

        override fun getRecipeId(): Identifier = id

        override fun getSerializer(): RecipeSerializer<*> = serializer

        override fun toAdvancementJson(): JsonObject = advancementBuilder.toJson()

        override fun getAdvancementId(): Identifier? = advancementId
    }
}