package com.kazakago.swr.example.todolist.server

import androidx.compose.runtime.compositionLocalOf

val LocalMockServer = compositionLocalOf<MockServer> {
    MockServerSucceed
}

interface MockServer {

    suspend fun getToDoList(): List<String>

    suspend fun addToDo(value: String): List<String>

    suspend fun editToDo(index: Int, value: String): List<String>

    suspend fun removeToDo(index: Int): List<String>
}
