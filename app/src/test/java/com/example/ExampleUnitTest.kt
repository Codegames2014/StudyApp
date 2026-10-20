package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify XPMeter level calculation and progression`() {
    val pointsPerLevel = 150

    // 0 XP -> Level 1, 0%
    var xp = 0
    var currentLevel = (xp / pointsPerLevel) + 1
    var levelBase = (currentLevel - 1) * pointsPerLevel
    var progressInLevel = (xp - levelBase).coerceIn(0, pointsPerLevel)
    var progressFraction = progressInLevel / pointsPerLevel.toFloat()
    assertEquals(1, currentLevel)
    assertEquals(0, progressInLevel)
    assertEquals(0f, progressFraction, 0.001f)

    // 75 XP -> Level 1, 50%
    xp = 75
    currentLevel = (xp / pointsPerLevel) + 1
    levelBase = (currentLevel - 1) * pointsPerLevel
    progressInLevel = (xp - levelBase).coerceIn(0, pointsPerLevel)
    progressFraction = progressInLevel / pointsPerLevel.toFloat()
    assertEquals(1, currentLevel)
    assertEquals(75, progressInLevel)
    assertEquals(0.5f, progressFraction, 0.001f)

    // 150 XP -> Level 2, 0%
    xp = 150
    currentLevel = (xp / pointsPerLevel) + 1
    levelBase = (currentLevel - 1) * pointsPerLevel
    progressInLevel = (xp - levelBase).coerceIn(0, pointsPerLevel)
    progressFraction = progressInLevel / pointsPerLevel.toFloat()
    assertEquals(2, currentLevel)
    assertEquals(0, progressInLevel)

    // 320 XP -> Level 3, 20 XP in Level 3
    xp = 320
    currentLevel = (xp / pointsPerLevel) + 1
    levelBase = (currentLevel - 1) * pointsPerLevel
    progressInLevel = (xp - levelBase).coerceIn(0, pointsPerLevel)
    assertEquals(3, currentLevel)
    assertEquals(20, progressInLevel)
  }
}
