package com.example.ami.coinz

import com.google.gson.GsonBuilder
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.GeoJson
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RetrofitClient {

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
        val dateArray = formatted as IntArray?

        var year = dateArray?.copyOfRange(0,3).toString()
        var month = dateArray?.copyOfRange(4,5).toString()
        var day = dateArray?.copyOfRange(6,7).toString()

        var dateString = year + "/" + month + "/" + day
        updatedURL = baseURL + dateString + endOfUrl


        return updatedURL
    }

    fun getClient() : ArrayList<List<Double>> {

        var featureColl = FeatureCollection.fromJson(updatedURL(BASE_URL))
        var features = featureColl.features()
        var sizeOffeatureList : Int = features?.size as Int

        for (i in 0..sizeOffeatureList) {
            geometryList.add(features[i].geometry() as Geometry)
        }


        for (i in 0..geometryList.size) {
            pointList.add(geometryList[i] as Point)
            coordinatesList.add(pointList[i].coordinates())
        }

        return coordinatesList

    }
}