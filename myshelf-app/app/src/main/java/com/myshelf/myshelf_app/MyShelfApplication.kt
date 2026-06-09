package com.myshelf.myshelf_app

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.myshelf.myshelf_app.data.remote.RetrofitClient
import com.myshelf.myshelf_app.data.sync.SyncManager
import com.myshelf.myshelf_app.util.LocaleManager
import com.myshelf.myshelf_app.util.StringResources

class MyShelfApplication : Application() {

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            SyncManager.requestImmediateSync(this@MyShelfApplication)
        }
    }

    override fun attachBaseContext(base: Context) {
        val language = LocaleManager.getSavedLanguage(base)
        val localizedContext = LocaleManager.wrapContext(base, language)
        StringResources.init(localizedContext)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        SyncManager.schedulePeriodicSync(this)
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
}
