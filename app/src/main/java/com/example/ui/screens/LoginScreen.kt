package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.LoginMode
import com.example.model.getLocalizedStrings
import com.example.ui.components.LanguageSwitcherRow
import com.example.ui.components.LiveClockTimezoneBadge
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
import com.example.viewmodel.NevadaCineUiState

@Composable
fun LoginScreen(
    uiState: NevadaCineUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectMode: (LoginMode) -> Unit,
    onLocalFilePicked: (Uri?, String) -> Unit,
    onUpdateXtream: (server: String?, user: String?, pass: String?) -> Unit,
    onClearChangeAccount: () -> Unit = {},
    onSmartPasteLink: (String) -> Boolean,
    onUpdateM3uUrl: (String) -> Unit,
    onFillPublicM3uUrl: () -> Unit,
    onConnectAndCache: () -> Unit
) {
    val strings = getLocalizedStrings(uiState.language)
    val context = LocalContext.current
    val composeClipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val connectButtonFocusRequester = remember { FocusRequester() }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            connectButtonFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    fun readClipboardText(): String {
        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val sysText = sysClipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (!sysText.isNullOrBlank()) return sysText.trim()
        return composeClipboard.getText()?.text?.trim().orEmpty()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var displayName = uri.lastPathSegment ?: "playlist.m3u"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
            onLocalFilePicked(uri, displayName)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
    ) {
        // Subtle Dark Cinema Hero Backdrop with Gradient Scrim
        Image(
            painter = painterResource(id = R.drawable.img_hero_cinema_1794932283013),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.22f,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CinemaObsidian.copy(alpha = 0.82f),
                            CinemaObsidian.copy(alpha = 0.94f),
                            CinemaObsidian
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Utility Bar: Clock/Timezone + Language Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveClockTimezoneBadge()
                LanguageSwitcherRow(
                    selectedLanguage = uiState.language,
                    onSelectLanguage = onSelectLanguage
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Center Login Card (TV & Mobile Adaptive)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CinemaMidnight.copy(alpha = 0.95f),
                                CinemaObsidian.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.5f), CinemaGold.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: NC+ Vector Logo + NEVADA CINE PLUS — powered by BE STREAM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NcPlusVectorLogo(size = 62.dp)
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = NeonCyan, fontWeight = FontWeight.Bold)) {
                                    append("NEVADA ")
                                }
                                withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                                    append("CINE ")
                                }
                                withStyle(SpanStyle(color = CinemaGold, fontWeight = FontWeight.Bold)) {
                                    append("PLUS")
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = strings.poweredBy,
                            style = MaterialTheme.typography.labelLarge,
                            color = CinemaGold,
                            letterSpacing = 1.4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Strictly 3 Input Modes Only: 1. Playlist, 2. Xtream Codes, 3. URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaObsidian, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple(LoginMode.PLAYLIST, strings.modePlaylist, Icons.Default.UploadFile),
                        Triple(LoginMode.XTREAM, strings.modeXtream, Icons.Default.Dns),
                        Triple(LoginMode.URL, strings.modeUrl, Icons.Default.Link)
                    )

                    modes.forEach { (mode, label, icon) ->
                        val selected = uiState.loginMode == mode
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("login_mode_${mode.name.lowercase()}")
                                .cinemaDpadFocusable(
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { onSelectMode(mode) }
                                )
                                .background(
                                    brush = if (selected) {
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.22f), CinemaGold.copy(alpha = 0.18f))
                                        )
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (selected) 1.5.dp else 0.dp,
                                    color = if (selected) NeonCyan else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mode-Specific Input Form
                when (uiState.loginMode) {
                    LoginMode.PLAYLIST -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CinemaSurface.copy(alpha = 0.7f))
                                .border(
                                    width = 1.dp,
                                    color = if (uiState.selectedLocalFileUri != null) NeonCyan else BorderSubtle,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.uploadFileTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.uploadFileSub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .testTag("pick_m3u_file_button")
                                    .cinemaDpadFocusable(
                                        shape = RoundedCornerShape(12.dp),
                                        onClick = {
                                            filePickerLauncher.launch(arrayOf("*/*"))
                                        }
                                    )
                                    .background(CinemaSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = NeonCyan
                                    )
                                    Text(
                                        text = if (uiState.selectedLocalFileName.isNotBlank()) {
                                            "Selected: ${uiState.selectedLocalFileName}"
                                        } else {
                                            strings.chooseFileBtn
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    LoginMode.XTREAM -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Smart Auto-Fill / One-Click Paste Parser Box above Xtream Codes Form
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                CinemaSurfaceElevated.copy(alpha = 0.9f),
                                                CinemaSurface.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.65f), CinemaGold.copy(alpha = 0.65f))
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = null,
                                            tint = CinemaGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = strings.smartPasteTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CinemaGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Prominent One-Click "Paste from Clipboard" Button
                                    Box(
                                        modifier = Modifier
                                            .testTag("smart_paste_clipboard_button")
                                            .minimumInteractiveComponentSize()
                                            .cinemaDpadFocusable(
                                                shape = RoundedCornerShape(10.dp),
                                                accentColor = NeonCyan,
                                                onClick = {
                                                    val clipText = readClipboardText()
                                                    if (clipText.isNotBlank()) {
                                                        onSmartPasteLink(clipText)
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Clipboard is empty",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            )
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(NeonCyan, Color(0xFF0099CC))
                                                ),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = strings.pasteFromClipboard,
                                                tint = CinemaObsidian,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = strings.pasteFromClipboard,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = CinemaObsidian,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                // Smart Paste Input Field (auto-parses full M3U URL on paste/type)
                                OutlinedTextField(
                                    value = uiState.smartPasteInput,
                                    onValueChange = { text ->
                                        onSmartPasteLink(text)
                                    },
                                    placeholder = {
                                        Text(
                                            text = strings.smartPasteHint,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = NeonCyan
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.smartPasteInput.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onSmartPasteLink("") },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    colors = cinemaTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("smart_paste_input")
                                )

                                AnimatedVisibility(visible = uiState.smartParseAutoFilled) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EmeraldSuccess.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = strings.smartParseSuccess,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Pre-filled BE STREAM Account Status + "Clear / Change Account" Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "BE STREAM Server Ready",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmeraldSuccess
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .testTag("clear_change_account_button")
                                        .cinemaDpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            accentColor = CinemaGold,
                                            onClick = onClearChangeAccount
                                        )
                                        .background(CinemaSurface, RoundedCornerShape(8.dp))
                                        .border(1.dp, CinemaGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = strings.clearChangeAccountBtn,
                                            tint = CinemaGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = strings.clearChangeAccountBtn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 2. Server URL Input + Dedicated TV Remote Paste Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.xtreamServerUrl,
                                    onValueChange = { input ->
                                        // If user pastes a full M3U URL directly into Server URL field, smart-parse it!
                                        if (input.contains("username=", ignoreCase = true) &&
                                            input.contains("password=", ignoreCase = true)
                                        ) {
                                            onSmartPasteLink(input)
                                        } else {
                                            onUpdateXtream(input, null, null)
                                        }
                                    },
                                    label = { Text(strings.serverUrlLabel) },
                                    placeholder = { Text("http://domain.com:80") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Dns, contentDescription = null, tint = NeonCyan)
                                    },
                                    trailingIcon = {
                                        if (uiState.xtreamServerUrl.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onUpdateXtream("", null, null) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    colors = cinemaTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("xtream_server_input")
                                )

                                FieldPasteActionButton(
                                    label = strings.pasteBtn,
                                    testTag = "paste_server_url_button",
                                    onClick = {
                                        val clip = readClipboardText()
                                        if (clip.isNotBlank()) {
                                            if (!onSmartPasteLink(clip)) {
                                                onUpdateXtream(clip, null, null)
                                            }
                                        }
                                    }
                                )
                            }

                            // 3. Username Input + Dedicated TV Remote Paste Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.xtreamUsername,
                                    onValueChange = { onUpdateXtream(null, it, null) },
                                    label = { Text(strings.usernameLabel) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = CinemaGold)
                                    },
                                    trailingIcon = {
                                        if (uiState.xtreamUsername.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onUpdateXtream(null, "", null) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    colors = cinemaTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("xtream_username_input")
                                )

                                FieldPasteActionButton(
                                    label = strings.pasteBtn,
                                    testTag = "paste_username_button",
                                    onClick = {
                                        val clip = readClipboardText()
                                        if (clip.isNotBlank()) {
                                            if (!onSmartPasteLink(clip)) {
                                                onUpdateXtream(null, clip, null)
                                            }
                                        }
                                    }
                                )
                            }

                            // 4. Password Input + Dedicated TV Remote Paste Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.xtreamPassword,
                                    onValueChange = { onUpdateXtream(null, null, it) },
                                    label = { Text(strings.passwordLabel) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = CinemaGold)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { showPassword = !showPassword },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            onConnectAndCache()
                                        }
                                    ),
                                    singleLine = true,
                                    colors = cinemaTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("xtream_password_input")
                                )

                                FieldPasteActionButton(
                                    label = strings.pasteBtn,
                                    testTag = "paste_password_button",
                                    onClick = {
                                        val clip = readClipboardText()
                                        if (clip.isNotBlank()) {
                                            if (!onSmartPasteLink(clip)) {
                                                onUpdateXtream(null, null, clip)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    LoginMode.URL -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.directM3uUrl,
                                    onValueChange = onUpdateM3uUrl,
                                    label = { Text(strings.m3uUrlLabel) },
                                    placeholder = { Text("https://example.com/playlist.m3u8") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = NeonCyan)
                                    },
                                    trailingIcon = {
                                        if (uiState.directM3uUrl.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onUpdateM3uUrl("") },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = false,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            onConnectAndCache()
                                        }
                                    ),
                                    colors = cinemaTextFieldColors(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("direct_m3u_url_input")
                                )

                                FieldPasteActionButton(
                                    label = strings.pasteBtn,
                                    testTag = "paste_m3u_url_button",
                                    onClick = {
                                        val clip = readClipboardText()
                                        if (clip.isNotBlank()) {
                                            onUpdateM3uUrl(clip)
                                        }
                                    }
                                )
                            }

                            // Helper pill to fill public global FTA M3U URL for instant testing
                            Box(
                                modifier = Modifier
                                    .testTag("fill_public_m3u_button")
                                    .cinemaDpadFocusable(
                                        shape = RoundedCornerShape(10.dp),
                                        accentColor = CinemaGold,
                                        onClick = onFillPublicM3uUrl
                                    )
                                    .background(CinemaSurface, RoundedCornerShape(10.dp))
                                    .border(1.dp, CinemaGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = strings.loadPublicIptvOrgUrl,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CinemaGold
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Message Banner
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(LiveCrimson.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, LiveCrimson, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = LiveCrimson
                        )
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                // Streaming Chunk Parser Progress Bar
                AnimatedVisibility(visible = uiState.isParsingOrConnecting) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(CinemaSurface, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = uiState.progressMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            color = CinemaGold,
                            trackColor = CinemaObsidian,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Primary CTA: Connect & Cache (Auto-focused on TV launch for 1-click OK login)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connect_and_cache_button")
                        .focusRequester(connectButtonFocusRequester)
                        .cinemaDpadFocusable(
                            shape = RoundedCornerShape(14.dp),
                            scaleOnFocus = 1.03f,
                            onClick = onConnectAndCache
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonCyan, Color(0xFF00A8E8), CinemaGold)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CinemaObsidian
                        )
                        Text(
                            text = strings.connectAndCache,
                            style = MaterialTheme.typography.titleLarge,
                            color = CinemaObsidian,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Large D-Pad & Touch Friendly Paste Button next to each input field for Android TV remotes
 */
@Composable
private fun FieldPasteActionButton(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .height(56.dp)
            .cinemaDpadFocusable(
                shape = RoundedCornerShape(12.dp),
                accentColor = NeonCyan,
                onClick = onClick
            )
            .background(CinemaSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = label,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun cinemaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = TextSecondary,
    cursorColor = NeonCyan,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CinemaSurface.copy(alpha = 0.65f),
    unfocusedContainerColor = CinemaSurface.copy(alpha = 0.45f)
)
