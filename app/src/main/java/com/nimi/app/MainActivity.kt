package com.nimi.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.location.Address
import android.location.Geocoder
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Patterns
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================================
// 1. REPLACEABLE OPEN-SOURCE INFRASTRUCTURE CONFIGURATION
// ============================================================================

private enum class OpenFreeMapStyle(val label: String, val styleUrl: String, val isDark: Boolean) {
    LIBERTY("Liberty 3D", "https://tiles.openfreemap.org/styles/liberty", false),
    POSITRON("Positron Light", "https://tiles.openfreemap.org/styles/positron", false),
    BRIGHT("Bright Day", "https://tiles.openfreemap.org/styles/bright", false),
    DARK("Night Dark", "https://tiles.openfreemap.org/styles/dark", true),
    FIORD("Fiord Slate", "https://tiles.openfreemap.org/styles/fiord", true)
}

private data class InfrastructureConfig(
    val activeStyle: OpenFreeMapStyle = OpenFreeMapStyle.LIBERTY,
    val customTileStyleUrl: String = "",
    val osrmBaseUrl: String = "https://router.project-osrm.org",
    val photonBaseUrl: String = "https://photon.komoot.io",
    val overpassUrl: String = "https://overpass-api.de/api/interpreter",
    val nimiBackendUrl: String = "" // Optional self-hosted PostGIS + WebSocket/REST backend
) {
    val resolvedStyleUrl: String
        get() = customTileStyleUrl.ifBlank { activeStyle.styleUrl }
}

// ============================================================================
// 2. HIGH-CRAFT EDITORIAL DESIGN SYSTEM
// ============================================================================

private object NimiDesign {
    val Canvas = Color(0xFFF5F4F0)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceRecessed = Color(0xFFEBE9E2)
    val Obsidian = Color(0xFF0F1115)
    val Charcoal = Color(0xFF1C1F26)
    val Slate = Color(0xFF525866)
    val Muted = Color(0xFF8A909E)
    val Hairline = Color(0xFFDFDDD4)

    val Crimson = Color(0xFFE11D48)
    val CrimsonSoft = Color(0xFFFFE4E8)
    val Emerald = Color(0xFF059669)
    val EmeraldSoft = Color(0xFFD1FAE5)
    val Cobalt = Color(0xFF2563EB)
    val CobaltSoft = Color(0xFFDBEAFE)
    val Amber = Color(0xFFD97706)
    val AmberSoft = Color(0xFFFEF3C7)
    val Violet = Color(0xFF7C3AED)
    val VioletSoft = Color(0xFFEDE9FE)
}

private val NimiTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 28.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.5).sp,
        fontWeight = FontWeight.Bold,
        color = NimiDesign.Obsidian
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
        fontWeight = FontWeight.Bold,
        color = NimiDesign.Obsidian
    ),
    titleLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Bold,
        color = NimiDesign.Obsidian
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.SemiBold,
        color = NimiDesign.Obsidian
    ),
    bodyLarge = TextStyle(
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = NimiDesign.Slate
    ),
    bodyMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = NimiDesign.Slate
    ),
    labelMedium = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = NimiDesign.Muted
    )
)

@Composable
private fun NimiTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = NimiDesign.Crimson,
        onPrimary = Color.White,
        secondary = NimiDesign.Obsidian,
        onSecondary = Color.White,
        background = NimiDesign.Canvas,
        onBackground = NimiDesign.Obsidian,
        surface = NimiDesign.Surface,
        onSurface = NimiDesign.Obsidian,
        surfaceVariant = NimiDesign.SurfaceRecessed,
        onSurfaceVariant = NimiDesign.Slate,
        outline = NimiDesign.Hairline,
        error = NimiDesign.Crimson
    )
    MaterialTheme(colorScheme = scheme, typography = NimiTypography, content = content)
}

private fun Modifier.tactilePress(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tactileScale"
    )
    this
        .scale(scale)
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Composable
private fun TactileInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    iconTint: Color = NimiDesign.Slate,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onAction: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NimiDesign.SurfaceRecessed)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(leadingIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 13.sp, color = NimiDesign.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NimiDesign.Obsidian),
                cursorBrush = SolidColor(NimiDesign.Crimson),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onAny = { onAction() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        trailingContent?.invoke()
    }
}

// ============================================================================
// 3. DOMAIN MODELS (ZERO FABRICATED DATA)
// ============================================================================

