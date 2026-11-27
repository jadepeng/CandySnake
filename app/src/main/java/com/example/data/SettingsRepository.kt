package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.ControlMode
import com.example.game.GameMode
import com.example.game.SnakeSkin
import com.example.game.SpeedPreset
import com.example.game.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.candySettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "candy_snake_settings"
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val SELECTED_MODE = stringPreferencesKey("selected_mode")
        val CONTROL_MODE = stringPreferencesKey("control_mode")
        val SNAKE_SKIN = stringPreferencesKey("snake_skin")
        val SPEED_PRESET = stringPreferencesKey("speed_preset")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }

    val settingsFlow: Flow<UserSettings> = context.candySettingsDataStore.data.map { prefs ->
        UserSettings(
            selectedMode = GameMode.fromKey(prefs[Keys.SELECTED_MODE]),
            controlMode = ControlMode.fromKey(prefs[Keys.CONTROL_MODE]),
            snakeSkin = SnakeSkin.fromKey(prefs[Keys.SNAKE_SKIN]),
            speedPreset = SpeedPreset.fromKey(prefs[Keys.SPEED_PRESET]),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
        )
    }

    suspend fun updateMode(mode: GameMode) {
        context.candySettingsDataStore.edit { it[Keys.SELECTED_MODE] = mode.storageKey }
    }

    suspend fun updateControlMode(controlMode: ControlMode) {
        context.candySettingsDataStore.edit { it[Keys.CONTROL_MODE] = controlMode.storageKey }
    }

    suspend fun updateSkin(skin: SnakeSkin) {
        context.candySettingsDataStore.edit { it[Keys.SNAKE_SKIN] = skin.storageKey }
    }

    suspend fun updateSpeedPreset(preset: SpeedPreset) {
        context.candySettingsDataStore.edit { it[Keys.SPEED_PRESET] = preset.storageKey }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.candySettingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.candySettingsDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }
}
