package com.example.rewind.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.rewind.presentation.screens.addmovie.AddMovieScreen
import com.example.rewind.presentation.screens.detail.DetailScreen
import com.example.rewind.presentation.screens.home.HomeScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Home> {
            HomeScreen(
                onAddClick = { navController.navigate(Route.AddMovie()) },
                onMovieClick = { id -> navController.navigate(Route.MovieDetail(movieId = id)) }
            )
        }
        composable<Route.AddMovie> {
            AddMovieScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable<Route.MovieDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieDetail>()
            DetailScreen(
                movieId = route.movieId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}