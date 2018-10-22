package com.example.ami.coinz

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Response {

    @SerializedName("data")
    private var data: Data? = null

    @SerializedName("success")
    @Expose
    private var success: Boolean = false

    fun setData(data: Data) {
        this.data = data
    }

    fun getData(): Data? {
        return data
    }

    fun setSuccess(success: Boolean) {
        this.success = success
    }

    fun isSuccess(): Boolean {
        return success
    }

    override fun toString(): String {
        return "Response{" +
                "data = '" + data + '\''.toString() +
                ",success = '" + success + '\''.toString() +
                "}"
    }
}