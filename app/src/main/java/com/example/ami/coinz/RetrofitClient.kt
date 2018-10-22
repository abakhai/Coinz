package com.example.ami.coinz

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RetrofitClient {

    private val BASE_URL = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"



    fun updatedURL(baseURL: String) : String {

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)
        var dateArray = formatted as IntArray

        var year = dateArray.copyOfRange(0,3).toString()
        var month = dateArray.copyOfRange(4,5).toString()
        var day = dateArray.copyOfRange(6,7).toString()

        var dateString = year + "/" + month + "/" + day
        updatedURL = baseURL + dateString + endOfUrl

        println(updatedURL)
        return updatedURL
    }

    fun getClient() : Retrofit {

        var builder: Retrofit.Builder = Retrofit.Builder()
                .baseUrl(updatedURL(BASE_URL))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

        var retrofit: Retrofit = builder.build()



        return retrofit

    }
}