private data class GnssFix(
    val lat: Double,
    val lon: Double,
    val accuracyMeters: Float,
    val provider: String,
    val fixTimestampMillis: Long,
    val elapsedRealtimeNanos: Long,
    val satellitesUsed: Int
) {
    val ageSeconds: Long
        get() = ((SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000_000L).coerceAtLeast(0L)

    val isFresh: Boolean
        get() = ageSeconds <= 30L
}

private enum class FlagType(
    val code: String,
    val label: String,
    val hexColor: String,
    val composeColor: Color,
    val softBg: Color,
    val isSafeHaven: Boolean
) {
    SAFE_HAVEN("SAFE", "Safe Haven (24/7)", "#059669", NimiDesign.Emerald, NimiDesign.EmeraldSoft, true),
    POOR_LIGHT("LIGHT", "Dark / Unlit Street", "#D97706", NimiDesign.Amber, NimiDesign.AmberSoft, false),
    HARASSMENT("HARASS", "Harassment Spot", "#E11D48", NimiDesign.Crimson, NimiDesign.CrimsonSoft, false),
    CROWD("CROWD", "Unsafe Gathering", "#7C3AED", NimiDesign.Violet, NimiDesign.VioletSoft, false);

    companion object {
        fun fromCode(code: String): FlagType = entries.find { it.code == code } ?: POOR_LIGHT
    }
}

private enum class OsmPoiCategory(
    val code: String,
    val label: String,
    val hexColor: String,
    val composeColor: Color
) {
    POLICE("police", "Police Station", "#2563EB", NimiDesign.Cobalt),
    HOSPITAL("hospital", "Hospital / Emergency", "#E11D48", NimiDesign.Crimson),
    PHARMACY("pharmacy", "Pharmacy / Medical", "#059669", NimiDesign.Emerald)
}

private data class OsmEmergencyPoi(
    val osmId: Long,
    val name: String,
    val category: OsmPoiCategory,
    val lat: Double,
    val lon: Double,
    val phone: String?,
    val openingHours: String?,
    val operatorName: String?,
    val address: String?
)

private enum class TravelMode(
    val label: String,
    val icon: ImageVector,
    val osrmProfile: String,
    val avgSpeedKmh: Double
) {
    CAR("Car / Cab", Icons.Outlined.DirectionsCar, "driving", 28.0),
    BIKE("Bike / Scooty", Icons.Outlined.TwoWheeler, "bike", 24.0),
    WALK("Walk", Icons.Outlined.DirectionsWalk, "foot", 4.8)
}

private data class PlaceSuggestion(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lon: Double
)

private data class UserAccount(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String
)

private data class SpatialPin(
    val id: Long,
    val title: String,
    val note: String,
    val type: FlagType,
    val lat: Double,
    val lon: Double,
    val confirmations: Int,
    val reporterEmail: String,
    val createdAt: String
)

private data class EmergencyContact(
    val id: Long,
    val ownerEmail: String,
    val name: String,
    val phone: String
)

private enum class SosDeliveryState(val label: String, val badgeColor: Color) {
    QUEUED_LOCALLY("Saved Locally (Pending Network)", NimiDesign.Amber),
    SMS_HANDOFF_OPENED("SMS App Opened (Verify Send)", NimiDesign.Cobalt),
    CONFIRMED_BY_BACKEND("Confirmed by Nimi Server", NimiDesign.Emerald),
    BACKEND_RETRY_PENDING("Server Unreachable • Retrying", NimiDesign.Crimson)
}

private data class SosOutboxEvent(
    val id: Long,
    val userName: String,
    val userEmail: String,
    val lat: Double?,
    val lon: Double?,
    val accuracyMeters: Float?,
    val fixAgeSeconds: Long?,
    val createdAt: String,
    val deliveryState: SosDeliveryState,
    val retryCount: Int
)

private data class RouteSegment(
    val points: List<LatLng>,
    val isDangerZone: Boolean
)

private data class RouteResult(
    val name: String,
    val allPoints: List<LatLng>,
    val segments: List<RouteSegment>,
    val distanceKm: Double,
    val durationMinutes: Map<TravelMode, Int>,
    val flagsOnRoute: List<SpatialPin>,
    val isSaferBypass: Boolean
)

private data class EvidenceItem(
    val displayName: String,
    val sizeBytes: Long?,
    val sha256: String,
    val sourceUri: String
)

// ============================================================================
// 4. SQLITE PERSISTENCE & OUTBOX QUEUE (ZERO SEEDED FAKE DATA)
// ============================================================================

private class NimiDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "nimi_foss_v6.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE COLLATE NOCASE,
                phone TEXT NOT NULL,
                password_hash TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE map_pins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                note TEXT NOT NULL,
                type TEXT NOT NULL,
                lat REAL NOT NULL,
                lon REAL NOT NULL,
                confirmations INTEGER NOT NULL DEFAULT 1,
                reporter_email TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE emergency_contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_email TEXT NOT NULL,
                name TEXT NOT NULL,
                phone TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE sos_outbox (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_name TEXT NOT NULL,
                user_email TEXT NOT NULL,
                lat REAL,
                lon REAL,
                accuracy REAL,
                fix_age_sec INTEGER,
                created_at TEXT NOT NULL,
                delivery_state TEXT NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE osm_poi_cache (
                osm_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                lat REAL NOT NULL,
                lon REAL NOT NULL,
                phone TEXT,
                opening_hours TEXT,
                operator_name TEXT,
                address TEXT,
                cached_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE geocode_cache (
                `query` TEXT PRIMARY KEY COLLATE NOCASE,
                payload_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun hashPassword(email: String, rawPassword: String): String {
        val salted = "${email.lowercase(Locale.ROOT).trim()}:$rawPassword:nimi_foss_salt"
        return MessageDigest.getInstance("SHA-256")
            .digest(salted.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun emailExists(email: String): Boolean {
        readableDatabase.rawQuery("SELECT id FROM users WHERE email = ?", arrayOf(email.trim()))
            .use { c ->
                return c.moveToFirst()
            }
    }

    fun registerUser(name: String, email: String, phone: String, rawPass: String): UserAccount? {
        val cleanEmail = email.trim().lowercase(Locale.ROOT)
        if (emailExists(cleanEmail)) return null
        val cv = ContentValues().apply {
            put("name", name.trim())
            put("email", cleanEmail)
            put("phone", phone.trim())
            put("password_hash", hashPassword(cleanEmail, rawPass))
        }
        val id = writableDatabase.insert("users", null, cv)
        return if (id != -1L) UserAccount(id, name.trim(), cleanEmail, phone.trim()) else null
    }

    fun loginUser(email: String, rawPass: String): UserAccount? {
        val cleanEmail = email.trim().lowercase(Locale.ROOT)
        val hashed = hashPassword(cleanEmail, rawPass)
        readableDatabase.rawQuery(
            "SELECT id, name, email, phone FROM users WHERE email = ? AND password_hash = ?",
            arrayOf(cleanEmail, hashed)
        ).use { c ->
            if (c.moveToFirst()) return UserAccount(
                c.getLong(0),
                c.getString(1),
                c.getString(2),
                c.getString(3)
            )
        }
        return null
    }

    fun getUserByEmail(email: String): UserAccount? {
        readableDatabase.rawQuery(
            "SELECT id, name, email, phone FROM users WHERE email = ?",
            arrayOf(email.trim().lowercase(Locale.ROOT))
        ).use { c ->
            if (c.moveToFirst()) return UserAccount(
                c.getLong(0),
                c.getString(1),
                c.getString(2),
                c.getString(3)
            )
        }
        return null
    }

    fun getAllPins(): List<SpatialPin> {
        val list = mutableListOf<SpatialPin>()
        readableDatabase.rawQuery(
            "SELECT id, title, note, type, lat, lon, confirmations, reporter_email, created_at FROM map_pins ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    SpatialPin(
                        id = c.getLong(0),
                        title = c.getString(1),
                        note = c.getString(2),
                        type = FlagType.fromCode(c.getString(3)),
                        lat = c.getDouble(4),
                        lon = c.getDouble(5),
                        confirmations = c.getInt(6),
                        reporterEmail = c.getString(7),
                        createdAt = c.getString(8)
                    )
                )
            }
        }
        return list
    }

    fun insertPin(
        title: String,
        note: String,
        type: FlagType,
        lat: Double,
        lon: Double,
        reporterEmail: String
    ): SpatialPin {
        val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date())
        val cv = ContentValues().apply {
            put("title", title)
            put("note", note)
            put("type", type.code)
            put("lat", lat)
            put("lon", lon)
            put("confirmations", 1)
            put("reporter_email", reporterEmail)
            put("created_at", now)
        }
        val id = writableDatabase.insert("map_pins", null, cv)
        return SpatialPin(id, title, note, type, lat, lon, 1, reporterEmail, now)
    }

    fun confirmPin(id: Long) {
        writableDatabase.execSQL(
            "UPDATE map_pins SET confirmations = confirmations + 1 WHERE id = ?",
            arrayOf(id)
        )
    }

    fun deletePin(id: Long) {
        writableDatabase.delete("map_pins", "id = ?", arrayOf(id.toString()))
    }

    fun getContactsForUser(email: String): List<EmergencyContact> {
        val list = mutableListOf<EmergencyContact>()
        readableDatabase.rawQuery(
            "SELECT id, owner_email, name, phone FROM emergency_contacts WHERE owner_email = ? ORDER BY id DESC",
            arrayOf(email)
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    EmergencyContact(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3)
                    )
                )
            }
        }
        return list
    }

    fun addContact(ownerEmail: String, name: String, phone: String): EmergencyContact {
        val cv = ContentValues().apply {
            put("owner_email", ownerEmail)
            put("name", name)
            put("phone", phone)
        }
        val id = writableDatabase.insert("emergency_contacts", null, cv)
        return EmergencyContact(id, ownerEmail, name, phone)
    }

    fun deleteContact(id: Long) {
        writableDatabase.delete("emergency_contacts", "id = ?", arrayOf(id.toString()))
    }

    fun enqueueSosEvent(
        user: UserAccount,
        fix: GnssFix?,
        initialState: SosDeliveryState
    ): SosOutboxEvent {
        val now = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.US).format(Date())
        val cv = ContentValues().apply {
            put("user_name", user.name)
            put("user_email", user.email)
            if (fix != null) {
                put("lat", fix.lat)
                put("lon", fix.lon)
                put("accuracy", fix.accuracyMeters)
                put("fix_age_sec", fix.ageSeconds)
            }
            put("created_at", now)
            put("delivery_state", initialState.name)
            put("retry_count", 0)
        }
        val id = writableDatabase.insert("sos_outbox", null, cv)
        return SosOutboxEvent(
            id = id,
            userName = user.name,
            userEmail = user.email,
            lat = fix?.lat,
            lon = fix?.lon,
            accuracyMeters = fix?.accuracyMeters,
            fixAgeSeconds = fix?.ageSeconds,
            createdAt = now,
            deliveryState = initialState,
            retryCount = 0
        )
    }

    fun updateSosDeliveryState(id: Long, state: SosDeliveryState, retryCount: Int) {
        val cv = ContentValues().apply {
            put("delivery_state", state.name)
            put("retry_count", retryCount)
        }
        writableDatabase.update("sos_outbox", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getSosOutboxEvents(userEmail: String): List<SosOutboxEvent> {
        val list = mutableListOf<SosOutboxEvent>()
        readableDatabase.rawQuery(
            "SELECT id, user_name, user_email, lat, lon, accuracy, fix_age_sec, created_at, delivery_state, retry_count FROM sos_outbox WHERE user_email = ? ORDER BY id DESC LIMIT 20",
            arrayOf(userEmail)
        ).use { c ->
            while (c.moveToNext()) {
                val state = runCatching { SosDeliveryState.valueOf(c.getString(8)) }
                    .getOrDefault(SosDeliveryState.QUEUED_LOCALLY)
                list.add(
                    SosOutboxEvent(
                        id = c.getLong(0),
                        userName = c.getString(1),
                        userEmail = c.getString(2),
                        lat = if (c.isNull(3)) null else c.getDouble(3),
                        lon = if (c.isNull(4)) null else c.getDouble(4),
                        accuracyMeters = if (c.isNull(5)) null else c.getFloat(5),
                        fixAgeSeconds = if (c.isNull(6)) null else c.getLong(6),
                        createdAt = c.getString(7),
                        deliveryState = state,
                        retryCount = c.getInt(9)
                    )
                )
            }
        }
        return list
    }

    fun cacheOsmPois(pois: List<OsmEmergencyPoi>) {
        val now = System.currentTimeMillis()
        writableDatabase.beginTransaction()
        try {
            pois.forEach { poi ->
                val cv = ContentValues().apply {
                    put("osm_id", poi.osmId)
                    put("name", poi.name)
                    put("category", poi.category.code)
                    put("lat", poi.lat)
                    put("lon", poi.lon)
                    put("phone", poi.phone)
                    put("opening_hours", poi.openingHours)
                    put("operator_name", poi.operatorName)
                    put("address", poi.address)
                    put("cached_at", now)
                }
                writableDatabase.insertWithOnConflict(
                    "osm_poi_cache",
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun getCachedOsmPoisNear(
        lat: Double,
        lon: Double,
        maxRadiusMeters: Double = 6000.0
    ): List<OsmEmergencyPoi> {
        val list = mutableListOf<OsmEmergencyPoi>()
        readableDatabase.rawQuery(
            "SELECT osm_id, name, category, lat, lon, phone, opening_hours, operator_name, address FROM osm_poi_cache",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val pLat = c.getDouble(3)
                val pLon = c.getDouble(4)
                if (haversineMeters(lat, lon, pLat, pLon) <= maxRadiusMeters) {
                    val catCode = c.getString(2)
                    val cat =
                        OsmPoiCategory.entries.find { it.code == catCode } ?: OsmPoiCategory.POLICE
                    list.add(
                        OsmEmergencyPoi(
                            osmId = c.getLong(0),
                            name = c.getString(1),
                            category = cat,
                            lat = pLat,
                            lon = pLon,
                            phone = c.getString(5),
                            openingHours = c.getString(6),
                            operatorName = c.getString(7),
                            address = c.getString(8)
                        )
                    )
                }
            }
        }
        return list
    }

    // 7 days in milliseconds
    private val GEOCODE_CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000

    fun getCachedGeocode(searchQuery: String): List<PlaceSuggestion>? {
        val cleanQuery = searchQuery.trim().lowercase(Locale.ROOT)
        if (cleanQuery.isEmpty()) return null

        val cutoffTime = System.currentTimeMillis() - GEOCODE_CACHE_TTL_MS

        readableDatabase.rawQuery(
            "SELECT payload_json, updated_at FROM geocode_cache WHERE `query` = ?",
            arrayOf(cleanQuery)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val updatedAt = cursor.getLong(1)
                // Cache Expiry Check: Evict stale records immediately
                if (updatedAt < cutoffTime) {
                    writableDatabase.delete("geocode_cache", "`query` = ?", arrayOf(cleanQuery))
                    return null
                }

                val payloadJson = cursor.getString(0)
                return try {
                    val arr = JSONArray(payloadJson)
                    val suggestions = mutableListOf<PlaceSuggestion>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        suggestions.add(
                            PlaceSuggestion(
                                title = obj.getString("t"),
                                subtitle = obj.optString("s", ""),
                                lat = obj.getDouble("lat"),
                                lon = obj.getDouble("lon")
                            )
                        )
                    }
                    suggestions
                } catch (e: Exception) {
                    // Self-healing: Delete corrupted JSON rows
                    writableDatabase.delete("geocode_cache", "`query` = ?", arrayOf(cleanQuery))
                    null
                }
            }
        }
        return null
    }

    fun saveCachedGeocode(searchQuery: String, suggestions: List<PlaceSuggestion>) {
        if (suggestions.isEmpty()) return
        val cleanQuery = searchQuery.trim().lowercase(Locale.ROOT)
        if (cleanQuery.length < 2) return // Never cache single-character debounce noise

        try {
            val arr = JSONArray()
            for (s in suggestions) {
                val obj = JSONObject().apply {
                    put("t", s.title)
                    put("s", s.subtitle)
                    put("lat", s.lat)
                    put("lon", s.lon)
                }
                arr.put(obj)
            }

            val cv = ContentValues().apply {
                put("`query`", cleanQuery)
                put("payload_json", arr.toString())
                put("updated_at", System.currentTimeMillis())
            }

            writableDatabase.insertWithOnConflict(
                "geocode_cache",
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            // Bound cache size: Keep only the 250 most recently updated entries
            writableDatabase.execSQL(
                """
                DELETE FROM geocode_cache 
                WHERE `query` NOT IN (
                    SELECT `query` FROM geocode_cache 
                    ORDER BY updated_at DESC 
                    LIMIT 250
                )
                """.trimIndent()
            )
        } catch (e: Exception) {
            // Non-fatal: Log or fail silently so search UX is not interrupted
        }
    }
}

// ============================================================================
// 5. NATIVE ANDROID GNSS TRACKER (ZERO GOOGLE PLAY SERVICES)
// ============================================================================

    private class NativeGnssTracker(private val context: Context) {
        private val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        private var activeListener: LocationListener? = null
        private var gnssCallback: GnssStatus.Callback? = null
        var satellitesUsedInFix: Int = 0
            private set

        fun isLocationEnabled(): Boolean {
            return runCatching {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.getOrDefault(false)
        }

        @SuppressLint("MissingPermission")
        fun startTracking(onFixUpdate: (GnssFix) -> Unit, onProviderDisabled: () -> Unit) {
            stopTracking()

            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return

            if (!isLocationEnabled()) {
                onProviderDisabled()
            }

            // Register GNSS Satellite status callback for genuine satellite count
            if (hasFine) {
                runCatching {
                    val cb = object : GnssStatus.Callback() {
                        override fun onSatelliteStatusChanged(status: GnssStatus) {
                            var used = 0
                            for (i in 0 until status.satelliteCount) {
                                if (status.usedInFix(i)) used++
                            }
                            satellitesUsedInFix = used
                        }
                    }
                    locationManager.registerGnssStatusCallback(cb, Handler(Looper.getMainLooper()))
                    gnssCallback = cb
                }
            }

            // Check genuine last-known hardware fix (never fabricate coordinates!)
            val providers = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
                add(LocationManager.GPS_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
                add(LocationManager.PASSIVE_PROVIDER)
            }

            var bestLastKnown: Location? = null
            for (p in providers) {
                val candidate =
                    runCatching { locationManager.getLastKnownLocation(p) }.getOrNull() ?: continue
                if (bestLastKnown == null || candidate.elapsedRealtimeNanos > bestLastKnown.elapsedRealtimeNanos) {
                    bestLastKnown = candidate
                }
            }
            bestLastKnown?.let { loc ->
                onFixUpdate(
                    GnssFix(
                        lat = loc.latitude,
                        lon = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        provider = loc.provider ?: "LAST_KNOWN",
                        fixTimestampMillis = loc.time,
                        elapsedRealtimeNanos = loc.elapsedRealtimeNanos,
                        satellitesUsed = satellitesUsedInFix
                    )
                )
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    onFixUpdate(
                        GnssFix(
                            lat = loc.latitude,
                            lon = loc.longitude,
                            accuracyMeters = loc.accuracy,
                            provider = loc.provider ?: "GNSS",
                            fixTimestampMillis = loc.time,
                            elapsedRealtimeNanos = loc.elapsedRealtimeNanos,
                            satellitesUsed = satellitesUsedInFix
                        )
                    )
                }

                override fun onProviderDisabled(provider: String) {
                    if (!isLocationEnabled()) onProviderDisabled()
                }

                override fun onProviderEnabled(provider: String) {}
            }
            activeListener = listener

            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && locationManager.isProviderEnabled(
                        LocationManager.FUSED_PROVIDER
                    )
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.FUSED_PROVIDER,
                        2000L,
                        2f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
                if (hasFine && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        2f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        5f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            }
        }

        fun stopTracking() {
            activeListener?.let { runCatching { locationManager.removeUpdates(it) } }
            activeListener = null
            gnssCallback?.let { runCatching { locationManager.unregisterGnssStatusCallback(it) } }
            gnssCallback = null
        }
    }

// ============================================================================
// 6. MAIN ACTIVITY & HARDWARE PANIC INTERCEPTOR
// ============================================================================

    class MainActivity : ComponentActivity() {
        private var lastVolDownTime = 0L
        private var triggerFakeCall by mutableStateOf(false)
        private var triggerSosBroadcast by mutableStateOf(false)

        override fun onCreate(savedInstanceState: Bundle?) {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)

            // Initialize MapLibre Native SDK (Open-Source, Zero API Key)
            MapLibre.getInstance(applicationContext)

            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            handleIntentAction(intent)

            setContent {
                NimiTheme {
                    NimiApp(
                        externalFakeCall = triggerFakeCall,
                        onConsumeFakeCall = { triggerFakeCall = false },
                        externalSos = triggerSosBroadcast,
                        onConsumeSos = { triggerSosBroadcast = false }
                    )
                }
            }
        }

        override fun onNewIntent(intent: Intent) {
            super.onNewIntent(intent)
            handleIntentAction(intent)
        }

        private fun handleIntentAction(intent: Intent?) {
            when (intent?.action) {
                "com.nimi.app.ACTION_FAKE_CALL" -> triggerFakeCall = true
                "com.nimi.app.ACTION_SISTER_SOS" -> triggerSosBroadcast = true
            }
        }

        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastVolDownTime < 420L) {
                    triggerFakeCall = true
                    lastVolDownTime = 0L
                    return true
                }
                lastVolDownTime = now
            }
            return super.onKeyDown(keyCode, event)
        }
    }

