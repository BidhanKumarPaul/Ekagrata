package com.example

import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.FocusMode
import com.example.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun goal_progressPercentage_isCalculatedCorrectly() {
    val goal = Goal(
      title = "Complete Calculus Chapter 4",
      targetHours = 10f,
      completedHours = 8f
    )
    assertEquals(80, goal.progressPercentage)
    assertFalse(goal.isCompleted)
  }

  @Test
  fun appInfo_defaults_areSane() {
    val app = AppInfo(
      packageName = "com.google.android.calculator",
      activityName = "com.google.android.calculator.Calculator",
      label = "Calculator",
      category = AppCategory.ESSENTIAL,
      isEssential = true,
      isAllowedInFocus = true
    )
    assertTrue(app.isEssential)
    assertTrue(app.isAllowedInFocus)
    assertFalse(app.isFavorite)
  }
}

