package com.example.ami.coinz

import io.reactivex.Observable
import retrofit2.http.GET



interface ApiInterface{

    @GET("get_features")
    abstract fun getFeatures(): Observable<Response>

    @GET("get_geometry")
    abstract fun getGeometry() : Observable<Response>

}