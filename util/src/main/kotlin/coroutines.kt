package top.e404.skiko.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <A, B> Iterable<A>.pmap(block: suspend A.() -> B) =
    coroutineScope {
        map { async { block(it) } }.awaitAll()
    }.toMutableList()

suspend fun <A, B> Iterable<A>.pmapIndexed(block: suspend A.(Int) -> B): MutableList<B> =
    coroutineScope {
        mapIndexed { index, a -> async { a.block(index) } }.awaitAll()
    }.toMutableList()