// ============================================================================
// 7. APPLICATION SHELL & REAL GNSS LIFECYCLE MANAGEMENT
// ============================================================================

    private enum class AppTab(val label: String, val icon: ImageVector) {
        Home("Cockpit", Icons.Outlined.Shield),
        Map("Safe Map", Icons.Outlined.Explore),
        Circle("SOS Outbox", Icons.Outlined.Sos),
        Rights("Rights", Icons.AutoMirrored.Outlined.MenuBook),
        Vault("Vault", Icons.Outlined.Lock)
    }

    @Composable
    private fun NimiApp(
        externalFakeCall: Boolean,
        onConsumeFakeCall: () -> Unit,
        externalSos: Boolean,
        onConsumeSos: () -> Unit
    ) {
        val context = LocalContext.current
        val prefs =
            remember { context.getSharedPreferences("nimi_foss_prefs", Context.MODE_PRIVATE) }
        val db = remember { NimiDatabaseHelper(context) }
        val gnssTracker = remember { NativeGnssTracker(context) }

        var infraConfig by remember {
            mutableStateOf(
                InfrastructureConfig(
                    nimiBackendUrl = prefs.getString("custom_backend_url", "") ?: ""
                )
            )
        }

        var currentUser by remember {
            val savedEmail = prefs.getString("logged_in_email", null)
            mutableStateOf(if (savedEmail != null) db.getUserByEmail(savedEmail) else null)
        }

        val pins = remember { mutableStateListOf<SpatialPin>().apply { addAll(db.getAllPins()) } }
        val emergencyContacts = remember(currentUser?.email) {
            mutableStateListOf<EmergencyContact>().apply {
                currentUser?.email?.let { addAll(db.getContactsForUser(it)) }
            }
        }
        val sosOutbox = remember(currentUser?.email) {
            mutableStateListOf<SosOutboxEvent>().apply {
                currentUser?.email?.let { addAll(db.getSosOutboxEvents(it)) }
            }
        }
        val evidenceItems = remember { mutableStateListOf<EvidenceItem>() }

        var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }
        var activeFakeCall by rememberSaveable { mutableStateOf(false) }
        var fakeCallerName by rememberSaveable { mutableStateOf("Papa") }
        var fakeCallerNumber by rememberSaveable { mutableStateOf("+91 98110 44210") }
        var stealthMode by rememberSaveable { mutableStateOf(false) }

        // Genuine GNSS Fix State (NULL until real hardware fix is acquired — never fabricated!)
        var currentGnssFix by remember { mutableStateOf<GnssFix?>(null) }
        var isLocationServiceEnabled by remember { mutableStateOf(gnssTracker.isLocationEnabled()) }
        var fixTickCounter by remember { mutableIntStateOf(0) }

        // Tick every 2 seconds so location freshness ("4s ago") updates live
        LaunchedEffect(currentGnssFix) {
            while (currentGnssFix != null) {
                delay(2000L)
                fixTickCounter++
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val requestLocationLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                gnssTracker.startTracking(
                    onFixUpdate = { fix ->
                        currentGnssFix = fix
                        isLocationServiceEnabled = true
                    },
                    onProviderDisabled = { isLocationServiceEnabled = false }
                )
            }
        }

        DisposableEffect(Unit) {
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                gnssTracker.startTracking(
                    onFixUpdate = { fix ->
                        currentGnssFix = fix
                        isLocationServiceEnabled = true
                    },
                    onProviderDisabled = { isLocationServiceEnabled = false }
                )
            } else {
                requestLocationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            onDispose { gnssTracker.stopTracking() }
        }

        fun executeResilientSos(user: UserAccount) {
            triggerHapticPulse(context)
            // 1. Persist immediately to SQLite Outbox
            val event = db.enqueueSosEvent(user, currentGnssFix, SosDeliveryState.QUEUED_LOCALLY)
            sosOutbox.add(0, event)

            // 2. Open SMS Handoff with Freshness Metadata if contacts exist
            if (emergencyContacts.isNotEmpty()) {
                val openedSms = dispatchSmsWithFreshness(context, currentGnssFix, emergencyContacts)
                if (openedSms) {
                    db.updateSosDeliveryState(event.id, SosDeliveryState.SMS_HANDOFF_OPENED, 0)
                    val idx = sosOutbox.indexOfFirst { it.id == event.id }
                    if (idx >= 0) sosOutbox[idx] =
                        sosOutbox[idx].copy(deliveryState = SosDeliveryState.SMS_HANDOFF_OPENED)
                }
            }

            // 3. If self-hosted Nimi Backend URL is configured, transmit with retry
            if (infraConfig.nimiBackendUrl.isNotBlank()) {
                scope.launch {
                    val delivered =
                        transmitSosToSelfHostedBackend(infraConfig.nimiBackendUrl, event)
                    val newState =
                        if (delivered) SosDeliveryState.CONFIRMED_BY_BACKEND else SosDeliveryState.BACKEND_RETRY_PENDING
                    db.updateSosDeliveryState(event.id, newState, if (delivered) 0 else 1)
                    val idx = sosOutbox.indexOfFirst { it.id == event.id }
                    if (idx >= 0) sosOutbox[idx] = sosOutbox[idx].copy(
                        deliveryState = newState,
                        retryCount = if (delivered) 0 else 1
                    )
                }
            }
        }

        LaunchedEffect(externalFakeCall) {
            if (externalFakeCall) {
                activeFakeCall = true
                onConsumeFakeCall()
            }
        }

        LaunchedEffect(externalSos) {
            if (externalSos) {
                currentUser?.let { u ->
                    executeResilientSos(u)
                    selectedTab = AppTab.Circle
                }
                onConsumeSos()
            }
        }

        if (activeFakeCall) {
            RealisticPhoneCallScreen(
                callerName = fakeCallerName,
                callerNumber = fakeCallerNumber,
                onEndCall = { activeFakeCall = false }
            )
            return
        }

        if (stealthMode) {
            StealthNotesScreen(onUnlock = { stealthMode = false })
            return
        }

        if (currentUser == null) {
            EditorialEmailAuthScreen(
                db = db,
                onAuthenticated = { account ->
                    prefs.edit().putString("logged_in_email", account.email).apply()
                    currentUser = account
                },
                onTriggerEmergencyFakeCall = { activeFakeCall = true },
                onDial112 = { dialNumber(context, "112", scope, snackbarHostState) }
            )
            return
        }

        val user = currentUser!!

        Scaffold(
            containerColor = NimiDesign.Canvas,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (selectedTab == AppTab.Map) 0.dp else 82.dp)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "TabContent"
                    ) { tab ->
                        when (tab) {
                            AppTab.Home -> BentoCockpitScreen(
                                user = user,
                                gnssFix = currentGnssFix,
                                fixTickCounter = fixTickCounter,
                                isLocationEnabled = isLocationServiceEnabled,
                                pins = pins,
                                sosOutbox = sosOutbox,
                                emergencyContacts = emergencyContacts,
                                onRequestGps = {
                                    if (!gnssTracker.isLocationEnabled()) {
                                        runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                                    } else {
                                        requestLocationLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                onTriggerFakeCall = { name, number ->
                                    fakeCallerName = name
                                    fakeCallerNumber = number
                                    activeFakeCall = true
                                },
                                onTriggerSos = {
                                    executeResilientSos(user)
                                    selectedTab = AppTab.Circle
                                },
                                onOpenMap = { selectedTab = AppTab.Map },
                                onOpenCircle = { selectedTab = AppTab.Circle },
                                onOpenRights = { selectedTab = AppTab.Rights },
                                onEnterStealth = { stealthMode = true },
                                onLogout = {
                                    prefs.edit().remove("logged_in_email").apply()
                                    currentUser = null
                                },
                                snackbarHostState = snackbarHostState
                            )

                            AppTab.Map -> MapLibreVectorMapScreen(
                                user = user,
                                gnssFix = currentGnssFix,
                                isLocationEnabled = isLocationServiceEnabled,
                                infraConfig = infraConfig,
                                onUpdateInfraConfig = { updated ->
                                    infraConfig = updated
                                    prefs.edit()
                                        .putString("custom_backend_url", updated.nimiBackendUrl)
                                        .apply()
                                },
                                db = db,
                                pins = pins,
                                onRequestGpsPermission = {
                                    if (!gnssTracker.isLocationEnabled()) {
                                        runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                                    } else {
                                        requestLocationLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                onDropPin = { title, note, type, lat, lon ->
                                    val saved =
                                        db.insertPin(title, note, type, lat, lon, user.email)
                                    pins.add(0, saved)
                                    scope.launch { snackbarHostState.showSnackbar("Flagged '${type.label}' on vector map.") }
                                },
                                onConfirmPin = { pin ->
                                    db.confirmPin(pin.id)
                                    val idx = pins.indexOfFirst { it.id == pin.id }
                                    if (idx >= 0) pins[idx] =
                                        pin.copy(confirmations = pin.confirmations + 1)
                                },
                                onDeletePin = { pin ->
                                    db.deletePin(pin.id)
                                    pins.removeAll { it.id == pin.id }
                                },
                                snackbarHostState = snackbarHostState
                            )

                            AppTab.Circle -> SosOutboxAndCircleScreen(
                                user = user,
                                gnssFix = currentGnssFix,
                                fixTickCounter = fixTickCounter,
                                infraConfig = infraConfig,
                                onUpdateBackendUrl = { url ->
                                    infraConfig = infraConfig.copy(nimiBackendUrl = url)
                                    prefs.edit().putString("custom_backend_url", url).apply()
                                },
                                sosOutbox = sosOutbox,
                                emergencyContacts = emergencyContacts,
                                onTriggerSos = { executeResilientSos(user) },
                                onRetryOutboxEvent = { ev ->
                                    if (infraConfig.nimiBackendUrl.isNotBlank()) {
                                        scope.launch {
                                            val ok = transmitSosToSelfHostedBackend(
                                                infraConfig.nimiBackendUrl,
                                                ev
                                            )
                                            val nextState =
                                                if (ok) SosDeliveryState.CONFIRMED_BY_BACKEND else SosDeliveryState.BACKEND_RETRY_PENDING
                                            val nextRetry = ev.retryCount + 1
                                            db.updateSosDeliveryState(ev.id, nextState, nextRetry)
                                            val idx = sosOutbox.indexOfFirst { it.id == ev.id }
                                            if (idx >= 0) sosOutbox[idx] = ev.copy(
                                                deliveryState = nextState,
                                                retryCount = nextRetry
                                            )
                                        }
                                    } else {
                                        dispatchSmsWithFreshness(
                                            context,
                                            currentGnssFix,
                                            emergencyContacts
                                        )
                                    }
                                },
                                onAddContact = { name, phone ->
                                    val added = db.addContact(user.email, name, phone)
                                    emergencyContacts.add(0, added)
                                },
                                onRemoveContact = { id ->
                                    db.deleteContact(id)
                                    emergencyContacts.removeAll { it.id == id }
                                }
                            )

                            AppTab.Rights -> LegalEducationAndDrillsScreen()
                            AppTab.Vault -> EvidenceVaultScreen(
                                evidenceItems = evidenceItems,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }

                // Floating Glass Pill Navigation Bar
                Surface(
                    color = NimiDesign.Obsidian.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(62.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val pillBg by animateColorAsState(
                                if (isSelected) NimiDesign.Crimson else Color.Transparent,
                                label = "navPill"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(pillBg)
                                    .tactilePress { selectedTab = tab }
                                    .padding(
                                        horizontal = if (isSelected) 14.dp else 10.dp,
                                        vertical = 8.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                        modifier = Modifier.size(19.dp)
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = tab.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1
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

// ============================================================================
// 8. MAPLIBRE NATIVE VECTOR MAP SCREEN (OPENFREEMAP + REAL OSM EMERGENCY POIS
//    + OFFLINE REGION DOWNLOADER + OSRM ROUTING)
// ============================================================================

    @Composable
    private fun MapLibreVectorMapScreen(
        user: UserAccount,
        gnssFix: GnssFix?,
        isLocationEnabled: Boolean,
        infraConfig: InfrastructureConfig,
        onUpdateInfraConfig: (InfrastructureConfig) -> Unit,
        db: NimiDatabaseHelper,
        pins: List<SpatialPin>,
        onRequestGpsPermission: () -> Unit,
        onDropPin: (String, String, FlagType, Double, Double) -> Unit,
        onConfirmPin: (SpatialPin) -> Unit,
        onDeletePin: (SpatialPin) -> Unit,
        snackbarHostState: SnackbarHostState
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        val lifecycleOwner = LocalLifecycleOwner.current

        var mapViewRef by remember { mutableStateOf<MapView?>(null) }
        var mapLibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }
        var isStyleLoaded by remember { mutableStateOf(false) }
        var hasCenteredInitialGps by remember { mutableStateOf(false) }

        // Genuine OpenStreetMap Emergency POIs (Police, Hospitals, Pharmacies)
        val osmEmergencyPois = remember { mutableStateListOf<OsmEmergencyPoi>() }
        var isScanningPois by remember { mutableStateOf(false) }
        var selectedOsmPoi by remember { mutableStateOf<OsmEmergencyPoi?>(null) }

        // Offline Map Downloader State
        var isDownloadingOffline by remember { mutableStateOf(false) }
        var offlineProgressPct by remember { mutableIntStateOf(0) }

        // On-Demand Pin Reporting Mode
        var isReportingMode by rememberSaveable { mutableStateOf(false) }
        var selectedFlagType by remember { mutableStateOf(FlagType.POOR_LIGHT) }
        var reportNote by remember { mutableStateOf("") }
        var selectedPin by remember { mutableStateOf<SpatialPin?>(null) }

        // Search & Autocomplete State
        var sourceText by rememberSaveable { mutableStateOf("My Live GNSS Fix") }
        var sourceCoord by remember { mutableStateOf<LatLng?>(null) }
        var destText by rememberSaveable { mutableStateOf("") }
        var destCoord by remember { mutableStateOf<LatLng?>(null) }

        var activeField by remember { mutableStateOf("NONE") }
        var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
        var isSearching by remember { mutableStateOf(false) }
        var searchJob by remember { mutableStateOf<Job?>(null) }

        // Routing State
        var selectedTravelMode by rememberSaveable { mutableStateOf(TravelMode.CAR) }
        var computedRoutes by remember { mutableStateOf<List<RouteResult>>(emptyList()) }
        var selectedRouteIdx by remember { mutableIntStateOf(0) }
        var isComputingRoute by remember { mutableStateOf(false) }

        // Load cached OSM Emergency POIs when GNSS fix arrives
        LaunchedEffect(gnssFix?.lat, gnssFix?.lon) {
            val fix = gnssFix ?: return@LaunchedEffect
            if (!hasCenteredInitialGps && mapLibreMapRef != null) {
                mapLibreMapRef?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(fix.lat, fix.lon))
                            .zoom(15.6)
                            .build()
                    ),
                    900
                )
                hasCenteredInitialGps = true
            }
            if (osmEmergencyPois.isEmpty()) {
                val cached =
                    withContext(Dispatchers.IO) { db.getCachedOsmPoisNear(fix.lat, fix.lon) }
                osmEmergencyPois.addAll(cached)
            }
        }

        fun triggerPlaceAutocomplete(queryString: String, field: String) {
            activeField = field
            searchJob?.cancel()
            val cleanQuery = queryString.trim()
            if (cleanQuery.length < 2 || cleanQuery == "My Live GNSS Fix") {
                suggestions = emptyList()
                isSearching = false
                return
            }
            searchJob = scope.launch {
                delay(320)
                isSearching = true
                suggestions = searchOpenStreetMapPlaces(
                    context = context,
                    db = db,
                    photonBaseUrl = infraConfig.photonBaseUrl,
                    query = cleanQuery,
                    biasFix = gnssFix
                )
                isSearching = false
            }
        }

        fun calculateRoutesTo(target: LatLng) {
            keyboardController?.hide()
            activeField = "NONE"
            suggestions = emptyList()
            val start = sourceCoord ?: gnssFix?.let { LatLng(it.lat, it.lon) }
            ?: mapLibreMapRef?.cameraPosition?.target
            if (start == null) {
                scope.launch { snackbarHostState.showSnackbar("Acquire GPS fix or enter a starting location first.") }
                return
            }
            scope.launch {
                isComputingRoute = true
                val routes = computeOsrmRoutes(infraConfig.osrmBaseUrl, start, target, pins)
                isComputingRoute = false
                computedRoutes = routes
                if (routes.isNotEmpty()) {
                    selectedRouteIdx = 0
                    val pts = routes[0].allPoints
                    if (pts.size >= 2) {
                        runCatching {
                            val b = LatLngBounds.Builder()
                            pts.forEach { b.include(it) }
                            mapLibreMapRef?.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    b.build(),
                                    150
                                ), 900
                            )
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar("Routing service unreachable.")
                }
            }
        }

        fun scanEmergencyPoisAroundMapCenter() {
            val center =
                mapLibreMapRef?.cameraPosition?.target ?: gnssFix?.let { LatLng(it.lat, it.lon) }
            if (center == null) {
                scope.launch { snackbarHostState.showSnackbar("Pan map or enable GPS to scan emergency services.") }
                return
            }
            scope.launch {
                isScanningPois = true
                val fetched = fetchOverpassEmergencyPois(
                    infraConfig.overpassUrl,
                    center.latitude,
                    center.longitude,
                    3500
                )
                isScanningPois = false
                if (fetched.isNotEmpty()) {
                    withContext(Dispatchers.IO) { db.cacheOsmPois(fetched) }
                    osmEmergencyPois.clear()
                    osmEmergencyPois.addAll(fetched)
                    snackbarHostState.showSnackbar("Loaded ${fetched.size} verified OSM Police & Hospital POIs.")
                } else {
                    val cached = withContext(Dispatchers.IO) {
                        db.getCachedOsmPoisNear(
                            center.latitude,
                            center.longitude
                        )
                    }
                    if (cached.isNotEmpty()) {
                        osmEmergencyPois.clear()
                        osmEmergencyPois.addAll(cached)
                        snackbarHostState.showSnackbar("Loaded ${cached.size} cached emergency POIs.")
                    } else {
                        snackbarHostState.showSnackbar("No OSM emergency POIs returned in this sector.")
                    }
                }
            }
        }

        fun downloadOfflineRegionForCurrentView() {
            val map = mapLibreMapRef ?: return
            val bounds = map.projection.visibleRegion.latLngBounds
            isDownloadingOffline = true
            offlineProgressPct = 1

            val definition = OfflineTilePyramidRegionDefinition(
                infraConfig.resolvedStyleUrl,
                bounds,
                12.0,
                16.0,
                context.resources.displayMetrics.density
            )
            val metadata = JSONObject().apply {
                put("name", "Nimi Offline Safety Pack")
                put("timestamp", System.currentTimeMillis())
            }.toString().toByteArray()

            OfflineManager.getInstance(context).createOfflineRegion(
                definition,
                metadata,
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: OfflineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
                        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                            override fun onStatusChanged(status: OfflineRegionStatus) {
                                val pct = if (status.requiredResourceCount > 0) {
                                    ((100.0 * status.completedResourceCount) / status.requiredResourceCount).roundToInt()
                                } else 0
                                offlineProgressPct = pct.coerceIn(0, 100)
                                if (status.isComplete) {
                                    isDownloadingOffline = false
                                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Offline vector map region saved to device storage.")
                                    }
                                }
                            }

                            override fun onError(error: OfflineRegionError) {
                                isDownloadingOffline = false
                            }

                            override fun mapboxTileCountLimitExceeded(limit: Long) {
                                isDownloadingOffline = false
                            }
                        })
                    }

                    override fun onError(error: String) {
                        isDownloadingOffline = false
                        scope.launch { snackbarHostState.showSnackbar("Offline download error: $error") }
                    }
                }
            )
        }

        // Bind MapView to Android Lifecycle
        DisposableEffect(lifecycleOwner, mapViewRef) {
            val mapView = mapViewRef
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView?.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                    Lifecycle.Event.ON_STOP -> mapView?.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val overlaySignature = remember(
            isStyleLoaded,
            infraConfig.resolvedStyleUrl,
            gnssFix,
            pins.size,
            pins.sumOf { it.confirmations },
            osmEmergencyPois.size,
            computedRoutes,
            selectedRouteIdx,
            destCoord
        ) {
            "${isStyleLoaded}_${infraConfig.resolvedStyleUrl}_${gnssFix?.lat}_${gnssFix?.accuracyMeters}_${pins.size}_${pins.sumOf { it.confirmations }}_${osmEmergencyPois.size}_${computedRoutes.hashCode()}_${selectedRouteIdx}_${destCoord?.latitude}"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            // 1. BASE LAYER: MAPLIBRE NATIVE VECTOR MAP VIEW
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        setOnTouchListener { v, event ->
                            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            false
                        }
                        getMapAsync { map ->
                            mapLibreMapRef = map
                            map.uiSettings.apply {
                                isCompassEnabled = true
                                isRotateGesturesEnabled = true
                                isTiltGesturesEnabled = true
                                isAttributionEnabled = true
                                attributionGravity = Gravity.BOTTOM or Gravity.START
                                setAttributionMargins(36, 0, 0, 260)
                                isLogoEnabled = false
                            }

                            // Initial camera position: real GNSS fix if available, otherwise India overview
                            val initialTarget =
                                gnssFix?.let { LatLng(it.lat, it.lon) } ?: LatLng(22.5937, 78.9629)
                            val initialZoom = if (gnssFix != null) 15.5 else 4.6
                            map.cameraPosition = CameraPosition.Builder()
                                .target(initialTarget)
                                .zoom(initialZoom)
                                .build()

                            map.setStyle(
                                Style.Builder().fromUri(infraConfig.resolvedStyleUrl)
                            ) { style ->
                                setupMapLibreSafetyLayers(style)
                                isStyleLoaded = true
                            }

                            map.addOnMapClickListener { clickedLatLng ->
                                keyboardController?.hide()
                                activeField = "NONE"

                                // Hit-test user safety pins or OSM emergency POIs within 120m
                                val hitPin = pins.minByOrNull {
                                    haversineMeters(
                                        clickedLatLng.latitude,
                                        clickedLatLng.longitude,
                                        it.lat,
                                        it.lon
                                    )
                                }
                                val hitPoi = osmEmergencyPois.minByOrNull {
                                    haversineMeters(
                                        clickedLatLng.latitude,
                                        clickedLatLng.longitude,
                                        it.lat,
                                        it.lon
                                    )
                                }

                                val pinDist = hitPin?.let {
                                    haversineMeters(
                                        clickedLatLng.latitude,
                                        clickedLatLng.longitude,
                                        it.lat,
                                        it.lon
                                    )
                                } ?: Double.MAX_VALUE
                                val poiDist = hitPoi?.let {
                                    haversineMeters(
                                        clickedLatLng.latitude,
                                        clickedLatLng.longitude,
                                        it.lat,
                                        it.lon
                                    )
                                } ?: Double.MAX_VALUE

                                when {
                                    pinDist < 120.0 && pinDist <= poiDist -> {
                                        selectedPin = hitPin
                                        selectedOsmPoi = null
                                    }

                                    poiDist < 120.0 -> {
                                        selectedOsmPoi = hitPoi
                                        selectedPin = null
                                    }

                                    else -> {
                                        selectedPin = null
                                        selectedOsmPoi = null
                                    }
                                }
                                true
                            }
                        }
                        mapViewRef = this
                    }
                },
                update = { _ ->
                    val map = mapLibreMapRef ?: return@AndroidView
                    val currentStyle = map.style
                    if (currentStyle == null || !currentStyle.isFullyLoaded) return@AndroidView

                    if (overlaySignature.isNotEmpty()) {
                        updateMapLibreGeoJsonSources(
                            style = currentStyle,
                            gnssFix = gnssFix,
                            pins = pins,
                            osmPois = osmEmergencyPois,
                            routes = computedRoutes,
                            selectedRouteIdx = selectedRouteIdx,
                            destCoord = destCoord
                        )
                    }
                }
            )

            // 2. ON-DEMAND PIN-DROP CROSSHAIR
            if (isReportingMode) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = selectedFlagType.composeColor,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = "Aim Crosshair • ${selectedFlagType.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(selectedFlagType.composeColor)
                            .border(3.dp, Color.White, CircleShape)
                    )
                }
            }

            // 3. TOP FLOATING SEARCH CAPSULE, STYLE SWITCHER & POI BAR
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Source Input
                        TactileInputBox(
                            value = sourceText,
                            onValueChange = {
                                sourceText = it
                                triggerPlaceAutocomplete(it, "SOURCE")
                            },
                            placeholder = "Start location",
                            leadingIcon = Icons.Outlined.MyLocation,
                            iconTint = NimiDesign.Cobalt,
                            imeAction = ImeAction.Next,
                            trailingContent = {
                                if (sourceText != "My Live GNSS Fix") {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Use Live GNSS",
                                        tint = NimiDesign.Cobalt,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                sourceText = "My Live GNSS Fix"
                                                sourceCoord = null
                                                suggestions = emptyList()
                                            }
                                    )
                                }
                            }
                        )

                        // Destination Input
                        TactileInputBox(
                            value = destText,
                            onValueChange = {
                                destText = it
                                triggerPlaceAutocomplete(it, "DEST")
                            },
                            placeholder = "Where to? (Search street, metro, landmark)...",
                            leadingIcon = Icons.Outlined.Place,
                            iconTint = NimiDesign.Crimson,
                            imeAction = ImeAction.Search,
                            onAction = {
                                if (suggestions.isNotEmpty()) {
                                    val first = suggestions.first()
                                    destText = first.title
                                    val pt = LatLng(first.lat, first.lon)
                                    destCoord = pt
                                    calculateRoutesTo(pt)
                                }
                            },
                            trailingContent = {
                                if (destText.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear",
                                        tint = NimiDesign.Slate,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                destText = ""
                                                destCoord = null
                                                computedRoutes = emptyList()
                                                suggestions = emptyList()
                                            }
                                    )
                                }
                            }
                        )

                        // Quick Action Pill Row: Scan Real Police/Hospitals, Offline Pack & OpenFreeMap Styles
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = NimiDesign.CobaltSoft,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.tactilePress { scanEmergencyPoisAroundMapCenter() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.LocalPolice,
                                        null,
                                        tint = NimiDesign.Cobalt,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isScanningPois) "Scanning OSM..." else "Police & Hospitals (${osmEmergencyPois.size})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Cobalt
                                    )
                                }
                            }

                            Surface(
                                color = NimiDesign.EmeraldSoft,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.tactilePress {
                                    if (!isDownloadingOffline) downloadOfflineRegionForCurrentView()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.DownloadForOffline,
                                        null,
                                        tint = NimiDesign.Emerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isDownloadingOffline) "Caching $offlineProgressPct%..." else "Save Offline Map",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Emerald
                                    )
                                }
                            }

                            OpenFreeMapStyle.entries.forEach { mapStyle ->
                                val isCurrent = infraConfig.activeStyle == mapStyle
                                Surface(
                                    color = if (isCurrent) NimiDesign.Obsidian else NimiDesign.SurfaceRecessed,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        onUpdateInfraConfig(infraConfig.copy(activeStyle = mapStyle))
                                        isStyleLoaded = false
                                        mapLibreMapRef?.setStyle(
                                            Style.Builder().fromUri(mapStyle.styleUrl)
                                        ) { s ->
                                            setupMapLibreSafetyLayers(s)
                                            isStyleLoaded = true
                                        }
                                    }
                                ) {
                                    Text(
                                        text = mapStyle.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else NimiDesign.Slate,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        }

                        if (isSearching || isComputingRoute || isScanningPois) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = NimiDesign.Crimson
                            )
                        }

                        // Live Autocomplete Dropdown
                        if (suggestions.isNotEmpty() && activeField != "NONE") {
                            HorizontalDivider(color = NimiDesign.Hairline)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 190.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                suggestions.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val pt = LatLng(item.lat, item.lon)
                                                if (activeField == "SOURCE") {
                                                    sourceText = item.title
                                                    sourceCoord = pt
                                                    suggestions = emptyList()
                                                    activeField = "NONE"
                                                    mapLibreMapRef?.animateCamera(
                                                        CameraUpdateFactory.newLatLngZoom(pt, 15.5)
                                                    )
                                                    destCoord?.let { calculateRoutesTo(it) }
                                                } else {
                                                    destText = item.title
                                                    destCoord = pt
                                                    calculateRoutesTo(pt)
                                                }
                                            }
                                            .padding(vertical = 9.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            null,
                                            tint = NimiDesign.Crimson,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                item.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NimiDesign.Obsidian,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (item.subtitle.isNotBlank()) {
                                                Text(
                                                    item.subtitle,
                                                    fontSize = 11.sp,
                                                    color = NimiDesign.Slate,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Explicit GPS Missing Warning Banner (Never fabricate coordinates!)
                if (gnssFix == null) {
                    Surface(
                        color = NimiDesign.AmberSoft,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NimiDesign.Amber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRequestGpsPermission() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (!isLocationEnabled) "GPS Hardware Disabled • Tap to turn on Location"
                                else "Acquiring satellite GNSS fix... Tap to grant/refresh",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NimiDesign.Obsidian
                            )
                            Icon(
                                Icons.Outlined.GpsFixed,
                                null,
                                tint = NimiDesign.Amber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Selected User Pin Inspector Popup
                selectedPin?.let { pin ->
                    Surface(
                        color = NimiDesign.Surface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.5.dp, pin.type.composeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = pin.type.composeColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${pin.type.label.uppercase(Locale.ROOT)} • ${pin.confirmations} REPORTS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 3.dp
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = { selectedPin = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(pin.title, style = MaterialTheme.typography.titleMedium)
                            Text(pin.note, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Reported by ${pin.reporterEmail} • ${pin.createdAt}",
                                fontSize = 10.sp,
                                color = NimiDesign.Muted
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = pin.type.composeColor,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.tactilePress { onConfirmPin(pin) }
                                ) {
                                    Text(
                                        text = "Confirm (+1)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 7.dp
                                        )
                                    )
                                }
                                if (pin.reporterEmail.equals(user.email, ignoreCase = true)) {
                                    Surface(
                                        color = NimiDesign.SurfaceRecessed,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.tactilePress {
                                            onDeletePin(pin)
                                            selectedPin = null
                                        }
                                    ) {
                                        Text(
                                            text = "Remove My Pin",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NimiDesign.Crimson,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 7.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Real OpenStreetMap Emergency POI Inspector Popup
                selectedOsmPoi?.let { poi ->
                    Surface(
                        color = NimiDesign.Surface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.5.dp, poi.category.composeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = poi.category.composeColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED OSM ${poi.category.label.uppercase(Locale.ROOT)}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 3.dp
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = { selectedOsmPoi = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(poi.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = buildString {
                                    append("Operator: ${poi.operatorName ?: "Not listed in OSM"}")
                                    append(" • Hours: ${poi.openingHours ?: "Not listed in OSM"}")
                                    if (!poi.address.isNullOrBlank()) append("\nAddress: ${poi.address}")
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = poi.category.composeColor,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.tactilePress {
                                        val pt = LatLng(poi.lat, poi.lon)
                                        destText = poi.name
                                        destCoord = pt
                                        selectedOsmPoi = null
                                        calculateRoutesTo(pt)
                                    }
                                ) {
                                    Text(
                                        text = "Route Here",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 7.dp
                                        )
                                    )
                                }

                                if (!poi.phone.isNullOrBlank()) {
                                    Surface(
                                        color = NimiDesign.EmeraldSoft,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.tactilePress {
                                            dialNumber(context, poi.phone, scope, snackbarHostState)
                                        }
                                    ) {
                                        Text(
                                            text = "Call ${poi.phone}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NimiDesign.Emerald,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 7.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. RIGHT-SIDE FLOATING MAP CONTROLS
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        val fix = gnssFix
                        if (fix != null) {
                            mapLibreMapRef?.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder().target(LatLng(fix.lat, fix.lon))
                                        .zoom(16.5).build()
                                ),
                                700
                            )
                        } else {
                            onRequestGpsPermission()
                        }
                    },
                    containerColor = NimiDesign.Surface,
                    contentColor = if (gnssFix != null) NimiDesign.Cobalt else NimiDesign.Amber,
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = "My Live GNSS")
                }

                SmallFloatingActionButton(
                    onClick = { mapLibreMapRef?.animateCamera(CameraUpdateFactory.zoomIn()) },
                    containerColor = NimiDesign.Surface,
                    contentColor = NimiDesign.Obsidian,
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Zoom In")
                }

                SmallFloatingActionButton(
                    onClick = { mapLibreMapRef?.animateCamera(CameraUpdateFactory.zoomOut()) },
                    containerColor = NimiDesign.Surface,
                    contentColor = NimiDesign.Obsidian,
                    shape = CircleShape
                ) {
                    Icon(Icons.Outlined.Remove, contentDescription = "Zoom Out")
                }

                ExtendedFloatingActionButton(
                    onClick = { isReportingMode = !isReportingMode },
                    containerColor = if (isReportingMode) NimiDesign.Obsidian else NimiDesign.Crimson,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        if (isReportingMode) Icons.Outlined.Close else Icons.Outlined.AddLocationAlt,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isReportingMode) "Cancel" else "Flag Spot",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. ON-DEMAND PIN REPORTING SHEET
            if (isReportingMode) {
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 14.dp, end = 14.dp, bottom = 84.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Report Safety Flag", style = MaterialTheme.typography.titleLarge)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlagType.entries.forEach { type ->
                                val selected = selectedFlagType == type
                                Surface(
                                    color = if (selected) type.composeColor else type.softBg,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { selectedFlagType = type }
                                ) {
                                    Text(
                                        text = type.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else NimiDesign.Obsidian,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 7.dp
                                        )
                                    )
                                }
                            }
                        }

                        TactileInputBox(
                            value = reportNote,
                            onValueChange = { reportNote = it },
                            placeholder = "Short note (e.g. Streetlamp out, 24/7 pharmacy guard)...",
                            leadingIcon = Icons.Outlined.EditNote
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (gnssFix != null) {
                                Surface(
                                    color = NimiDesign.SurfaceRecessed,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.tactilePress {
                                        onDropPin(
                                            selectedFlagType.label,
                                            reportNote.ifBlank { "Flagged at live GNSS coordinate" },
                                            selectedFlagType,
                                            gnssFix.lat,
                                            gnssFix.lon
                                        )
                                        reportNote = ""
                                        isReportingMode = false
                                        destCoord?.let { calculateRoutesTo(it) }
                                    }
                                ) {
                                    Text(
                                        text = "At My GPS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Obsidian,
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 13.dp
                                        )
                                    )
                                }
                            }

                            Surface(
                                color = selectedFlagType.composeColor,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePress {
                                        val target = mapLibreMapRef?.cameraPosition?.target
                                        if (target != null) {
                                            onDropPin(
                                                selectedFlagType.label,
                                                reportNote.ifBlank { "Flagged on vector street map" },
                                                selectedFlagType,
                                                target.latitude,
                                                target.longitude
                                            )
                                            reportNote = ""
                                            isReportingMode = false
                                            destCoord?.let { calculateRoutesTo(it) }
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 13.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Drop Flag at Crosshair",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. MULTI-MODAL ROUTE SUMMARY SHEET (CAR / BIKE / WALK + DANGER STRETCH COUNTER)
            if (!isReportingMode && computedRoutes.isNotEmpty()) {
                val activeRoute =
                    computedRoutes.getOrNull(selectedRouteIdx) ?: computedRoutes.first()
                val hazardCount = activeRoute.flagsOnRoute.size
                val totalConfirmations = activeRoute.flagsOnRoute.sumOf { it.confirmations }

                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 12.dp,
                    border = BorderStroke(
                        1.5.dp,
                        if (hazardCount > 0) NimiDesign.Crimson else NimiDesign.Emerald
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 14.dp, end = 14.dp, bottom = 84.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NimiDesign.SurfaceRecessed)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TravelMode.entries.forEach { mode ->
                                val isSel = selectedTravelMode == mode
                                val mins = activeRoute.durationMinutes[mode] ?: 5
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) NimiDesign.Obsidian else Color.Transparent)
                                        .clickable { selectedTravelMode = mode }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = mode.icon,
                                            contentDescription = mode.label,
                                            tint = if (isSel) Color.White else NimiDesign.Slate,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "$mins min",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else NimiDesign.Obsidian
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hazardCount == 0) "SAFE CORRIDOR • 0 DANGER FLAGS"
                                    else "RED DANGER STRETCH • $hazardCount FLAG(S) ($totalConfirmations reports)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (hazardCount > 0) NimiDesign.Crimson else NimiDesign.Emerald
                                )
                                Text(
                                    text = "${activeRoute.name} • ${
                                        String.format(
                                            Locale.US,
                                            "%.1f km",
                                            activeRoute.distanceKm
                                        )
                                    } (${activeRoute.durationMinutes[selectedTravelMode]} min by ${selectedTravelMode.label})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NimiDesign.Obsidian
                                )
                            }

                            if (computedRoutes.size > 1) {
                                Surface(
                                    color = NimiDesign.EmeraldSoft,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.tactilePress {
                                        selectedRouteIdx =
                                            (selectedRouteIdx + 1) % computedRoutes.size
                                    }
                                ) {
                                    Text(
                                        text = if (activeRoute.isSaferBypass) "Main Route" else "Safer Route →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Emerald,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 7.dp
                                        )
                                    )
                                }
                            }
                        }

                        // Recalculate Route from Current Live GNSS Fix
                        Surface(
                            color = NimiDesign.Cobalt,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress {
                                    destCoord?.let { dst ->
                                        sourceCoord = null
                                        sourceText = "My Live GNSS Fix"
                                        calculateRoutesTo(dst)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.MyLocation,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Recalculate Live Route from Current GPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

// ============================================================================
// 9. MAPLIBRE STYLE & GEOJSON LAYER ENGINE
// ============================================================================

    private fun setupMapLibreSafetyLayers(style: Style) {
        if (style.getSource("nimi-accuracy-source") != null) return

        style.addSource(GeoJsonSource("nimi-accuracy-source"))
        style.addSource(GeoJsonSource("nimi-alt-route-source"))
        style.addSource(GeoJsonSource("nimi-safe-route-source"))
        style.addSource(GeoJsonSource("nimi-danger-route-source"))
        style.addSource(GeoJsonSource("nimi-osm-poi-source"))
        style.addSource(GeoJsonSource("nimi-pins-source"))
        style.addSource(GeoJsonSource("nimi-user-source"))

        // 1. GNSS Accuracy Polygon Fill Layer
        style.addLayer(
            org.maplibre.android.style.layers.FillLayer(
                "nimi-accuracy-layer",
                "nimi-accuracy-source"
            ).withProperties(
                PropertyFactory.fillColor(AndroidColor.parseColor("#2563EB")),
                PropertyFactory.fillOpacity(0.16f)
            )
        )

        // 2. Alternative Unselected Route Layer
        style.addLayer(
            LineLayer("nimi-alt-route-layer", "nimi-alt-route-source").withProperties(
                PropertyFactory.lineColor(AndroidColor.parseColor("#94A3B8")),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        // 3. Safe Route Segments Layer
        style.addLayer(
            LineLayer("nimi-safe-route-layer", "nimi-safe-route-source").withProperties(
                PropertyFactory.lineColor(AndroidColor.parseColor("#2563EB")),
                PropertyFactory.lineWidth(7f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        // 4. Danger Route Segments Layer (Thick Crimson Red)
        style.addLayer(
            LineLayer("nimi-danger-route-layer", "nimi-danger-route-source").withProperties(
                PropertyFactory.lineColor(AndroidColor.parseColor("#E11D48")),
                PropertyFactory.lineWidth(10f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        // 5. Verified OpenStreetMap Emergency POIs Layer (Police / Hospitals / Pharmacies)
        style.addLayer(
            CircleLayer("nimi-osm-poi-layer", "nimi-osm-poi-source").withProperties(
                PropertyFactory.circleColor(Expression.toColor(Expression.get("color"))),
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleStrokeColor(AndroidColor.WHITE),
                PropertyFactory.circleStrokeWidth(2.5f)
            )
        )

        // 6. User-Reported Safety & Hazard Pins Layer
        style.addLayer(
            CircleLayer("nimi-pins-layer", "nimi-pins-source").withProperties(
                PropertyFactory.circleColor(Expression.toColor(Expression.get("color"))),
                PropertyFactory.circleRadius(10f),
                PropertyFactory.circleStrokeColor(AndroidColor.WHITE),
                PropertyFactory.circleStrokeWidth(3f)
            )
        )

        // 7. User Live GNSS Puck Layer
        style.addLayer(
            CircleLayer("nimi-user-layer", "nimi-user-source").withProperties(
                PropertyFactory.circleColor(AndroidColor.parseColor("#2563EB")),
                PropertyFactory.circleRadius(8.5f),
                PropertyFactory.circleStrokeColor(AndroidColor.WHITE),
                PropertyFactory.circleStrokeWidth(3.5f)
            )
        )
    }

    private fun updateMapLibreGeoJsonSources(
        style: Style,
        gnssFix: GnssFix?,
        pins: List<SpatialPin>,
        osmPois: List<OsmEmergencyPoi>,
        routes: List<RouteResult>,
        selectedRouteIdx: Int,
        destCoord: LatLng?
    ) {
        // 1. Update User GNSS Dot & Accuracy Circle Polygon
        val userSource = style.getSourceAs<GeoJsonSource>("nimi-user-source")
        val accSource = style.getSourceAs<GeoJsonSource>("nimi-accuracy-source")
        if (gnssFix != null) {
            userSource?.setGeoJson(Feature.fromGeometry(Point.fromLngLat(gnssFix.lon, gnssFix.lat)))
            accSource?.setGeoJson(
                buildAccuracyCirclePolygon(
                    gnssFix.lat,
                    gnssFix.lon,
                    gnssFix.accuracyMeters.toDouble()
                )
            )
        } else {
            userSource?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
            accSource?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        }

        // 2. Update User Safety & Hazard Pins (+ Destination Marker)
        val pinFeatures = mutableListOf<Feature>()
        pins.forEach { pin ->
            val f = Feature.fromGeometry(Point.fromLngLat(pin.lon, pin.lat)).apply {
                addStringProperty("color", pin.type.hexColor)
                addStringProperty("title", pin.title)
            }
            pinFeatures.add(f)
        }
        destCoord?.let { dst ->
            val dstFeature =
                Feature.fromGeometry(Point.fromLngLat(dst.longitude, dst.latitude)).apply {
                    addStringProperty("color", "#0F1115")
                    addStringProperty("title", "Destination")
                }
            pinFeatures.add(dstFeature)
        }
        style.getSourceAs<GeoJsonSource>("nimi-pins-source")
            ?.setGeoJson(FeatureCollection.fromFeatures(pinFeatures))

        // 3. Update OpenStreetMap Emergency POIs
        val poiFeatures = osmPois.map { poi ->
            Feature.fromGeometry(Point.fromLngLat(poi.lon, poi.lat)).apply {
                addStringProperty("color", poi.category.hexColor)
                addStringProperty("name", poi.name)
            }
        }
        style.getSourceAs<GeoJsonSource>("nimi-osm-poi-source")
            ?.setGeoJson(FeatureCollection.fromFeatures(poiFeatures))

        // 4. Update Route Polylines (Safe vs Danger Segments)
        val altFeatures = mutableListOf<Feature>()
        val safeFeatures = mutableListOf<Feature>()
        val dangerFeatures = mutableListOf<Feature>()

        routes.forEachIndexed { idx, route ->
            if (idx != selectedRouteIdx) {
                val linePts = route.allPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                if (linePts.size >= 2) altFeatures.add(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            linePts
                        )
                    )
                )
            }
        }

        routes.getOrNull(selectedRouteIdx)?.segments?.forEach { seg ->
            val linePts = seg.points.map { Point.fromLngLat(it.longitude, it.latitude) }
            if (linePts.size >= 2) {
                val feat = Feature.fromGeometry(LineString.fromLngLats(linePts))
                if (seg.isDangerZone) dangerFeatures.add(feat) else safeFeatures.add(feat)
            }
        }

        style.getSourceAs<GeoJsonSource>("nimi-alt-route-source")
            ?.setGeoJson(FeatureCollection.fromFeatures(altFeatures))
        style.getSourceAs<GeoJsonSource>("nimi-safe-route-source")
            ?.setGeoJson(FeatureCollection.fromFeatures(safeFeatures))
        style.getSourceAs<GeoJsonSource>("nimi-danger-route-source")
            ?.setGeoJson(FeatureCollection.fromFeatures(dangerFeatures))
    }

    private fun buildAccuracyCirclePolygon(
        lat: Double,
        lon: Double,
        radiusMeters: Double
    ): Feature {
        val r = radiusMeters.coerceIn(8.0, 800.0)
        val earthRadius = 6371000.0
        val ring = mutableListOf<Point>()
        val steps = 36
        for (i in 0..steps) {
            val angle = Math.toRadians((i * 360.0) / steps)
            val dLat = (r * cos(angle)) / earthRadius
            val dLon = (r * sin(angle)) / (earthRadius * cos(Math.toRadians(lat)))
            val pLat = lat + Math.toDegrees(dLat)
            val pLon = lon + Math.toDegrees(dLon)
            ring.add(Point.fromLngLat(pLon, pLat))
        }
        return Feature.fromGeometry(Polygon.fromLngLats(listOf(ring)))
    }

// ============================================================================
// 10. BENTO COCKPIT SCREEN (HONEST GNSS FRESHNESS & RESILIENT SOS)
// ============================================================================

    @Composable
    private fun BentoCockpitScreen(
        user: UserAccount,
        gnssFix: GnssFix?,
        fixTickCounter: Int,
        isLocationEnabled: Boolean,
        pins: List<SpatialPin>,
        sosOutbox: List<SosOutboxEvent>,
        emergencyContacts: List<EmergencyContact>,
        onRequestGps: () -> Unit,
        onTriggerFakeCall: (String, String) -> Unit,
        onTriggerSos: () -> Unit,
        onOpenMap: () -> Unit,
        onOpenCircle: () -> Unit,
        onOpenRights: () -> Unit,
        onEnterStealth: () -> Unit,
        onLogout: () -> Unit,
        snackbarHostState: SnackbarHostState
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val callers = listOf(
            "Papa" to "+91 98110 44210",
            "Mom" to "+91 98112 78390",
            "Police Control" to "112"
        )
        var callerIdx by rememberSaveable { mutableIntStateOf(0) }

        // Read fixTickCounter to keep ageSeconds live
        val liveAgeSec = remember(gnssFix, fixTickCounter) { gnssFix?.ageSeconds }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { onRequestGps() }
                        ) {
                            val dotColor = when {
                                gnssFix == null -> NimiDesign.Crimson
                                gnssFix.isFresh -> NimiDesign.Emerald
                                else -> NimiDesign.Amber
                            }
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape)
                                    .background(dotColor)
                            )
                            Text(
                                text = when {
                                    gnssFix == null && !isLocationEnabled -> "GPS OFF • TAP TO ENABLE"
                                    gnssFix == null -> "WAITING FOR GNSS SATELLITE FIX..."
                                    else -> "GNSS ±${gnssFix.accuracyMeters.roundToInt()}m • ${liveAgeSec}s ago (${gnssFix.satellitesUsed} sats)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = dotColor
                            )
                        }
                        Text(user.name, style = MaterialTheme.typography.displayLarge)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = NimiDesign.Surface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, NimiDesign.Hairline),
                            modifier = Modifier.tactilePress(onClick = onEnterStealth)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.VisibilityOff,
                                    null,
                                    tint = NimiDesign.Obsidian,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Stealth",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NimiDesign.Obsidian
                                )
                            }
                        }
                        IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Outlined.Logout,
                                contentDescription = "Sign Out",
                                tint = NimiDesign.Slate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Primary SOS & Fake Call Cockpit
            item {
                Surface(
                    color = NimiDesign.Obsidian,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "EMERGENCY ACTUATORS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NimiDesign.Crimson,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Vol-Down ×2 = Fake Call",
                                fontSize = 10.sp,
                                color = NimiDesign.Muted
                            )
                        }

                        Surface(
                            color = NimiDesign.Crimson,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress(onClick = onTriggerSos)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Sos,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Persist & Dispatch SOS",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (gnssFix != null) "Logs ±${gnssFix.accuracyMeters.roundToInt()}m fix & opens SMS to ${emergencyContacts.size} contact(s)"
                                            else "Queues SOS locally & opens emergency SMS",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowForward,
                                    null,
                                    tint = Color.White
                                )
                            }
                        }

                        val (callerName, callerNum) = callers[callerIdx]
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                color = NimiDesign.Charcoal,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePress { onTriggerFakeCall(callerName, callerNum) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 13.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.RingVolume,
                                        null,
                                        tint = NimiDesign.Emerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            "Ring Fake Call: $callerName",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "Hardware ringtone & timer",
                                            fontSize = 10.sp,
                                            color = NimiDesign.Muted
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = NimiDesign.Charcoal,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.tactilePress {
                                    callerIdx = (callerIdx + 1) % callers.size
                                }
                            ) {
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 18.dp
                                    ), contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Switch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Asymmetric Bento Grid (MapLibre Vector Map + SOS Outbox + BNSS Rights)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = NimiDesign.Surface,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, NimiDesign.Hairline),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(190.dp)
                            .tactilePress(onClick = onOpenMap)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(NimiDesign.CobaltSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Explore,
                                        null,
                                        tint = NimiDesign.Cobalt,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Surface(
                                    color = NimiDesign.EmeraldSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "MAPLIBRE FOSS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Emerald,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Vector Safe Map\n& OSM Police",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    lineHeight = 23.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NimiDesign.Obsidian
                                )
                                Text(
                                    "${pins.size} user flags • Offline regions",
                                    fontSize = 11.sp,
                                    color = NimiDesign.Slate
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(190.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = NimiDesign.Surface,
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, NimiDesign.Hairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .tactilePress(onClick = onOpenCircle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    Icons.Outlined.Sos,
                                    null,
                                    tint = NimiDesign.Crimson,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text("SOS Outbox", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${sosOutbox.size} logged • ${emergencyContacts.size} contacts",
                                        fontSize = 10.sp,
                                        color = NimiDesign.Muted
                                    )
                                }
                            }
                        }

                        Surface(
                            color = NimiDesign.Surface,
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, NimiDesign.Hairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .tactilePress(onClick = onOpenRights)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.MenuBook,
                                    null,
                                    tint = NimiDesign.Cobalt,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        "BNSS & POCSO",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Zero FIR & drills",
                                        fontSize = 10.sp,
                                        color = NimiDesign.Muted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // National Emergency Dispatch
            item {
                Text(
                    "OFFLINE-CAPABLE NATIONAL HELPLINES",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("112", "Emergency SOS", NimiDesign.CrimsonSoft),
                        Triple("181", "Women Helpline", NimiDesign.CobaltSoft),
                        Triple("1098", "Childline", NimiDesign.EmeraldSoft),
                        Triple("1930", "Cyber Fraud", NimiDesign.AmberSoft)
                    ).forEach { (num, label, bg) ->
                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.tactilePress {
                                dialNumber(
                                    context,
                                    num,
                                    scope,
                                    snackbarHostState
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Call,
                                    null,
                                    tint = NimiDesign.Obsidian,
                                    modifier = Modifier.size(15.dp)
                                )
                                Column {
                                    Text(
                                        num,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NimiDesign.Obsidian
                                    )
                                    Text(label, fontSize = 10.sp, color = NimiDesign.Slate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

// ============================================================================
// 11. RESILIENT SOS OUTBOX & TRUSTED CONTACTS SCREEN (HONEST DELIVERY STATUS)
// ============================================================================

    @Composable
    private fun SosOutboxAndCircleScreen(
        user: UserAccount,
        gnssFix: GnssFix?,
        fixTickCounter: Int,
        infraConfig: InfrastructureConfig,
        onUpdateBackendUrl: (String) -> Unit,
        sosOutbox: List<SosOutboxEvent>,
        emergencyContacts: List<EmergencyContact>,
        onTriggerSos: () -> Unit,
        onRetryOutboxEvent: (SosOutboxEvent) -> Unit,
        onAddContact: (String, String) -> Unit,
        onRemoveContact: (Long) -> Unit
    ) {
        val context = LocalContext.current
        var nameInput by rememberSaveable { mutableStateOf("") }
        var phoneInput by rememberSaveable { mutableStateOf("") }
        var backendUrlInput by rememberSaveable { mutableStateOf(infraConfig.nimiBackendUrl) }
        val liveAgeSec = remember(gnssFix, fixTickCounter) { gnssFix?.ageSeconds }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
        ) {
            item {
                Text("Resilient SOS Outbox", style = MaterialTheme.typography.displayLarge)
            }

            // SOS Transmitter with Explicit Location Freshness Validation
            item {
                Surface(
                    color = NimiDesign.Obsidian,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CRITICAL SAFETY TRANSMITTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NimiDesign.Crimson,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (gnssFix != null) {
                                "Coord: ${
                                    String.format(
                                        Locale.US,
                                        "%.5f, %.5f",
                                        gnssFix.lat,
                                        gnssFix.lon
                                    )
                                }\nAccuracy: ±${gnssFix.accuracyMeters.roundToInt()}m • Freshness: ${liveAgeSec}s ago"
                            } else {
                                "No active GNSS fix • SOS will queue locally & send emergency alert text"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                color = NimiDesign.Crimson,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .tactilePress(onClick = onTriggerSos)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 13.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Log & Dispatch SOS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.tactilePress {
                                    val osmLink = if (gnssFix != null) {
                                        "https://www.openstreetmap.org/?mlat=${gnssFix.lat}&mlon=${gnssFix.lon}#map=17/${gnssFix.lat}/${gnssFix.lon} (±${gnssFix.accuracyMeters.roundToInt()}m, ${gnssFix.ageSeconds}s old)"
                                    } else "GPS Fix Unavailable"
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "EMERGENCY SOS from ${user.name}! Location: $osmLink"
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            share,
                                            "Share Emergency Location"
                                        )
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 13.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Share,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        "Share",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Persistent SOS Outbox Queue with Explicit Delivery Status
            if (sosOutbox.isNotEmpty()) {
                item {
                    Text(
                        "PERSISTENT SOS OUTBOX LOG (${sosOutbox.size})",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                items(sosOutbox, key = { it.id }) { ev ->
                    Surface(
                        color = NimiDesign.Surface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, ev.deliveryState.badgeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = ev.deliveryState.badgeColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = ev.deliveryState.label.uppercase(Locale.ROOT),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                                Text(
                                    text = if (ev.lat != null && ev.lon != null) {
                                        "${
                                            String.format(
                                                Locale.US,
                                                "%.5f, %.5f",
                                                ev.lat,
                                                ev.lon
                                            )
                                        } (±${ev.accuracyMeters?.roundToInt() ?: 0}m)"
                                    } else "Location unavailable at trigger time",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Logged: ${ev.createdAt} • Retries: ${ev.retryCount}",
                                    fontSize = 10.sp,
                                    color = NimiDesign.Muted
                                )
                            }
                            Surface(
                                color = NimiDesign.SurfaceRecessed,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.tactilePress { onRetryOutboxEvent(ev) }
                            ) {
                                Text(
                                    text = "Retry",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NimiDesign.Obsidian,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Add Trusted Emergency Contact Card
            item {
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Trusted Emergency Contacts (${emergencyContacts.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                        TactileInputBox(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = "Contact Name",
                            leadingIcon = Icons.Outlined.Person
                        )
                        TactileInputBox(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = "Mobile Number",
                            leadingIcon = Icons.Outlined.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                        Surface(
                            color = if (nameInput.isNotBlank() && phoneInput.isNotBlank()) NimiDesign.Obsidian else NimiDesign.SurfaceRecessed,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress {
                                    if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                        onAddContact(nameInput.trim(), phoneInput.trim())
                                        nameInput = ""
                                        phoneInput = ""
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Save Contact",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nameInput.isNotBlank() && phoneInput.isNotBlank()) Color.White else NimiDesign.Muted
                                )
                            }
                        }
                    }
                }
            }

            items(emergencyContacts, key = { it.id }) { contact ->
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, style = MaterialTheme.typography.titleMedium)
                            Text(contact.phone, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:${contact.phone}")
                                    )
                                )
                            }) {
                                Icon(Icons.Outlined.Call, null, tint = NimiDesign.Emerald)
                            }
                            IconButton(onClick = { onRemoveContact(contact.id) }) {
                                Icon(Icons.Outlined.DeleteOutline, null, tint = NimiDesign.Crimson)
                            }
                        }
                    }
                }
            }

            // Self-Hosted PostGIS Backend Endpoint Configuration
            item {
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Self-Hosted Nimi PostGIS Backend (Optional)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TactileInputBox(
                            value = backendUrlInput,
                            onValueChange = {
                                backendUrlInput = it
                                onUpdateBackendUrl(it.trim())
                            },
                            placeholder = "https://api.nimi.org.in/v1/sos",
                            leadingIcon = Icons.Outlined.Dns,
                            imeAction = ImeAction.Done
                        )
                    }
                }
            }
        }
    }

// ============================================================================
// 12. EMAIL AUTH, LEGAL DRILLS, FAKE CALL & VAULT SCREENS
// ============================================================================

    @Composable
    private fun EditorialEmailAuthScreen(
        db: NimiDatabaseHelper,
        onAuthenticated: (UserAccount) -> Unit,
        onTriggerEmergencyFakeCall: () -> Unit,
        onDial112: () -> Unit
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        var isSignUp by rememberSaveable { mutableStateOf(false) }
        var fullName by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        var phone by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        fun submitAuth() {
            keyboardController?.hide()
            val cleanEmail = email.trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                errorMessage = "Enter a valid email address."
                return
            }
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters."
                return
            }
            if (isSignUp) {
                if (fullName.trim().length < 2) {
                    errorMessage = "Enter your full name."
                    return
                }
                val created = db.registerUser(
                    fullName,
                    cleanEmail,
                    phone.ifBlank { "Not provided" },
                    password
                )
                if (created == null) {
                    errorMessage = "Email already registered. Switch to Sign In."
                } else {
                    onAuthenticated(created)
                }
            } else {
                val account = db.loginUser(cleanEmail, password)
                if (account != null) {
                    onAuthenticated(account)
                } else if (!db.emailExists(cleanEmail)) {
                    errorMessage = "No account found with this email. Tap 'Create Account'."
                } else {
                    errorMessage = "Incorrect password."
                }
            }
        }

        Scaffold(
            containerColor = NimiDesign.Canvas,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "nimi.",
                        fontFamily = FontFamily.Serif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = NimiDesign.Obsidian
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = NimiDesign.Surface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, NimiDesign.Hairline),
                            modifier = Modifier.tactilePress(onClick = onTriggerEmergencyFakeCall)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.RingVolume,
                                    null,
                                    tint = NimiDesign.Obsidian,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Fake Call",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NimiDesign.Obsidian
                                )
                            }
                        }
                        Surface(
                            color = NimiDesign.Crimson,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.tactilePress(onClick = onDial112)
                        ) {
                            Text(
                                text = "112 SOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = NimiDesign.Obsidian,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "100% OPEN-SOURCE PUBLIC-BENEFIT SAFETY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NimiDesign.Crimson,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            "MapLibre Vector Maps • Native GNSS • Resilient SOS",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NimiDesign.SurfaceRecessed)
                                .padding(4.dp)
                        ) {
                            listOf(
                                false to "Sign In",
                                true to "Create Account"
                            ).forEach { (mode, label) ->
                                val active = isSignUp == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) NimiDesign.Obsidian else Color.Transparent)
                                        .clickable {
                                            isSignUp = mode
                                            errorMessage = null
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else NimiDesign.Slate
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isSignUp) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                TactileInputBox(
                                    value = fullName,
                                    onValueChange = { fullName = it; errorMessage = null },
                                    placeholder = "Full Name",
                                    leadingIcon = Icons.Outlined.Person,
                                    imeAction = ImeAction.Next,
                                    onAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                                TactileInputBox(
                                    value = phone,
                                    onValueChange = { phone = it; errorMessage = null },
                                    placeholder = "Mobile Number (Optional)",
                                    leadingIcon = Icons.Outlined.Phone,
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next,
                                    onAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            }
                        }

                        TactileInputBox(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            placeholder = "Email Address (you@example.com)",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        TactileInputBox(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            placeholder = "Password (6+ characters)",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            onAction = { submitAuth() }
                        )

                        errorMessage?.let { err ->
                            Surface(
                                color = NimiDesign.CrimsonSoft,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    err,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NimiDesign.Crimson,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Surface(
                            color = NimiDesign.Crimson,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress { submitAuth() }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSignUp) "Create Account" else "Sign In with Email",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LegalEducationAndDrillsScreen() {
        var selectedDrillIndex by rememberSaveable { mutableIntStateOf(0) }
        var chosenOptionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

        val drills = remember {
            listOf(
                Triple(
                    "11:30 PM Cab Police Stop",
                    "Officers stop your cab at night and demand you unlock your phone or visit the station.",
                    listOf(
                        Triple(
                            "Hand over unlocked phone",
                            false,
                            "Never: Warrant or formal seizure memo required (Art. 21 Privacy)."
                        ),
                        Triple(
                            "Cite BNSS § 179 Proviso & § 43(5): 'Women cannot be summoned to a station or detained at night.'",
                            true,
                            "Correct: Citing exact BNSS sections stops unlawful detention."
                        ),
                        Triple(
                            "Argue without naming the law",
                            false,
                            "State the exact section number for immediate legal leverage."
                        )
                    )
                ),
                Triple(
                    "Police Refuse FIR ('Wrong Area')",
                    "Station officer tells you to go to another station where the incident occurred.",
                    listOf(
                        Triple(
                            "Travel to the other station",
                            false,
                            "Wastes critical evidence-collection time."
                        ),
                        Triple(
                            "Demand Mandatory Zero FIR under BNSS § 173(1); refusal is punishable under BNS § 199.",
                            true,
                            "Correct: Every station must register a Zero FIR nationwide."
                        ),
                        Triple(
                            "Leave an informal note",
                            false,
                            "Always demand a free signed FIR copy under BNSS § 173(2)."
                        )
                    )
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
        ) {
            item {
                Text("Statutory Rights & Drills", style = MaterialTheme.typography.displayLarge)
            }

            item {
                val (title, prompt, options) = drills[selectedDrillIndex]
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = NimiDesign.Obsidian, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "SCENARIO ${selectedDrillIndex + 1}/${drills.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Next Scenario →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NimiDesign.Crimson,
                                modifier = Modifier.clickable {
                                    chosenOptionIndex = null
                                    selectedDrillIndex = (selectedDrillIndex + 1) % drills.size
                                }
                            )
                        }

                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text(prompt, style = MaterialTheme.typography.bodyLarge)

                        options.forEachIndexed { idx, (optionText, isRight, explanation) ->
                            val isChosen = chosenOptionIndex == idx
                            val cardBg = when {
                                !isChosen -> NimiDesign.Canvas
                                isRight -> NimiDesign.EmeraldSoft
                                else -> NimiDesign.CrimsonSoft
                            }
                            Surface(
                                color = cardBg,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tactilePress { chosenOptionIndex = idx }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(optionText, style = MaterialTheme.typography.titleMedium)
                                    AnimatedVisibility(visible = isChosen) {
                                        Text(
                                            text = explanation,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRight) NimiDesign.Emerald else NimiDesign.Crimson
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("TAP TO FLIP • MYTH VS. STATUTE", style = MaterialTheme.typography.labelMedium)
            }

            item {
                FlipMythCard(
                    myth = "MYTH: Police can summon a woman to the station for routine questioning.",
                    reality = "LAW (BNSS § 179 Proviso): Women & minors under 15 cannot be forced to attend a station; statements must be recorded at home."
                )
            }
            item {
                FlipMythCard(
                    myth = "MYTH: Non-consensual morphed photos take weeks to remove.",
                    reality = "LAW (IT Rules 3(2)(b)): Social platforms must strip non-consensual intimate images within 24 hours of reporting."
                )
            }
        }
    }

    @Composable
    private fun FlipMythCard(myth: String, reality: String) {
        var flipped by rememberSaveable { mutableStateOf(false) }
        val bg by animateColorAsState(
            if (flipped) NimiDesign.EmeraldSoft else NimiDesign.Surface,
            label = "flip"
        )

        Surface(
            color = bg,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (flipped) NimiDesign.Emerald else NimiDesign.Hairline),
            modifier = Modifier
                .fillMaxWidth()
                .tactilePress { flipped = !flipped }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = if (flipped) NimiDesign.Emerald else NimiDesign.Crimson,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (flipped) "ENFORCED LAW" else "MYTH (TAP TO FLIP)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Icon(
                        Icons.Outlined.Cached,
                        null,
                        tint = NimiDesign.Slate,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(if (flipped) reality else myth, style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    @Composable
    private fun RealisticPhoneCallScreen(
        callerName: String,
        callerNumber: String,
        onEndCall: () -> Unit
    ) {
        val context = LocalContext.current
        var accepted by remember { mutableStateOf(false) }
        var callSeconds by remember { mutableIntStateOf(0) }

        var isMuted by remember { mutableStateOf(false) }
        var isSpeakerOn by remember { mutableStateOf(false) }
        var isOnHold by remember { mutableStateOf(false) }
        var showKeypad by remember { mutableStateOf(false) }
        var dtmfDigits by remember { mutableStateOf("") }
        var ringtoneRef by remember { mutableStateOf<Ringtone?>(null) }

        BackHandler { onEndCall() }

        DisposableEffect(Unit) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = runCatching { RingtoneManager.getRingtone(context, uri) }.getOrNull()
            ringtoneRef = r
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r?.isLooping = true
                }
                r?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                r?.play()
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 900, 700), 0))
            }

            onDispose {
                runCatching { r?.stop() }
                runCatching { vibrator?.cancel() }
            }
        }

        LaunchedEffect(accepted) {
            if (accepted) {
                runCatching { ringtoneRef?.stop() }
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                runCatching { vibrator?.cancel() }
                while (true) {
                    delay(1000)
                    if (!isOnHold) callSeconds++
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = CircleShape,
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            callerName.take(1),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (dtmfDigits.isNotEmpty() && showKeypad) dtmfDigits else callerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(callerNumber, fontSize = 14.sp, color = Color(0xFF94A3B8))
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (accepted) {
                        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                "HD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = when {
                            !accepted -> "Incoming mobile call..."
                            isOnHold -> "On Hold"
                            else -> String.format(
                                Locale.US,
                                "%02d:%02d",
                                callSeconds / 60,
                                callSeconds % 60
                            )
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (accepted) Color(0xFF34D399) else Color(0xFFCBD5E1)
                    )
                }
            }

            if (accepted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showKeypad) {
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("*", "0", "#")
                        )
                        keys.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { digit ->
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clickable { dtmfDigits += digit }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                digit,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        TextButton(onClick = { showKeypad = false }) {
                            Text("Hide Keypad", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallOptionButton(
                                icon = if (isMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                                label = "Mute",
                                active = isMuted,
                                onClick = { isMuted = !isMuted }
                            )
                            InCallOptionButton(
                                icon = Icons.Outlined.Dialpad,
                                label = "Keypad",
                                active = showKeypad,
                                onClick = { showKeypad = true }
                            )
                            InCallOptionButton(
                                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                                label = "Speaker",
                                active = isSpeakerOn,
                                onClick = { isSpeakerOn = !isSpeakerOn }
                            )
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallOptionButton(Icons.Outlined.PersonAdd, "Add call", false) {}
                            InCallOptionButton(Icons.Outlined.Videocam, "Video call", false) {}
                            InCallOptionButton(
                                icon = Icons.Outlined.Pause,
                                label = "Hold",
                                active = isOnHold,
                                onClick = { isOnHold = !isOnHold }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!accepted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = onEndCall,
                            containerColor = NimiDesign.Crimson,
                            shape = CircleShape,
                            modifier = Modifier.size(70.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CallEnd,
                                "Decline",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Decline", fontSize = 12.sp, color = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = { accepted = true },
                            containerColor = Color(0xFF10B981),
                            shape = CircleShape,
                            modifier = Modifier.size(70.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Call,
                                "Answer",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Answer", fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = NimiDesign.Crimson,
                        shape = CircleShape,
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CallEnd,
                            "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun InCallOptionButton(
        icon: ImageVector,
        label: String,
        active: Boolean,
        onClick: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(76.dp)
        ) {
            Surface(
                color = if (active) Color.White else Color(0xFF1E293B),
                shape = CircleShape,
                modifier = Modifier
                    .size(58.dp)
                    .clickable(onClick = onClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        label,
                        tint = if (active) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1)
        }
    }

    @Composable
    private fun EvidenceVaultScreen(
        evidenceItems: MutableList<EvidenceItem>,
        snackbarHostState: SnackbarHostState
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var complainantName by rememberSaveable { mutableStateOf("") }
        var stationName by rememberSaveable { mutableStateOf("") }
        var incidentSummary by rememberSaveable { mutableStateOf("") }

        val importLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                if (uris.isNotEmpty()) {
                    scope.launch {
                        val results = withContext(Dispatchers.IO) {
                            uris.map { uri -> runCatching { hashLocalDocument(context, uri) } }
                        }
                        results.forEach { it.onSuccess { item -> evidenceItems.add(item) } }
                        snackbarHostState.showSnackbar("${results.count { it.isSuccess }} file(s) hashed locally.")
                    }
                }
            }

        val pdfLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
                if (uri != null) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            runCatching { writeEvidenceInventoryPdf(context, uri, evidenceItems) }
                        }.onSuccess {
                            snackbarHostState.showSnackbar("PDF Evidence Inventory saved.")
                        }
                    }
                }
            }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
        ) {
            item {
                Text("SHA-256 Vault & Drafts", style = MaterialTheme.typography.displayLarge)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = NimiDesign.Crimson,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .tactilePress { importLauncher.launch(arrayOf("*/*")) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.UploadFile,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Hash Evidence Files",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    if (evidenceItems.isNotEmpty()) {
                        Surface(
                            color = NimiDesign.Obsidian,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .tactilePress { pdfLauncher.launch("Nimi_Evidence_Hashes.pdf") }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Export PDF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            items(evidenceItems) { item ->
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatBytes(item.sizeBytes),
                                fontSize = 11.sp,
                                color = NimiDesign.Slate
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "SHA-256: ${item.sha256}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NimiDesign.Emerald
                        )
                    }
                }
            }

            item {
                Surface(
                    color = NimiDesign.Surface,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, NimiDesign.Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "BNSS § 173(4) SP Notice / POSH Draft",
                            style = MaterialTheme.typography.titleLarge
                        )
                        TactileInputBox(
                            value = complainantName,
                            onValueChange = { complainantName = it },
                            placeholder = "Your Full Name",
                            leadingIcon = Icons.Outlined.Person
                        )
                        TactileInputBox(
                            value = stationName,
                            onValueChange = { stationName = it },
                            placeholder = "Police Station / Employer Name",
                            leadingIcon = Icons.Outlined.AccountBalance
                        )
                        TactileInputBox(
                            value = incidentSummary,
                            onValueChange = { incidentSummary = it },
                            placeholder = "Incident Summary (Date, Time, Place)",
                            leadingIcon = Icons.Outlined.Description,
                            imeAction = ImeAction.Done
                        )

                        Surface(
                            color = NimiDesign.Obsidian,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .tactilePress {
                                    val draft = """
                                    TO: The Superintendent of Police / Presiding Officer
                                    TARGET: ${stationName.ifBlank { "[Police Station / Organization]" }}
                                    SUBJECT: STATUTORY REPRESENTATION UNDER SECTION 173(4) BNSS 2023

                                    1. Complainant: ${complainantName.ifBlank { "[Complainant Name]" }}
                                    2. Summary: ${incidentSummary.ifBlank { "[Details]" }}
                                    3. SHA-256 Evidence Files Preserved: ${evidenceItems.size}

                                    Prayer: Register Zero FIR / Initiate statutory inquiry immediately.
                                """.trimIndent()
                                    val cb =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Legal Notice", draft))
                                    scope.launch { snackbarHostState.showSnackbar("Legal Notice copied.") }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Copy Legal Notice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun StealthNotesScreen(onUnlock: () -> Unit) {
        val context = LocalContext.current
        BackHandler { (context as? ComponentActivity)?.moveTaskToBack(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NimiDesign.Canvas)
                .systemBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notes", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onUnlock) {
                    Text("Done", fontSize = 12.sp, color = NimiDesign.Slate)
                }
            }
            Surface(
                color = NimiDesign.Surface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, NimiDesign.Hairline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Weekly Checklist", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "• Green tea, almonds, milk\n• Notebook refills\n• Return library books Friday",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

// ============================================================================
// 13. OPEN-SOURCE GEOCODING, OSRM ROUTING, OVERPASS POI & BACKEND CLIENTS
// ============================================================================

    @Suppress("DEPRECATION")
    private suspend fun searchOpenStreetMapPlaces(
        context: Context,
        db: NimiDatabaseHelper,
        photonBaseUrl: String,
        query: String,
        biasFix: GnssFix?
    ): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) return@withContext emptyList()

        // 1. L1 Cache Check
        db.getCachedGeocode(cleanQuery)?.let { cached ->
            if (cached.isNotEmpty()) return@withContext cached
        }

        val results = mutableListOf<PlaceSuggestion>()

        // 2. Primary: Photon OpenStreetMap Geocoder (HTTP with proper connection pooling & stream closure)
        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val biasParams = if (biasFix != null) {
                "&lat=${biasFix.lat}&lon=${biasFix.lon}"
            } else {
                "&lat=28.6139&lon=77.2090" // Default Indian National Capital Region center
            }
            val targetUrl = "$photonBaseUrl/api/?q=$encoded$biasParams&limit=5"
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "NimiPublicSafety/4.0 (Android; OpenSource)")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 3500
                readTimeout = 3500
                instanceFollowRedirects = true
            }

            try {
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonText =
                        conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val root = JSONObject(jsonText)
                    val features = root.optJSONArray("features") ?: JSONArray()

                    for (i in 0 until features.length()) {
                        val feature = features.optJSONObject(i) ?: continue
                        val geom = feature.optJSONObject("geometry") ?: continue
                        val coords = geom.optJSONArray("coordinates") ?: continue

                        if (coords.length() >= 2) {
                            val lon = coords.optDouble(0, Double.NaN)
                            val lat = coords.optDouble(1, Double.NaN)
                            if (lat.isNaN() || lon.isNaN()) continue

                            val props = feature.optJSONObject("properties") ?: JSONObject()
                            val name = props.optString("name", "").ifBlank { cleanQuery }
                            val city = props.optString("city", props.optString("state", "India"))
                            val street = props.optString("street", props.optString("district", ""))
                            val subtitle =
                                listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")

                            results.add(PlaceSuggestion(name, subtitle, lat, lon))
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            // Network failure, timeout, or DNS resolution failure: proceed to Android Geocoder fallback
        }

        // 3. Fallback: Native Android On-Device Geocoder
        if (results.isEmpty()) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ Async Geocoder API using suspendCancellableCoroutine
                    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocationName(
                            "$cleanQuery, India",
                            5,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    val mapped = addresses.map { addr ->
                                        val title =
                                            addr.featureName ?: addr.subLocality ?: addr.locality
                                            ?: cleanQuery
                                        val sub = listOfNotNull(
                                            addr.subLocality,
                                            addr.locality,
                                            addr.adminArea
                                        )
                                            .distinct()
                                            .joinToString(", ")
                                        PlaceSuggestion(title, sub, addr.latitude, addr.longitude)
                                    }
                                    continuation.resume(mapped, null)
                                }

                                override fun onError(errorMessage: String?) {
                                    continuation.resume(emptyList(), null)
                                }
                            })
                    }.let { results.addAll(it) }
                } else {
                    // Legacy Synchronous Geocoder
                    val addresses: List<Address>? =
                        geocoder.getFromLocationName("$cleanQuery, India", 5)
                    addresses?.forEach { addr ->
                        val title =
                            addr.featureName ?: addr.subLocality ?: addr.locality ?: cleanQuery
                        val sub = listOfNotNull(addr.subLocality, addr.locality, addr.adminArea)
                            .distinct()
                            .joinToString(", ")
                        results.add(PlaceSuggestion(title, sub, addr.latitude, addr.longitude))
                    }
                }
            } catch (_: Exception) {
                // Geocoder service backend unserviceable or device offline
            }
        }

        // 4. Update L1 Cache with successful suggestions
        if (results.isNotEmpty()) {
            db.saveCachedGeocode(cleanQuery, results)
        }

        results
    }

    private suspend fun fetchOverpassEmergencyPois(
        overpassUrl: String,
        lat: Double,
        lon: Double,
        radiusMeters: Int
    ): List<OsmEmergencyPoi> = withContext(Dispatchers.IO) {
        val list = mutableListOf<OsmEmergencyPoi>()
        runCatching {
            val query = """
            [out:json][timeout:10];
            (
              node["amenity"="police"](around:$radiusMeters,$lat,$lon);
              way["amenity"="police"](around:$radiusMeters,$lat,$lon);
              node["amenity"="hospital"](around:$radiusMeters,$lat,$lon);
              way["amenity"="hospital"](around:$radiusMeters,$lat,$lon);
              node["amenity"="pharmacy"](around:$radiusMeters,$lat,$lon);
            );
            out center 35;
        """.trimIndent()
            val encoded = "data=" + URLEncoder.encode(query, "UTF-8")
            val conn = (URL(overpassUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("User-Agent", "NimiFOSSSafety/6.0")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connectTimeout = 7000
                readTimeout = 7000
            }
            OutputStreamWriter(conn.outputStream).use { it.write(encoded) }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val elements = json.getJSONArray("elements")

            for (i in 0 until elements.length()) {
                val el = elements.getJSONObject(i)
                val tags = el.optJSONObject("tags") ?: continue
                val name = tags.optString("name", "").ifBlank { tags.optString("name:en", "") }
                if (name.isBlank()) continue

                val pLat = if (el.has("lat")) el.getDouble("lat") else el.optJSONObject("center")
                    ?.optDouble("lat") ?: continue
                val pLon = if (el.has("lon")) el.getDouble("lon") else el.optJSONObject("center")
                    ?.optDouble("lon") ?: continue
                val amenity = tags.optString("amenity", "police")
                val cat = when (amenity) {
                    "hospital", "clinic" -> OsmPoiCategory.HOSPITAL
                    "pharmacy" -> OsmPoiCategory.PHARMACY
                    else -> OsmPoiCategory.POLICE
                }
                val phone =
                    tags.optString("phone", tags.optString("contact:phone", "")).ifBlank { null }
                val hours = tags.optString("opening_hours", "").ifBlank { null }
                val operator = tags.optString("operator", "").ifBlank { null }
                val addr = listOf(
                    tags.optString("addr:street", ""),
                    tags.optString("addr:suburb", ""),
                    tags.optString("addr:city", "")
                ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null }

                list.add(
                    OsmEmergencyPoi(
                        osmId = el.getLong("id"),
                        name = name,
                        category = cat,
                        lat = pLat,
                        lon = pLon,
                        phone = phone,
                        openingHours = hours,
                        operatorName = operator,
                        address = addr
                    )
                )
            }
        }
        list
    }

    private suspend fun computeOsrmRoutes(
        osrmBaseUrl: String,
        start: LatLng,
        end: LatLng,
        pins: List<SpatialPin>
    ): List<RouteResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<RouteResult>()

        runCatching {
            val urlStr =
                "$osrmBaseUrl/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson&alternatives=true"
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "NimiFOSSSafety/6.0")
                connectTimeout = 6000
                readTimeout = 6000
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val routesArr = json.getJSONArray("routes")

            for (i in 0 until routesArr.length()) {
                val rObj = routesArr.getJSONObject(i)
                val distKm = (rObj.getDouble("distance") / 1000.0).coerceAtLeast(0.1)
                val coords = rObj.getJSONObject("geometry").getJSONArray("coordinates")
                val pts = mutableListOf<LatLng>()
                for (j in 0 until coords.length()) {
                    val pair = coords.getJSONArray(j)
                    pts.add(LatLng(pair.getDouble(1), pair.getDouble(0)))
                }
                val hazards = findFlagsNearRoute(pts, pins)
                results.add(
                    RouteResult(
                        name = if (i == 0) "Primary Street Route" else "Alternative Route ${i + 1}",
                        allPoints = pts,
                        segments = classifyRouteSegments(pts, pins),
                        distanceKm = distKm,
                        durationMinutes = computeModalTimes(distKm),
                        flagsOnRoute = hazards,
                        isSaferBypass = i > 0 && hazards.isEmpty()
                    )
                )
            }
        }

        if (results.size == 1 && results[0].flagsOnRoute.isNotEmpty()) {
            val hazard = results[0].flagsOnRoute.first()
            val safeWaypoint =
                pins.firstOrNull { it.type.isSafeHaven }?.let { LatLng(it.lat, it.lon) }
                    ?: LatLng(hazard.lat + 0.0038, hazard.lon - 0.0038)
            val detourPts = interpolateLatLng(start, safeWaypoint, 16) + interpolateLatLng(
                safeWaypoint,
                end,
                16
            )
            val detourDistKm = results[0].distanceKm * 1.15
            val detourHazards = findFlagsNearRoute(detourPts, pins)
            results.add(
                RouteResult(
                    name = "Safer Bypass Route",
                    allPoints = detourPts,
                    segments = classifyRouteSegments(detourPts, pins),
                    distanceKm = detourDistKm,
                    durationMinutes = computeModalTimes(detourDistKm),
                    flagsOnRoute = detourHazards,
                    isSaferBypass = true
                )
            )
        }

        results
    }

    private suspend fun transmitSosToSelfHostedBackend(
        backendUrl: String,
        event: SosOutboxEvent
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(backendUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }
            val payload = JSONObject().apply {
                put("user_name", event.userName)
                put("user_email", event.userEmail)
                put("lat", event.lat)
                put("lon", event.lon)
                put("accuracy_meters", event.accuracyMeters)
                put("fix_age_seconds", event.fixAgeSeconds)
                put("created_at", event.createdAt)
            }.toString()
            OutputStreamWriter(conn.outputStream).use { it.write(payload) }
            conn.responseCode in 200..299
        }.getOrDefault(false)
    }

    private fun classifyRouteSegments(
        points: List<LatLng>,
        pins: List<SpatialPin>
    ): List<RouteSegment> {
        if (points.size < 2) return emptyList()
        val segments = mutableListOf<RouteSegment>()
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val midLat = (p1.latitude + p2.latitude) / 2.0
            val midLon = (p1.longitude + p2.longitude) / 2.0
            val isDanger = pins.any {
                !it.type.isSafeHaven && haversineMeters(
                    midLat,
                    midLon,
                    it.lat,
                    it.lon
                ) <= 220.0
            }
            segments.add(RouteSegment(listOf(p1, p2), isDanger))
        }
        return segments
    }

    private fun findFlagsNearRoute(points: List<LatLng>, pins: List<SpatialPin>): List<SpatialPin> {
        return pins.filter { pin ->
            !pin.type.isSafeHaven && points.any { pt ->
                haversineMeters(pt.latitude, pt.longitude, pin.lat, pin.lon) <= 220.0
            }
        }
    }

    private fun computeModalTimes(distanceKm: Double): Map<TravelMode, Int> {
        return TravelMode.entries.associateWith { mode ->
            ((distanceKm / mode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(1)
        }
    }

    private fun interpolateLatLng(start: LatLng, end: LatLng, steps: Int): List<LatLng> {
        return (0..steps).map { i ->
            val f = i.toDouble() / steps.toDouble()
            LatLng(
                start.latitude + (end.latitude - start.latitude) * f,
                start.longitude + (end.longitude - start.longitude) * f
            )
        }
    }

    private fun triggerHapticPulse(context: Context) {
        runCatching {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(140, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun dispatchSmsWithFreshness(
        context: Context,
        fix: GnssFix?,
        contacts: List<EmergencyContact>
    ): Boolean {
        val locText = if (fix != null) {
            val freshnessTag =
                if (fix.isFresh) "LIVE FIX (${fix.ageSeconds}s ago)" else "LAST KNOWN (${fix.ageSeconds}s old)"
            "https://www.openstreetmap.org/?mlat=${fix.lat}&mlon=${fix.lon}#map=17/${fix.lat}/${fix.lon} [$freshnessTag, ±${fix.accuracyMeters.roundToInt()}m]"
        } else {
            "[GPS fix unavailable at trigger time]"
        }
        val body = "EMERGENCY SOS from Nimi! I need assistance. Location: $locText"
        val numbers = contacts.joinToString(";") { it.phone }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$numbers")
            putExtra("sms_body", body)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private suspend fun hashLocalDocument(context: Context, uri: Uri): EvidenceItem =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            var displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected file"
            var declaredSize: Long? = null
            resolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) displayName = cursor.getString(nameIndex) ?: displayName
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) declaredSize =
                        cursor.getLong(sizeIndex)
                }
            }
            val digest = MessageDigest.getInstance("SHA-256")
            var actualBytes = 0L
            val input = resolver.openInputStream(uri) ?: throw IOException("Cannot open file")
            input.use { stream ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (count > 0) {
                        digest.update(buffer, 0, count)
                        actualBytes += count
                    }
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            EvidenceItem(displayName, declaredSize ?: actualBytes, hash, uri.toString())
        }

    private fun formatBytes(bytes: Long?): String {
        if (bytes == null || bytes < 0L) return "Unknown"
        return when {
            bytes >= 1024L * 1024L -> String.format(
                Locale.ROOT,
                "%.1f MB",
                bytes / (1024.0 * 1024.0)
            )

            bytes >= 1024L -> String.format(Locale.ROOT, "%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun dialNumber(
        context: Context,
        number: String,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        }.onFailure {
            scope.launch { snackbarHostState.showSnackbar("Dialer unavailable.") }
        }
    }

    private fun writeEvidenceInventoryPdf(
        context: Context,
        destination: Uri,
        items: List<EvidenceItem>
    ) {
        val document = PdfDocument()
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(15, 17, 21)
            textSize = 20f
            typeface = Typeface.create("serif", Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(82, 88, 102)
            textSize = 10f
        }
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawText("NIMI SHA-256 EVIDENCE INVENTORY", 40f, 55f, titlePaint)
        canvas.drawText(
            "Generated: ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}",
            40f,
            75f,
            bodyPaint
        )
        var y = 110f
        items.forEachIndexed { idx, item ->
            if (y < 780f) {
                canvas.drawText(
                    "${idx + 1}. ${item.displayName} (${formatBytes(item.sizeBytes)})",
                    40f,
                    y,
                    bodyPaint
                )
                y += 16f
                canvas.drawText("   SHA-256: ${item.sha256}", 40f, y, bodyPaint)
                y += 24f
            }
        }
        document.finishPage(page)
        try {
            context.contentResolver.openOutputStream(destination, "w")?.use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }