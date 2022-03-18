@file:Suppress("UNUSED")

package top.e404.skiko.handler

import top.e404.skiko.ImageHandler

enum class Handlers(alias: Regex, handler: ImageHandler) {
    Blur(Regex("(?i)(高斯)?模糊|blur"), top.e404.skiko.handler.filter.BlurFilter),
}