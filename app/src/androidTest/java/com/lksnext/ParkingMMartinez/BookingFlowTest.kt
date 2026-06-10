package com.lksnext.ParkingMMartinez

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class BookingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCompleteStandardReservationFlowSuccess() {

        val todayCalendar = Calendar.getInstance()
        val currentDayStr = todayCalendar.get(Calendar.DAY_OF_MONTH).toString()

        val todayDateTag = "${TestTags.MAP_DATE_ITEM_PREFIX}$currentDayStr"
        val standardZoneTag = "${TestTags.MAP_ZONE_CARD_PREFIX}Standard Zone"

        composeTestRule.onNodeWithTag(todayDateTag).performClick()

        composeTestRule.onNodeWithTag(standardZoneTag).performClick()

        composeTestRule.onNodeWithTag(TestTags.BOOKING_DURATION_SLIDER).assertExists()

        composeTestRule.onNodeWithTag(TestTags.BOOKING_DURATION_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }

        composeTestRule.onNodeWithTag(TestTags.BOOKING_SUBMIT_BTN).assertIsEnabled()

        composeTestRule.onNodeWithTag(TestTags.BOOKING_SUBMIT_BTN).performClick()

        composeTestRule.onNodeWithTag(TestTags.MAP_HEADER).assertExists()
    }
}