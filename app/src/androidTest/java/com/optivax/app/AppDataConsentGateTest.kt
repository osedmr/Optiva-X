package com.optivax.app

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.optivax.app.data.system.UsageStatsHelper
import com.optivax.app.presentation.components.AppDataConsentGate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppDataConsentGateTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences get() = context.getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)

    @Before fun resetConsent() {
        // An upgrade with previous media consent must still disclose app data separately.
        preferences.edit().clear().putBoolean("local_media_v1", true).commit()
    }

    @Test fun previousMediaConsentDoesNotGrantAppConsent() {
        compose.setContent { MaterialTheme { AppDataConsentGate { Text("APP_CONTENT") } } }
        compose.onNodeWithText("APP_CONTENT").assertDoesNotExist()
        val helper = UsageStatsHelper(context)
        assertTrue(helper.getInstalledApplications(true).isEmpty())
        assertTrue(helper.queryUsageStatsMap(0, System.currentTimeMillis()).isEmpty())
    }

    @Test fun declineAndReviewKeepQueriesBlockedUntilAcceptance() {
        compose.setContent { MaterialTheme { AppDataConsentGate { Text("APP_CONTENT") } } }
        compose.onNodeWithText("Şimdi değil").performScrollTo().performClick()
        compose.onNodeWithText("APP_CONTENT").assertDoesNotExist()
        assertFalse(preferences.getBoolean("local_apps_v1", false))
        compose.onNodeWithText("Açıklamayı tekrar göster").performScrollTo().performClick()
        compose.onNodeWithText("APP_CONTENT").assertDoesNotExist()
        compose.onNodeWithText("Kabul et ve devam et").performScrollTo().performClick()
        compose.onNodeWithText("APP_CONTENT").assertIsDisplayed()
        assertTrue(preferences.getBoolean("local_apps_v1", false))
    }
}
