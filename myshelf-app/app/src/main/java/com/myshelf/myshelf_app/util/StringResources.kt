package com.myshelf.myshelf_app.util

import android.content.Context
import androidx.annotation.StringRes

object StringResources {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context
    }

    fun getString(@StringRes resId: Int): String = appContext.getString(resId)

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        appContext.getString(resId, *formatArgs)
}
