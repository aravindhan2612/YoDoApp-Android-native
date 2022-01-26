package com.example.ytsample.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface RetrofitInterface {

    @Streaming
    @GET
    fun downloadFileUsingUrl(@Url url: String): Call<ResponseBody>
}