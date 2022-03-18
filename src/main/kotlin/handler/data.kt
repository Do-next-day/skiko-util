package top.e404.skiko.handler

import top.e404.skiko.ExtraData

data class IntData(var data: Int) : ExtraData
data class IntPairData(val i1: Int, val i2: Int): ExtraData
data class LongData(val data: Long) : ExtraData
data class DoubleData(val data: Double) : ExtraData
data class FloatData(val data: Float) : ExtraData
data class StringData(val data: String): ExtraData
data class StringPairData(val s1: String, val s2: String): ExtraData