package com.example.exerciselog.ui.exerciseloglist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.exerciselog.MainActivity
import com.example.exerciselog.utils.TestTags
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class ExerciseLogsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun add_exerciseLog() {
        composeRule.onNodeWithTag(TestTags.ADD_EXERCISE_LOG).performClick()

        composeRule.onNodeWithTag(TestTags.CALORIES_TEXT_FIELD).performTextInput("1000")
        composeRule.onNodeWithTag(TestTags.SELECTING_DATE).performClick()
        composeRule.onNodeWithTag(TestTags.OK_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.SELECTING_TIME).performClick()
        composeRule.onNodeWithTag(TestTags.OK_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.SELECTING_DURATION).performClick()
        composeRule.onNodeWithTag(TestTags.OK_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.SAVE_EXERCISE_LOG).performClick()

        composeRule.onNodeWithText("OTHER WORKOUT").assertIsDisplayed()
    }

}