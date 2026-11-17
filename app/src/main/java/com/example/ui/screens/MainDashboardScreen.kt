package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppLanguage
import com.example.model.CategoryBouquetEntity
import com.example.model.LocalizedStrings
import com.example.model.MediaCategoryType
import com.example.model.NavSection
import com.example.model.SeriesDetailsModel
import com.example.model.SportsMatch
import com.example.model.StreamItemEntity
import com.example.model.SubtitleBgStyle
import com.example.model.SubtitleFontSize
import com.example.model.SubtitlePreferenceState
import com.example.model.VodMovieDetailsModel
import com.example.model.getLocalizedStrings
import com.example.ui.components.LanguageSwitcherRow
import com.example.ui.components.LiveClockTimezoneBadge
import com.example.ui.components.NcPlusVectorLogo
import com.example.ui.components.cinemaDpadFocusable
import com.example.ui.player.MultiLanguageSubtitlesDialog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NevadaCineUiState

@Composable
fun MainDashboardScreen(
    uiState: NevadaCineUiState,
    totalCachedStreams: Int,
    allBouquets: List<CategoryBouquetEntity>,
    recentlyAddedMovies: List<StreamItemEntity>,
    onSelectNavSection: (NavSection) -> Unit,
    onSelectCategoryBouquet: (String) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onCardFocusedOrHovered: (StreamItemEntity) -> Unit,
    onPlayStream: (StreamItemEntity) -> Unit,
    onPlayMovieImmediately: (StreamItemEntity) -> Unit,
    onOpenExternalPlayer: (StreamItemEntity) -> Unit,
    onToggleFavorite: (StreamItemEntity) -> Unit,
    onLoadNextWindowBatch: () -> Unit,
    onRefreshMatches: () -> Unit,
    onSelectMatchForSmartChannels: (SportsMatch) -> Unit,
    onDismissSmartChannels: () -> Unit,
    onDismissMovieDetails: () -> Unit = {},
    onDismissSeriesDetails: () -> Unit = {},
    onUpdateSubtitlePref: (
        enabled: Boolean?,
        languageCode: String?,
        languageLabel: String?,
        externalUri: Uri?,
        externalLabel: String?,
        fontSize: SubtitleFontSize?,
        bgStyle: SubtitleBgStyle?
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onSelectLanguage: (AppLanguage) -> Unit,
    onSwitchServer: () -> Unit
) {
    val strings = getLocalizedStrings(uiState.language)

    // Handle Back key on secondary tabs to return to HOME cleanly
    if (uiState.activeNavSection != NavSection.HOME &&
        uiState.selectedMovieDetails == null &&
        uiState.selectedSeriesDetails == null &&
        uiState.selectedMatchForSmartChannels == null
    ) {
        BackHandler {
            onSelectNavSection(NavSection.HOME)
        }
    }

    val filteredBouquets = allBouquets.filter { bouquet ->
        when (uiState.activeNavSection) {
            NavSection.HOME -> true
            NavSection.LIVE_TV -> bouquet.categoryType == MediaCategoryType.LIVE.name
            NavSection.MOVIES -> bouquet.categoryType == MediaCategoryType.MOVIE.name
            NavSection.SERIES -> bouquet.categoryType == MediaCategoryType.SERIES.name
            NavSection.TODAYS_MATCHES -> bouquet.categoryType == MediaCategoryType.LIVE.name
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWideTvLayout = maxWidth >= 680.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Compact Left Icon Rail (Home, Live TV, Today's Matches, Movies, Series + Switch Server)
            CompactLeftIconRail(
                activeSection = uiState.activeNavSection,
                strings = strings,
                onSelectSection = onSelectNavSection,
                onSwitchServer = onSwitchServer
            )

            // 2. Real Server Categories / Bouquets Sidebar (on Wide/TV screens)
            if (isWideTvLayout && uiState.activeNavSection != NavSection.TODAYS_MATCHES) {
                ServerBouquetsSidebar(
                    bouquets = filteredBouquets,
                    selectedCategoryId = uiState.selectedCategoryId,
                    totalCachedCount = totalCachedStreams,
                    strings = strings,
                    onSelectCategory = onSelectCategoryBouquet
                )
            }

            // 3. Main Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Floating Top Navigation Pills + Search + Clock & Language
                FloatingTopBar(
                    uiState = uiState,
                    strings = strings,
                    isWideTvLayout = isWideTvLayout,
                    onSelectNavSection = onSelectNavSection,
                    onUpdateSearchQuery = onUpdateSearchQuery,
                    onSelectLanguage = onSelectLanguage
                )

                // On Compact screens, show Horizontal Bouquet Filter Chips
                if (!isWideTvLayout && uiState.activeNavSection != NavSection.TODAYS_MATCHES) {
                    CompactBouquetFilterRow(
                        bouquets = filteredBouquets,
                        selectedCategoryId = uiState.selectedCategoryId,
                        strings = strings,
                        onSelectCategory = onSelectCategoryBouquet
                    )
                }

                // Content Body: Either Automated Today's Matches Hub OR Hero + Virtual 30-Card Windowed Grid
                if (uiState.activeNavSection == NavSection.TODAYS_MATCHES) {
                    MatchesHubSection(
                        matches = uiState.todaysMatches,
                        isLoading = uiState.isLoadingMatches,
                        errorMessage = uiState.matchesError,
                        strings = strings,
                        onRefresh = onRefreshMatches,
                        onMatchSelected = onSelectMatchForSmartChannels,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    val isMoviesTab = uiState.activeNavSection == NavSection.MOVIES
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isMoviesTab) 160.dp else if (isWideTvLayout) 175.dp else 150.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .focusGroup()
                            .testTag("virtual_streams_grid")
                    ) {
                        // Cinematic Hero Banner at top of Grid (auto-updates on card focus/hover)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CinematicHeroBanner(
                                focusedItem = uiState.heroFocusedItem,
                                strings = strings,
                                onWatchNow = { item -> onPlayStream(item) },
                                onOpenExternal = { item -> onOpenExternalPlayer(item) }
                            )
                        }

                        // Prominent "Recently Added Movies" (آخر الأفلام المضافة) Horizontal Poster Row on HOME & MOVIES
                        if ((uiState.activeNavSection == NavSection.HOME || uiState.activeNavSection == NavSection.MOVIES) &&
                            recentlyAddedMovies.isNotEmpty() &&
                            uiState.searchQuery.isBlank()
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                RecentlyAddedMoviesRow(
                                    movies = recentlyAddedMovies,
                                    strings = strings,
                                    onViewAllMovies = { onSelectNavSection(NavSection.MOVIES) },
                                    onMovieFocused = onCardFocusedOrHovered,
                                    onMovieClicked = onPlayStream
                                )
                            }
                        }

                        // Today's Matches Quick Strip on HOME tab
                        if (uiState.activeNavSection == NavSection.HOME && uiState.todaysMatches.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HomeMatchesQuickRow(
                                    matches = uiState.todaysMatches.take(6),
                                    strings = strings,
                                    onViewAllMatches = { onSelectNavSection(NavSection.TODAYS_MATCHES) },
                                    onMatchClick = onSelectMatchForSmartChannels
                                )
                            }
                        }

                        // Virtual / Windowed Rendering: 30 cards per viewport batch
                        items(
                            items = uiState.windowedStreams,
                            key = { it.id }
                        ) { streamItem ->
                            StreamMediaCard(
                                item = streamItem,
                                isHeroSelected = uiState.heroFocusedItem?.id == streamItem.id,
                                onFocused = { onCardFocusedOrHovered(streamItem) },
                                onClick = {
                                    onCardFocusedOrHovered(streamItem)
                                    onPlayStream(streamItem)
                                },
                                onToggleFavorite = { onToggleFavorite(streamItem) }
                            )
                        }

                        // Auto-trigger or click to load next 30-card batch when scrolling near end
                        if (uiState.windowedStreams.size < uiState.totalFilteredCount) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LaunchedEffect(uiState.windowedStreams.size) {
                                    onLoadNextWindowBatch()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .testTag("load_more_batch_button")
                                            .cinemaDpadFocusable(
                                                shape = RoundedCornerShape(12.dp),
                                                onClick = onLoadNextWindowBatch
                                            )
                                            .background(CinemaSurfaceElevated, RoundedCornerShape(12.dp))
                                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "${strings.loadMoreBatch} (${uiState.windowedStreams.size} / ${uiState.totalFilteredCount})",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = NeonCyan,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Channel Matching Modal when user clicks a fixture
        val selectedMatch = uiState.selectedMatchForSmartChannels
        if (selectedMatch != null) {
            SmartChannelMatchDialog(
                match = selectedMatch,
                matchedChannels = uiState.smartMatchedChannels,
                onDismiss = onDismissSmartChannels,
                onLaunchStream = onPlayMovieImmediately,
                onOpenExternal = onOpenExternalPlayer
            )
        }

        // Loading indicator when fetching `get_vod_info` or `get_series_info`
        if (uiState.isLoadingMovieDetails || uiState.isLoadingSeriesDetails) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinemaObsidian.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(CinemaMidnight, RoundedCornerShape(18.dp))
                        .border(1.dp, NeonCyan, RoundedCornerShape(18.dp))
                        .padding(horizontal = 28.dp, vertical = 22.dp)
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (uiState.isLoadingMovieDetails) {
                            "Loading Movie Details, Cast & Subtitles (get_vod_info)..."
                        } else {
                            "Loading Series Seasons & Episodes (get_series_info)..."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // EveryCine-style Rich Movie Details Overlay (`action=get_vod_info&vod_id=ID`)
        val movieDetails = uiState.selectedMovieDetails
        if (movieDetails != null) {
            MovieDetailsDialog(
                details = movieDetails,
                subtitlePref = uiState.subtitlePreference,
                strings = strings,
                onDismiss = onDismissMovieDetails,
                onPlayMovie = {
                    onPlayMovieImmediately(movieDetails.streamItem)
                },
                onOpenExternal = {
                    onOpenExternalPlayer(movieDetails.streamItem)
                },
                onUpdateSubtitlePref = onUpdateSubtitlePref
            )
        }

        // Series Seasons & Playable Episodes Modal (`get_series_info&series_id=ID`)
        val seriesDetails = uiState.selectedSeriesDetails
        if (seriesDetails != null) {
            SeriesDetailsDialog(
                details = seriesDetails,
                onDismiss = onDismissSeriesDetails,
                onPlayEpisode = { episodeStream ->
                    onDismissSeriesDetails()
                    onPlayMovieImmediately(episodeStream)
                },
                onOpenExternal = { episodeStream ->
                    onOpenExternalPlayer(episodeStream)
                }
            )
        }
    }
}

/**
 * Prominent "Recently Added Movies" (آخر الأفلام المضافة) Horizontal Poster Row
 */
@Composable
private fun RecentlyAddedMoviesRow(
    movies: List<StreamItemEntity>,
    strings: LocalizedStrings,
    onViewAllMovies: () -> Unit,
    onMovieFocused: (StreamItemEntity) -> Unit,
    onMovieClicked: (StreamItemEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusGroup()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NewReleases,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strings.recentlyAddedMoviesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Box(
                modifier = Modifier
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(8.dp),
                        onClick = onViewAllMovies
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "All Movies →",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup()
                .testTag("recently_added_movies_row")
        ) {
            items(
                items = movies,
                key = { "recent_${it.id}" }
            ) { movie ->
                Column(
                    modifier = Modifier
                        .width(142.dp)
                        .cinemaDpadFocusable(
                            shape = RoundedCornerShape(14.dp),
                            scaleOnFocus = 1.06f,
                            onFocused = { onMovieFocused(movie) },
                            onClick = {
                                onMovieFocused(movie)
                                onMovieClicked(movie)
                            }
                        )
                        .background(CinemaMidnight, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(8.dp)
                ) {
                    // 2:3 Cinema Poster Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(185.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CinemaObsidian),
                        contentAlignment = Alignment.Center
                    ) {
                        if (movie.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = movie.logoUrl,
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = NeonCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Star Rating Badge (Top-Left)
                        if (movie.rating.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .background(CinemaObsidian.copy(alpha = 0.88f), RoundedCornerShape(6.dp))
                                    .border(1.dp, CinemaGold, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⭐ ${movie.rating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // Year / Resolution Badge (Bottom-End)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(NeonCyan.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = movie.releaseYear.ifBlank { movie.resolutionBadge },
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaObsidian,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = movie.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * EveryCine-Style Rich Movie Details Overlay (`action=get_vod_info&vod_id=ID`)
 * Displays Movie Title, Release Year, Duration, Genre, Star Rating (⭐ 8.4 / 10),
 * Cast & Director with actor pills, Plot / Synopsis, and auto-focused "Play Movie",
 * "Subtitles / الترجمة" selector, and "Open in External Player (MX / VLC)" buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailsDialog(
    details: VodMovieDetailsModel,
    subtitlePref: SubtitlePreferenceState,
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onPlayMovie: () -> Unit,
    onOpenExternal: () -> Unit,
    onUpdateSubtitlePref: (
        enabled: Boolean?,
        languageCode: String?,
        languageLabel: String?,
        externalUri: Uri?,
        externalLabel: String?,
        fontSize: SubtitleFontSize?,
        bgStyle: SubtitleBgStyle?
    ) -> Unit
) {
    val context = LocalContext.current
    val playMovieFocusRequester = remember { FocusRequester() }
    var showSubtitlesConfig by remember { mutableStateOf(false) }

    val subtitleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = uri.lastPathSegment ?: "subtitles.srt"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(idx) ?: fileName
                }
            }
            onUpdateSubtitlePref(true, null, "External: $fileName", uri, fileName, null, null)
        }
    }

    LaunchedEffect(details.vodId) {
        try {
            playMovieFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CinemaMidnight,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 820.dp)
                .border(1.5.dp, NeonCyan, RoundedCornerShape(24.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle Blurred Backdrop Art
                if (details.backdropUrl.isNotBlank()) {
                    AsyncImage(
                        model = details.backdropUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.22f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CinemaObsidian.copy(alpha = 0.96f),
                                    CinemaMidnight.copy(alpha = 0.92f),
                                    CinemaObsidian.copy(alpha = 0.94f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Top Row: Poster + Metadata + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // High-Res Movie Poster
                        Box(
                            modifier = Modifier
                                .size(width = 148.dp, height = 216.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CinemaObsidian)
                                .border(1.dp, CinemaGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (details.posterUrl.isNotBlank()) {
                                AsyncImage(
                                    model = details.posterUrl,
                                    contentDescription = details.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        // Right Details Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = details.title,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .testTag("close_movie_details_button")
                                        .cinemaDpadFocusable(
                                            shape = CircleShape,
                                            onClick = onDismiss
                                        )
                                        .background(CinemaSurfaceElevated, CircleShape)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = TextPrimary
                                    )
                                }
                            }

                            // Badges Row: Star Rating (⭐ 8.4 / 10), Release Year, Duration, Resolution, Genre
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Star Rating Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(CinemaGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .border(1.dp, CinemaGold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = details.ratingDisplay,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                // Release Year
                                if (details.releaseYear.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(CinemaSurfaceElevated, RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = details.releaseYear,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                // Duration
                                if (details.duration.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .background(CinemaSurfaceElevated, RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = details.duration,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                // Resolution Badge
                                Box(
                                    modifier = Modifier
                                        .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${details.resolutionBadge} • ${details.containerExtension}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                // Genre
                                if (details.genre.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(CinemaSurface, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = details.genre,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Plot / Synopsis
                            Column {
                                Text(
                                    text = strings.plotSynopsisLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CinemaGold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = details.plot,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Director & Cast Actor Pills
                            if (details.director.isNotBlank() || details.castList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${strings.directorLabel}: ${details.director} • ${strings.castAndCrewLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (details.castList.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            details.castList.forEach { actor ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier
                                                        .background(CinemaSurface, RoundedCornerShape(50))
                                                        .border(1.dp, BorderSubtle, RoundedCornerShape(50))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = CinemaGold,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = actor,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Action Buttons: 1. Play Movie (Auto-Focused), 2. Subtitles / الترجمة, 3. Open in External Player (MX / VLC)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusGroup(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Play Movie Button (Auto-focused for instant OK press on TV remote)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("movie_details_play_button")
                                .focusRequester(playMovieFocusRequester)
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(14.dp),
                                    scaleOnFocus = 1.04f,
                                    onClick = onPlayMovie
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(NeonCyan, Color(0xFF0099CC), CinemaGold)
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 14.dp, horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = CinemaObsidian,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = strings.playMovieBtn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CinemaObsidian,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // 2. Subtitles / الترجمة Selector Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("movie_details_subtitles_button")
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(14.dp),
                                    accentColor = NeonCyan,
                                    onClick = { showSubtitlesConfig = true }
                                )
                                .background(CinemaSurfaceElevated, RoundedCornerShape(14.dp))
                                .border(1.dp, NeonCyan, RoundedCornerShape(14.dp))
                                .padding(vertical = 14.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${strings.subtitlesSelectorBtn} (${if (subtitlePref.enabled) subtitlePref.preferredLanguageCode.uppercase() else "OFF"})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 3. Open in External Player (MX / VLC)
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("movie_details_external_button")
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(14.dp),
                                    accentColor = CinemaGold,
                                    onClick = onOpenExternal
                                )
                                .background(CinemaSurface, RoundedCornerShape(14.dp))
                                .border(1.dp, CinemaGold, RoundedCornerShape(14.dp))
                                .padding(vertical = 14.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "MX Player / VLC",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CinemaGold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubtitlesConfig) {
        MultiLanguageSubtitlesDialog(
            subtitlePref = subtitlePref,
            embeddedTracks = emptyList(),
            strings = strings,
            onSelectLanguage = { enabled, code, label, _ ->
                onUpdateSubtitlePref(enabled, code, label, null, null, null, null)
            },
            onPickExternalSrtFile = {
                subtitleFilePicker.launch(arrayOf("*/*"))
            },
            onSelectFontSize = { size ->
                onUpdateSubtitlePref(null, null, null, null, null, size, null)
            },
            onSelectBgStyle = { style ->
                onUpdateSubtitlePref(null, null, null, null, null, null, style)
            },
            onDismiss = { showSubtitlesConfig = false }
        )
    }
}

@Composable
private fun CompactLeftIconRail(
    activeSection: NavSection,
    strings: LocalizedStrings,
    onSelectSection: (NavSection) -> Unit,
    onSwitchServer: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(82.dp)
            .fillMaxHeight()
            .focusGroup()
            .background(CinemaMidnight)
            .border(width = 1.dp, color = BorderSubtle)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NcPlusVectorLogo(size = 40.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val railItems = listOf(
                Triple(NavSection.HOME, Icons.Default.Home, strings.navHome),
                Triple(NavSection.LIVE_TV, Icons.Default.LiveTv, strings.navLiveTv),
                Triple(NavSection.TODAYS_MATCHES, Icons.Default.SportsSoccer, strings.navMatches),
                Triple(NavSection.MOVIES, Icons.Default.Movie, strings.navMovies),
                Triple(NavSection.SERIES, Icons.Default.Tv, strings.navSeries)
            )

            railItems.forEach { (section, icon, label) ->
                val isSelected = activeSection == section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rail_nav_${section.name.lowercase()}")
                        .cinemaDpadFocusable(
                            shape = RoundedCornerShape(12.dp),
                            onClick = { onSelectSection(section) }
                        )
                        .background(
                            if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) NeonCyan else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) NeonCyan else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isSelected) NeonCyan else TextSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .testTag("switch_server_button")
                .cinemaDpadFocusable(
                    shape = RoundedCornerShape(12.dp),
                    accentColor = CinemaGold,
                    onClick = onSwitchServer
                )
                .background(CinemaSurface, RoundedCornerShape(12.dp))
                .border(1.dp, CinemaGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = strings.switchSource,
                tint = CinemaGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ServerBouquetsSidebar(
    bouquets: List<CategoryBouquetEntity>,
    selectedCategoryId: String,
    totalCachedCount: Int,
    strings: LocalizedStrings,
    onSelectCategory: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .focusGroup()
            .background(CinemaMidnight.copy(alpha = 0.75f))
            .border(width = 1.dp, color = BorderSubtle)
            .padding(12.dp)
    ) {
        Text(
            text = strings.bouquetsHeader,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaGold,
            letterSpacing = 1.4.sp,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${strings.cachedStreamsLabel}: $totalCachedCount",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                val isAllSelected = selectedCategoryId.isEmpty()
                BouquetSidebarRow(
                    name = strings.allBouquets,
                    count = bouquets.sumOf { it.itemCount },
                    isSelected = isAllSelected,
                    onClick = { onSelectCategory("") }
                )
            }

            items(
                items = bouquets,
                key = { "${it.categoryType}_${it.categoryId}" }
            ) { bouquet ->
                BouquetSidebarRow(
                    name = bouquet.categoryName,
                    count = bouquet.itemCount,
                    isSelected = selectedCategoryId == bouquet.categoryId,
                    onClick = { onSelectCategory(bouquet.categoryId) }
                )
            }
        }
    }
}

@Composable
private fun BouquetSidebarRow(
    name: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cinemaDpadFocusable(
                shape = RoundedCornerShape(10.dp),
                scaleOnFocus = 1.02f,
                onClick = onClick
            )
            .background(
                if (isSelected) NeonCyan.copy(alpha = 0.18f) else CinemaSurface.copy(alpha = 0.55f),
                RoundedCornerShape(10.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) NeonCyan else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .background(CinemaObsidian, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) NeonCyan else CinemaGold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun CompactBouquetFilterRow(
    bouquets: List<CategoryBouquetEntity>,
    selectedCategoryId: String,
    strings: LocalizedStrings,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .focusGroup()
    ) {
        item {
            val isAll = selectedCategoryId.isEmpty()
            Box(
                modifier = Modifier
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(50),
                        onClick = { onSelectCategory("") }
                    )
                    .background(
                        if (isAll) NeonCyan else CinemaSurface,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "${strings.allBouquets} (${bouquets.sumOf { it.itemCount }})",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isAll) CinemaObsidian else TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        items(
            items = bouquets,
            key = { "${it.categoryType}_${it.categoryId}" }
        ) { bouquet ->
            val selected = selectedCategoryId == bouquet.categoryId
            Box(
                modifier = Modifier
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(50),
                        onClick = { onSelectCategory(bouquet.categoryId) }
                    )
                    .background(
                        if (selected) NeonCyan else CinemaSurface,
                        RoundedCornerShape(50)
                    )
                    .border(1.dp, if (selected) NeonCyan else BorderSubtle, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "${bouquet.categoryName} (${bouquet.itemCount})",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) CinemaObsidian else TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun FloatingTopBar(
    uiState: NevadaCineUiState,
    strings: LocalizedStrings,
    isWideTvLayout: Boolean,
    onSelectNavSection: (NavSection) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .background(CinemaMidnight.copy(alpha = 0.95f), RoundedCornerShape(50))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                val pills = listOf(
                    Triple(NavSection.HOME, strings.navHome, Icons.Default.Home),
                    Triple(NavSection.LIVE_TV, strings.navLiveTv, Icons.Default.LiveTv),
                    Triple(NavSection.TODAYS_MATCHES, strings.navMatches, Icons.Default.SportsSoccer),
                    Triple(NavSection.MOVIES, strings.navMovies, Icons.Default.Movie),
                    Triple(NavSection.SERIES, strings.navSeries, Icons.Default.Tv)
                )

                items(pills) { (section, label, icon) ->
                    val active = uiState.activeNavSection == section
                    Row(
                        modifier = Modifier
                            .testTag("top_pill_${section.name.lowercase()}")
                            .cinemaDpadFocusable(
                                shape = RoundedCornerShape(50),
                                onClick = { onSelectNavSection(section) }
                            )
                            .background(
                                brush = if (active) {
                                    Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0099CC)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                },
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (active) CinemaObsidian else NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) CinemaObsidian else TextPrimary,
                            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (isWideTvLayout) {
                Spacer(modifier = Modifier.width(10.dp))
                LiveClockTimezoneBadge()
                Spacer(modifier = Modifier.width(8.dp))
                LanguageSwitcherRow(
                    selectedLanguage = uiState.language,
                    onSelectLanguage = onSelectLanguage
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onUpdateSearchQuery,
                placeholder = { Text(strings.searchHint, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onUpdateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = CinemaSurface,
                    unfocusedContainerColor = CinemaMidnight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("dashboard_search_input")
            )

            Box(
                modifier = Modifier
                    .background(CinemaSurface, RoundedCornerShape(50))
                    .border(1.dp, CinemaGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "${strings.viewportWindowInfo} • ${uiState.windowedStreams.size}/${uiState.totalFilteredCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Cinematic Hero Banner
 */
@Composable
private fun CinematicHeroBanner(
    focusedItem: StreamItemEntity?,
    strings: LocalizedStrings,
    onWatchNow: (StreamItemEntity) -> Unit,
    onOpenExternal: (StreamItemEntity) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CinemaMidnight)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.6f), CinemaGold.copy(alpha = 0.4f))),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_cinema_1794932283013),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (focusedItem != null && focusedItem.backdropUrl.isNotBlank()) {
            AsyncImage(
                model = focusedItem.backdropUrl,
                contentDescription = focusedItem.title,
                contentScale = ContentScale.Crop,
                alpha = 0.45f,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CinemaObsidian.copy(alpha = 0.96f),
                            CinemaObsidian.copy(alpha = 0.82f),
                            CinemaObsidian.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(CinemaGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = focusedItem?.resolutionBadge ?: "4K UHD",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaObsidian,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (focusedItem != null && focusedItem.rating.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(CinemaObsidian.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .border(1.dp, CinemaGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⭐ ${focusedItem.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = focusedItem?.streamFormat ?: "HLS / MPEG-TS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = focusedItem?.categoryName ?: "NEVADA CINE PLUS • powered by BE STREAM",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                Text(
                    text = focusedItem?.title ?: "NEVADA CINE PLUS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = focusedItem?.description?.ifBlank {
                        "Hardware-accelerated streaming powered by BE STREAM"
                    } ?: "Focus or tap any channel, movie, or series card below to preview and stream in 4K/FHD.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (focusedItem != null) {
                val isSeries = focusedItem.categoryType == MediaCategoryType.SERIES.name
                val isMovie = focusedItem.categoryType == MediaCategoryType.MOVIE.name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("hero_watch_now_button")
                            .cinemaDpadFocusable(
                                shape = RoundedCornerShape(12.dp),
                                onClick = { onWatchNow(focusedItem) }
                            )
                            .background(
                                Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0099CC))),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isSeries -> Icons.Default.VideoLibrary
                                    isMovie -> Icons.Default.Movie
                                    else -> Icons.Default.PlayArrow
                                },
                                contentDescription = null,
                                tint = CinemaObsidian,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when {
                                    isSeries -> "Seasons & Episodes"
                                    isMovie -> "${strings.playMovieBtn} & Info"
                                    else -> strings.watchNow
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = CinemaObsidian,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .testTag("hero_external_player_button")
                            .cinemaDpadFocusable(
                                shape = RoundedCornerShape(12.dp),
                                accentColor = CinemaGold,
                                onClick = { onOpenExternal(focusedItem) }
                            )
                            .background(CinemaSurfaceElevated.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                            .border(1.dp, CinemaGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strings.openExternalPlayer,
                                style = MaterialTheme.typography.labelLarge,
                                color = CinemaGold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMatchesQuickRow(
    matches: List<SportsMatch>,
    strings: LocalizedStrings,
    onViewAllMatches: () -> Unit,
    onMatchClick: (SportsMatch) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusGroup()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strings.navMatches,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Box(
                modifier = Modifier
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(8.dp),
                        onClick = onViewAllMatches
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup()
        ) {
            items(matches, key = { it.idEvent }) { match ->
                Row(
                    modifier = Modifier
                        .width(280.dp)
                        .cinemaDpadFocusable(
                            shape = RoundedCornerShape(14.dp),
                            onClick = { onMatchClick(match) }
                        )
                        .background(CinemaSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${match.strHomeTeam} vs ${match.strAwayTeam}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${match.localKickoffTime} • ${match.strLeague}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamMediaCard(
    item: StreamItemEntity,
    isHeroSelected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isMovieOrSeries = item.categoryType == MediaCategoryType.MOVIE.name ||
        item.categoryType == MediaCategoryType.SERIES.name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stream_card_${item.id}")
            .cinemaDpadFocusable(
                shape = RoundedCornerShape(16.dp),
                scaleOnFocus = 1.05f,
                onFocused = onFocused,
                onClick = onClick
            )
            .background(
                if (isHeroSelected) CinemaSurfaceElevated else CinemaMidnight,
                RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHeroSelected) 1.5.dp else 1.dp,
                color = if (isHeroSelected) NeonCyan else BorderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isMovieOrSeries) 165.dp else 105.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CinemaObsidian),
            contentAlignment = Alignment.Center
        ) {
            if (item.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = item.logoUrl,
                    contentDescription = item.title,
                    contentScale = if (item.categoryType == MediaCategoryType.LIVE.name) ContentScale.Fit else ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (item.categoryType == MediaCategoryType.LIVE.name) 10.dp else 0.dp)
                )
            } else {
                Text(
                    text = item.title.take(3).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(CinemaObsidian.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                    .border(1.dp, CinemaGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (item.rating.isNotBlank()) "⭐ ${item.rating}" else item.resolutionBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) LiveCrimson else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(
                        when (item.categoryType) {
                            MediaCategoryType.LIVE.name -> LiveCrimson
                            MediaCategoryType.SERIES.name -> NeonCyan
                            else -> EmeraldSuccess
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = item.releaseYear.ifBlank { item.categoryType },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.categoryType == MediaCategoryType.SERIES.name) CinemaObsidian else TextPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.categoryName,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Series Seasons & Playable Episodes Dialog (`action=get_series_info&series_id=ID`)
 */
@Composable
fun SeriesDetailsDialog(
    details: SeriesDetailsModel,
    onDismiss: () -> Unit,
    onPlayEpisode: (StreamItemEntity) -> Unit,
    onOpenExternal: (StreamItemEntity) -> Unit
) {
    var selectedSeasonIndex by remember(details.seriesId) { mutableIntStateOf(0) }
    val currentSeason = details.seasons.getOrNull(selectedSeasonIndex) ?: details.seasons.firstOrNull()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CinemaMidnight,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .border(1.5.dp, NeonCyan, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (details.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = details.coverUrl,
                                contentDescription = details.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 72.dp, height = 98.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = details.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(CinemaGold, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${details.seasons.size} Seasons",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaObsidian,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (details.rating.isNotBlank()) {
                                    Text(
                                        text = "★ ${details.rating}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CinemaGold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (details.genre.isNotBlank()) {
                                    Text(
                                        text = details.genre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NeonCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (details.plot.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = details.plot,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .testTag("close_series_dialog_button")
                            .cinemaDpadFocusable(
                                shape = CircleShape,
                                onClick = onDismiss
                            )
                            .background(CinemaSurfaceElevated, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(details.seasons.size) { idx ->
                        val season = details.seasons[idx]
                        val selected = idx == selectedSeasonIndex
                        Box(
                            modifier = Modifier
                                .testTag("season_tab_${season.seasonNumber}")
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(50),
                                    onClick = { selectedSeasonIndex = idx }
                                )
                                .background(
                                    if (selected) NeonCyan else CinemaSurface,
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    1.dp,
                                    if (selected) NeonCyan else BorderSubtle,
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${season.seasonName} (${season.episodes.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) CinemaObsidian else TextPrimary,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val episodes = currentSeason?.episodes.orEmpty()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = episodes,
                        key = { it.id }
                    ) { ep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("episode_row_${ep.id}")
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { onPlayEpisode(ep.streamItem) }
                                )
                                .background(CinemaSurface, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(CinemaObsidian, RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "E${ep.episodeNum}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NeonCyan,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ep.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append(ep.containerExtension)
                                            if (ep.duration.isNotBlank()) append(" • ${ep.duration}")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaGold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .cinemaDpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { onPlayEpisode(ep.streamItem) }
                                        )
                                        .background(NeonCyan, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Episode",
                                            tint = CinemaObsidian,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Play",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CinemaObsidian,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .cinemaDpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            accentColor = CinemaGold,
                                            onClick = { onOpenExternal(ep.streamItem) }
                                        )
                                        .background(CinemaSurfaceElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, CinemaGold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "External Player",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
