package com.example.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    FRENCH("fr", "Français", false),
    ARABIC("ar", "العربية", true)
}

enum class LoginMode {
    PLAYLIST, // 1. Local .m3u / .m3u8 file upload
    XTREAM,   // 2. Xtream Codes: Server URL, Username, Password
    URL       // 3. Direct M3U URL input
}

enum class MediaCategoryType {
    LIVE,
    MOVIE,
    SERIES
}

enum class NavSection {
    HOME,
    LIVE_TV,
    TODAYS_MATCHES,
    MOVIES,
    SERIES
}

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    SIXTEEN_NINE("16:9")
}

enum class PlayerEngineMode(val label: String) {
    NATIVE_EXO("HW+ Native (HLS / MPEG-TS)"),
    WEB_DUAL("HLS.js + mpegts.js Engine")
}

enum class SubtitleFontSize(val label: String, val sizeSp: Float) {
    NORMAL("Normal", 18f),
    LARGE_TV("Large TV", 24f),
    EXTRA_LARGE("XL Cinema", 30f)
}

enum class SubtitleBgStyle(val label: String) {
    SEMI_DARK("Semi-Dark Box"),
    SOLID_BLACK("Solid Black"),
    OUTLINE_ONLY("Outline Only")
}

data class SubtitlePreferenceState(
    val enabled: Boolean = true,
    val preferredLanguageCode: String = "ar", // "ar", "fr", "en", or "auto"
    val preferredLanguageLabel: String = "العربية / Auto",
    val externalSubtitleUri: Uri? = null,
    val externalSubtitleLabel: String = "",
    val fontSize: SubtitleFontSize = SubtitleFontSize.LARGE_TV,
    val bgStyle: SubtitleBgStyle = SubtitleBgStyle.SEMI_DARK
)

@Entity(
    tableName = "stream_items",
    indices = [
        Index(value = ["categoryType"]),
        Index(value = ["categoryId"]),
        Index(value = ["title"]),
        Index(value = ["addedTimestamp"])
    ]
)
data class StreamItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val streamUrl: String,
    val backupTsUrl: String = "",
    val logoUrl: String = "",
    val backdropUrl: String = "",
    val categoryId: String,
    val categoryName: String,
    val categoryType: String, // "LIVE", "MOVIE", "SERIES"
    val resolutionBadge: String = "FHD", // "4K UHD", "FHD", "HD", "HEVC"
    val streamFormat: String = "HLS", // "HLS (.m3u8)" or "MPEG-TS (.ts)"
    val description: String = "",
    val rating: String = "",
    val releaseYear: String = "",
    val addedTimestamp: Long = 0L,
    val isFavorite: Boolean = false
)

@Entity(
    tableName = "category_bouquets",
    primaryKeys = ["categoryId", "categoryType"]
)
data class CategoryBouquetEntity(
    val categoryId: String,
    val categoryName: String,
    val categoryType: String, // "LIVE", "MOVIE", "SERIES"
    val itemCount: Int
)

data class SportsMatch(
    val idEvent: String,
    val strEvent: String,
    val strLeague: String,
    val strHomeTeam: String,
    val strAwayTeam: String,
    val strHomeTeamBadge: String,
    val strAwayTeamBadge: String,
    val dateEvent: String,
    val utcTime: String,
    val localKickoffTime: String,
    val statusBadge: String, // "LIVE", "UPCOMING", "FT"
    val homeScore: String?,
    val awayScore: String?,
    val thumbUrl: String,
    val venue: String,
    val suggestedChannels: List<String>
)

data class VodMovieDetailsModel(
    val vodId: String,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String,
    val releaseYear: String,
    val duration: String,
    val genre: String,
    val ratingDisplay: String, // e.g., "8.4 / 10"
    val director: String,
    val castList: List<String>,
    val plot: String,
    val containerExtension: String,
    val resolutionBadge: String,
    val availableSubtitles: List<String>,
    val streamItem: StreamItemEntity
)

