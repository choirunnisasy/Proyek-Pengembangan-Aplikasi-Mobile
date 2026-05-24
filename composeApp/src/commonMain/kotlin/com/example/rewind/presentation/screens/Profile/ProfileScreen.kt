package com.example.rewind.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewind.domain.model.WatchStatus
import com.example.rewind.presentation.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        // Background glows
        Box(
            modifier = Modifier.size(300.dp).offset(x = (-80).dp, y = (-60).dp).blur(120.dp)
                .background(Brush.radialGradient(
                    listOf(TheaterRed.copy(alpha = 0.18f), Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier.size(200.dp).align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 100.dp).blur(100.dp)
                .background(Brush.radialGradient(
                    listOf(GoldAmber.copy(alpha = 0.1f), Color.Transparent)), CircleShape)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(onNavigateBack = onNavigateBack)

            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = GoldAmber, strokeWidth = 1.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                is ProfileUiState.Success -> ProfileContent(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(
                listOf(SurfaceDark, SurfaceDark.copy(alpha = 0.8f), BackgroundDark)
            ))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // Gold divider bottom
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.BottomCenter)
                .background(Brush.horizontalGradient(listOf(
                    Color.Transparent, BorderGold.copy(alpha = 0.4f),
                    GoldAmber.copy(alpha = 0.3f), BorderGold.copy(alpha = 0.4f), Color.Transparent
                )))
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(10.dp))
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) { Text("←", color = GoldAmber, fontSize = 17.sp) }

            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("MY PROFILE", color = GoldAmber, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Text("Stats & Collection", color = TextWarm, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    viewModel: ProfileViewModel
) {
    val isEditMode by viewModel.isEditMode.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val editBio by viewModel.editBio.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        IdentityCard(
            state = state,
            isEditMode = isEditMode,
            editName = editName,
            editBio = editBio,
            onNameChange = { viewModel.editName.value = it },
            onBioChange = { viewModel.editBio.value = it },
            onEditClick = { viewModel.startEdit() },
            onSaveClick = { viewModel.saveEdit() },
            onCancelClick = { viewModel.cancelEdit() }
        )

        QuickStatsRow(state = state)
        SectionTitle("WATCH STATUS")
        StatusBreakdown(state = state)
        if (state.topGenres.isNotEmpty()) {
            SectionTitle("TOP GENRES")
            GenreBreakdown(state = state)
        }
        SectionTitle("ACHIEVEMENTS")
        AchievementsGrid(achievements = state.achievements)
        if (state.recentMovies.isNotEmpty()) {
            SectionTitle("RECENT ACTIVITY")
            RecentActivity(movies = state.recentMovies)
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun IdentityCard(
    state: ProfileUiState.Success,
    isEditMode: Boolean,
    editName: String,
    editBio: String,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(SurfaceElevated)
            .border(BorderStroke(1.dp, Brush.linearGradient(
                listOf(BorderGold.copy(alpha = 0.5f), BorderSubtle, Color.Transparent)
            )), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier.size(120.dp).offset(x = (-20).dp, y = (-20).dp).blur(40.dp)
                .background(Brush.radialGradient(
                    listOf(GoldAmber.copy(alpha = 0.12f), Color.Transparent)), CircleShape)
        )

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(84.dp).blur(20.dp)
                        .background(Brush.radialGradient(
                            listOf(GoldAmber.copy(alpha = 0.3f), Color.Transparent)), CircleShape))
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                            .background(Brush.linearGradient(
                                colorStops = arrayOf(0f to VelvetRed, 1f to TheaterRed)))
                            .border(BorderStroke(2.dp, Brush.linearGradient(
                                listOf(GoldAmber.copy(alpha = 0.6f), GoldAmberDim.copy(alpha = 0.3f))
                            )), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("🎬", fontSize = 30.sp) }
                    Box(
                        modifier = Modifier.size(14.dp).align(Alignment.BottomEnd)
                            .clip(CircleShape).background(StatusFinished)
                            .border(BorderStroke(2.dp, SurfaceElevated), CircleShape)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f) // Kunci agar teks fleksibel dan tidak menabrak tombol kanan
                ) {
                    if (isEditMode) {
                        BasicTextField(
                            value = editName,
                            onValueChange = onNameChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextWarm,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .border(BorderStroke(1.dp, GoldAmber.copy(alpha = 0.5f)),
                                            RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) { innerTextField() }
                            }
                        )
                    } else {
                        Text(state.userName, color = TextWarm, fontSize = 20.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
                    }

                    Text("@cinephile", color = GoldAmber, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium)

                    if (isEditMode) {
                        BasicTextField(
                            value = editBio,
                            onValueChange = onBioChange,
                            maxLines = 3,
                            textStyle = TextStyle(
                                color = TextMuted,
                                fontSize = 12.sp
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .border(BorderStroke(1.dp, BorderSubtle),
                                            RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) { innerTextField() }
                            }
                        )
                    } else {
                        Text(state.userBio, color = TextMuted, fontSize = 12.sp,
                            fontStyle = FontStyle.Italic, lineHeight = 17.sp)
                    }
                }

                if (isEditMode) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Tombol Save
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(GoldAmberDim, GoldAmber)))
                                .clickable(onClick = onSaveClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Save", color = BackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // Tombol Cancel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(8.dp))
                                .clickable(onClick = onCancelClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Cancel", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAmber.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, GoldAmber.copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
                            .clickable(onClick = onEditClick)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("✏️", fontSize = 13.sp)
                    }
                }
            }
            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp)
                .background(Brush.horizontalGradient(
                    listOf(BorderGold.copy(alpha = 0.3f), Color.Transparent))))

            // Mini stats
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                MiniStat("${state.totalMovies}", "Total")
                StatDivider()
                MiniStat("${state.statusCounts[WatchStatus.COMPLETED] ?: 0}", "Done")
                StatDivider()
                MiniStat(if (state.averageRating > 0f) "${state.averageRating}★" else "—", "Avg")
                StatDivider()
                MiniStat("${state.favoriteCount}", "Faves")
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = GoldAmber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(28.dp)
        .background(BorderSubtle.copy(alpha = 0.5f)))
}

