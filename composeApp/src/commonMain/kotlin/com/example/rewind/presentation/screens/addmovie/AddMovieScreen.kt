package com.example.rewind.presentation.screens.addmovie

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewind.domain.model.MovieGenre
import com.example.rewind.domain.model.MovieType
import com.example.rewind.domain.model.WatchStatus
import com.example.rewind.presentation.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddMovieScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMovieViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(MovieGenre.OTHER) }
    var selectedType by remember { mutableStateOf(MovieType.MOVIE) }
    var selectedStatus by remember { mutableStateOf(WatchStatus.PLAN_TO_WATCH) }
    var rating by remember { mutableStateOf(0f) }
    var review by remember { mutableStateOf("") }
    var totalEpisodesText by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AddMovieUiState.Success) onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(SurfaceDark, BackgroundDark)))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = GoldAmber, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "TAMBAH",
                        color = GoldAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Film Baru",
                        color = TextWarm,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            FieldLabel("JUDUL")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Judul film atau series...", color = TextSecondary, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = rewindTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = TextWarm, fontSize = 14.sp)
            )

            FieldLabel("JENIS")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MovieType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        modifier = Modifier.clickable { selectedType = type },
                        color = if (isSelected) GoldAmber else SurfaceDark,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = type.displayName,
                            color = if (isSelected) BackgroundDark else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            FieldLabel("GENRE")
            GenreDropdown(
                selected = selectedGenre,
                onSelect = { selectedGenre = it }
            )

            FieldLabel("STATUS")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                val statusList = listOf(
                    WatchStatus.PLAN_TO_WATCH to "Plan",
                    WatchStatus.WATCHING to "Watch",
                    WatchStatus.COMPLETED to "Done",
                    WatchStatus.ON_HOLD to "Hold",
                    WatchStatus.DROPPED to "Drop"
                )
                val colors = mapOf(
                    WatchStatus.PLAN_TO_WATCH to StatusWantToWatch,
                    WatchStatus.WATCHING to StatusWatching,
                    WatchStatus.COMPLETED to StatusFinished,
                    WatchStatus.ON_HOLD to StatusOnHold,
                    WatchStatus.DROPPED to StatusDropped
                )
                statusList.forEach { (status, label) ->
                    val isSelected = selectedStatus == status
                    val color = colors[status] ?: GoldAmber
                    Surface(
                        modifier = Modifier.clickable { selectedStatus = status },
                        color = if (isSelected) color.copy(alpha = 0.2f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) color else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (selectedType != MovieType.MOVIE) {
                FieldLabel("TOTAL EPISODE")
                OutlinedTextField(
                    value = totalEpisodesText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalEpisodesText = it },
                    placeholder = { Text("Jumlah episode...", color = TextSecondary, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = rewindTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(color = TextWarm, fontSize = 14.sp)
                )
            }

            FieldLabel("RATING  ${"★".repeat(rating.toInt())}${"☆".repeat(5 - rating.toInt())}  (${rating.toInt()}/5)")
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = GoldAmber,
                    activeTrackColor = GoldAmber,
                    inactiveTrackColor = VelvetRed.copy(alpha = 0.3f)
                )
            )

            FieldLabel("REVIEW / CATATAN")
            OutlinedTextField(
                value = review,
                onValueChange = { review = it },
                placeholder = { Text("Tulis kesanmu tentang film ini...", color = TextSecondary, fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                maxLines = 5,
                colors = rewindTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(color = TextWarm, fontSize = 14.sp)
            )

            if (uiState is AddMovieUiState.Error) {
                Text(
                    text = (uiState as AddMovieUiState.Error).message,
                    color = TheaterRed,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = {
                    val formattedRating: Float? = if (rating > 0f) rating else null
                    val formattedEpisodes: Int? = totalEpisodesText.toIntOrNull()

                    viewModel.saveMovie(
                        title = title,
                        genre = selectedGenre,
                        type = selectedType,
                        status = selectedStatus,
                        rating = formattedRating,
                        review = review,
                        totalEpisodes = formattedEpisodes
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState !is AddMovieUiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAmber,
                    contentColor = BackgroundDark,
                    disabledContainerColor = GoldAmber.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState is AddMovieUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BackgroundDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Simpan ke Koleksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = GoldAmber,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
private fun GenreDropdown(selected: MovieGenre, onSelect: (MovieGenre) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            color = SurfaceDark,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.displayName,
                    color = TextWarm,
                    fontSize = 14.sp
                )
                Text("▾", color = GoldAmber, fontSize = 14.sp)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceDark)
        ) {
            MovieGenre.entries.forEach { genre ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = genre.displayName,
                            color = if (genre == selected) GoldAmber else TextWarm,
                            fontSize = 14.sp,
                            fontWeight = if (genre == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(genre)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun rewindTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldAmber,
    unfocusedBorderColor = VelvetRed.copy(alpha = 0.4f),
    focusedTextColor = TextWarm,
    unfocusedTextColor = TextWarm,
    cursorColor = GoldAmber,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark,
    focusedLabelColor = GoldAmber
)