package com.kazakago.swr.runtime

public data class SWRValidateOptions(
    public val retryCount: Int,
    public val dedupe: Boolean,
)
