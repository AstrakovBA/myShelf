package com.myshelf.myshelf_app.util

object Constants {

    // Network
    // ✅ Для ЭМУЛЯТОРА Android Studio: используйте http://10.0.2.2:8080/api/
    // ✅ Для ФИЗИЧЕСКОГО смартфона (локальная сеть): используйте http://192.168.0.14:8080/api/
    // 📝 Рекомендуется использовать ApiConfig.BASE_URL вместо константы BASE_URL для большей гибкости
    const val BASE_URL = ApiConfig.BASE_URL
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
    const val PREF_IS_GUEST = "pref_is_guest"

    const val GUEST_EMAIL = "guest@local"

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

    const val DEFAULT_LANGUAGE = "en"
}
