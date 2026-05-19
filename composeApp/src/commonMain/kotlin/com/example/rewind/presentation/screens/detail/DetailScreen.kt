package com.example.rewind.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewind.domain.model.Movie
import com.example.rewind.domain.model.WatchStatus
import com.example.rewind.presentation.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailScreen(
    movieId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Hapus dari Koleksi?", color = TextWarm, fontWeight = FontWeight.Bold) },
            text = { Text("Film ini akan dihapus secara permanen.", color = TextSecondary, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMovie(movieId) { onNavigateBack() }
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = TheaterRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAmber, strokeWidth = 2.dp)
                }
            }
            is DetailUiState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🎞️", fontSize = 48.sp)
                        Text("Film tidak ditemukan", color = TextSecondary, fontSize = 15.sp)
                    }
                }
            }
            is DetailUiState.Success -> {
                MovieDetail(
                    movie = state.movie,
                    onBack = onNavigateBack,
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
private fun MovieDetail(movie: Movie, onBack: () -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(VelvetRed, TheaterRed, SurfaceDark)))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BackgroundDark.copy(alpha = 0.1f), BackgroundDark),
                            startY = 80f
                        )
                    )
            )
            Text(
                text = movie.title.take(2).uppercase(),
                color = TextWarm.copy(alpha = 0.12f),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(BackgroundDark.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = TextWarm, fontSize = 18.sp)
                }
                Box(
                    modifier = Modifier
                        .background(TheaterRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("🗑 Hapus", color = TheaterRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = movie.title,
                color = TextWarm,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = movie.status)
                Surface(color = SurfaceDark, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = movie.type.displayName,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(color = SurfaceDark, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = movie.genre.displayName,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (movie.rating != null && movie.rating > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("★".repeat(movie.rating.toInt()), color = GoldAmber, fontSize = 18.sp)
                    Text("${movie.rating}/5", color = TextSecondary, fontSize = 13.sp)
                }
            }

            if (movie.totalEpisodes != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Episode", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "${movie.watchedEpisodes}/${movie.totalEpisodes}",
                            color = TextWarm,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { movie.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = GoldAmber,
                        trackColor = VelvetRed.copy(alpha = 0.3f)
                    )
                }
            }

            HorizontalDivider(color = SurfaceDark, thickness = 1.dp)

            if (movie.review.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "REVIEW",
                        color = GoldAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Surface(
                        color = SurfaceDark,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = movie.review,
                            color = TextWarm,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (movie.isFavorite) {
                Surface(
                    color = GoldAmber.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⭐", fontSize = 16.sp)
                        Text(
                            text = "Film favorit — rating di atas 8",
                            color = GoldAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: WatchStatus) {
    val (color, label) = when (status) {
        WatchStatus.COMPLETED -> StatusFinished to "Selesai"
        WatchStatus.WATCHING -> StatusWatching to "Watching"
        WatchStatus.PLAN_TO_WATCH -> StatusWantToWatch to "Planned"
        WatchStatus.ON_HOLD -> StatusOnHold to "On Hold"
        WatchStatus.DROPPED -> StatusDropped to "Dropped"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}