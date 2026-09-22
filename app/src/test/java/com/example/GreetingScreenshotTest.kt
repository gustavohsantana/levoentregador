package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.api.RouteParser
import com.example.ui.components.ActiveStopCard
import com.example.ui.theme.MyApplicationTheme
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
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun delivery_card_screenshot() {
        val demoRoute = RouteParser.createDemoRoute()
        val stop = demoRoute.stops[0]

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                ActiveStopCard(
                    stop = stop,
                    totalStops = demoRoute.totalStops,
                    onStartClick = {},
                    onDeliveredClick = {},
                    onFailedClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}

