package com.kazakago.swr.store.persister

public interface Persister<KEY : Any, DATA> {

    public suspend fun loadData(key: KEY): DATA?

    public suspend fun saveData(key: KEY, data: DATA?)
}

public fun <KEY : Any, DATA> Persister(
    loadData: suspend (key: KEY) -> DATA?,
    saveData: suspend (key: KEY, data: DATA?) -> Unit,
): Persister<KEY, DATA> = object : Persister<KEY, DATA> {
    override suspend fun loadData(key: KEY) = loadData(key)
    override suspend fun saveData(key: KEY, data: DATA?) = saveData(key, data)
}
