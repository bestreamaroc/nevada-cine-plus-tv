package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Custom D-Pad Remote & Touch Focus modifier with:
 * - High-contrast glowing cyan (#00D2FF) focus border & outer shadow
 * - Smooth automatic scroll into view (`bringIntoViewRequester`) so D-Pad focus never clips off-screen
 * - Enter / DPAD_CENTER / GamePad A activation for Android TV & Mobile
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.cinemaDpadFocusable(
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    scaleOnFocus: Float = 1.04f,
    accentColor: Color = NeonCyan,
    onFocused: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleOnFocus else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "focus_scale"
    )

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .scale(scale)
        .onFocusChanged { focusState ->
            val nowFocused = focusState.isFocused
            if (nowFocused && !isFocused) {
                onFocused?.invoke()
                coroutineScope.launch {
                    try {
                        bringIntoViewRequester.bringIntoView()
                    } catch (_: Exception) {
                    }
                }
            }
            isFocused = nowFocused
        }
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown &&
                (event.key == Key.DirectionCenter ||
                    event.key == Key.Enter ||
                    event.key == Key.NumPadEnter ||
                    event.key == Key.ButtonA)
            ) {
                onClick()
                true
            } else {
                false
            }
        }
        .then(
            if (isFocused) {
                Modifier
                    .shadow(
                        elevation = 18.dp,
                        shape = shape,
                        ambientColor = accentColor,
                        spotColor = accentColor
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(NeonCyan, accentColor, CinemaGold)),
                        shape = shape
                    )
            } else {
                Modifier
            }
        )
        .clip(shape)
        .focusable()
        .clickable { onClick() }
}

/**
 * Crisp Vector SVG App Logo ("NC+") rendered via hardware-accelerated Compose Canvas
 */
@Composable
fun NcPlusVectorLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nc_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.32f * glowPulse),
                    CinemaGold.copy(alpha = 0.14f * glowPulse),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.62f
            )
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0A1022), CinemaObsidian)
            ),
            topLeft = Offset(w * 0.06f, h * 0.06f),
            size = Size(w * 0.88f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.24f, h * 0.24f)
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(NeonCyan, CinemaGold)
            ),
            topLeft = Offset(w * 0.06f, h * 0.06f),
            size = Size(w * 0.88f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.24f, h * 0.24f),
            style = Stroke(width = w * 0.035f)
        )

        val nPath = Path().apply {
            moveTo(w * 0.22f, h * 0.72f)
            lineTo(w * 0.22f, h * 0.28f)
            lineTo(w * 0.45f, h * 0.70f)
            lineTo(w * 0.45f, h * 0.28f)
        }
        drawPath(
            path = nPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFDF7A), CinemaGold, Color(0xFFC88210))
            ),
            style = Stroke(
                width = w * 0.075f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(NeonCyan, Color(0xFF60EFFF))
            ),
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.46f, h * 0.28f),
            size = Size(w * 0.34f, h * 0.44f),
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        )

        val playPath = Path().apply {
            moveTo(w * 0.59f, h * 0.43f)
            lineTo(w * 0.69f, h * 0.50f)
            lineTo(w * 0.59f, h * 0.57f)
            close()
        }
        drawPath(
            path = playPath,
            color = NeonCyan
        )

        val plusCenterX = w * 0.80f
        val plusCenterY = h * 0.50f
        val arm = w * 0.065f
        drawLine(
            color = NeonCyan,
            start = Offset(plusCenterX - arm, plusCenterY),
            end = Offset(plusCenterX + arm, plusCenterY),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = CinemaGold,
            start = Offset(plusCenterX, plusCenterY - arm),
            end = Offset(plusCenterX, plusCenterY + arm),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Automatic Local Timezone & Live Clock Display
 */
@Composable
fun LiveClockTimezoneBadge(modifier: Modifier = Modifier) {
    var timeString by remember { mutableStateOf("") }
    var timezoneString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val tz = TimeZone.getDefault()
        val offsetHours = tz.rawOffset / (1000 * 60 * 60)
        val sign = if (offsetHours >= 0) "+" else ""
        timezoneString = "${tz.id} (GMT$sign$offsetHours)"

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            timeString = sdf.format(Date())
            delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .background(CinemaSurface.copy(alpha = 0.85f), RoundedCornerShape(50))
            .border(1.dp, BorderSubtle, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Local Time",
            tint = NeonCyan,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = "•",
            color = CinemaGold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = timezoneString,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Clean Language Switcher (English, Français, العربية)
 */
@Composable
fun LanguageSwitcherRow(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(CinemaMidnight, RoundedCornerShape(50))
            .border(1.dp, BorderSubtle, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selectedLanguage
            Box(
                modifier = Modifier
                    .cinemaDpadFocusable(
                        shape = RoundedCornerShape(50),
                        scaleOnFocus = 1.05f,
                        onClick = { onSelectLanguage(lang) }
                    )
                    .background(
                        brush = if (isSelected) {
                            Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0099CC)))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        },
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) CinemaObsidian else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * EveryCine Dark-Cinema Splash Screen (#060913)
 */
@Composable
fun NevadaSplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1B38),
                        CinemaObsidian,
                        Color(0xFF03050B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            NcPlusVectorLogo(
                size = 116.dp,
                modifier = Modifier.scale(pulseScale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("NEVADA ")
                    }
                    withStyle(
                        SpanStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("CINE ")
                    }
                    withStyle(
                        SpanStyle(
                            color = CinemaGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("PLUS")
                    }
                },
                style = MaterialTheme.typography.displayMedium,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(CinemaSurfaceElevated.copy(alpha = 0.7f), CircleShape)
                    .border(1.dp, CinemaGold.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(NeonCyan, CircleShape)
                )
                Text(
                    text = "powered by BE STREAM",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaGold,
                    letterSpacing = 1.8.sp
                )
            }
        }
    }
}
