package com.optivax.app.presentation.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optivax.app.R

/** Separate affirmative consent before any app inventory or usage queries. */
@Composable
fun AppDataConsentGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
    }
    var accepted by rememberSaveable {
        mutableStateOf(preferences.getBoolean("local_apps_v1", false))
    }
    var declined by rememberSaveable { mutableStateOf(false) }
    if (accepted) {
        content()
        return
    }
    BackHandler { declined = true }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.apps_disclosure_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.apps_disclosure_body))
        if (declined) {
            Text(stringResource(R.string.apps_disclosure_declined))
            Button(onClick = { declined = false }) {
                Text(stringResource(R.string.media_disclosure_review))
            }
        } else {
            Button(onClick = {
                preferences.edit().putBoolean("local_apps_v1", true).apply()
                accepted = true
            }) { Text(stringResource(R.string.media_disclosure_accept)) }
            TextButton(onClick = { declined = true }) {
                Text(stringResource(R.string.media_disclosure_decline))
            }
        }
    }
}
