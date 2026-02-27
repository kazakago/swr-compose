package com.kazakago.swr.store.persister

/**
 * Defines a persistence layer for SWR cached data.
 *
 * Implement this interface to persist data across app restarts (e.g. using a local database or file).
 * Pass an instance to `rememberSWR` or other SWR composables via the `persister` parameter.
 */
public interface Persister<KEY : Any, DATA> {

    /** Loads persisted data for [key], or returns `null` if no data has been saved. */
    public suspend fun loadData(key: KEY): DATA?

    /**
     * Saves [data] for [key].
     * Pass `null` to delete the persisted entry for [key].
     */
    public suspend fun saveData(key: KEY, data: DATA?)
}

/**
 * Creates a [Persister] from lambda functions.
 *
 * @param loadData Suspending function that loads data for a given key.
 * @param saveData Suspending function that saves (or deletes when `null`) data for a given key.
 */
public fun <KEY : Any, DATA> Persister(
    loadData: suspend (key: KEY) -> DATA?,
    saveData: suspend (key: KEY, data: DATA?) -> Unit,
): Persister<KEY, DATA> = object : Persister<KEY, DATA> {
    override suspend fun loadData(key: KEY) = loadData(key)
    override suspend fun saveData(key: KEY, data: DATA?) = saveData(key, data)
}
