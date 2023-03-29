package me.steven.indrev.mixin.common;

import me.steven.indrev.components.CraftingComponent;
import me.steven.indrev.recipes.machines.IRRecipe;
import me.steven.indrev.recipes.machines.entries.InputEntry;
import me.steven.indrev.recipes.machines.entries.OutputEntry;
import me.steven.indrev.utils.IRFluidTank;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractCookingRecipe.class)
public abstract class MixinAbstractCookingRecipe implements IRRecipe {

    @Shadow @Final protected Identifier id;

    @Shadow @Final protected int cookTime;

    @Shadow @Final protected ItemStack output;

    @Shadow @Final protected Ingredient input;

    private InputEntry[] indrev_inputEntries;

    private OutputEntry[] indrev_outputEntries;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void a(RecipeType<?> type, Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime, CallbackInfo ci) {
        this.indrev_inputEntries = new InputEntry[] { new InputEntry(input, 1) };
        this.indrev_outputEntries = new OutputEntry[] { new OutputEntry(output, 1d) };
    }

    @NotNull
    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @NotNull
    @Override
    public InputEntry @NotNull [] getInput() {
        return indrev_inputEntries;
    }

    @NotNull
    @Override
    public OutputEntry @NotNull [] getOutputs() {
        return indrev_outputEntries;
    }

    @Override
    public int getTicks() {
        return cookTime;
    }

    @Override
    public boolean matches(@NotNull List<ItemStack> inv, @NotNull List<IRFluidTank> fluidVolume) {
        return this.input.test(inv.get(0));
    }

    @NotNull
    @Override
    public List<ItemStack> craft(Random random) {
        return Collections.singletonList(output.copy());
    }

    @NotNull
    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @NotNull
    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();
        defaultedList.add(this.input);
        return defaultedList;
    }

    @Override
    public boolean isEmpty() {
        DefaultedList<Ingredient> defaultedList = this.getIngredients();
        return defaultedList.isEmpty() || defaultedList.stream().anyMatch((ingredient) -> ingredient.getMatchingStacks().length == 0);
    }

    @Override
    public boolean canStart(@NotNull CraftingComponent<?> component) {
        return component.fits(output);
    }
}
