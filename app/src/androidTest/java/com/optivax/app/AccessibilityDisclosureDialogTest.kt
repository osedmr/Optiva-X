package com.optivax.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.espresso.Espresso
import com.optivax.app.presentation.components.AccessibilityDisclosureDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccessibilityDisclosureDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test fun disclosureRequiresExplicitAcceptanceAndOffersRejection() {
        var accepted = 0
        var declined = 0
        compose.setContent {
            MaterialTheme {
                AccessibilityDisclosureDialog({ accepted++ }, { declined++ })
            }
        }
        compose.onNodeWithText("Kabul ediyorum").assertIsDisplayed()
        compose.onNodeWithText("Reddediyorum").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(0, accepted)
            assertEquals(1, declined)
        }
        compose.onNodeWithText("Kabul ediyorum").performClick()
        compose.runOnIdle { assertEquals(1, accepted) }
    }

    @Test fun backDismissesWithoutConsent() {
        var accepted = 0
        var declined = 0
        compose.setContent {
            MaterialTheme {
                AccessibilityDisclosureDialog({ accepted++ }, { declined++ })
            }
        }
        Espresso.pressBack()
        compose.runOnIdle {
            assertEquals(0, accepted)
            assertEquals(1, declined)
        }
    }
}
