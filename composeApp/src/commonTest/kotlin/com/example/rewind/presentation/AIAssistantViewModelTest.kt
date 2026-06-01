package com.example.rewind.presentation

import com.example.rewind.data.repository.FakeAIRepository
import com.example.rewind.domain.repository.WritingStyle
import com.example.rewind.domain.usecase.GenerateIdeasUseCase
import com.example.rewind.domain.usecase.ImproveWritingUseCase
import com.example.rewind.domain.usecase.SummarizeNoteUseCase
import com.example.rewind.presentation.screens.ai.AIAction
import com.example.rewind.presentation.screens.ai.AIAssistantViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class AIAssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeAIRepository: FakeAIRepository
    private lateinit var viewModel: AIAssistantViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAIRepository = FakeAIRepository()
        viewModel = AIAssistantViewModel(
            aiRepository = fakeAIRepository,
            summarizeUseCase = SummarizeNoteUseCase(fakeAIRepository),
            improveWritingUseCase = ImproveWritingUseCase(fakeAIRepository),
            generateIdeasUseCase = GenerateIdeasUseCase(fakeAIRepository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has correct default values`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.inputText)
        assertEquals(AIAction.CHAT, state.selectedAction)
        assertEquals(WritingStyle.NEUTRAL, state.writingStyle)
        assertFalse(state.isLoading)
        assertNull(state.result)
        assertNull(state.error)
    }

    @Test
    fun `onInputTextChange updates inputText and clears existing error`() = runTest {
        viewModel.executeAction()
        advanceUntilIdle()

        viewModel.onInputTextChange("Tell me about Interstellar")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Tell me about Interstellar", state.inputText)
        assertNull(state.error)
    }

    @Test
    fun `onActionSelected updates selectedAction in uiState`() = runTest {
        viewModel.onActionSelected(AIAction.SUMMARIZE)
        advanceUntilIdle()

        assertEquals(AIAction.SUMMARIZE, viewModel.uiState.value.selectedAction)
    }
}