package com.example.ami.coinz

import com.google.gson.annotations.SerializedName

class GeoItem {

        @SerializedName("type")
        private var type: String? = null

        @SerializedName("propeties")
        private var width: Int = 0

        @SerializedName("id")
        private var id: String? = null

        @SerializedName("url")
        private var url: String? = null

        @SerializedName("height")
        private var height: Int = 0


        override fun toString(): String {
            return "MemesItem{" +
                    "name = '" + name + '\''.toString() +
                    ",width = '" + width + '\''.toString() +
                    ",id = '" + id + '\''.toString() +
                    ",url = '" + url + '\''.toString() +
                    ",height = '" + height + '\''.toString() +
                    "}"
        }
}