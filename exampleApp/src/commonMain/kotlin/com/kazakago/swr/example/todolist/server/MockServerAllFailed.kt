package com.kazakago.swr.example.todolist.server

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object MockServerAllFailed : MockServer {

    override suspend fun getToDoList(): List<String> {
        delay(1.seconds)
        throw NullPointerException()
    }

    override suspend fun addToDo(value: String): List<String> {
        delay(1.seconds)
        throw NullPointerException()
    }

    override suspend fun editToDo(index: Int, value: String): List<String> {
        delay(1.seconds)
        throw NullPointerException()
    }

    override suspend fun removeToDo(index: Int): List<String> {
        delay(1.seconds)
        throw NullPointerException()
    }
}