data class SeriesEpisodeItem(
    val id: String,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String,
    val duration: String,
    val plot: String,
    val thumbUrl: String,
    val streamItem: StreamItemEntity
)

data class SeriesSeasonGroup(
    val seasonNumber: Int,
    val seasonName: String,
    val episodes: List<SeriesEpisodeItem>
)

data class SeriesDetailsModel(
    val seriesId: String,
    val title: String,
    val coverUrl: String,
    val backdropUrl: String,
    val plot: String,
    val cast: String,
    val genre: String,
    val releaseDate: String,
    val rating: String,
    val seasons: List<SeriesSeasonGroup>
)

data class LocalizedStrings(
    val appTitle: String,
    val poweredBy: String,
    val modePlaylist: String,
    val modeXtream: String,
    val modeUrl: String,
    val uploadFileTitle: String,
    val uploadFileSub: String,
    val chooseFileBtn: String,
    val serverUrlLabel: String,
    val usernameLabel: String,
    val passwordLabel: String,
    val m3uUrlLabel: String,
    val connectAndCache: String,
    val loadPublicIptvOrgUrl: String,
    val navHome: String,
    val navLiveTv: String,
    val navMatches: String,
    val navMovies: String,
    val navSeries: String,
    val allBouquets: String,
    val bouquetsHeader: String,
    val searchHint: String,
    val watchNow: String,
    val openExternalPlayer: String,
    val matchesTitle: String,
    val matchesSub: String,
    val findMatchingChannels: String,
    val refreshFixtures: String,
    val switchSource: String,
    val cachedStreamsLabel: String,
    val viewportWindowInfo: String,
    val loadMoreBatch: String,
    val audioTrackLabel: String,
    val subtitlesLabel: String,
    val aspectRatioLabel: String,
    val engineLabel: String,
    val smartPasteTitle: String,
    val smartPasteHint: String,
    val pasteFromClipboard: String,
    val smartParseSuccess: String,
    val pasteBtn: String,
    val clearChangeAccountBtn: String,
    val recentlyAddedMoviesTitle: String,
    val playMovieBtn: String,
    val subtitlesSelectorBtn: String,
    val castAndCrewLabel: String,
    val directorLabel: String,
    val plotSynopsisLabel: String,
    val loadExternalSrtBtn: String
)

