package com.kazakago.swr.runtime

/**
 * Handle for triggering a [SWRMutation].
 *
 * Obtained from [SWRMutationState.trigger][com.kazakago.swr.compose.SWRMutationState.trigger].
 */
public data class SWRTrigger<KEY : Any, DATA, ARG>(
    private val trigger: suspend (arg: ARG, config: SWRMutationConfig<KEY, DATA>.() -> Unit) -> Result<DATA>,
) {
    /**
     * Triggers the mutation.
     *
     * @param arg The argument passed to the mutation fetcher.
     * @param config Per-call configuration overrides applied on top of the default [SWRMutationConfig].
     * @return The result of the mutation.
     */
    public suspend operator fun invoke(
        arg: ARG,
        config: SWRMutationConfig<KEY, DATA>.() -> Unit = {},
    ): Result<DATA> = trigger(arg, config)
}
