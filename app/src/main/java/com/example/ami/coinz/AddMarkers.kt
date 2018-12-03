package com.example.ami.coinz

import android.content.Context
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class AddMarkers {


    private val BASE_URL = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"
    //var IF = IconFactory.getInstance()




    fun updatedURL(baseURL: String) : String {


        var now = Calendar.getInstance()
        var y = now.get(Calendar.YEAR)
        var year = y.toString()
        var m = now.get(Calendar.MONTH) //wrong
        var month = m.toString()
        var d = now.get(Calendar.DAY_OF_MONTH)
        var day = d.toString()


        var dateString = year + "/" + month + "/" + day
        updatedURL = baseURL + dateString + endOfUrl


        return updatedURL
    }

   // fun addMarkers(map : MapboxMap?, title : String, snippet : String, iconB : Icon, iconG : Icon, iconR : Icon, iconY : Icon) {
   fun addMarkers(map : MapboxMap?) {


        var FeatureCollection = FeatureCollection.fromJson(DownloadFileTask(DownloadCompleteRunner).execute(updatedURL(BASE_URL)).get()) //updatedURL(BASE_URL) //"http://homepages.inf.ed.ac.uk/stg/coinz/2018/11/12/coinzmap.geojson"
        var FeatureList = FeatureCollection.features()
        var featureSize = FeatureList?.size as Int

        for (index in 0..featureSize-1) {
            var feature = FeatureList[index]
            var geo = feature.geometry()
            var point = geo as Point
            var coords = point.coordinates()
            map?.addMarker(
                    MarkerOptions()
                            .position(LatLng(coords[1],coords[0]))
            )
        }

    }


}

//TODO do it with this class