@Composable
private fun QuickStatsRow(state: ProfileUiState.Success) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Completion rate card
        val completionRate = if (state.totalMovies > 0)
            ((state.statusCounts[WatchStatus.COMPLETED] ?: 0).toFloat() / state.totalMovies * 100).toInt()
        else 0

        QuickStatCard(
            modifier = Modifier.weight(1f),
            emoji = "🏆",
            value = "$completionRate%",
            label = "Completion",
            color = StatusFinished
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            emoji = "📺",
            value = "${state.statusCounts[WatchStatus.WATCHING] ?: 0}",
            label = "In Progress",
            color = StatusWatching
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            emoji = "🔖",
            value = "${state.statusCounts[WatchStatus.PLAN_TO_WATCH] ?: 0}",
            label = "Watchlist",
            color = StatusWantToWatch
        )
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.25f)), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(value, color = TextWarm, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatusBreakdown(state: ProfileUiState.Success) {
    val statusItems = listOf(
        WatchStatus.COMPLETED to (StatusFinished to "Completed"),
        WatchStatus.WATCHING to (StatusWatching to "Watching"),
        WatchStatus.PLAN_TO_WATCH to (StatusWantToWatch to "Planned"),
        WatchStatus.ON_HOLD to (StatusOnHold to "On Hold"),
        WatchStatus.DROPPED to (StatusDropped to "Dropped")
    )

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            statusItems.forEach { (status, pair) ->
                val (color, label) = pair
                val count = state.statusCounts[status] ?: 0
                val progress = if (state.totalMovies > 0)
                    count.toFloat() / state.totalMovies else 0f

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.width(70.dp)
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                            .border(BorderStroke(0.5.dp, color.copy(alpha = 0.3f)), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold) }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = color.copy(alpha = 0.08f)
                    )

                    Text("$count", color = TextWarm, fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(20.dp))
                }
            }
        }
    }
}

