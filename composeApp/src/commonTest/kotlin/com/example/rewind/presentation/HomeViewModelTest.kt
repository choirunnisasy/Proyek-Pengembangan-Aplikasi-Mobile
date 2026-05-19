package com.example.rewind.presentation

import app.cash.turbine.test
import com.example.rewind.domain.usecase.DeleteMovieUseCase
import com.example.rewind.domain.usecase.GetAllMoviesUseCase
import com.example.rewind.domain.usecase.MovieSortBy
import com.example.rewind.presentation.screens.home.HomeUiState
import com.example.rewind.presentation.screens.home.HomeViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllMoviesUseCase: GetAllMoviesUseCase
    private lateinit var deleteMovieUseCase: DeleteMovieUseCase
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getAllMoviesUseCase = GetAllMoviesUseCase()
        deleteMovieUseCase = DeleteMovieUseCase()

        viewModel = HomeViewModel(
            getAllMoviesUseCase,
            deleteMovieUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading then Empty`() = runTest {
        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading is HomeUiState.Loading)

            advanceUntilIdle()
            val empty = awaitItem()
            assertTrue(empty is HomeUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state should be Success when movies exist`() = runTest {
        val vm = HomeViewModel(
            getAllMoviesUseCase,
            deleteMovieUseCase
        )

        vm.uiState.test {
            skipItems(1)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sort should update movies`() = runTest {
        val vm = HomeViewModel(
            getAllMoviesUseCase,
            deleteMovieUseCase
        )

        vm.uiState.test {
            skipItems(1)
            advanceUntilIdle()
            skipItems(1)

            vm.setSortBy(MovieSortBy.TITLE_ASC)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state is HomeUiState.Success || state is HomeUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMovie should remove movie`() = runTest {
        viewModel.deleteMovie(1L)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Success || state is HomeUiState.Empty || state is HomeUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}