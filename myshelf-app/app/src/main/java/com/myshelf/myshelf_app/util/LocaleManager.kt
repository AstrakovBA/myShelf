package com.myshelf.myshelf_app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.myshelf.myshelf_app.util.Constants
import java.util.Locale

object LocaleManager {
    fun getSavedLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(Constants.PREF_LANGUAGE, Constants.DEFAULT_LANGUAGE)
            ?: Constants.DEFAULT_LANGUAGE
    }

    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun applyToActivity(activity: Activity, languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}