package com.example.rewind.presentation.screens.addmovie

sealed interface AddMovieUiState {
    data object Idle : AddMovieUiState
    data object Loading : AddMovieUiState
    data object Success : AddMovieUiState
    data class Error(val message: String) : AddMovieUiState
}