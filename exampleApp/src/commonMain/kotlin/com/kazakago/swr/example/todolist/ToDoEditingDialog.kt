package com.kazakago.swr.example.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ToDoEditingDialog(
    initialText: String,
    onSubmit: (text: String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    Dialog(onDismissRequest = {}) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    singleLine = true,
                    onValueChange = { text = it },
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSubmit(text) },
                ) {
                    Text(text = "OK")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCancel,
                ) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { onDelete() },
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewToDoEditingDialog() {
    MaterialTheme {
        Surface {
            ToDoEditingDialog(
                initialText = "hoge",
                onSubmit = {},
                onCancel = {},
                onDelete = {},
            )
        }
    }
}
