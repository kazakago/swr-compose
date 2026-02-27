package com.kazakago.swr.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ErrorContent(
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Validation failed.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.size(4.dp))
        if (onRetry != null) {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview
@Composable
fun ErrorContentPreview() {
    MaterialTheme {
        Surface {
            ErrorContent {}
        }
    }
}
