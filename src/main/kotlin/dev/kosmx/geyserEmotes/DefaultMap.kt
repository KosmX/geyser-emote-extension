package dev.kosmx.geyserEmotes

/**
 * Map with default values instead of nulls
 */
class DefaultMap<K, V> private constructor(val defaultValueFactory: () -> V, private val dataStore: MutableMap<K, V>  ): MutableMap<K, V> by dataStore {
    constructor(defaultValueFactory: () -> V) : this(defaultValueFactory, mutableMapOf())
    constructor(defaultValue: V) : this({ defaultValue })

    override fun get(key: K): V = dataStore[key] ?: defaultValueFactory()

    override fun put(key: K, value: V): V = dataStore.put(key, value) ?: defaultValueFactory()
}