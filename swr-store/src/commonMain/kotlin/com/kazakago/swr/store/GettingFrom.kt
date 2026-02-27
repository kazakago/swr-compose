package com.kazakago.swr.store

/**
 * Specifies the source to read data from when calling [SWRStore.get].
 */
public enum class GettingFrom {
    /** Read from the in-memory cache first; fall back to a remote fetch if no data is cached. */
    Both,

    /** Always fetch from the remote source, bypassing the in-memory cache. */
    RemoteOnly,

    /** Read only from the in-memory cache; never trigger a remote fetch. */
    LocalOnly,
}
