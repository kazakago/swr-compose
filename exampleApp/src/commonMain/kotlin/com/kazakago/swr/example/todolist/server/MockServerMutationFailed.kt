package com.kazakago.swr.example.todolist.server

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object MockServerMutationFailed : MockServer {

    private var todoList: List<String> = listOf(
        "Remember the milk",
        "Call bob at 5pm.",
    )

    override suspend fun getToDoList(): List<String> {
        delay(1.seconds)
        return todoList
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
