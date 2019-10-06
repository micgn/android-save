package de.mg.androidsave.server

import android.content.Context
import android.util.Log
import androidx.work.*
import com.fasterxml.jackson.databind.ObjectMapper
import de.mg.androidsave.db.OnlineStatusModelDao
import de.mg.androidsave.db.SaveDatabase
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.OnlineStatusModel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class CallServerWorkManager {

    private val uniqueWorkName = "sequence"
    private val mapper = ObjectMapper()


    fun create(entry: EntryModel, serverPassword: String, appContext: Context) {
        enqueueServerCall("POST", "entry", serverPassword, entry, appContext = appContext)
    }

    fun update(entry: EntryModel, serverPassword: String, appContext: Context) {
        enqueueServerCall("PUT", "entry", serverPassword, entry, appContext = appContext)
    }

    fun delete(name: String, serverPassword: String, appContext: Context) {
        enqueueServerCall("DELETE", "entry", serverPassword, name, appContext = appContext)
    }


    fun getEntries(serverPassword: String, appContext: Context) {
        if (!alreadyContainsGet())
            enqueueServerCall(
                "GET",
                "entry",
                serverPassword,
                tag = TAG_GET,
                appContext = appContext
            )
    }

    private fun alreadyContainsGet(): Boolean {

        var result = false
        runBlocking {
            result = WorkManager.getInstance().getWorkInfosByTag(TAG_GET).await()
                .any { !it.state.isFinished }
        }
        return result
    }

    fun getQueueInfo(): String? {
        var result: String? = null
        runBlocking {
            result = WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueWorkName).await()
                .groupBy { it.state }.mapValues { it.value.size }
                .map { "${it.key}:${it.value}" }.joinToString(", ")
        }
        return result
    }

    fun cancelAll() {
        WorkManager.getInstance().cancelAllWork()
    }

    private fun enqueueServerCall(
        httpMethod: String,
        path: String,
        serverPassword: String,
        requestEntity: Any? = null,
        tag: String? = null,
        appContext: Context
    ) {

        setOnlineStatus(false, appContext)

        val jsonPayload =
            if (requestEntity != null)
                if (requestEntity is String)
                    requestEntity
                else
                    mapper.writeValueAsString(requestEntity)
            else null
        val data = workDataOf(
            KEY_METHOD to httpMethod,
            KEY_PATH to path,
            KEY_JSON to jsonPayload,
            KEY_PW to serverPassword
        )

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val requestBuilder = OneTimeWorkRequestBuilder<CallTheServerWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setInputData(data)
        if (tag != null)
            requestBuilder.addTag(tag)

        // using a unique work sequence to prevent execution in parallel
        WorkManager.getInstance()
            .beginUniqueWork(uniqueWorkName, ExistingWorkPolicy.APPEND, requestBuilder.build())
            .enqueue()
    }


    class CallTheServerWorker(private val appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

        override fun doWork(): Result {

            val method = inputData.getString(KEY_METHOD)!!
            val path = inputData.getString(KEY_PATH)!!
            val pw = inputData.getString(KEY_PW)!!
            val json = inputData.getString(KEY_JSON)
            val response = try {
                HttpClientService.send(method, path, pw, json) ?: return retry()
            } catch (e: Exception) {
                Log.e("CallServerWorkManager", "error while sending", e)
                return retry()
            }
            setOnlineStatus(true, appContext)
            try {
                ServerResponseHandler(appContext).handle(method, path, response)
            } catch (e: Exception) {
                Log.e("CallServerWorkManager", "error while handling response", e)
            }

            return Result.success()
        }

        override fun onStopped() {
            super.onStopped()
            Log.e("CallServerWorkManager", "received STOP")
        }


        private fun retry() = if (runAttemptCount <= 5) Result.retry() else Result.failure()
    }


    companion object {
        const val TAG_GET = "get"
        const val KEY_METHOD = "method"
        const val KEY_PATH = "path"
        const val KEY_JSON = "json"
        const val KEY_PW = "pw"

        private fun setOnlineStatus(online: Boolean, appContext: Context) {
            val db: SaveDatabase = SaveDatabase.getDatabase(appContext)!!
            val onlineDao: OnlineStatusModelDao = db.OnlineStatusModelDao()
            val entity = onlineDao.findImmediate()
            if (entity == null)
                onlineDao.insert(OnlineStatusModel(online = online))
            else {
                entity.online = online
                onlineDao.update(entity)
            }
        }
    }

}