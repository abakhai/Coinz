package com.example.ami.coinz

import android.os.AsyncTask
import com.example.ami.coinz.DownloadCompleteRunner.result
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class DownloadFileTask (private val caller : DownloadCompleteListener) :
AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg urls: String): String = try {
        loadFileFromNetwork(urls[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection"
    }

    private fun loadFileFromNetwork(urlString : String): String {
        val stream : InputStream = downloadUrl(urlString)
        // TODO Read input from stream, build result as a String
        result = stream.bufferedReader().use { it.readText() }
        return result as String
    }

    //given a string representation of a URL, sets up a connection and gets an input stream
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        //Also available: HttpsURLconnection
        conn.readTimeout = 10000 // miliseconds
        conn.connectTimeout = 15000 // miliseonds
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect() // starts the query
        return conn.inputStream
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        caller.downloadComplete(result as String)
    }



}