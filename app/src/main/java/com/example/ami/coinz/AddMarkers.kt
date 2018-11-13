package com.example.ami.coinz

import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddMarkers {


    private val BASE_URL = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"
    lateinit var geometryList : ArrayList<Geometry>
    lateinit var pointList : ArrayList<Point>
    lateinit var coordinatesList : ArrayList<List<Double>>


    fun updatedURL(baseURL: String) : String {

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)
        var dateArray = IntArray(formatted.length)
        for (i in 0..formatted.length-1) {
            dateArray.set(i,formatted.get(i).toInt() - 48)
        }
        // TODO make this better
        val format = strings.map{formatted.toInt()}

        var year = dateArray?.copyOfRange(0,4).toString()
        var month = dateArray?.copyOfRange(4,6).toString()
        var day = dateArray?.copyOfRange(6,8).toString()
        var ba = dateArray?.copyOfRange(6,8)

        var dateString = year + "/" + month + "/" + day
        updatedURL = baseURL + dateString + endOfUrl


        return updatedURL
    }

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
                    MarkerOptions().position(LatLng(coords[1],coords[0]))
            )
        }

    }


}