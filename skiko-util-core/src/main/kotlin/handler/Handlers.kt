@file:Suppress("UNUSED")

package top.e404.skiko.handler

import top.e404.skiko.FontType
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.util.readJarFile

val handlers = readJarFile(FontType.Companion::class.java, "handlers.txt")
    .split("\n")
    .mapNotNull {
        val cls = Class.forName(it)
        cls.getField("INSTANCE").get(cls) as? FramesHandler
    }
