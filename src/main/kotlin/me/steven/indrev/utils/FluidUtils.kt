package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount

val NUGGET_AMOUNT = FluidAmount.of(10, 625)
val INGOT_AMOUNT = NUGGET_AMOUNT.mul(9)
val BLOCK_AMOUNT = INGOT_AMOUNT.mul(9)