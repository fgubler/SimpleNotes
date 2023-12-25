package ch.abwesend.simplenotes.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val SETTINGS_KEY = "settings"
private const val NOTES_VALUE_KEY = "notes"

object NoteRepository {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_KEY)
    private val repositoryScope = CoroutineScope(SupervisorJob())
    private val notesValueKey: Preferences.Key<String>
        get() = stringPreferencesKey(NOTES_VALUE_KEY)

    fun getNotes(context: Context): Flow<String> {
        return context.dataStore.data.map { it.getNotes() }
    }

    fun storeNotes(context: Context, newValue: String) {
        val dataStore = context.dataStore

        repositoryScope.launch {
            dataStore.edit { preferences ->
                preferences[notesValueKey] = newValue
            }
        }
    }

    private fun Preferences.getNotes(): String {
        return get(notesValueKey).orEmpty()
    }
}