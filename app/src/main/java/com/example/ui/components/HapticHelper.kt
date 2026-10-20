package com.example.ui.components

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticHelper {
    fun triggerSelection(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Throwable) {}
    }

    fun triggerImpact(haptic: HapticFeedback) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Throwable) {}
    }
}
