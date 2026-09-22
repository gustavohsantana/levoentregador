package com.example.data.api

import com.example.data.model.PingRequest
import com.example.data.model.ReplanRequest
import com.example.data.model.StopOutcomeRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface LevoApiService {

    @POST("api/driver/ping")
    suspend fun ping(
        @Body request: PingRequest
    ): Response<ResponseBody>

    @POST("api/driver/stop")
    suspend fun completeStop(
        @Body request: StopOutcomeRequest
    ): Response<ResponseBody>

    @POST("api/driver/replan")
    suspend fun replan(
        @Body request: ReplanRequest
    ): Response<ResponseBody>

    @GET("m/{token}")
    suspend fun getCourierPage(
        @Path("token") token: String,
        @Header("RSC") rscHeader: String? = null,
        @Header("Accept") acceptHeader: String? = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    ): Response<ResponseBody>
}
