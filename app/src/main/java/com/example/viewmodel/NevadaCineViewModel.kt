package com.example.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CinemaDatabase
import com.example.data.local.SessionPreferences
import com.example.data.remote.IptvAndSportsService
import com.example.model.AppLanguage
import com.example.model.CategoryBouquetEntity
import com.example.model.LoginMode
import com.example.model.MediaCategoryType
import com.example.model.NavSection
import com.example.model.SeriesDetailsModel
import com.example.model.SportsMatch
import com.example.model.StreamItemEntity
import com.example.model.SubtitleBgStyle
import com.example.model.SubtitleFontSize
import com.example.model.SubtitlePreferenceState
import com.example.model.VodMovieDetailsModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NevadaCineUiState(
    val isSplashVisible: Boolean = true,
    val isAuthenticated: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val loginMode: LoginMode = LoginMode.XTREAM,
    // Mode 1: Local Playlist
    val selectedLocalFileUri: Uri? = null,
    val selectedLocalFileName: String = "",
    // Mode 2: Xtream Codes + Smart Paste Link Parser (Pre-filled with default BE STREAM credentials)
    val smartPasteInput: String = "",
    val smartParseAutoFilled: Boolean = false,
    val xtreamServerUrl: String = NevadaCineViewModel.DEFAULT_XTREAM_SERVER_URL,
    val xtreamUsername: String = NevadaCineViewModel.DEFAULT_XTREAM_USERNAME,
    val xtreamPassword: String = NevadaCineViewModel.DEFAULT_XTREAM_PASSWORD,
    // Mode 3: Direct M3U URL
    val directM3uUrl: String = "",
    // Parsing & Connection state
    val isParsingOrConnecting: Boolean = false,
    val parsedStreamsProgressCount: Int = 0,
    val progressMessage: String = "",
    val errorMessage: String? = null,
    val serverLabel: String = "",
    // Dashboard state
    val activeNavSection: NavSection = NavSection.HOME,
    val selectedCategoryId: String = "", // "" means All Bouquets
    val searchQuery: String = "",
    // Windowed Virtual Rendering (30 cards per viewport batch)
    val windowedStreams: List<StreamItemEntity> = emptyList(),
    val totalFilteredCount: Int = 0,
    val currentWindowBatchCount: Int = 1, // 1 batch = 30 cards
    val isLoadingWindow: Boolean = false,
    // Hero Banner Focused Card
    val heroFocusedItem: StreamItemEntity? = null,
    // Movie Details Overlay (action=get_vod_info&vod_id=ID)
    val selectedMovieDetails: VodMovieDetailsModel? = null,
    val isLoadingMovieDetails: Boolean = false,
    // Series Details (action=get_series_info&series_id=ID)
    val selectedSeriesDetails: SeriesDetailsModel? = null,
    val isLoadingSeriesDetails: Boolean = false,
    // Multi-Language Subtitles Preferences
    val subtitlePreference: SubtitlePreferenceState = SubtitlePreferenceState(),
    // Today's Matches Hub (TheSportsDB)
    val todaysMatches: List<SportsMatch> = emptyList(),
    val isLoadingMatches: Boolean = false,
    val matchesError: String? = null,
    val selectedMatchForSmartChannels: SportsMatch? = null,
    val smartMatchedChannels: List<StreamItemEntity> = emptyList(),
    // Active Video Player Overlay
    val activePlayingStream: StreamItemEntity? = null
)

class NevadaCineViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val WINDOW_BATCH_SIZE = 30
        const val PUBLIC_IPTV_ORG_M3U_URL = "https://iptv-org.github.io/iptv/index.m3u"
        const val DEFAULT_XTREAM_SERVER_URL = "http://151535216079.4k-v2-2026.live:80"
        const val DEFAULT_XTREAM_USERNAME = "a5zwd8o75q"
        const val DEFAULT_XTREAM_PASSWORD = "oqh9qvcdal"
    }

    private val database = CinemaDatabase.getInstance(application)
    private val streamDao = database.streamDao()
    private val sessionPrefs = SessionPreferences(application)
    private val iptvService = IptvAndSportsService(application, streamDao)

    private val _uiState = MutableStateFlow(NevadaCineUiState())
    val uiState: StateFlow<NevadaCineUiState> = _uiState.asStateFlow()

    val totalCachedStreams: StateFlow<Int> = streamDao.observeTotalStreamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allBouquets: StateFlow<List<CategoryBouquetEntity>> = streamDao.observeAllBouquets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedMovies: StateFlow<List<StreamItemEntity>> = streamDao.observeRecentlyAddedMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var windowQueryJob: Job? = null
    private var isSyncingVodInBackground = false

    init {
        // Hydrate saved session & check cached streams in Room DB for <1s launch
        viewModelScope.launch {
            sessionPrefs.sessionFlow.collect { saved ->
                val count = streamDao.getTotalStreamCountOnce()
                val hasCache = saved.isConnectedAndCached && count > 0
                _uiState.update { state ->
                    state.copy(
                        language = saved.language,
                        loginMode = saved.lastLoginMode,
                        serverLabel = saved.serverLabel,
                        xtreamServerUrl = saved.xtreamServerUrl.ifBlank { state.xtreamServerUrl },
                        xtreamUsername = saved.xtreamUsername.ifBlank { state.xtreamUsername },
                        xtreamPassword = saved.xtreamPassword.ifBlank { state.xtreamPassword },
                        directM3uUrl = saved.m3uUrl.ifBlank { state.directM3uUrl },
                        isAuthenticated = hasCache || state.isAuthenticated
                    )
                }
                if (hasCache) {
                    if (_uiState.value.windowedStreams.isEmpty()) {
                        refreshWindowedStreams(resetBatch = true)
                    }
                    ensureMoviesCachedInBackground()
                }
            }
        }

        // Splash screen timer (~1.1s for crisp cinema reveal)
        viewModelScope.launch {
            delay(1100)
            _uiState.update { it.copy(isSplashVisible = false) }
        }

        // Pre-fetch Today's Matches from TheSportsDB API
        fetchTodaysMatches()
    }

    private fun ensureMoviesCachedInBackground() {
        if (isSyncingVodInBackground) return
        viewModelScope.launch {
            val movieCount = streamDao.getMovieCountOnce()
            val state = _uiState.value
            if (movieCount == 0 && state.loginMode == LoginMode.XTREAM) {
                isSyncingVodInBackground = true
                iptvService.syncVodCatalogOnly(
                    serverUrl = state.xtreamServerUrl.ifBlank { DEFAULT_XTREAM_SERVER_URL },
                    username = state.xtreamUsername.ifBlank { DEFAULT_XTREAM_USERNAME },
                    password = state.xtreamPassword.ifBlank { DEFAULT_XTREAM_PASSWORD }
                )
                isSyncingVodInBackground = false
                refreshWindowedStreams(resetBatch = false)
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        viewModelScope.launch {
            sessionPrefs.setLanguage(language)
        }
    }

    fun setLoginMode(mode: LoginMode) {
        _uiState.update { it.copy(loginMode = mode, errorMessage = null) }
    }

    fun onLocalFileSelected(uri: Uri?, fileName: String) {
        _uiState.update {
            it.copy(
                selectedLocalFileUri = uri,
                selectedLocalFileName = fileName,
                errorMessage = null
            )
        }
    }

    fun updateXtreamCredentials(serverUrl: String? = null, username: String? = null, password: String? = null) {
        _uiState.update {
            it.copy(
                xtreamServerUrl = serverUrl ?: it.xtreamServerUrl,
                xtreamUsername = username ?: it.xtreamUsername,
                xtreamPassword = password ?: it.xtreamPassword,
                smartParseAutoFilled = false,
                errorMessage = null
            )
        }
    }

    fun clearOrChangeXtreamAccount() {
        _uiState.update {
            it.copy(
                smartPasteInput = "",
                smartParseAutoFilled = false,
                xtreamServerUrl = "",
                xtreamUsername = "",
                xtreamPassword = "",
                errorMessage = null
            )
        }
    }

    fun parseAndAutoFillXtreamLink(rawText: String): Boolean {
        val trimmed = rawText.trim()
        _uiState.update { it.copy(smartPasteInput = rawText, errorMessage = null) }
        if (trimmed.isBlank()) return false

        val urlRegex = Regex("""(https?://[^\s"'<>]+)""", RegexOption.IGNORE_CASE)
        val candidateUrl = urlRegex.find(trimmed)?.value ?: trimmed

        try {
            val uri = Uri.parse(
                if (candidateUrl.startsWith("http://", true) || candidateUrl.startsWith("https://", true)) {
                    candidateUrl
                } else {
                    "http://$candidateUrl"
                }
            )
            val scheme = uri.scheme ?: "http"
            val host = uri.host
            val port = uri.port

            if (!host.isNullOrBlank()) {
                val baseServer = if (port != -1) "$scheme://$host:$port" else "$scheme://$host"

                val qUser = uri.getQueryParameter("username")
                    ?: uri.getQueryParameter("user")
                    ?: uri.getQueryParameter("u")
                val qPass = uri.getQueryParameter("password")
                    ?: uri.getQueryParameter("pass")
                    ?: uri.getQueryParameter("p")

                if (!qUser.isNullOrBlank() && !qPass.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            loginMode = LoginMode.XTREAM,
                            smartPasteInput = trimmed,
                            xtreamServerUrl = baseServer,
                            xtreamUsername = qUser.trim(),
                            xtreamPassword = qPass.trim(),
                            directM3uUrl = candidateUrl,
                            smartParseAutoFilled = true,
                            errorMessage = null
                        )
                    }
                    return true
                }

                val segments = uri.pathSegments
                if (segments != null && segments.size >= 3) {
                    val first = segments[0].lowercase()
                    val (pathUser, pathPass) = if (first == "live" || first == "movie" || first == "series") {
                        if (segments.size >= 4) segments[1] to segments[2] else "" to ""
                    } else {
                        segments[0] to segments[1]
                    }
                    if (pathUser.isNotBlank() && pathPass.isNotBlank() && !pathUser.endsWith(".php")) {
                        _uiState.update {
                            it.copy(
                                loginMode = LoginMode.XTREAM,
                                smartPasteInput = trimmed,
                                xtreamServerUrl = baseServer,
                                xtreamUsername = pathUser.trim(),
                                xtreamPassword = pathPass.trim(),
                                smartParseAutoFilled = true,
                                errorMessage = null
                            )
                        }
                        return true
                    }
                }

                val userMatch = Regex("""(?:username|user|login)\s*[:=]\s*([^\s,;]+)""", RegexOption.IGNORE_CASE).find(trimmed)
                val passMatch = Regex("""(?:password|pass|pwd)\s*[:=]\s*([^\s,;]+)""", RegexOption.IGNORE_CASE).find(trimmed)
                if (userMatch != null && passMatch != null) {
                    _uiState.update {
                        it.copy(
                            loginMode = LoginMode.XTREAM,
                            smartPasteInput = trimmed,
                            xtreamServerUrl = baseServer,
                            xtreamUsername = userMatch.groupValues[1].trim(),
                            xtreamPassword = passMatch.groupValues[1].trim(),
                            smartParseAutoFilled = true,
                            errorMessage = null
                        )
                    }
                    return true
                }

                _uiState.update {
                    it.copy(
                        xtreamServerUrl = baseServer,
                        smartParseAutoFilled = false
                    )
                }
            }
        } catch (_: Exception) {
        }
        return false
    }

    fun updateDirectM3uUrl(url: String) {
        _uiState.update { it.copy(directM3uUrl = url, errorMessage = null) }
    }

    fun fillPublicFtaM3uUrl() {
        _uiState.update {
            it.copy(
                loginMode = LoginMode.URL,
                directM3uUrl = PUBLIC_IPTV_ORG_M3U_URL,
                errorMessage = null
            )
        }
    }

    fun connectAndCache() {
        val current = _uiState.value
        if (current.isParsingOrConnecting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isParsingOrConnecting = true,
                    parsedStreamsProgressCount = 0,
                    progressMessage = "Initializing BE STREAM hardware parser...",
                    errorMessage = null
                )
            }

            val result: Result<Int> = when (current.loginMode) {
                LoginMode.PLAYLIST -> {
                    val uri = current.selectedLocalFileUri
                    if (uri == null) {
                        Result.failure(IllegalArgumentException("Please select a local .m3u or .m3u8 file first."))
                    } else {
                        iptvService.parseAndCacheLocalM3u(uri) { count, msg ->
                            _uiState.update {
                                it.copy(parsedStreamsProgressCount = count, progressMessage = msg)
                            }
                        }
                    }
                }

                LoginMode.XTREAM -> {
                    if (current.xtreamServerUrl.isBlank() || current.xtreamUsername.isBlank() || current.xtreamPassword.isBlank()) {
                        Result.failure(IllegalArgumentException("Please enter Server URL, Username, and Password."))
                    } else {
                        iptvService.connectAndCacheXtreamCodes(
                            serverUrl = current.xtreamServerUrl,
                            username = current.xtreamUsername,
                            password = current.xtreamPassword
                        ) { count, msg ->
                            _uiState.update {
                                it.copy(parsedStreamsProgressCount = count, progressMessage = msg)
                            }
                        }
                    }
                }

                LoginMode.URL -> {
                    if (current.directM3uUrl.isBlank()) {
                        Result.failure(IllegalArgumentException("Please enter a valid M3U / M3U8 URL."))
                    } else {
                        iptvService.parseAndCacheRemoteM3uUrl(current.directM3uUrl) { count, msg ->
                            _uiState.update {
                                it.copy(parsedStreamsProgressCount = count, progressMessage = msg)
                            }
                        }
                    }
                }
            }

            result.fold(
                onSuccess = { _ ->
                    val label = when (current.loginMode) {
                        LoginMode.PLAYLIST -> current.selectedLocalFileName.ifBlank { "Local M3U Playlist" }
                        LoginMode.XTREAM -> current.xtreamServerUrl.replace("http://", "").replace("https://", "").substringBefore("/")
                        LoginMode.URL -> current.directM3uUrl.replace("http://", "").replace("https://", "").substringBefore("/")
                    }
                    sessionPrefs.saveConnectedSession(
                        mode = current.loginMode,
                        serverLabel = label,
                        xtreamUrl = current.xtreamServerUrl,
                        xtreamUser = current.xtreamUsername,
                        xtreamPass = current.xtreamPassword,
                        m3uUrl = current.directM3uUrl
                    )
                    _uiState.update {
                        it.copy(
                            isParsingOrConnecting = false,
                            isAuthenticated = true,
                            serverLabel = label,
                            activeNavSection = NavSection.HOME,
                            selectedCategoryId = "",
                            errorMessage = null
                        )
                    }
                    refreshWindowedStreams(resetBatch = true)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isParsingOrConnecting = false,
                            errorMessage = err.localizedMessage ?: "Failed to connect or parse playlist."
                        )
                    }
                }
            )
        }
    }

    fun selectNavSection(section: NavSection) {
        _uiState.update {
            it.copy(
                activeNavSection = section,
                selectedCategoryId = ""
            )
        }
        if (section == NavSection.MOVIES) {
            ensureMoviesCachedInBackground()
        }
        if (section == NavSection.TODAYS_MATCHES && _uiState.value.todaysMatches.isEmpty()) {
            fetchTodaysMatches()
        } else {
            refreshWindowedStreams(resetBatch = true)
        }
    }

    fun selectCategoryBouquet(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        refreshWindowedStreams(resetBatch = true)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshWindowedStreams(resetBatch = true)
    }

    fun loadNextWindowBatch() {
        val state = _uiState.value
        val currentLimit = state.currentWindowBatchCount * WINDOW_BATCH_SIZE
        if (state.isLoadingWindow || currentLimit >= state.totalFilteredCount) return
        _uiState.update { it.copy(currentWindowBatchCount = it.currentWindowBatchCount + 1) }
        refreshWindowedStreams(resetBatch = false)
    }

    private fun refreshWindowedStreams(resetBatch: Boolean) {
        windowQueryJob?.cancel()
        windowQueryJob = viewModelScope.launch {
            if (resetBatch) {
                _uiState.update { it.copy(currentWindowBatchCount = 1, isLoadingWindow = true) }
            } else {
                _uiState.update { it.copy(isLoadingWindow = true) }
            }

            val state = _uiState.value
            val categoryTypeFilter = when (state.activeNavSection) {
                NavSection.HOME -> ""
                NavSection.LIVE_TV -> MediaCategoryType.LIVE.name
                NavSection.MOVIES -> MediaCategoryType.MOVIE.name
                NavSection.SERIES -> MediaCategoryType.SERIES.name
                NavSection.TODAYS_MATCHES -> MediaCategoryType.LIVE.name
            }

            val totalLimit = _uiState.value.currentWindowBatchCount * WINDOW_BATCH_SIZE
            val items = streamDao.getWindowedStreams(
                categoryType = categoryTypeFilter,
                categoryId = state.selectedCategoryId,
                searchQuery = state.searchQuery.trim(),
                limit = totalLimit,
                offset = 0
            )
            val totalCount = streamDao.countMatchingStreams(
                categoryType = categoryTypeFilter,
                categoryId = state.selectedCategoryId,
                searchQuery = state.searchQuery.trim()
            )

            _uiState.update { current ->
                current.copy(
                    windowedStreams = items,
                    totalFilteredCount = totalCount,
                    isLoadingWindow = false,
                    heroFocusedItem = if (resetBatch) (items.firstOrNull() ?: current.heroFocusedItem) else (current.heroFocusedItem ?: items.firstOrNull())
                )
            }
        }
    }

    fun onCardFocusedOrHovered(item: StreamItemEntity) {
        _uiState.update { it.copy(heroFocusedItem = item) }
    }

    fun toggleFavorite(item: StreamItemEntity) {
        viewModelScope.launch {
            streamDao.updateFavorite(item.id, !item.isFavorite)
            refreshWindowedStreams(resetBatch = false)
        }
    }

    fun fetchTodaysMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMatches = true, matchesError = null) }
            val res = iptvService.fetchTodaysSoccerMatches()
            res.fold(
                onSuccess = { matches ->
                    _uiState.update {
                        it.copy(
                            todaysMatches = matches,
                            isLoadingMatches = false,
                            matchesError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingMatches = false,
                            matchesError = err.localizedMessage
                        )
                    }
                }
            )
        }
    }

    fun onMatchClickedForSmartChannels(match: SportsMatch) {
        viewModelScope.launch {
            val kw1 = match.strHomeTeam.split(" ").firstOrNull { it.length > 3 } ?: "Sport"
            val kw2 = match.strLeague.split(" ").firstOrNull { it.length > 3 } ?: "beIN"
            val matched = streamDao.findSmartSportsChannels(kw1, kw2)
            _uiState.update {
                it.copy(
                    selectedMatchForSmartChannels = match,
                    smartMatchedChannels = matched
                )
            }
        }
    }

    fun dismissSmartChannelsSheet() {
        _uiState.update {
            it.copy(
                selectedMatchForSmartChannels = null,
                smartMatchedChannels = emptyList()
            )
        }
    }

    /**
     * Handles selecting any card in the Dashboard:
     * - If it's a Movie (`MOVIE`), opens the EveryCine-style Movie Details Overlay (`action=get_vod_info&vod_id=ID`)
     * - If it's a Series (`SERIES` card `xt_ser_...`), opens the Series Seasons & Episodes Overlay (`action=get_series_info&series_id=ID`)
     * - Otherwise (Live TV channel or individual episode), launches directly in the Cinema Video Player
     */
    fun launchStreamInCinemaPlayer(item: StreamItemEntity) {
        if (item.categoryType == MediaCategoryType.MOVIE.name) {
            openMovieDetails(item)
            return
        }
        if (item.categoryType == MediaCategoryType.SERIES.name && item.id.startsWith("xt_ser_")) {
            openSeriesDetails(item)
            return
        }
        playStreamImmediately(item)
    }

    /**
     * Starts playback directly in the Cinema Video Player (used by "Play Movie" inside MovieDetailsDialog)
     */
    fun playStreamImmediately(item: StreamItemEntity) {
        _uiState.update {
            it.copy(
                activePlayingStream = item,
                selectedMovieDetails = null,
                selectedSeriesDetails = null,
                selectedMatchForSmartChannels = null
            )
        }
    }

    fun openMovieDetails(movieItem: StreamItemEntity) {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMovieDetails = true) }
            val res = iptvService.fetchXtreamVodInfo(
                serverUrl = current.xtreamServerUrl.ifBlank { DEFAULT_XTREAM_SERVER_URL },
                username = current.xtreamUsername.ifBlank { DEFAULT_XTREAM_USERNAME },
                password = current.xtreamPassword.ifBlank { DEFAULT_XTREAM_PASSWORD },
                movieItem = movieItem
            )
            res.fold(
                onSuccess = { details ->
                    _uiState.update {
                        it.copy(
                            isLoadingMovieDetails = false,
                            selectedMovieDetails = details
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoadingMovieDetails = false,
                            activePlayingStream = movieItem
                        )
                    }
                }
            )
        }
    }

    fun dismissMovieDetails() {
        _uiState.update {
            it.copy(
                selectedMovieDetails = null,
                isLoadingMovieDetails = false
            )
        }
    }

    fun updateSubtitlePreferences(
        enabled: Boolean? = null,
        languageCode: String? = null,
        languageLabel: String? = null,
        externalUri: Uri? = null,
        externalLabel: String? = null,
        fontSize: SubtitleFontSize? = null,
        bgStyle: SubtitleBgStyle? = null
    ) {
        _uiState.update { state ->
            val currentPref = state.subtitlePreference
            state.copy(
                subtitlePreference = currentPref.copy(
                    enabled = enabled ?: currentPref.enabled,
                    preferredLanguageCode = languageCode ?: currentPref.preferredLanguageCode,
                    preferredLanguageLabel = languageLabel ?: currentPref.preferredLanguageLabel,
                    externalSubtitleUri = externalUri ?: currentPref.externalSubtitleUri,
                    externalSubtitleLabel = externalLabel ?: currentPref.externalSubtitleLabel,
                    fontSize = fontSize ?: currentPref.fontSize,
                    bgStyle = bgStyle ?: currentPref.bgStyle
                )
            )
        }
    }

    fun openSeriesDetails(seriesItem: StreamItemEntity) {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSeriesDetails = true) }
            val res = iptvService.fetchXtreamSeriesInfo(
                serverUrl = current.xtreamServerUrl.ifBlank { DEFAULT_XTREAM_SERVER_URL },
                username = current.xtreamUsername.ifBlank { DEFAULT_XTREAM_USERNAME },
                password = current.xtreamPassword.ifBlank { DEFAULT_XTREAM_PASSWORD },
                seriesItem = seriesItem
            )
            res.fold(
                onSuccess = { details ->
                    _uiState.update {
                        it.copy(
                            isLoadingSeriesDetails = false,
                            selectedSeriesDetails = details
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoadingSeriesDetails = false,
                            activePlayingStream = seriesItem
                        )
                    }
                }
            )
        }
    }

    fun dismissSeriesDetails() {
        _uiState.update {
            it.copy(
                selectedSeriesDetails = null,
                isLoadingSeriesDetails = false
            )
        }
    }

    fun closeCinemaPlayer() {
        _uiState.update { it.copy(activePlayingStream = null) }
    }

    fun switchServerOrPlaylist() {
        viewModelScope.launch {
            sessionPrefs.disconnectSession()
            _uiState.update {
                it.copy(
                    isAuthenticated = false,
                    activePlayingStream = null,
                    selectedMovieDetails = null,
                    selectedSeriesDetails = null,
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Launches the stream in an external player (MX Player / VLC / Android TV Video Player)
     */
    fun openInExternalPlayer(context: Context, streamUrl: String, title: String) {
        try {
            val cleanNoScheme = streamUrl
                .removePrefix("http://")
                .removePrefix("https://")
            val scheme = if (streamUrl.startsWith("https://", ignoreCase = true)) "https" else "http"
            val intentUriStr = "intent://$cleanNoScheme#Intent;scheme=$scheme;type=video/*;S.title=${Uri.encode(title)};end"
            val parsedIntent = Intent.parseUri(intentUriStr, Intent.URI_INTENT_SCHEME).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(parsedIntent)
        } catch (e: Exception) {
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(streamUrl), "video/*")
                    putExtra("title", title)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(viewIntent, "Open in MX Player / VLC").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "No external video player (MX Player / VLC) installed on this device.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
