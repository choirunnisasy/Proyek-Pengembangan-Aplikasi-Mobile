package com.example.rewind.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.rewind.data.remote.dto.TmdbMovieDto
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    val trendingState by viewModel.trendingState.collectAsState()
    val addState by viewModel.addState.collectAsState()

    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Snackbar untuk notifikasi add to collection
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(addState) {
        when (val state = addState) {
            is AddToCollectionState.Success -> {
                snackbarHostState.showSnackbar("\"${state.title}\" ditambahkan ke koleksi!")
                viewModel.resetAddState()
            }
            is AddToCollectionState.Error -> {
                snackbarHostState.showSnackbar("Gagal: ${state.message}")
                viewModel.resetAddState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.onQueryChange(it)
                        },
                        placeholder = { Text("Cari film atau series...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {
                                    query = ""
                                    viewModel.clearSearch()
                                    focusManager.clearFocus()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Clear, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = searchState) {
                is SearchUiState.Idle -> {
                    // Tampilkan trending saat belum ada query
                    TrendingSection(
                        trendingState = trendingState,
                        onAddClick = { viewModel.addToCollection(it) }
                    )
                }
                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is SearchUiState.Success -> {
                    SearchResultList(
                        results = state.results,
                        onAddClick = { viewModel.addToCollection(it) }
                    )
                }
                is SearchUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Tidak ditemukan hasil untuk \"${query}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                is SearchUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gagal memuat hasil", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.onQueryChange(query) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== TRENDING SECTION ====================

@Composable
private fun TrendingSection(
    trendingState: TrendingUiState,
    onAddClick: (TmdbMovieDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "🔥 Trending Minggu Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (trendingState) {
            is TrendingUiState.Loading -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TrendingUiState.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingState.items) { item ->
                        TrendingCard(item = item, onAddClick = { onAddClick(item) })
                    }
                }
            }
            is TrendingUiState.Error -> {
                Text(
                    "Gagal memuat trending: ${trendingState.message}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TrendingCard(item: TmdbMovieDto, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.posterUrl("w300"),
                    contentDescription = item.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
                // Badge tipe (Film/Series)
                Surface(
                    modifier = Modifier.padding(4.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Icon(
                        if (item.isTvSeries) Icons.Default.Tv else Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).padding(2.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    item.displayTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " ${"%.1f".format(item.voteAverage)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add, "Tambah ke koleksi",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ==================== SEARCH RESULT LIST ====================

@Composable
private fun SearchResultList(
    results: List<TmdbMovieDto>,
    onAddClick: (TmdbMovieDto) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(results) { item ->
            SearchResultItem(item = item, onAddClick = { onAddClick(item) })
        }
    }
}

@Composable
private fun SearchResultItem(item: TmdbMovieDto, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            AsyncImage(
                model = item.posterUrl("w185"),
                contentDescription = item.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 60.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (item.isTvSeries) Icons.Default.Tv else Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (item.isTvSeries) "Series" else "Film",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.voteAverage > 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Star, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            " ${"%.1f".format(item.voteAverage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                item.displayDate?.let { date ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        date.take(4), // Hanya tahun
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item.overview?.let { overview ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            // Add button
            FilledTonalIconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Tambah ke koleksi")
            }
        }
    }
}