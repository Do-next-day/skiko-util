package top.e404.skiko.apt.annotation

import javax.annotation.processing.RoundEnvironment

fun <T : Annotation> RoundEnvironment.filterHasAnnotation(annotation: Class<T>) =
    rootElements.filter {
        it.getAnnotation(annotation) != null
    }.map {
        it.toString()
    }
