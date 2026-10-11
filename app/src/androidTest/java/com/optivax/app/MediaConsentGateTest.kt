package com.optivax.app

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.optivax.app.presentation.components.MediaConsentGate
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaConsentGateTest {
    @get:Rule val compose = createComposeRule()

    @Before fun clearConsent() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test fun mediaContentIsNotCreatedBeforeConsent() {
        compose.setContent {
            MaterialTheme { MediaConsentGate { Text("MEDIA_CONTENT") } }
        }
        compose.onNodeWithText("Fotoğraf, video ve ses verilerine erişim").assertIsDisplayed()
        compose.onNodeWithText("MEDIA_CONTENT").assertDoesNotExist()
    }

    @Test fun decliningAndReviewingDoesNotGrantConsent() {
        compose.setContent {
            MaterialTheme { MediaConsentGate { Text("MEDIA_CONTENT") } }
        }
        compose.onNodeWithText("Şimdi değil").performScrollTo().performClick()
        compose.onNodeWithText("MEDIA_CONTENT").assertDoesNotExist()
        compose.onNodeWithText("Açıklamayı tekrar göster").performScrollTo().performClick()
        compose.onNodeWithText("Kabul et ve devam et").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("MEDIA_CONTENT").assertDoesNotExist()
        val prefs = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("media_disclosure", Context.MODE_PRIVATE)
        org.junit.Assert.assertFalse(prefs.getBoolean("local_media_v1", false))
    }
}
