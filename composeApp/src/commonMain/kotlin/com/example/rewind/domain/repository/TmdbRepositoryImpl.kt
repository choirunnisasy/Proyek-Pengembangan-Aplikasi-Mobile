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
    // ==================== IN-MEMORY CACHE ====================

    // Cache trending: di-refresh setiap kali app dibuka
    private var trendingCache: List<TmdbMovieDto>? = null
    private var trendingCacheTime: Long = 0
    private val trendingCacheDuration = 30 * 60 * 1000L // 30 menit

    // Cache detail film/tv berdasarkan ID
    private val movieDetailCache = mutableMapOf<Int, TmdbMovieDetailDto>()
    private val tvDetailCache = mutableMapOf<Int, TmdbMovieDetailDto>()

    // ==================== SEARCH ====================

    /**
     * Search tidak di-cache karena query selalu berbeda-beda.
     * Filtering adult content dilakukan di sini sebagai safety layer tambahan.
     */
    override suspend fun searchMulti(
        query: String,
        page: Int
    ): NetworkResult<List<TmdbMovieDto>> {
        if (query.isBlank()) return NetworkResult.Success(emptyList())

        return safeApiCall {
            tmdbService.searchMulti(query = query, page = page)
                .results
                .filter { !it.adult }  // Filter konten dewasa
                .filter { it.mediaType == "movie" || it.mediaType == "tv" } // Hanya film & TV
        }
    }

    // ==================== MOVIE DETAIL ====================

    /**
     * Cek cache dulu, kalau ada langsung return.
     * Kalau tidak ada, fetch dari API lalu simpan ke cache.
     */
    override suspend fun getMovieDetail(tmdbId: Int): NetworkResult<TmdbMovieDetailDto> {
        movieDetailCache[tmdbId]?.let { cached ->
            return NetworkResult.Success(cached)
        }

        return safeApiCall {
            val detail = tmdbService.getMovieDetail(tmdbId)
            movieDetailCache[tmdbId] = detail  // Simpan ke cache
            detail
        }
    }

    // ==================== TV DETAIL ====================

    override suspend fun getTvDetail(tmdbId: Int): NetworkResult<TmdbMovieDetailDto> {
        tvDetailCache[tmdbId]?.let { cached ->
            return NetworkResult.Success(cached)
        }

        return safeApiCall {
            val detail = tmdbService.getTvDetail(tmdbId)
            tvDetailCache[tmdbId] = detail  // Simpan ke cache
            detail
        }
    }

    // ==================== TRENDING ====================

    /**
     * Cache trending selama 30 menit.
     * Jika cache masih valid, return dari cache.
     * Jika kadaluarsa atau belum ada, fetch dari API.
     */
    override suspend fun getTrending(): NetworkResult<List<TmdbMovieDto>> {
        val now = System.currentTimeMillis()
        val isCacheValid = trendingCache != null &&
                (now - trendingCacheTime) < trendingCacheDuration

        if (isCacheValid) {
            return NetworkResult.Success(trendingCache!!)
        }

        return safeApiCall {
            val result = tmdbService.getTrending()
                .results
                .filter { !it.adult }
            trendingCache = result
            trendingCacheTime = now
            result
        }
    }

    // ==================== CACHE MANAGEMENT ====================

    /**
     * Clear semua cache secara manual (misalnya saat pull-to-refresh)
     */
    fun clearCache() {
        trendingCache = null
        trendingCacheTime = 0
        movieDetailCache.clear()
        tvDetailCache.clear()
    }
}