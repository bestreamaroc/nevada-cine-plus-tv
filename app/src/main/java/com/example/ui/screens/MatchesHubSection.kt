package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.LocalizedStrings
import com.example.model.SportsMatch
import com.example.model.StreamItemEntity
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

@Composable
fun MatchesHubSection(
    matches: List<SportsMatch>,
    isLoading: Boolean,
    errorMessage: String?,
    strings: LocalizedStrings,
    onRefresh: () -> Unit,
    onMatchSelected: (SportsMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // Wide Horizontal Banner Header (Title + Subtitle on Left, Refresh Button on Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(CinemaSurfaceElevated, CinemaMidnight)
                    ),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CinemaObsidian, CircleShape)
                        .border(1.dp, CinemaGold.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.matchesTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.matchesSub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .testTag("refresh_matches_button")
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(12.dp),
                        onClick = onRefresh
                    )
                    .background(CinemaObsidian, RoundedCornerShape(12.dp))
                    .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = strings.refreshFixtures,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.refreshFixtures,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Syncing today's live fixtures & local kickoff times...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 310.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = matches,
                    key = { it.idEvent }
                ) { match ->
                    MatchFixtureCard(
                        match = match,
                        strings = strings,
                        onClick = { onMatchSelected(match) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchFixtureCard(
    match: SportsMatch,
    strings: LocalizedStrings,
    onClick: () -> Unit
) {
    val isLive = match.statusBadge == "LIVE"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.idEvent}")
            .cinemaDpadFocusable(
                shape = RoundedCornerShape(18.dp),
                scaleOnFocus = 1.03f,
                onClick = onClick
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(CinemaSurfaceElevated, CinemaMidnight)
                ),
                RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = if (isLive) LiveCrimson.copy(alpha = 0.7f) else BorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        // Top Row: League Badge + Kickoff Local Time / Live Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp)
                    .background(CinemaObsidian, RoundedCornerShape(8.dp))
                    .border(1.dp, CinemaGold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = match.strLeague,
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaGold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        if (isLive) LiveCrimson.copy(alpha = 0.2f) else CinemaObsidian,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isLive) LiveCrimson else NeonCyan,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isLive) "LIVE • ${match.localKickoffTime}" else match.localKickoffTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLive) LiveCrimson else NeonCyan,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Row: Home Team Logo & Name VS Away Team Logo & Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home Team
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamCrestBadge(badgeUrl = match.strHomeTeamBadge, teamName = match.strHomeTeam)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = match.strHomeTeam,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // VS / Score Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                if (match.homeScore != null && match.awayScore != null) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CinemaGold,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(CinemaObsidian, CircleShape)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = match.dateEvent,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Away Team
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamCrestBadge(badgeUrl = match.strAwayTeamBadge, teamName = match.strAwayTeam)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = match.strAwayTeam,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Suggested Broadcaster Pills
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            match.suggestedChannels.take(4).forEach { broadcaster ->
                Box(
                    modifier = Modifier
                        .background(CinemaObsidian.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = broadcaster,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Smart Channel Matching CTA Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeonCyan.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.findMatchingChannels,
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyan,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun TeamCrestBadge(badgeUrl: String, teamName: String) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(CinemaObsidian)
            .border(1.dp, BorderSubtle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (badgeUrl.isNotBlank()) {
            AsyncImage(
                model = badgeUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            )
        } else {
            Text(
                text = teamName.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = CinemaGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Smart Channel Matching Dialog when a user clicks a match card
 */
@Composable
fun SmartChannelMatchDialog(
    match: SportsMatch,
    matchedChannels: List<StreamItemEntity>,
    onDismiss: () -> Unit,
    onLaunchStream: (StreamItemEntity) -> Unit,
    onOpenExternal: (StreamItemEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CinemaMidnight,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .border(1.5.dp, NeonCyan, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${match.strHomeTeam} vs ${match.strAwayTeam}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${match.strLeague} • Kickoff: ${match.localKickoffTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Watch on Channel: ${match.suggestedChannels.joinToString(" • ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (matchedChannels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CinemaSurface, RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching sports channels (beIN, SSC, DAZN, Canal+, Arryadia, Sky Sports) found in current playlist cache.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = matchedChannels,
                            key = { it.id }
                        ) { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cinemaDpadFocusable(
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = { onLaunchStream(channel) }
                                    )
                                    .background(CinemaSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = channel.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = channel.resolutionBadge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldSuccess,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Text(
                                        text = channel.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .cinemaDpadFocusable(
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = { onLaunchStream(channel) }
                                            )
                                            .background(NeonCyan, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
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
                                                onClick = { onOpenExternal(channel) }
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
}
