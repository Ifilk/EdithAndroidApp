package sulic.androidproject.edith.service.impl

import android.util.Log
import com.alibaba.fastjson2.toJSONString
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import sulic.androidproject.edith.dto.CompletionDto
import sulic.androidproject.edith.service.LLMRemoteService
import sulic.androidproject.edith.service.Properties
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class DefaultLLMRemoteService @Inject constructor(
    private val properties: Properties
): LLMRemoteService {
    override fun completion(dto: CompletionDto): Call{
        return post("http://${properties.SERVER_IP}/completion", dto.toJSONString())
    }


    @Throws(IOException::class)
    private fun post(url: String, jsonString: String?): Call {
        val client = OkHttpClient().newBuilder().build()
        val body = jsonString!!.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request: Request = Request.Builder()
            .post(body)
            .url(url)
            .build()
        return client.newCall(request)
    }
}