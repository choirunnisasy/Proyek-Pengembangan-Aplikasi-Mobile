package com.example.rewind.data.repository

import com.example.rewind.core.network.NetworkResult
import com.example.rewind.core.network.safeApiCall
import com.example.rewind.data.remote.api.TmdbService
import com.example.rewind.data.remote.dto.TmdbMovieDetailDto
import com.example.rewind.data.remote.dto.TmdbMovieDto
import com.example.rewind.domain.repository.TmdbRepository

class TmdbRepositoryImpl(
    private val tmdbService: TmdbService
) : TmdbRepository {

}