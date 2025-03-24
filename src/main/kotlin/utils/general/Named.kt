package utils.general

import org.junit.jupiter.api.Named

fun <T> List<T>.named(name: String): Named<List<T>> = Named.named("${this.size} $name", this)

fun Int.named(name: String): Named<Int> = Named.named("$name = $this", this)

fun <K, V> Map<K, V>.named(): Named<Map<K, V>> {
    val resultStr = this.entries.joinToString(separator = " and ", transform = { "${it.key} is '${it.value}'" })
    return Named.named(resultStr, this)
}

fun Any.named(name: String): Any = Named.named(name, this)