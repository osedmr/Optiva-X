package com.optivax.app.presentation.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.optivax.app.R
import com.optivax.app.core.PermissionHelper

/** No media-reading screen or ViewModel is created before explicit consent.
 * Version the key when the disclosed data practices change. Backup rules exclude this preference.
 */
@Composable
fun MediaConsentGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
    }
    var accepted by rememberSaveable {
        mutableStateOf(preferences.getBoolean("local_media_v1", false))
    }
    var declined by rememberSaveable { mutableStateOf(false) }
    var requesting by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        requesting = false
        accepted = true
    }

    if (accepted && !requesting) {
        content()
        return
    }

    // Back is rejection, never consent; Home leaves this screen pending.
    BackHandler { declined = true }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.media_disclosure_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.media_disclosure_body))
        if (declined) {
            Text(stringResource(R.string.media_disclosure_declined))
            Button(onClick = { declined = false }) {
                Text(stringResource(R.string.media_disclosure_review))
            }
        } else {
            Button(enabled = !requesting, onClick = {
                // Persist only an affirmative action, before requesting any OS media permission.
                preferences.edit().putBoolean("local_media_v1", true).apply()
                requesting = true
                launcher.launch(PermissionHelper.getRequiredStoragePermissions())
            }) {
                Text(stringResource(R.string.media_disclosure_accept))
            }
            TextButton(enabled = !requesting, onClick = { declined = true }) {
                Text(stringResource(R.string.media_disclosure_decline))
            }
        }
    }
}
