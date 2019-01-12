package com.njp.robotloomo.network

import com.google.gson.Gson
import com.njp.robotloomo.bean.*
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 网络连接入口
 */
object NetworkManager {

    private val retrofit = Retrofit.Builder()
            .baseUrl("http://openapi.tuling123.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val service = retrofit.create(NetWorkService::class.java)


    fun send(content: String): Observable<ChatResponseBody> {
        val body = ChatRequestBody(
                Perception(InputText(content)),
                0,
                UserInfo("446ece0dd0d64c8a94423c164bb9657d", "loomo")
        )
        val strBody = Gson().toJson(body)
        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),strBody)
        return service.send(requestBody)
    }

}