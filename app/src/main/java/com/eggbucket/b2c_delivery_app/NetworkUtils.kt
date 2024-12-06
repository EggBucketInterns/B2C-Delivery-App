package com.eggbucket.b2c_delivery_app.utils

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.enqueue(
    onResponse: (Response<T>) -> Unit,
    onFailure: (Throwable) -> Unit
) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            onResponse(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            onFailure(t)
        }
    })
}