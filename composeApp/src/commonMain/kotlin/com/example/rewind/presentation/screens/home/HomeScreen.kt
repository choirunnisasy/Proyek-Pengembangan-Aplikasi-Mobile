package com.example.rewind.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewind.data.remote.dto.TmdbMovieDto
import com.example.rewind.domain.model.Movie
import com.example.rewind.domain.model.WatchStatus
import com.example.rewind.presentation.theme.BorderGold
import com.example.rewind.presentation.theme.GoldAmber
import com.example.rewind.presentation.theme.GoldAmberDim
import com.example.rewind.presentation.theme.StatusDropped
import com.example.rewind.presentation.theme.StatusFinished
import com.example.rewind.presentation.theme.StatusOnHold
import com.example.rewind.presentation.theme.StatusWantToWatch
import com.example.rewind.presentation.theme.StatusWatching
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tmdbState by viewModel.tmdbState.collectAsState()
    val addMessage by viewModel.addMessage.collectAsState()
    var selectedFilter by remember { mutableStateOf<WatchStatus?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Tampilkan snackbar saat berhasil/gagal add ke koleksi
    LaunchedEffect(addMessage) {
        addMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearAddMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Dekoratif blur background
        Box(
            modifier = Modifier.size(300.dp).offset(x = (-60).dp, y = (-40).dp).blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f), Color.Transparent)
                    ), shape = CircleShape
                )
        )
        Box(
            modifier = Modifier.size(200.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = 20.dp).blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), Color.Transparent)
                    ), shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader()
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )

            // Kalau ada query dan hasil lokal kosong → tampilkan hasil TMDB
            val isSearching = searchQuery.length >= 2
            val localEmpty = uiState is HomeUiState.NoResults

            if (isSearching && localEmpty) {
                // Hasil TMDB
                TmdbSearchSection(
                    tmdbState = tmdbState,
                    onAddClick = { viewModel.addTmdbToCollection(it) }
                )
            } else {
                // Hasil lokal seperti biasa
                FilterRow(selected = selectedFilter, onSelect = { selectedFilter = it })

                when (val state = uiState) {
                    is HomeUiState.Loading -> LoadingState()
                    is HomeUiState.Empty -> EmptyState()
                    is HomeUiState.NoResults -> NoResultsState(query = state.query)
                    is HomeUiState.Success -> {
                        val displayed = if (selectedFilter != null) {
                            state.movies.filter { it.status == selectedFilter }
                        } else state.movies
                        if (displayed.isEmpty()) EmptyFilterState()
                        else MovieList(movies = displayed, onMovieClick = onMovieClick)
                    }
                    is HomeUiState.Error -> ErrorState(message = state.message)
                }
            }
        }

        // FAB
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            Box(
                modifier = Modifier.size(70.dp).blur(20.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
                        ), shape = CircleShape
                    )
            )
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(12.dp)
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun TmdbSearchSection(
    tmdbState: TmdbSearchState,
    onAddClick: (TmdbMovieDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🎬 Temukan di TMDB",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
            Text(
                text = "Tidak ada di koleksi lokal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        when (tmdbState) {
            is TmdbSearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            is TmdbSearchState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tmdbState.results.take(10)) { item ->
                        TmdbResultCard(
                            item = item,
                            onAddClick = { onAddClick(item) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(96.dp)) }
                }
            }

            is TmdbSearchState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tidak ditemukan di TMDB",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            is TmdbSearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Gagal memuat: ${tmdbState.message}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            is TmdbSearchState.Idle -> Unit
        }
    }
}

