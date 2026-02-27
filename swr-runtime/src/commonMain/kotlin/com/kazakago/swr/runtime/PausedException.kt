package com.kazakago.swr.runtime

/**
 * Thrown internally when a revalidation is skipped because [SWRConfig.isPaused] returned `true`.
 */
public class PausedException : RuntimeException()
