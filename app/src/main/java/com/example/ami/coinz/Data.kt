package com.example.ami.coinz

import com.google.gson.annotations.SerializedName


public class Data {

    lateinit var geojson: List<GeoItem>

    @SerializedName("geoj")


    override fun toString(): String {
        return "Data{" +
                "geoj = '" + geojson + '\''.toString() +
                "}"
    }
}