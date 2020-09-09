package me.steven.indrev.mixin;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import me.steven.indrev.recipes.machines.IRRecipe;
import me.steven.indrev.recipes.machines.entries.InputEntry;
import me.steven.indrev.recipes.machines.entries.OutputEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(AbstractCookingRecipe.class)
public abstract class MixinAbstractCookingRecipe implements IRRecipe {
    @Shadow @Final protected RecipeType<?> type;

    @Shadow @Final protected Identifier id;

    @Shadow @Final protected int cookTime;

    @Shadow @Final protected ItemStack output;

    @Shadow @Final protected Ingredient input;

    @NotNull
    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @NotNull
    @Override
    public InputEntry @NotNull [] getInput() {
        return new InputEntry[] { new InputEntry(input, 1) };
    }

    @NotNull
    @Override
    public OutputEntry @NotNull [] getOutputs() {
        return new OutputEntry[] { new OutputEntry(output, 1d) };
    }

    @Override
    public int getTicks() {
        return cookTime;
    }

    @Override
    public boolean matches(@NotNull Inventory inv, @Nullable FluidVolume fluidVolume) {
        return ((AbstractCookingRecipe) (Object) this).matches(inv, null);
    }

    @NotNull
    @Override
    public ItemStack @NotNull [] craft(Random random) {
        return new ItemStack[] { output.copy() };
    }

    @NotNull
    @Override
    public Identifier getId() {
        return id;
    }
}
