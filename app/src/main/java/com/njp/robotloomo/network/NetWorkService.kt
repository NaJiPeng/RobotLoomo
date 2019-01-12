package com.njp.robotloomo.network

import com.njp.robotloomo.bean.ChatResponseBody
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * 网络连接接口
 */
interface NetWorkService {

    @Headers("Content-type:application/json;charset=UTF-8")
    @POST("openapi/api/v2")
    fun send(
            @Body body: RequestBody
    ):Observable<ChatResponseBody>
}