package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.SancakKombiTheme
import com.example.ui.screens.LoginScreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [35])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      SancakKombiTheme {
        LoginScreen(
          isLoading = false,
          errorMessage = null,
          onLogin = { _, _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun remember_me_is_selected_by_default_and_can_be_disabled() {
    composeTestRule.setContent {
      SancakKombiTheme {
        LoginScreen(
          isLoading = false,
          errorMessage = null,
          onLogin = { _, _ -> }
        )
      }
    }

    val rememberMe = composeTestRule.onNodeWithTag("remember_me_checkbox")
    rememberMe.assertIsOn()
    rememberMe.performClick()
    rememberMe.assertIsOff()
  }
}
