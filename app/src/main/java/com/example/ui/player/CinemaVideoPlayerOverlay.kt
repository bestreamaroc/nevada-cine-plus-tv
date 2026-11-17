package com.example.ui.player

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioMode
import com.example.model.LocalizedStrings
import com.example.model.PlayerEngineMode
import com.example.model.StreamItemEntity
import com.example.model.SubtitleBgStyle
import com.example.model.SubtitleFontSize
import com.example.model.SubtitlePreferenceState
import com.example.ui.components.NcPlusVectorLogo
import com.example.ui.components.cinemaDpadFocusable
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
import kotlinx.coroutines.delay

data class TrackOption(
    val label: String,
    val languageCode: String,
    val groupIndex: Int,
    val trackIndex: Int
)

@OptIn(UnstableApi::class)
@Composable
fun CinemaVideoPlayerOverlay(
    streamItem: StreamItemEntity,
    subtitlePref: SubtitlePreferenceState,
    strings: LocalizedStrings,
    onUpdateSubtitlePref: (
        enabled: Boolean?,
        languageCode: String?,
        languageLabel: String?,
        externalUri: Uri?,
        externalLabel: String?,
        fontSize: SubtitleFontSize?,
        bgStyle: SubtitleBgStyle?
    ) -> Unit,
    onClose: () -> Unit,
    onOpenExternalPlayer: (StreamItemEntity) -> Unit
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var showHud by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackStatusText by remember { mutableStateOf("Connecting to stream...") }
    var activeStreamUrl by remember(streamItem.id) { mutableStateOf(streamItem.streamUrl) }
    var reconnectCount by remember(streamItem.id) { mutableIntStateOf(0) }

    var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.FIT) }
    var engineMode by remember { mutableStateOf(PlayerEngineMode.NATIVE_EXO) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubtitlesModal by remember { mutableStateOf(false) }

    val audioTracks = remember { mutableStateListOf<TrackOption>() }
    val embeddedSubtitleTracks = remember { mutableStateListOf<TrackOption>() }
    var selectedAudioLabel by remember { mutableStateOf("Default Audio") }

    // External .SRT / .VTT file picker
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

    val isRawTs = activeStreamUrl.contains(".ts", ignoreCase = true) &&
        !activeStreamUrl.contains(".m3u8", ignoreCase = true)

    // Configure hardware-accelerated ExoPlayer with DefaultDataSource (supports both HTTP and content:// external .srt/.vtt)
    val exoPlayer = remember(context) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("NevadaCinePlus/2.0 (Android TV; powered by BE STREAM)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(25000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 2500,
                /* maxBufferMs = */ 18000,
                /* bufferForPlaybackMs = */ 1000,
                /* bufferForPlaybackAfterRebufferMs = */ 2000
            )
            .build()

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    // Load or reload stream URL + optional external .srt / .vtt subtitle configuration
    LaunchedEffect(activeStreamUrl, engineMode, subtitlePref.externalSubtitleUri) {
        if (engineMode == PlayerEngineMode.NATIVE_EXO) {
            isBuffering = true
            playbackStatusText = if (isRawTs) {
                "MPEG-TS Engine • Connecting..."
            } else {
                "HLS / VOD Engine • Connecting..."
            }
            val builder = MediaItem.Builder().setUri(activeStreamUrl)
            val extUri = subtitlePref.externalSubtitleUri
            if (extUri != null) {
                val isVtt = subtitlePref.externalSubtitleLabel.endsWith(".vtt", ignoreCase = true)
                val subConfig = MediaItem.SubtitleConfiguration.Builder(extUri)
                    .setMimeType(if (isVtt) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP)
                    .setLanguage(subtitlePref.preferredLanguageCode)
                    .setLabel(subtitlePref.externalSubtitleLabel.ifBlank { "External Subtitles" })
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                builder.setSubtitleConfigurations(listOf(subConfig))
            }
            exoPlayer.setMediaItem(builder.build())
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.pause()
            isBuffering = false
            playbackStatusText = if (isRawTs) "mpegts.js Live Engine" else "HLS.js Live Engine"
        }
    }

    // Apply preferred subtitle language & enable/disable state to ExoPlayer
    LaunchedEffect(subtitlePref.enabled, subtitlePref.preferredLanguageCode) {
        val paramsBuilder = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !subtitlePref.enabled)
        if (subtitlePref.enabled && subtitlePref.preferredLanguageCode != "auto") {
            paramsBuilder.setPreferredTextLanguage(subtitlePref.preferredLanguageCode)
        }
        exoPlayer.trackSelectionParameters = paramsBuilder.build()
    }

    // Attach ExoPlayer listener for audio tracks, embedded subtitle tracks (C.TRACK_TYPE_TEXT), buffering, and auto-reconnect
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        playbackStatusText = "Buffering stream..."
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        playbackStatusText = if (isRawTs) "MPEG-TS (HW+)" else "Cinema Stream (HW+)"
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        playbackStatusText = "Playback completed"
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onTracksChanged(tracks: Tracks) {
                audioTracks.clear()
                embeddedSubtitleTracks.clear()
                tracks.groups.forEachIndexed { groupIdx, group ->
                    if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (trackIdx in 0 until group.length) {
                            val format = group.getTrackFormat(trackIdx)
                            val lang = format.language ?: "Track ${trackIdx + 1}"
                            val label = format.label ?: lang.uppercase()
                            audioTracks.add(
                                TrackOption(
                                    label = "$label (${format.channelCount}ch)",
                                    languageCode = lang,
                                    groupIndex = groupIdx,
                                    trackIndex = trackIdx
                                )
                            )
                        }
                    } else if (group.type == C.TRACK_TYPE_TEXT) {
                        for (trackIdx in 0 until group.length) {
                            val format = group.getTrackFormat(trackIdx)
                            val lang = format.language ?: "sub_${trackIdx + 1}"
                            val label = format.label ?: when {
                                lang.startsWith("ar", true) -> "العربية (Arabic)"
                                lang.startsWith("fr", true) -> "Français (French)"
                                lang.startsWith("en", true) -> "English (CC)"
                                else -> lang.uppercase()
                            }
                            embeddedSubtitleTracks.add(
                                TrackOption(
                                    label = label,
                                    languageCode = lang,
                                    groupIndex = groupIdx,
                                    trackIndex = trackIdx
                                )
                            )
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                if (reconnectCount < 2) {
                    reconnectCount++
                    if (streamItem.backupTsUrl.isNotBlank() && activeStreamUrl != streamItem.backupTsUrl) {
                        playbackStatusText = "Switching to MPEG-TS (.ts) backup stream..."
                        activeStreamUrl = streamItem.backupTsUrl
                    } else {
                        playbackStatusText = "Auto-reconnecting ($reconnectCount/2)..."
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                } else {
                    playbackStatusText = "Stream notice: Try HLS.js/mpegts.js engine or External Player (MX/VLC)"
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Auto-hide HUD after 6.5 seconds of inactivity when playing (unless a modal is open)
    LaunchedEffect(showHud, isPlaying, showSubtitlesModal, showAudioMenu) {
        if (showHud && isPlaying && !showSubtitlesModal && !showAudioMenu) {
            delay(6500)
            showHud = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showHud = !showHud
            },
        contentAlignment = Alignment.Center
    ) {
        val surfaceModifier = when (aspectRatioMode) {
            AspectRatioMode.FIT -> Modifier.fillMaxSize()
            AspectRatioMode.FILL -> Modifier.fillMaxSize()
            AspectRatioMode.SIXTEEN_NINE -> Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        }

        if (engineMode == PlayerEngineMode.NATIVE_EXO) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        player = exoPlayer
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    view.resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                    view.subtitleView?.let { subView ->
                        subView.alpha = if (subtitlePref.enabled) 1f else 0f
                        subView.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, subtitlePref.fontSize.sizeSp)
                        val captionStyle = when (subtitlePref.bgStyle) {
                            SubtitleBgStyle.SEMI_DARK -> CaptionStyleCompat(
                                AndroidColor.WHITE,
                                AndroidColor.argb(185, 6, 9, 19),
                                AndroidColor.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                                AndroidColor.BLACK,
                                Typeface.DEFAULT_BOLD
                            )
                            SubtitleBgStyle.SOLID_BLACK -> CaptionStyleCompat(
                                AndroidColor.rgb(245, 185, 66), // Gold cinema text on solid black
                                AndroidColor.BLACK,
                                AndroidColor.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_NONE,
                                AndroidColor.BLACK,
                                Typeface.DEFAULT_BOLD
                            )
                            SubtitleBgStyle.OUTLINE_ONLY -> CaptionStyleCompat(
                                AndroidColor.WHITE,
                                AndroidColor.TRANSPARENT,
                                AndroidColor.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                                AndroidColor.BLACK,
                                Typeface.DEFAULT_BOLD
                            )
                        }
                        subView.setStyle(captionStyle)
                    }
                },
                modifier = surfaceModifier
            )
        } else {
            DualJsWebEnginePlayer(
                streamUrl = activeStreamUrl,
                aspectRatioMode = aspectRatioMode,
                modifier = surfaceModifier
            )
        }

        // Loading / Buffering Spinner Overlay
        if (isBuffering) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(CinemaObsidian.copy(alpha = 0.78f), RoundedCornerShape(18.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = playbackStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Rich TV HUD Controls Overlay
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CinemaObsidian.copy(alpha = 0.88f),
                                Color.Transparent,
                                Color.Transparent,
                                CinemaObsidian.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(20.dp)
            ) {
                // Top HUD Bar: Back Button + Channel/Movie Info + Live/Engine Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("player_close_button")
                                .cinemaDpadFocusable(
                                    shape = CircleShape,
                                    onClick = onClose
                                )
                                .background(CinemaSurfaceElevated, CircleShape)
                                .border(1.dp, NeonCyan, CircleShape)
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        NcPlusVectorLogo(size = 40.dp)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = streamItem.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${streamItem.categoryName} • $playbackStatusText",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaGold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(LiveCrimson.copy(alpha = 0.22f), RoundedCornerShape(50))
                                .border(1.dp, LiveCrimson, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(LiveCrimson, CircleShape)
                            )
                            Text(
                                text = streamItem.streamFormat,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(CinemaGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = streamItem.resolutionBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaObsidian,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Bottom HUD Controls Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(CinemaMidnight.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Transport Controls: -10s, Play/Pause, +10s, Reconnect
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HudControlButton(
                                icon = Icons.Default.Replay10,
                                label = "-10s",
                                onClick = {
                                    val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(target)
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .testTag("player_play_pause_button")
                                    .cinemaDpadFocusable(
                                        shape = CircleShape,
                                        onClick = {
                                            if (exoPlayer.isPlaying) {
                                                exoPlayer.pause()
                                            } else {
                                                exoPlayer.play()
                                            }
                                        }
                                    )
                                    .background(NeonCyan, CircleShape)
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = CinemaObsidian,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            HudControlButton(
                                icon = Icons.Default.Forward10,
                                label = "+10s",
                                onClick = {
                                    exoPlayer.seekTo(exoPlayer.currentPosition + 10_000L)
                                }
                            )

                            HudControlButton(
                                icon = Icons.Default.Refresh,
                                label = "Reconnect",
                                onClick = {
                                    if (streamItem.backupTsUrl.isNotBlank() && activeStreamUrl == streamItem.streamUrl) {
                                        activeStreamUrl = streamItem.backupTsUrl
                                    } else {
                                        activeStreamUrl = streamItem.streamUrl
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    }
                                }
                            )
                        }

                        // Right Utility Controls: Aspect Ratio, Audio Track, Multi-Language Subtitles (CC / الترجمة), Engine
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HudControlButton(
                                icon = Icons.Default.AspectRatio,
                                label = "${strings.aspectRatioLabel}: ${aspectRatioMode.label}",
                                onClick = {
                                    aspectRatioMode = when (aspectRatioMode) {
                                        AspectRatioMode.FIT -> AspectRatioMode.FILL
                                        AspectRatioMode.FILL -> AspectRatioMode.SIXTEEN_NINE
                                        AspectRatioMode.SIXTEEN_NINE -> AspectRatioMode.FIT
                                    }
                                }
                            )

                            // Audio Track Selector
                            Box {
                                HudControlButton(
                                    icon = Icons.Default.GraphicEq,
                                    label = "${strings.audioTrackLabel}: $selectedAudioLabel",
                                    onClick = { showAudioMenu = true }
                                )
                                DropdownMenu(
                                    expanded = showAudioMenu,
                                    onDismissRequest = { showAudioMenu = false },
                                    modifier = Modifier.background(CinemaSurfaceElevated)
                                ) {
                                    if (audioTracks.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Default Stereo (Auto)", color = TextPrimary) },
                                            onClick = { showAudioMenu = false }
                                        )
                                    } else {
                                        audioTracks.forEach { track ->
                                            DropdownMenuItem(
                                                text = { Text(track.label, color = TextPrimary) },
                                                onClick = {
                                                    selectedAudioLabel = track.label
                                                    val groups = exoPlayer.currentTracks.groups
                                                    if (track.groupIndex < groups.size) {
                                                        val mediaGroup = groups[track.groupIndex].mediaTrackGroup
                                                        exoPlayer.trackSelectionParameters =
                                                            exoPlayer.trackSelectionParameters
                                                                .buildUpon()
                                                                .setOverrideForType(
                                                                    TrackSelectionOverride(mediaGroup, track.trackIndex)
                                                                )
                                                                .build()
                                                    }
                                                    showAudioMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Multi-Language Subtitles (CC / الترجمة) Selector Button
                            HudControlButton(
                                icon = if (subtitlePref.enabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                                label = if (subtitlePref.enabled) {
                                    "CC / الترجمة: ${subtitlePref.preferredLanguageLabel}"
                                } else {
                                    "CC / الترجمة: OFF"
                                },
                                onClick = { showSubtitlesModal = true }
                            )

                            // Dual Engine Switcher (HW+ ExoPlayer <-> HLS.js + mpegts.js)
                            HudControlButton(
                                icon = Icons.Default.Memory,
                                label = engineMode.label,
                                onClick = {
                                    engineMode = if (engineMode == PlayerEngineMode.NATIVE_EXO) {
                                        PlayerEngineMode.WEB_DUAL
                                    } else {
                                        PlayerEngineMode.NATIVE_EXO
                                    }
                                }
                            )
                        }
                    }

                    // Row 2: One-Click "Open in External Player (MX Player / VLC)" Button using Android Intent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_external_intent_button")
                            .cinemaDpadFocusable(
                                shape = RoundedCornerShape(12.dp),
                                accentColor = CinemaGold,
                                onClick = {
                                    onOpenExternalPlayer(streamItem.copy(streamUrl = activeStreamUrl))
                                }
                            )
                            .background(
                                Brush.horizontalGradient(
                                    listOf(CinemaSurfaceElevated, CinemaSurface)
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, CinemaGold, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(18.dp)
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

        // Professional Multi-Language Subtitles (CC / الترجمة) Modal
        if (showSubtitlesModal) {
            MultiLanguageSubtitlesDialog(
                subtitlePref = subtitlePref,
                embeddedTracks = embeddedSubtitleTracks,
                strings = strings,
                onSelectLanguage = { enabled, code, label, trackOpt ->
                    onUpdateSubtitlePref(enabled, code, label, null, null, null, null)
                    if (trackOpt != null) {
                        val groups = exoPlayer.currentTracks.groups
                        if (trackOpt.groupIndex < groups.size) {
                            val mediaGroup = groups[trackOpt.groupIndex].mediaTrackGroup
                            exoPlayer.trackSelectionParameters =
                                exoPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                    .setOverrideForType(
                                        TrackSelectionOverride(mediaGroup, trackOpt.trackIndex)
                                    )
                                    .build()
                        }
                    }
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
                onDismiss = { showSubtitlesModal = false }
            )
        }
    }
}

@kotlin.OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiLanguageSubtitlesDialog(
    subtitlePref: SubtitlePreferenceState,
    embeddedTracks: List<TrackOption>,
    strings: LocalizedStrings,
    onSelectLanguage: (enabled: Boolean, code: String, label: String, track: TrackOption?) -> Unit,
    onPickExternalSrtFile: () -> Unit,
    onSelectFontSize: (SubtitleFontSize) -> Unit,
    onSelectBgStyle: (SubtitleBgStyle) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CinemaMidnight,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .border(1.5.dp, NeonCyan, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "Subtitles & Captions • إعدادات الترجمة",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Multi-language embedded tracks (العربية / Français / English) & external .SRT / .VTT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .cinemaDpadFocusable(shape = CircleShape, onClick = onDismiss)
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

                // 1. Language Tracks (Arabic, French, English, Off + Any Embedded Stream Tracks)
                Text(
                    text = "1. Subtitle Language Track (لغة الترجمة)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaGold
                )

                val standardLangs = listOf(
                    Triple(true, "ar", "العربية (Arabic)"),
                    Triple(true, "fr", "Français (French)"),
                    Triple(true, "en", "English (CC)"),
                    Triple(false, "off", "Off / إيقاف الترجمة")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    standardLangs.forEach { (enableFlag, code, label) ->
                        val isSelected = if (!enableFlag) {
                            !subtitlePref.enabled
                        } else {
                            subtitlePref.enabled && subtitlePref.preferredLanguageCode == code
                        }

                        Box(
                            modifier = Modifier
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = { onSelectLanguage(enableFlag, code, label, null) }
                                )
                                .background(
                                    if (isSelected) NeonCyan else CinemaSurface,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else BorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CinemaObsidian,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) CinemaObsidian else TextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Also show any extra embedded stream tracks discovered by ExoPlayer
                    embeddedTracks.forEach { track ->
                        val isSelected = subtitlePref.enabled && subtitlePref.preferredLanguageLabel == track.label
                        Box(
                            modifier = Modifier
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = {
                                        onSelectLanguage(true, track.languageCode, track.label, track)
                                    }
                                )
                                .background(
                                    if (isSelected) NeonCyan else CinemaSurfaceElevated,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(1.dp, CinemaGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Stream: ${track.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CinemaObsidian else CinemaGold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // 2. Load External .SRT / .VTT File
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .testTag("load_external_srt_button")
                            .cinemaDpadFocusable(
                                shape = RoundedCornerShape(10.dp),
                                accentColor = CinemaGold,
                                onClick = onPickExternalSrtFile
                            )
                            .background(CinemaSurfaceElevated, RoundedCornerShape(10.dp))
                            .border(1.dp, CinemaGold, RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (subtitlePref.externalSubtitleLabel.isNotBlank()) {
                                    "Loaded: ${subtitlePref.externalSubtitleLabel}"
                                } else {
                                    strings.loadExternalSrtBtn
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = CinemaGold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // 3. TV Readability: Font Size & Background Style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. TV Font Size (حجم الخط)",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SubtitleFontSize.entries.forEach { sizeOption ->
                                val selected = subtitlePref.fontSize == sizeOption
                                Box(
                                    modifier = Modifier
                                        .cinemaDpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { onSelectFontSize(sizeOption) }
                                        )
                                        .background(
                                            if (selected) NeonCyan else CinemaSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, if (selected) NeonCyan else BorderSubtle, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = sizeOption.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) CinemaObsidian else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3. Background Style (خلفية الترجمة)",
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SubtitleBgStyle.entries.forEach { bgOption ->
                                val selected = subtitlePref.bgStyle == bgOption
                                Box(
                                    modifier = Modifier
                                        .cinemaDpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { onSelectBgStyle(bgOption) }
                                        )
                                        .background(
                                            if (selected) NeonCyan else CinemaSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, if (selected) NeonCyan else BorderSubtle, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = bgOption.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) CinemaObsidian else TextPrimary,
                                        fontWeight = FontWeight.Bold,
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
}

@Composable
private fun HudControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .cinemaDpadFocusable(
                shape = RoundedCornerShape(10.dp),
                onClick = onClick
            )
            .background(CinemaSurface, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Embedded Hardware-Accelerated Dual-Engine (HLS.js + mpegts.js) WebView Player
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun DualJsWebEnginePlayer(
    streamUrl: String,
    aspectRatioMode: AspectRatioMode,
    modifier: Modifier = Modifier
) {
    val objectFitCss = when (aspectRatioMode) {
        AspectRatioMode.FIT -> "contain"
        AspectRatioMode.FILL -> "cover"
        AspectRatioMode.SIXTEEN_NINE -> "fill"
    }

    val htmlContent = remember(streamUrl, objectFitCss) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
          <script src="https://cdn.jsdelivr.net/npm/mpegts.js@latest/dist/mpegts.js"></script>
          <style>
            html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
            video { width: 100%; height: 100%; object-fit: $objectFitCss; background: #000; }
          </style>
        </head>
        <body>
          <video id="cinemaVideo" autoplay playsinline></video>
          <script>
            const url = "${streamUrl.replace("\"", "\\\"")}";
            const video = document.getElementById('cinemaVideo');
            const isTs = url.toLowerCase().includes('.ts') && !url.toLowerCase().includes('.m3u8');

            if (isTs && window.mpegts && mpegts.getFeatureList().mseLivePlayback) {
              const player = mpegts.createPlayer({
                type: 'mpegts',
                isLive: true,
                url: url
              }, {
                enableWorker: true,
                lazyLoadMaxDuration: 3 * 60,
                seekType: 'range',
                liveBufferLatencyChasing: true
              });
              player.attachMediaElement(video);
              player.load();
              player.play();
            } else if (window.Hls && Hls.isSupported()) {
              const hls = new Hls({
                enableWorker: true,
                lowLatencyMode: true,
                backBufferLength: 60
              });
              hls.loadSource(url);
              hls.attachMedia(video);
              hls.on(Hls.Events.MANIFEST_PARSED, function() {
                video.play();
              });
              hls.on(Hls.Events.ERROR, function(event, data) {
                if (data.fatal) {
                  if (data.type === Hls.ErrorTypes.NETWORK_ERROR) hls.startLoad();
                  else if (data.type === Hls.ErrorTypes.MEDIA_ERROR) hls.recoverMediaError();
                }
              });
            } else {
              video.src = url;
              video.play();
            }
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webChromeClient = WebChromeClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://nevadacineplus.local/", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}
