package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount

val NUGGET_AMOUNT: FluidAmount = FluidAmount.of(10, 625)
val INGOT_AMOUNT: FluidAmount = NUGGET_AMOUNT.mul(9)
val BLOCK_AMOUNT: FluidAmount = INGOT_AMOUNT.mul(9)
val SCRAP_AMOUNT: FluidAmount = INGOT_AMOUNT.div(4)