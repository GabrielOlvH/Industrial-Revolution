package me.steven.indrev.utils

import me.steven.indrev.blockentities.crafting.CraftingMachineBlockEntity
import me.steven.indrev.recipes.MachineRecipe
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ProcessingBoost(val text: MutableText, val multiplier: Double, val color: Int) {

    fun append(tooltip: MutableList<Text>) {
        tooltip.add(text.copy().styled { s -> s.withColor(color).withItalic(true) })
    }

    companion object {
        fun appendTickTime(boosts: List<ProcessingBoost>, recipe: MachineRecipe, processSpeed: Double): MutableText {
                val text = Text.literal("")
                val ticksText = Text.literal(" ticks")
                val originalTicks = Text.literal(format(recipe.ticks / processSpeed))
                if (boosts.isNotEmpty()) {
                    var multiplier = processSpeed
                    var prevText = originalTicks
                    text.append(prevText)
                    boosts.forEach { boost ->
                        multiplier += boost.multiplier
                        val totalTicks = recipe.ticks / multiplier
                        prevText.formatted(Formatting.STRIKETHROUGH)
                        val newText = Text.literal(format(totalTicks)).styled { s -> s.withColor(boost.color) }
                        text.append(" ").append(newText)
                        prevText = newText
                        ticksText.styled { s -> s.withColor(boost.color) }
                    }

                } else {
                    text.append(originalTicks)
                }
                text.append(ticksText).formatted(Formatting.GRAY)
                return text
        }

        private fun format(v: Double) = if (v % 1 == 0.0) "%.0f".format(v) else "%.1f".format(v)

        fun getActiveBoosts(machine: CraftingMachineBlockEntity): List<ProcessingBoost> {
            val boosts = mutableListOf<ProcessingBoost>()

            if (machine.temperatureController.exists() && machine.temperatureController.isFullEfficiency()) {
                boosts.add(ProcessingBoost(Text.literal("Boosted by temperature"), machine.config!!.temperatureBoost, 0x3cf000))
            }

            if (machine.upgrades.exists() && machine.upgrades.getSpeedMultiplier() > 0) {
                boosts.add(ProcessingBoost(Text.literal("Overclocked"), machine.upgrades.getSpeedMultiplier(), 0xf07800))
            }

            return boosts
        }
    }
}