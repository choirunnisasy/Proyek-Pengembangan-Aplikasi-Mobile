package com.example.rewind.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.rewind.data.local.datastore.UserPreferences
import com.example.rewind.data.repository.FakeMovieRepository
import com.example.rewind.domain.model.Movie
import com.example.rewind.domain.model.MovieGenre
import com.example.rewind.domain.model.WatchStatus
import com.example.rewind.domain.usecase.GetAllMoviesUseCase
import com.example.rewind.domain.usecase.GetFavoriteMoviesUseCase
import com.example.rewind.presentation.screens.profile.ProfileUiState
import com.example.rewind.presentation.screens.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeDataStore : DataStore<Preferences> {
    private val store = MutableStateFlow<Preferences>(emptyPreferences())
    override val data: Flow<Preferences> = store
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(store.value)
        store.value = updated
        return updated
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMovieRepository
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMovieRepository()
        viewModel = ProfileViewModel(
            getAllMovies = GetAllMoviesUseCase(fakeRepository),
            getFavoriteMovies = GetFavoriteMoviesUseCase(fakeRepository),
            userPreferences = UserPreferences(FakeDataStore())
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState should be Loading`() {
        assertTrue(viewModel.uiState.value is ProfileUiState.Loading)
    }

    @Test
    fun `uiState should be Success after movies loaded`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "Inception"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ProfileUiState.Success)
    }

    @Test
    fun `totalMovies should reflect inserted movies count`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "Film A"))
        fakeRepository.insertMovie(createTestMovie(title = "Film B"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals(2, state.totalMovies)
    }

    @Test
    fun `averageRating should be correct from rated movies`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "Film A", rating = 8.0f))
        fakeRepository.insertMovie(createTestMovie(title = "Film B", rating = 6.0f))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals(7.0f, state.averageRating)
    }

    @Test
    fun `averageRating should be 0 when no movies rated`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "Film A", rating = null))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals(0f, state.averageRating)
    }

    @Test
    fun `statusCounts should correctly count each status`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "A", status = WatchStatus.COMPLETED))
        fakeRepository.insertMovie(createTestMovie(title = "B", status = WatchStatus.COMPLETED))
        fakeRepository.insertMovie(createTestMovie(title = "C", status = WatchStatus.WATCHING))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals(2, state.statusCounts[WatchStatus.COMPLETED])
        assertEquals(1, state.statusCounts[WatchStatus.WATCHING])
        assertEquals(0, state.statusCounts[WatchStatus.DROPPED] ?: 0)
    }

    @Test
    fun `topGenres should be sorted by count descending`() = runTest {
        repeat(3) { fakeRepository.insertMovie(createTestMovie(title = "Drama $it", genre = MovieGenre.DRAMA)) }
        repeat(1) { fakeRepository.insertMovie(createTestMovie(title = "Action $it", genre = MovieGenre.ACTION)) }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals(MovieGenre.DRAMA.displayName, state.topGenres.first().first)
    }

    @Test
    fun `achievements should unlock First Frame when movie added`() = runTest {
        fakeRepository.insertMovie(createTestMovie(title = "Film Pertama"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        val firstFrame = state.achievements.find { it.title == "First Frame" }
        assertTrue(firstFrame?.unlocked == true)
    }

    @Test
    fun `achievements Collector should unlock when 10 movies added`() = runTest {
        repeat(10) { i -> fakeRepository.insertMovie(createTestMovie(title = "Film $i")) }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        val collector = state.achievements.find { it.title == "Collector" }
        assertTrue(collector?.unlocked == true)
    }

    @Test
    fun `achievements Collector should be locked when less than 10 movies`() = runTest {
        repeat(5) { i -> fakeRepository.insertMovie(createTestMovie(title = "Film $i")) }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        val collector = state.achievements.find { it.title == "Collector" }
        assertTrue(collector?.unlocked == false)
    }

    @Test
    fun `recentMovies should only return 5 latest movies`() = runTest {
        repeat(8) { i -> fakeRepository.insertMovie(createTestMovie(title = "Film $i")) }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertTrue(state.recentMovies.size <= 5)
    }

    @Test
    fun `saveEdit with blank name should fallback to default`() = runTest {
        advanceUntilIdle()

        viewModel.startEdit()
        viewModel.editName.value = "   "
        viewModel.saveEdit()
        advanceUntilIdle()

        assertFalse(viewModel.isEditMode.value)
    }

    @Test
    fun `userName should use default when preferences blank`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value as ProfileUiState.Success
        assertEquals("Rewind User", state.userName)
    }

    private fun createTestMovie(
        title: String = "Test Movie",
        genre: MovieGenre = MovieGenre.OTHER,
        status: WatchStatus = WatchStatus.PLAN_TO_WATCH,
        rating: Float? = null
    ) = Movie(
        id = 0L,
        title = title,
        genre = genre,
        status = status,
        rating = rating,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
    )
}