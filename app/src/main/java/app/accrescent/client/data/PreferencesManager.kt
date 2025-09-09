package app.accrescent.client.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.NetworkType
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

    val data = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val dynamicColor = data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] == true
    }
    val theme = data.map { preferences ->
        preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM.name
    }
    val automaticUpdates = data.map { preferences ->
        preferences[PreferencesKeys.AUTOMATIC_UPDATES] != false
    }
    val networkType = data.map { preferences ->
        preferences[PreferencesKeys.UPDATER_NETWORK_TYPE] ?: NetworkType.UNMETERED.name
    }
    val donateRequestLastSeen = data.map { preferences ->
        preferences[PreferencesKeys.DONATE_REQUEST_LAST_SEEN] ?: 0
    }

    suspend fun setDynamicColor(dynamicColor: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.DYNAMIC_COLOR] = dynamicColor
        }
    }

    suspend fun setTheme(theme: Theme) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun setAutomaticUpdates(automaticUpdates: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AUTOMATIC_UPDATES] = automaticUpdates
        }
    }

    suspend fun setNetworkType(networkType: String) {
        context.dataStore.edit {
            it[PreferencesKeys.UPDATER_NETWORK_TYPE] = networkType
        }
    }

    suspend fun setDonateRequestLastSeen(lastSeen: Long) {
        context.dataStore.edit {
            it[PreferencesKeys.DONATE_REQUEST_LAST_SEEN] = lastSeen
        }
    }

    private object PreferencesKeys {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEME = stringPreferencesKey("theme")
        val AUTOMATIC_UPDATES = booleanPreferencesKey("automatic_updates")
        val UPDATER_NETWORK_TYPE = stringPreferencesKey("updater_network_type")
        val DONATE_REQUEST_LAST_SEEN = longPreferencesKey("donate_request_last_seen")
    }
}
