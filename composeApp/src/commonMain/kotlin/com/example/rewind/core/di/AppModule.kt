package com.example.rewind.core.di

import com.example.rewind.core.network.HttpClientFactory
import com.example.rewind.core.util.DatabaseDriverFactory
import com.example.rewind.data.local.RewindDatabase
import com.example.rewind.data.local.datastore.DataStoreFactory
import com.example.rewind.data.local.datastore.UserPreferences
import com.example.rewind.data.local.datastore.create
import com.example.rewind.data.remote.api.GeminiService
import com.example.rewind.data.repository.AIRepositoryImpl
import com.example.rewind.domain.repository.AIRepository

// --- IMPORT REPOSITORY ---
import com.example.rewind.domain.repository.MovieRepository
import com.example.rewind.data.repository.MovieRepositoryImpl

// --- IMPORT USE CASES ---
import com.example.rewind.domain.usecase.GetAllMoviesUseCase
import com.example.rewind.domain.usecase.GetMovieByIDUseCase
import com.example.rewind.domain.usecase.SearchMoviesUseCase
import com.example.rewind.domain.usecase.GetMoviesByStatusUseCase
import com.example.rewind.domain.usecase.GetFavoriteMoviesUseCase
import com.example.rewind.domain.usecase.SaveMovieUseCase
import com.example.rewind.domain.usecase.DeleteMovieUseCase

// --- IMPORT VIEWMODELS ---
import com.example.rewind.presentation.screens.home.HomeViewModel
import com.example.rewind.presentation.screens.addmovie.AddMovieViewModel
import com.example.rewind.presentation.screens.detail.DetailViewModel

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create(enableLogging = true) }
    singleOf(::GeminiService)
}

val databaseModule = module {
    single {
        val driverFactory: DatabaseDriverFactory = get()
        RewindDatabase(driverFactory.createDriver())
    }
}

val preferencesModule = module {
    single { get<DataStoreFactory>().create() }
    single { UserPreferences(get()) }
}

val repositoryModule = module {
    singleOf(::MovieRepositoryImpl) bind MovieRepository::class
    singleOf(::AIRepositoryImpl) bind AIRepository::class
}

// REGISTER SEMUA USE CASES
val useCaseModule = module {
    singleOf(::GetAllMoviesUseCase)
    singleOf(::GetMovieByIDUseCase)
    singleOf(::SearchMoviesUseCase)
    singleOf(::GetMoviesByStatusUseCase)
    singleOf(::GetFavoriteMoviesUseCase)
    singleOf(::SaveMovieUseCase)
    singleOf(::DeleteMovieUseCase)
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddMovieViewModel)
    viewModelOf(::DetailViewModel)
}

val sharedModules = listOf(
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

fun initKoin(
    platformModules: List<Module> = emptyList(),
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        modules(platformModules + sharedModules)
    }
}