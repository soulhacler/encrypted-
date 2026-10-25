package io.github.soulhacler.encrypted.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.soulhacler.encrypted.core.engine.GameConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Settings(
    val roundSeconds: Int = GameConfig.DEFAULT_ROUND_SECONDS,
    val soundEnabled: Boolean = true,
    val countdownSounds: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val teamNames: List<String> = emptyList(),
    val playerNames: List<String> = emptyList(),
    /** Best Classic score for each round length, in seconds. */
    val classicBest: Map<Int, Int> = emptyMap(),
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val ROUND_SECONDS = intPreferencesKey("round_seconds")
        val SOUND = booleanPreferencesKey("sound")
        val COUNTDOWN_SOUNDS = booleanPreferencesKey("countdown_sounds")
        val HAPTICS = booleanPreferencesKey("haptics")
        val TEAM_NAMES = stringPreferencesKey("team_names")
        val PLAYER_NAMES = stringPreferencesKey("player_names")
        const val BEST_PREFIX = "classic_best_"
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            roundSeconds = (prefs[Keys.ROUND_SECONDS] ?: GameConfig.DEFAULT_ROUND_SECONDS)
                .coerceIn(GameConfig.MIN_ROUND_SECONDS, GameConfig.MAX_ROUND_SECONDS),
            soundEnabled = prefs[Keys.SOUND] ?: true,
            countdownSounds = prefs[Keys.COUNTDOWN_SOUNDS] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            teamNames = prefs[Keys.TEAM_NAMES].decodeNames(),
            playerNames = prefs[Keys.PLAYER_NAMES].decodeNames(),
            classicBest = prefs.asMap().mapNotNull { (key, value) ->
                val seconds = key.name.removePrefix(Keys.BEST_PREFIX)
                if (key.name.startsWith(Keys.BEST_PREFIX) && value is Int) seconds.toIntOrNull()?.let { it to value } else null
            }.toMap(),
        )
    }

    suspend fun setRoundSeconds(seconds: Int) = edit { it[Keys.ROUND_SECONDS] = seconds }
    suspend fun setSoundEnabled(enabled: Boolean) = edit { it[Keys.SOUND] = enabled }
    suspend fun setCountdownSounds(enabled: Boolean) = edit { it[Keys.COUNTDOWN_SOUNDS] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = edit { it[Keys.HAPTICS] = enabled }
    suspend fun setTeamNames(names: List<String>) = edit { it[Keys.TEAM_NAMES] = names.encodeNames() }
    suspend fun setPlayerNames(names: List<String>) = edit { it[Keys.PLAYER_NAMES] = names.encodeNames() }

    /** Records a Classic score; returns true if it beat the previous best. */
    suspend fun submitClassicScore(roundSeconds: Int, score: Int): Boolean {
        var isBest = false
        edit { prefs ->
            val key = intPreferencesKey(Keys.BEST_PREFIX + roundSeconds)
            val previous = prefs[key] ?: 0
            if (score > previous) {
                prefs[key] = score
                isBest = true
            }
        }
        return isBest
    }

    suspend fun resetBestScores() = edit { prefs ->
        prefs.asMap().keys.filter { it.name.startsWith(Keys.BEST_PREFIX) }.forEach { prefs -= it }
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }

    private fun String?.decodeNames(): List<String> =
        this?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()

    private fun List<String>.encodeNames(): String = joinToString(SEPARATOR.toString())

    private companion object {
        const val SEPARATOR = '\u001F'
    }
}
