package com.kazakago.swr.runtime.internal

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set

internal class ContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        applicationContext = context.applicationContext
        return applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
