package com.example.rewind.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rewind.domain.usecase.DeleteMovieUseCase
import com.example.rewind.domain.usecase.GetAllMoviesUseCase
import com.example.rewind.domain.usecase.MovieSortBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAllMovies: GetAllMoviesUseCase,
    private val deleteMovie: DeleteMovieUseCase
) : ViewModel() {

    private val _sortBy = MutableStateFlow(MovieSortBy.UPDATED_DESC)
    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<HomeUiState> = combine(
        _sortBy.flatMapLatest { sort -> getAllMovies(sort) },
        searchQuery.debounce(300)
    ) { movies, query ->
        val filtered = if (query.isBlank()) {
            movies
        } else {
            movies.filter { movie ->
                movie.title.contains(query, ignoreCase = true) ||
                        movie.genre.displayName.contains(query, ignoreCase = true) ||
                        movie.type.displayName.contains(query, ignoreCase = true)
            }
        }

        when {
            filtered.isEmpty() && query.isNotBlank() -> HomeUiState.NoResults(query)
            filtered.isEmpty() -> HomeUiState.Empty
            else -> HomeUiState.Success(filtered, _sortBy.value, query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun setSortBy(sortBy: MovieSortBy) {
        _sortBy.value = sortBy
    }

    fun deleteMovie(id: Long) {
        viewModelScope.launch {
            deleteMovie.invoke(id)
        }
    }
}