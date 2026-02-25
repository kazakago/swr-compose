package com.kazakago.swr.runtime

public data class SWRTrigger<KEY : Any, DATA, ARG>(
    private val trigger: suspend (arg: ARG, config: SWRMutationConfig<KEY, DATA>.() -> Unit) -> Result<DATA>,
) {
    public suspend operator fun invoke(
        arg: ARG,
        config: SWRMutationConfig<KEY, DATA>.() -> Unit = {},
    ): Result<DATA> = trigger(arg, config)
}
