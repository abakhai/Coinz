package com.example.ami.coinz

import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Geometry
import org.json.JSONObject
import java.util.*

class GeoItem {

        @SerializedName("type")
        private var type: String? = null

        @SerializedName("propeties")
        private var properties: JSONObject? = null

        @SerializedName("geometry")
        private var geometry: Geometry? = null



        override fun toString(): String {
            return "GeoItem{" +
                    "type = '" + type + '\''.toString() +
                    ",properties = '" + properties + '\''.toString() +
                    ",geometry = '" + geometry + '\''.toString() +
                    "}"
        }
}