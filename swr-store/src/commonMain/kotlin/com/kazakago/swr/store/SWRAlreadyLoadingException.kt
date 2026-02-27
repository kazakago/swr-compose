package com.kazakago.swr.store

/**
 * Thrown when a fetch is triggered while another fetch for the same key is already in progress.
 */
public class SWRAlreadyLoadingException : RuntimeException()
