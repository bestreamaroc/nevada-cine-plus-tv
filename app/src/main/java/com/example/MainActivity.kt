package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.getLocalizedStrings
import com.example.ui.components.NevadaSplashScreen
import com.example.ui.player.CinemaVideoPlayerOverlay
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NevadaCineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force sensorLandscape widescreen TV mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NevadaCinePlusApp()
            }
        }
    }
}

@Composable
fun NevadaCinePlusApp(
    viewModel: NevadaCineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalCachedStreams by viewModel.totalCachedStreams.collectAsStateWithLifecycle()
    val allBouquets by viewModel.allBouquets.collectAsStateWithLifecycle()
    val recentlyAddedMovies by viewModel.recentlyAddedMovies.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val layoutDirection = if (uiState.language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = Triple(
                    uiState.isSplashVisible,
                    uiState.isAuthenticated,
                    uiState.activePlayingStream != null
                ),
                label = "root_navigation_crossfade"
            ) { (isSplash, isAuth, _) ->
                when {
                    isSplash -> {
                        NevadaSplashScreen()
                    }
                    !isAuth -> {
                        LoginScreen(
                            uiState = uiState,
                            onSelectLanguage = viewModel::setLanguage,
                            onSelectMode = viewModel::setLoginMode,
                            onLocalFilePicked = viewModel::onLocalFileSelected,
                            onUpdateXtream = viewModel::updateXtreamCredentials,
                            onClearChangeAccount = viewModel::clearOrChangeXtreamAccount,
                            onSmartPasteLink = viewModel::parseAndAutoFillXtreamLink,
                            onUpdateM3uUrl = viewModel::updateDirectM3uUrl,
                            onFillPublicM3uUrl = viewModel::fillPublicFtaM3uUrl,
                            onConnectAndCache = viewModel::connectAndCache
                        )
                    }
                    else -> {
                        MainDashboardScreen(
                            uiState = uiState,
                            totalCachedStreams = totalCachedStreams,
                            allBouquets = allBouquets,
                            recentlyAddedMovies = recentlyAddedMovies,
                            onSelectNavSection = viewModel::selectNavSection,
                            onSelectCategoryBouquet = viewModel::selectCategoryBouquet,
                            onUpdateSearchQuery = viewModel::updateSearchQuery,
                            onCardFocusedOrHovered = viewModel::onCardFocusedOrHovered,
                            onPlayStream = viewModel::launchStreamInCinemaPlayer,
                            onPlayMovieImmediately = viewModel::playStreamImmediately,
                            onOpenExternalPlayer = { item ->
                                viewModel.openInExternalPlayer(
                                    context = context,
                                    streamUrl = item.streamUrl,
                                    title = item.title
                                )
                            },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onLoadNextWindowBatch = viewModel::loadNextWindowBatch,
                            onRefreshMatches = viewModel::fetchTodaysMatches,
                            onSelectMatchForSmartChannels = viewModel::onMatchClickedForSmartChannels,
                            onDismissSmartChannels = viewModel::dismissSmartChannelsSheet,
                            onDismissMovieDetails = viewModel::dismissMovieDetails,
                            onDismissSeriesDetails = viewModel::dismissSeriesDetails,
                            onUpdateSubtitlePref = viewModel::updateSubtitlePreferences,
                            onSelectLanguage = viewModel::setLanguage,
                            onSwitchServer = viewModel::switchServerOrPlaylist
                        )
                    }
                }
            }

            // Full-Screen Hardware-Accelerated Cinema Video Player Overlay
            val activeStream = uiState.activePlayingStream
            if (activeStream != null) {
                CinemaVideoPlayerOverlay(
                    streamItem = activeStream,
                    subtitlePref = uiState.subtitlePreference,
                    strings = getLocalizedStrings(uiState.language),
                    onUpdateSubtitlePref = viewModel::updateSubtitlePreferences,
                    onClose = viewModel::closeCinemaPlayer,
                    onOpenExternalPlayer = { item ->
                        viewModel.openInExternalPlayer(
                            context = context,
                            streamUrl = item.streamUrl,
                            title = item.title
                        )
                    }
                )
            }
        }
    }
}
