package com.martin.storage.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "grocery_app_v1")

val appJson = Json {
    encodeDefaults   = true
    ignoreUnknownKeys = true
    prettyPrint       = false
    coerceInputValues = true
}

object DataKeys {
    val GROCERY_ITEMS   = stringPreferencesKey("grocery_items")
    val RECIPES         = stringPreferencesKey("recipes")
    val MEAL_PLAN       = stringPreferencesKey("meal_plan")
    val PREPARED_MEALS  = stringPreferencesKey("prepared_meals")
    val USER_SETTINGS   = stringPreferencesKey("user_settings")
}

suspend inline fun <reified T> Context.saveList(key: Preferences.Key<String>, items: List<T>) {
    appDataStore.edit { it[key] = appJson.encodeToString(items) }
}

inline fun <reified T> Context.loadList(key: Preferences.Key<String>): Flow<List<T>> =
    appDataStore.data.map { prefs ->
        prefs[key]?.let { json ->
            try { appJson.decodeFromString<List<T>>(json) } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

suspend inline fun <reified T> Context.saveObject(key: Preferences.Key<String>, obj: T) {
    appDataStore.edit { it[key] = appJson.encodeToString(obj) }
}

inline fun <reified T> Context.loadObject(key: Preferences.Key<String>, default: T): Flow<T> =
    appDataStore.data.map { prefs ->
        prefs[key]?.let { json ->
            try { appJson.decodeFromString<T>(json) } catch (_: Exception) { default }
        } ?: default
    }