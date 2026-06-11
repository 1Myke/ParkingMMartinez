package com.lksnext.ParkingMMartinez

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.ui.constants.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class BookingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearSessionBeforeTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionManager(context)
        session.clearSession()
    }

    @Test
    fun testFullWorkflowFromLoginToBookingSuccess() {
        // --- 1. LOGIN CON EL USUARIO MANUAL REAL ---
        composeTestRule.onNodeWithTag(TestTags.LOGIN_EMAIL_FIELD).performTextInput("pruebas@lksnext.com")
        composeTestRule.onNodeWithTag(TestTags.LOGIN_PASSWORD_FIELD).performTextInput("Password123!")
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag(TestTags.LOGIN_SUBMIT_BTN).performClick()

        // --- 2. ESPERAR AL MAPA ---
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithTag(TestTags.MAP_HEADER).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // --- 3. SELECCIONAR DÍA Y ENTRAR A LA ZONA ESTÁNDAR ---
        val todayCalendar = Calendar.getInstance()
        val currentDayStr = todayCalendar.get(Calendar.DAY_OF_MONTH).toString()
        val todayDateTag = "${TestTags.MAP_DATE_ITEM_PREFIX}$currentDayStr"
        val standardZoneTag = "${TestTags.MAP_ZONE_CARD_PREFIX}Standard Zone"

        composeTestRule.onNodeWithTag(todayDateTag).performClick()
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag(standardZoneTag).performClick()

        // --- 4. CONFIGURAR DURACIÓN EN EL SLIDER DE BOOKINGSCREEN ---
        composeTestRule.onNodeWithTag(TestTags.BOOKING_DURATION_SLIDER).assertExists()
        Thread.sleep(1000)

        // Movemos el slider semánticamente a 3 horas
        composeTestRule.onNodeWithTag(TestTags.BOOKING_DURATION_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { it(3f) }
        Thread.sleep(1500)

        // --- 5. CONFIRMAR RESERVA ---
        composeTestRule.onNodeWithTag(TestTags.BOOKING_SUBMIT_BTN).assertIsEnabled()
        composeTestRule.onNodeWithTag(TestTags.BOOKING_SUBMIT_BTN).performClick()
        Thread.sleep(2000)

        // --- 6. VERIFICACIÓN FINAL ---
        composeTestRule.onNodeWithTag(TestTags.MAP_HEADER).assertExists()
    }
}