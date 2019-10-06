package de.mg.androidsave.server

import android.util.Log
import de.mg.androidsave.BuildConfig
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*


object HttpClientService {

    private const val basePath = BuildConfig.SERVER_URL
    private const val basicAutUser = BuildConfig.SERVER_USER
    private const val basicAuthPassword = BuildConfig.SERVER_PASSWORD

    fun send(
        method: String,
        path: String,
        serverPassword: String,
        httpEntity: String? = null
    ): String? {

        Log.d("HttpClientService", "$method -> $path: $httpEntity")

        val url = URL("$basePath/$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = method
        conn.doInput = true
        conn.doOutput = httpEntity != null
        conn.setRequestProperty("Accept", "application/json")

        val encoded = Base64.getEncoder()
            .encodeToString("$basicAutUser:$basicAuthPassword".toByteArray(StandardCharsets.UTF_8))
        conn.setRequestProperty("Authorization", "Basic $encoded")

        conn.setRequestProperty("password", serverPassword)

        if (httpEntity != null) {

            conn.setRequestProperty("Content-Type", "application/json; utf-8")

            val os = conn.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(httpEntity)
            writer.flush()
            writer.close()
            os.close()
        }

        val responseCode = conn.responseCode

        return if (responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            Log.i("HttpClientService", "received: $response")
            response
        } else {
            Log.e("HttpClientService", "response code = $responseCode")
            null
        }
    }
}