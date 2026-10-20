package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NcertDatabase
import com.example.data.local.StudyFluidRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StudyFluid", appName)
    }

    @Test
    fun `verify multi student profile switching and isolation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyFluidRepository(context)

        val initialProfiles = repo.profiles.value
        assertTrue(initialProfiles.size >= 2)

        val firstProfile = repo.activeProfile
        assertEquals("Aarav Sharma", firstProfile.name)
        assertEquals(10, firstProfile.cbseClass)

        // Switch to second profile (Diya)
        val siblingId = initialProfiles[1].id
        repo.switchProfile(siblingId)
        val activeAfterSwitch = repo.activeProfile
        assertEquals(siblingId, activeAfterSwitch.id)
        assertEquals(7, activeAfterSwitch.cbseClass)
    }

    @Test
    fun `verify NCERT database chapters and citations`() {
        val class10Chapters = NcertDatabase.getAllChaptersForClass(10)
        assertTrue(class10Chapters.isNotEmpty())

        val chemicalReactions = class10Chapters.find { it.chapterNumber == 1 }
        assertNotNull(chemicalReactions)
        assertTrue(chemicalReactions!!.citations.isNotEmpty())
        assertTrue(chemicalReactions.flashcards.isNotEmpty())
        assertTrue(chemicalReactions.podcastScript.isNotEmpty())
    }

    @Test
    fun `verify offline chapter caching logic`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyFluidRepository(context)

        assertTrue(repo.isChapterOffline("cl10_sci_ch1"))
        assertFalse(repo.isChapterOffline("cl10_sci_ch6"))

        repo.removeChapterFromCache("cl10_sci_ch1")
        assertFalse(repo.isChapterOffline("cl10_sci_ch1"))
    }

    @Test
    fun `verify mastery skill tree and task completion`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyFluidRepository(context)

        val initialXp = repo.activeProfile.knowledgePoints
        val firstTask = repo.revisionTasks.value.first()
        repo.toggleTaskCompletion(firstTask.id)

        val taskAfter = repo.revisionTasks.value.find { it.id == firstTask.id }
        assertTrue(taskAfter!!.isCompleted)
        assertTrue(repo.activeProfile.knowledgePoints > initialXp)

        // Complete a skill node
        repo.completeSkillNode("node_chem_2")
        val masteredNode = repo.skillNodes.value.find { it.id == "node_chem_2" }
        assertTrue(masteredNode!!.isMastered)
    }

    @Test
    fun `verify shared sibling study room scratchpad and chat sync`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyFluidRepository(context)

        val room = repo.sharedStudyRoom.value
        assertNotNull(room)
        assertTrue(room.sharedScratchpadText.isNotEmpty())

        repo.updateSharedScratchpad("New formula added by siblings: E = mc^2")
        assertEquals("New formula added by siblings: E = mc^2", repo.sharedStudyRoom.value.sharedScratchpadText)

        val msgCountBefore = repo.sharedStudyRoom.value.siblingMessages.size
        repo.sendSiblingMessage("Hey Diya, ready for biology quiz?")
        assertEquals(msgCountBefore + 1, repo.sharedStudyRoom.value.siblingMessages.size)
    }

    @Test
    fun `verify AI spaced repetition plan generation and task reordering`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = StudyFluidRepository(context)

        val taskCountBefore = repo.revisionTasks.value.size
        repo.generateCustomSpacedSchedule(
            topic = "Light Optics & Refraction",
            subject = "Physics",
            examDays = 14,
            dailyMinutes = 45,
            confidence = "Intermediate"
        )
        val tasksAfter = repo.revisionTasks.value
        assertEquals(taskCountBefore + 4, tasksAfter.size)

        // Verify task reordering
        val firstTitle = tasksAfter[0].title
        val secondTitle = tasksAfter[1].title
        repo.reorderTasks(0, 1)
        val reordered = repo.revisionTasks.value
        assertEquals(secondTitle, reordered[0].title)
        assertEquals(firstTitle, reordered[1].title)
    }
}
