package com.optivax.app.presentation.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.optivax.app.R

@Composable
fun AccessibilityDisclosureDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(stringResource(R.string.accessibility_disclosure_title)) },
        text = {
            Text(stringResource(R.string.accessibility_disclosure_body),
                modifier = Modifier.verticalScroll(rememberScrollState()))
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.accessibility_disclosure_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.accessibility_disclosure_decline))
            }
        }
    )
}
