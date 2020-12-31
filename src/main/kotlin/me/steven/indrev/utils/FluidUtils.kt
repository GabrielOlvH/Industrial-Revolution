package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount

val NUGGET_AMOUNT: FluidAmount = FluidAmount.of(10, 625)
val INGOT_AMOUNT: FluidAmount = NUGGET_AMOUNT.mul(9)
val BLOCK_AMOUNT: FluidAmount = INGOT_AMOUNT.mul(9)
val SCRAP_AMOUNT: FluidAmount = INGOT_AMOUNT.div(4)

val MB: FluidAmount = FluidAmount.BUCKET.div(1000)

operator fun FluidAmount.plus(volume: FluidAmount): FluidAmount {
    return add(volume)
}

operator fun FluidAmount.minus(volume: FluidAmount): FluidAmount {
    return sub(volume)
}

operator fun FluidAmount.times(volume: FluidAmount): FluidAmount {
    return mul(volume)
}

operator fun FluidAmount.times(whole: Int): FluidAmount {
    return mul(whole.toLong())
}

operator fun FluidAmount.div(volume: FluidAmount): FluidAmount {
    return div(volume)
}