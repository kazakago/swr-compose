package com.kazakago.swr.runtime.internal

import com.kazakago.swr.runtime.DedupingIntervalException
import com.kazakago.swr.runtime.PausedException
import com.kazakago.swr.runtime.SWRConfig
import com.kazakago.swr.runtime.SWRValidateOptions
import com.kazakago.swr.store.SWRStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SWRValidate<KEY : Any, DATA>(
    private val store: SWRStore<KEY, DATA>,
    private val scope: CoroutineScope,
    private val config: SWRConfig<KEY, DATA>,
) {
    private val retryingJobs = mutableSetOf<Job>()
    private var dedupingIntervalJob: Job? = null

    suspend operator fun invoke(options: SWRValidateOptions? = null): Result<DATA> {
        if (config.isPaused?.invoke() == true) {
            return Result.failure(PausedException())
        }
        if (dedupingIntervalJob?.isActive == true && options == null) {
            return Result.failure(DedupingIntervalException())
        }
        dedupingIntervalJob = scope.launch {
            delay(config.dedupingInterval)
        }
        val loadingTimeoutJob = scope.launch {
            delay(config.loadingTimeout)
            config.onLoadingSlow?.invoke(store.key, config)
        }
        return store.validate()
            .onSuccess { data ->
                loadingTimeoutJob.cancel()
                retryingJobs.clear()
                config.onSuccess?.invoke(data, store.key, config)
            }
            .onFailure { error ->
                loadingTimeoutJob.cancel()
                config.onError?.invoke(error, store.key, config)
                if (config.shouldRetryOnError) {
                    val revalidateOptions = createValidateOptions(options?.retryCount ?: 0)
                    retryingJobs += scope.launch {
                        config.onErrorRetry(error, store.key, config, ::invoke, revalidateOptions)
                    }
                }
            }
    }

    private fun createValidateOptions(currentRetryCount: Int): SWRValidateOptions {
        return SWRValidateOptions(
            retryCount = currentRetryCount + 1,
            dedupe = (0 == currentRetryCount) && retryingJobs.any { it.isActive },
        )
    }
}