@Composable
private fun GenreBreakdown(state: ProfileUiState.Success) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.topGenres.take(5).forEachIndexed { index, (genre, count) ->
                val progress = if (state.totalMovies > 0)
                    count.toFloat() / state.totalMovies else 0f
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${index + 1}", color = TextMuted, fontSize = 11.sp,
                        modifier = Modifier.width(14.dp))
                    Text(genre, color = TextSecondary, fontSize = 12.sp,
                        modifier = Modifier.width(80.dp), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GoldAmber,
                        trackColor = GoldAmber.copy(alpha = 0.08f)
                    )
                    Text("$count", color = TextMuted, fontSize = 12.sp,
                        modifier = Modifier.width(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AchievementsGrid(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        achievements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty slot kalau ganjil
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, modifier: Modifier) {
    val unlocked = achievement.unlocked

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (unlocked)
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to GoldAmberDim.copy(alpha = 0.25f),
                            0.6f to SurfaceElevated,
                            1f to VelvetRed.copy(alpha = 0.08f)
                        )
                    )
                else
                    Brush.linearGradient(listOf(SurfaceDark, SurfaceDark))
            )
            .border(
                BorderStroke(
                    if (unlocked) 1.dp else 0.5.dp,
                    if (unlocked)
                        Brush.linearGradient(
                            listOf(GoldAmber.copy(alpha = 0.6f), BorderGold.copy(alpha = 0.2f))
                        )
                    else
                        Brush.linearGradient(
                            listOf(BorderSubtle.copy(alpha = 0.3f), BorderSubtle.copy(alpha = 0.1f))
                        )
                ),
                RoundedCornerShape(18.dp)
            )
    ) {
        // Glow effect untuk yang unlocked
        if (unlocked) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .blur(30.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldAmber.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Emoji + lock indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (unlocked)
                                GoldAmber.copy(alpha = 0.12f)
                            else
                                BorderSubtle.copy(alpha = 0.08f)
                        )
                        .border(
                            BorderStroke(
                                0.5.dp,
                                if (unlocked) GoldAmber.copy(alpha = 0.3f)
                                else BorderSubtle.copy(alpha = 0.2f)
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = achievement.emoji,
                        fontSize = 22.sp,
                        modifier = if (!unlocked) Modifier.blur(2.dp) else Modifier
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (unlocked)
                                StatusFinished.copy(alpha = 0.15f)
                            else
                                BorderSubtle.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (unlocked) "✓" else "🔒",
                        color = if (unlocked) StatusFinished else TextMuted.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title + description
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = achievement.title,
                    color = if (unlocked) GoldAmber else TextMuted.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = achievement.description,
                    color = if (unlocked) TextSecondary else TextMuted.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentActivity(movies: List<com.example.rewind.domain.model.Movie>) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            movies.forEachIndexed { index, movie ->
                val (statusColor, statusLabel) = when (movie.status) {
                    WatchStatus.COMPLETED -> StatusFinished to "Done"
                    WatchStatus.WATCHING -> StatusWatching to "Watching"
                    WatchStatus.PLAN_TO_WATCH -> StatusWantToWatch to "Planned"
                    WatchStatus.ON_HOLD -> StatusOnHold to "On Hold"
                    WatchStatus.DROPPED -> StatusDropped to "Dropped"
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Index number
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(SurfaceDark)
                            .border(BorderStroke(1.dp, BorderSubtle), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = TextMuted, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold)
                    }

                    Column(modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(movie.title, color = TextWarm, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Text("${movie.type.displayName} · ${movie.genre.displayName}",
                            color = TextMuted, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                            .border(BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f)),
                                RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(statusLabel, color = statusColor, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }

                if (index < movies.size - 1) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp)
                        .background(BorderSubtle.copy(alpha = 0.4f)))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = GoldAmber, fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold, letterSpacing = 2.5.sp)
}