package me.steven.indrev.utils

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.Vec3i


operator fun Vec3i.component1() = x
operator fun Vec3i.component2() = y
operator fun Vec3i.component3() = z

operator fun Vec3d.component1() = this.x
operator fun Vec3d.component2() = this.y
operator fun Vec3d.component3() = this.z

operator fun Vec3f.component1() = this.x
operator fun Vec3f.component2() = this.y
operator fun Vec3f.component3() = this.z

operator fun ItemStack.component1(): ItemStack = this
operator fun ItemStack.component2(): Item = item