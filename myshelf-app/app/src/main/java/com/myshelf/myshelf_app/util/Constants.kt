package com.myshelf.myshelf_app.util

object Constants {

    // Network
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // SharedPreferences
    const val PREFS_NAME = "myshelf_preferences"
    const val PREF_ACCESS_TOKEN = "pref_access_token"
    const val PREF_REFRESH_TOKEN = "pref_refresh_token"
    const val PREF_USER_ID = "pref_user_id"
    const val PREF_USER_EMAIL = "pref_user_email"
    const val PREF_LAST_SYNC_TIMESTAMP = "pref_last_sync_timestamp"
    const val PREF_IS_LOGGED_IN = "pref_is_logged_in"

    // DataStore
    const val DATASTORE_NAME = "myshelf_datastore"
    const val DS_ACCESS_TOKEN = "ds_access_token"
    const val DS_REFRESH_TOKEN = "ds_refresh_token"
    const val DS_USER_ID = "ds_user_id"
    const val DS_USER_EMAIL = "ds_user_email"
    const val DS_THEME_MODE = "ds_theme_mode"
    const val DS_ONBOARDING_COMPLETED = "ds_onboarding_completed"
    const val DS_LAST_SYNC_TIMESTAMP = "ds_last_sync_timestamp"
    const val DS_OFFLINE_MODE_ENABLED = "ds_offline_mode_enabled"
    const val DS_LANGUAGE = "ds_language"

    const val DEFAULT_LANGUAGE = "ru"
}
