package app.accrescent.client.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.NetworkType
import app.accrescent.client.util.isPrivileged
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
    }
    val requireUserAction = data.map { preferences ->
        preferences[PreferencesKeys.REQUIRE_USER_ACTION] ?: !context.isPrivileged()
    }
    val automaticUpdates = data.map { preferences ->
        preferences[PreferencesKeys.AUTOMATIC_UPDATES] ?: true
    }
    val networkType = data.map { preferences ->
        preferences[PreferencesKeys.UPDATER_NETWORK_TYPE] ?: NetworkType.CONNECTED.name
    }
    val pitchBlackBackground = data.map { preferences ->
        preferences[PreferencesKeys.PITCH_BLACK_BACKGROUND] ?: false
    }

    suspend fun setDynamicColor(dynamicColor: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.DYNAMIC_COLOR] = dynamicColor
        }
    }

    suspend fun setAutomaticUpdates(automaticUpdates: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AUTOMATIC_UPDATES] = automaticUpdates
        }
    }

    suspend fun setRequireUserAction(requireUserAction: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.REQUIRE_USER_ACTION] = requireUserAction
        }
    }

    suspend fun setNetworkType(networkType: String) {
        context.dataStore.edit {
            it[PreferencesKeys.UPDATER_NETWORK_TYPE] = networkType
        }
    }

    suspend fun setPitchBlackBackground(pitchBlackBackground: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.PITCH_BLACK_BACKGROUND] = pitchBlackBackground
        }
    }

    private object PreferencesKeys {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REQUIRE_USER_ACTION = booleanPreferencesKey("require_user_action")
        val AUTOMATIC_UPDATES = booleanPreferencesKey("automatic_updates")
        val UPDATER_NETWORK_TYPE = stringPreferencesKey("updater_network_type")
        val PITCH_BLACK_BACKGROUND = booleanPreferencesKey("pitch_black_background")
    }
}
