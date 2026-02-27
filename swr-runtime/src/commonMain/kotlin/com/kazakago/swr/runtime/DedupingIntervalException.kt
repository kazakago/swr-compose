package com.kazakago.swr.runtime

/**
 * Thrown internally when a revalidation is skipped because an identical request was made
 * within the [dedupingInterval][SWRConfig.dedupingInterval].
 */
public class DedupingIntervalException : RuntimeException()