@Composable
private fun TmdbResultCard(item: TmdbMovieDto, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.isTvSeries) "📺" else "🎬",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.isTvSeries) "Series" else "Film",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.voteAverage > 0) {
                        val roundedRating = (item.voteAverage * 10).roundToInt() / 10.0

                        Text(
                            text = "★ $roundedRating",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item.displayDate?.take(4)?.let { year ->
                        Text(
                            text = year,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item.overview?.let { overview ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onAddClick,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("+ Tambah", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to MaterialTheme.colorScheme.surface,
                        0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        1f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 22.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BorderGold.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            BorderGold.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("REWIND", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 5.sp)
            Text("My Collection", color = MaterialTheme.colorScheme.onBackground, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search collection or find on TMDB...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
        leadingIcon = { Text("🔍", fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { onQueryChange("") }.padding(4.dp)) {
                    Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).heightIn(min = 48.dp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        textStyle = TextStyle(fontSize = 13.sp)
    )
}

@Composable
private fun FilterRow(selected: WatchStatus?, onSelect: (WatchStatus?) -> Unit) {
    val filters = listOf(null to "All", WatchStatus.WATCHING to "Watching", WatchStatus.COMPLETED to "Completed", WatchStatus.PLAN_TO_WATCH to "Planned", WatchStatus.ON_HOLD to "On Hold", WatchStatus.DROPPED to "Dropped")
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (status, label) ->
            val isSelected = selected == status
            if (isSelected) {
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Brush.horizontalGradient(listOf(GoldAmberDim, GoldAmber))).clickable { onSelect(status) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = label, color = MaterialTheme.colorScheme.background, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                }
            } else {
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp)).clickable { onSelect(status) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
                }
            }
        }
    }
}

@Composable
private fun MovieList(movies: List<Movie>, onMovieClick: (Long) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(movies, key = { it.id }) { movie -> MovieCard(movie = movie, onClick = { onMovieClick(movie.id) }) }
        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: () -> Unit) {
    val (statusColor, statusLabel) = when (movie.status) {
        WatchStatus.COMPLETED -> StatusFinished to "Completed"
        WatchStatus.WATCHING -> StatusWatching to "Watching"
        WatchStatus.PLAN_TO_WATCH -> StatusWantToWatch to "Planned"
        WatchStatus.ON_HOLD -> StatusOnHold to "On Hold"
        WatchStatus.DROPPED -> StatusDropped to "Dropped"
    }
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colorStops = arrayOf(0f to MaterialTheme.colorScheme.surfaceVariant, 1f to MaterialTheme.colorScheme.surface)))
            .border(BorderStroke(1.dp, Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent))), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(80.dp).offset(x = (-10).dp, y = (-10).dp).blur(30.dp).background(Brush.radialGradient(colors = listOf(statusColor.copy(alpha = 0.15f), Color.Transparent)), shape = CircleShape))
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(66.dp).blur(12.dp).background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), Color.Transparent)), shape = CircleShape))
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(13.dp))
                        .background(Brush.linearGradient(colorStops = arrayOf(0f to MaterialTheme.colorScheme.tertiary, 1f to MaterialTheme.colorScheme.secondary)))
                        .border(BorderStroke(1.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent))), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = movie.title.take(1).uppercase(), color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = movie.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = 0.1.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(modifier = Modifier.background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(5.dp)).border(BorderStroke(0.5.dp, statusColor.copy(alpha = 0.35f)), RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 3.dp)) {
                        Text(text = statusLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                    }
                    if (movie.rating != null && movie.rating > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("★", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            Text(text = movie.rating.toString(), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Text(text = "${movie.type.displayName}  ·  ${movie.genre.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = 0.2.sp)
                if (movie.status == WatchStatus.WATCHING && movie.totalEpisodes != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(progress = { movie.progressPercent / 100f }, modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                        Text(text = "${movie.watchedEpisodes}/${movie.totalEpisodes}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 20.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 1.5.dp, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(100.dp).blur(30.dp).background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), Color.Transparent)), shape = CircleShape))
                Text("🎞️", fontSize = 52.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Your collection is empty", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
            Text(text = "Tap + to add your first title", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyFilterState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔍", fontSize = 36.sp)
            Text(text = "No titles in this category", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NoResultsState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🎬", fontSize = 40.sp)
            Text(text = "No results for \"$query\"", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Try a different title or genre", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
    }
}