package com.kazakago.swr.compose

import com.kazakago.swr.compose.internal.GlobalNetworkMonitor
import com.kazakago.swr.compose.internal.NetworkMonitor
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class NetworkRule : TestRule {

    private val networkMonitor = mockk<NetworkMonitor>()
    private val connectionObserver = Channel<Boolean>(Channel.CONFLATED)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    setup()
                    base.evaluate()
                } finally {
                    teardown()
                }
            }
        }
    }

    private fun setup() {
        every { networkMonitor.isOnline() } returns true
        every { networkMonitor.onlineStatusFlow } returns connectionObserver.receiveAsFlow()
        GlobalNetworkMonitor = networkMonitor
    }

    private fun teardown() {
    }

    fun changeNetwork(isConnected: Boolean) {
        every { networkMonitor.isOnline() } returns isConnected
        connectionObserver.trySend(isConnected)
    }
}
