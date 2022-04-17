@file:Suppress("UNUSED")

package top.e404.skiko.handler

import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.util.readJarFile

val handlers = readJarFile("handlers.txt")
    .split("\n")
    .mapNotNull { Class.forName(it).kotlin.objectInstance as? FramesHandler }