fun getLocalizedStrings(language: AppLanguage): LocalizedStrings {
    return when (language) {
        AppLanguage.ENGLISH -> LocalizedStrings(
            appTitle = "NEVADA CINE PLUS",
            poweredBy = "powered by BE STREAM",
            modePlaylist = "Playlist",
            modeXtream = "Xtream Codes",
            modeUrl = "URL",
            uploadFileTitle = "Local .M3U / .M3U8 Playlist File",
            uploadFileSub = "Hardware-accelerated chunk parser • Supports 50,000+ streams with zero UI freeze",
            chooseFileBtn = "Select .m3u / .m3u8 File",
            serverUrlLabel = "Server URL (e.g. http://line.server-dns.com:8080)",
            usernameLabel = "Username",
            passwordLabel = "Password",
            m3uUrlLabel = "Direct M3U / M3U8 URL",
            connectAndCache = "Connect & Cache",
            loadPublicIptvOrgUrl = "Fill Public FTA Global M3U URL (iptv-org)",
            navHome = "Home",
            navLiveTv = "Live TV",
            navMatches = "Today's Matches",
            navMovies = "Movies",
            navSeries = "Series",
            allBouquets = "All Bouquets",
            bouquetsHeader = "SERVER BOUQUETS",
            searchHint = "Search channels, movies, series, or sports...",
            watchNow = "Watch Stream",
            openExternalPlayer = "Open in External Player (MX Player / VLC)",
            matchesTitle = "Today's Live & Upcoming Matches",
            matchesSub = "Auto-synced via TheSportsDB API in your local timezone • Click any fixture for Smart Channel Matching",
            findMatchingChannels = "Smart Match Channels",
            refreshFixtures = "Refresh Today's Fixtures",
            switchSource = "Switch Server / Playlist",
            cachedStreamsLabel = "Cached in Local DB",
            viewportWindowInfo = "Virtual Window: 30 cards/batch",
            loadMoreBatch = "Load Next 30 Cards",
            audioTrackLabel = "Audio",
            subtitlesLabel = "Subtitles / الترجمة",
            aspectRatioLabel = "Aspect",
            engineLabel = "Engine",
            smartPasteTitle = "Smart Paste M3U / Xtream Link",
            smartPasteHint = "Paste full M3U URL (http://domain.com:80/get.php?username=USER&password=PASS...)",
            pasteFromClipboard = "Paste from Clipboard",
            smartParseSuccess = "Auto-filled Server URL, Username & Password!",
            pasteBtn = "Paste",
            clearChangeAccountBtn = "Clear / Change Account",
            recentlyAddedMoviesTitle = "Recently Added Movies • آخر الأفلام المضافة",
            playMovieBtn = "Play Movie",
            subtitlesSelectorBtn = "Subtitles / الترجمة",
            castAndCrewLabel = "Cast & Actors",
            directorLabel = "Director",
            plotSynopsisLabel = "Synopsis / Plot",
            loadExternalSrtBtn = "Load External .SRT / .VTT File"
        )
        AppLanguage.FRENCH -> LocalizedStrings(
            appTitle = "NEVADA CINE PLUS",
            poweredBy = "propulsé par BE STREAM",
            modePlaylist = "Playlist",
            modeXtream = "Xtream Codes",
            modeUrl = "URL",
            uploadFileTitle = "Fichier Playlist Local .M3U / .M3U8",
            uploadFileSub = "Analyseur par blocs ultra-rapide • Supporte 50 000+ flux sans blocage UI",
            chooseFileBtn = "Choisir un fichier .m3u / .m3u8",
            serverUrlLabel = "URL du Serveur (ex: http://line.server-dns.com:8080)",
            usernameLabel = "Nom d'utilisateur",
            passwordLabel = "Mot de passe",
            m3uUrlLabel = "Lien Direct M3U / M3U8",
            connectAndCache = "Connecter & Mettre en Cache",
            loadPublicIptvOrgUrl = "Remplir l'URL M3U Publique (iptv-org)",
            navHome = "Accueil",
            navLiveTv = "TV en Direct",
            navMatches = "Matchs du Jour",
            navMovies = "Films",
            navSeries = "Séries",
            allBouquets = "Tous les Bouquets",
            bouquetsHeader = "BOUQUETS SERVEUR",
            searchHint = "Rechercher chaînes, films, séries, bouquets...",
            watchNow = "Regarder en Direct",
            openExternalPlayer = "Ouvrir dans Lecteur Externe (MX Player / VLC)",
            matchesTitle = "Matchs du Jour en Direct & À Venir",
            matchesSub = "Synchronisé via TheSportsDB dans votre fuseau horaire • Cliquez pour trouver la chaîne sportive",
            findMatchingChannels = "Chaînes Sportives",
            refreshFixtures = "Actualiser les Matchs",
            switchSource = "Changer de Serveur / Playlist",
            cachedStreamsLabel = "En Cache Local",
            viewportWindowInfo = "Fenêtre Virtuelle: 30 cartes/lot",
            loadMoreBatch = "Charger 30 Cartes Suivantes",
            audioTrackLabel = "Audio",
            subtitlesLabel = "Sous-titres / الترجمة",
            aspectRatioLabel = "Format",
            engineLabel = "Moteur",
            smartPasteTitle = "Collage Intelligent Lien M3U / Xtream",
            smartPasteHint = "Collez le lien M3U complet (http://domain.com:80/get.php?username=USER&password=PASS...)",
            pasteFromClipboard = "Coller du Presse-papiers",
            smartParseSuccess = "URL du serveur, utilisateur et mot de passe remplis automatiquement !",
            pasteBtn = "Coller",
            clearChangeAccountBtn = "Effacer / Changer de Compte",
            recentlyAddedMoviesTitle = "Films Récemment Ajoutés • آخر الأفلام المضافة",
            playMovieBtn = "Lire le Film",
            subtitlesSelectorBtn = "Sous-titres / الترجمة",
            castAndCrewLabel = "Distribution & Acteurs",
            directorLabel = "Réalisateur",
            plotSynopsisLabel = "Synopsis / Résumé",
            loadExternalSrtBtn = "Charger un fichier .SRT / .VTT"
        )
        AppLanguage.ARABIC -> LocalizedStrings(
            appTitle = "NEVADA CINE PLUS",
            poweredBy = "مدعوم بواسطة BE STREAM",
            modePlaylist = "ملف M3U",
            modeXtream = "إكستريم كود",
            modeUrl = "رابط مباشر",
            uploadFileTitle = "رفع ملف القنوات المحلي .M3U / .M3U8",
            uploadFileSub = "معالج فائق السرعة بدون تجميد • يدعم أكثر من 50,000 قناة وفيلم",
            chooseFileBtn = "اختر ملف .m3u / .m3u8",
            serverUrlLabel = "رابط السيرفر (Server URL)",
            usernameLabel = "اسم المستخدم (Username)",
            passwordLabel = "كلمة المرور (Password)",
            m3uUrlLabel = "رابط M3U / M3U8 المباشر",
            connectAndCache = "اتصال وحفظ سريع",
            loadPublicIptvOrgUrl = "تعبئة رابط القنوات العالمية المفتوحة (iptv-org)",
            navHome = "الرئيسية",
            navLiveTv = "البث المباشر",
            navMatches = "مباريات اليوم",
            navMovies = "الأفلام",
            navSeries = "المسلسلات",
            allBouquets = "جميع الباقات",
            bouquetsHeader = "باقات السيرفر",
            searchHint = "ابحث عن القنوات، الأفلام، المسلسلات أو القنوات الرياضية...",
            watchNow = "مشاهدة الآن",
            openExternalPlayer = "فتح في مشغل خارجي (MX Player / VLC)",
            matchesTitle = "جدول مباريات اليوم المباشرة والقادمة",
            matchesSub = "محدث تلقائياً بتوقيت جهازك المحلي • اضغط على أي مباراة للبحث الذكي عن القنوات الناقلة",
            findMatchingChannels = "القنوات الناقلة الذكية",
            refreshFixtures = "تحديث المباريات",
            switchSource = "تغيير السيرفر / القائمة",
            cachedStreamsLabel = "محفوظ في الذاكرة الفورية",
            viewportWindowInfo = "العرض الذكي: 30 بطاقة لكل دفعة",
            loadMoreBatch = "عرض 30 بطاقة إضافية",
            audioTrackLabel = "الصوت",
            subtitlesLabel = "الترجمة / CC",
            aspectRatioLabel = "الأبعاد",
            engineLabel = "المشغل",
            smartPasteTitle = "اللصق الذكي لرابط M3U / إكستريم",
            smartPasteHint = "الصق رابط M3U الكامل (http://domain.com:80/get.php?username=USER&password=PASS...)",
            pasteFromClipboard = "لصق من الحافظة",
            smartParseSuccess = "تم استخراج وتعبئة السيرفر واسم المستخدم وكلمة المرور تلقائياً!",
            pasteBtn = "لصق",
            clearChangeAccountBtn = "مسح / تغيير الحساب",
            recentlyAddedMoviesTitle = "آخر الأفلام المضافة • Recently Added Movies",
            playMovieBtn = "تشغيل الفيلم",
            subtitlesSelectorBtn = "الترجمة / Subtitles",
            castAndCrewLabel = "طاقم التمثيل",
            directorLabel = "المخرج",
            plotSynopsisLabel = "قصة الفيلم",
            loadExternalSrtBtn = "تحميل ملف ترجمة خارجي .SRT / .VTT"
        )
    }
}
