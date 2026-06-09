package com.myshelf.myshelf_app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.myshelf.myshelf_app.data.remote.RetrofitClient
import com.myshelf.myshelf_app.data.repository.RepositoryProvider
import com.myshelf.myshelf_app.util.LocaleManager
import com.myshelf.myshelf_app.util.Result as AppResult
import com.myshelf.myshelf_app.util.StringResources
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val appContext = applicationContext
        StringResources.init(
            LocaleManager.wrapContext(appContext, LocaleManager.getSavedLanguage(appContext))
        )
        RetrofitClient.init(appContext)

        val tokenManager = RetrofitClient.getTokenManager(appContext)
        val userId = tokenManager.getUserId()
        val token = tokenManager.getToken()

        if (
            userId.isNullOrBlank() ||
            token.isNullOrBlank() ||
            tokenManager.isGuestMode() ||
            !tokenManager.isLoggedIn()
        ) {
            return ListenableWorker.Result.success()
        }

        val itemsRepository = RepositoryProvider.itemsRepository(appContext)
        val outfitsRepository = RepositoryProvider.outfitsRepository(appContext)

        return try {
            val itemsResult = itemsRepository.syncItemsWithServer(userId)
            val outfitsResult = outfitsRepository.syncOutfitsWithServer(userId)

            when {
                shouldRetry(itemsResult) || shouldRetry(outfitsResult) ->
                    ListenableWorker.Result.retry()
                itemsResult is AppResult.Error -> ListenableWorker.Result.failure()
                outfitsResult is AppResult.Error -> ListenableWorker.Result.failure()
                else -> ListenableWorker.Result.success()
            }
        } catch (e: IOException) {
            ListenableWorker.Result.retry()
        } catch (e: UnknownHostException) {
            ListenableWorker.Result.retry()
        } catch (e: SocketTimeoutException) {
            ListenableWorker.Result.retry()
        }
    }

    private fun shouldRetry(result: AppResult<Unit>): Boolean {
        if (result !is AppResult.Error) return false
        val cause = result.cause
        return cause is IOException ||
            cause is UnknownHostException ||
            cause is SocketTimeoutException
    }
}
