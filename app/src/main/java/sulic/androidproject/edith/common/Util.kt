package sulic.androidproject.edith.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.ConnectException

@Throws(Exception::class)
fun post(url: String, params: Map<String, String>): String{
    val okHttpClient = OkHttpClient()
    val formBodyBuilder = FormBody.Builder()
    for((k, v) in params){
        formBodyBuilder.add(k, v)
    }
    val request: Request = Request.Builder()
        .url(url)
        .post(formBodyBuilder.build())
        .build()
    val response = try {
        okHttpClient.newCall(request).execute()
    } catch (e: ConnectException) {
        Log.d("TAG", "服务器连接失败")
        null
    }
    if (!response!!.isSuccessful) {
        Log.d("TAG", "upload: fall")
    }
    val result = response.body!!.string()
    response.body!!.close()
    return result
}

@Throws(IOException::class)
fun post(url: String, jsonString: String?): String {
    val client = OkHttpClient().newBuilder().build()
    val body = jsonString!!.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request: Request = Request.Builder()
        .post(body)
        .url(url)
        .build()
    val call = client.newCall(request)
    try {
        val response = call.execute()
        return response.body!!.string()
    } catch (e: IOException) {
        return e.message!!
    }
}

@Throws(Exception::class)
fun upload(url: String, fileName: String, file: File): String {
    val okHttpClient = OkHttpClient()
    val fileBody: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", fileName, fileBody)
        .build()
    val request: Request = Request.Builder()
        .url(url)
        .post(multipartBody)
        .build()
    val response = try {
        okHttpClient.newCall(request).execute()
    } catch (e: ConnectException) {
        Log.d("TAG", "服务器连接失败")
        null
    }
    if (!response!!.isSuccessful) {
        Log.d("TAG", "upload: fall")
    }
    val result = response.body!!.string()
    response.body!!.close()
    return result
}

fun showMsg(context: Context,msg: String){
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}

//@Throws(IOException::class)
//fun InputStreamToString(`is`: InputStream?): String {
//    val os = ByteArrayOutputStream()
//    val data = ByteArray(1024)
//    var len = -1
//    while ((`is`!!.read(data).also { len = it }) != -1) {
//        os.write(data, 0, len)
//    }
//    os.flush()
//    os.close()
//    val result = String(data, charset("UTF-8"))
//    return result
//}
//
//interface HttpCallBack {
//    fun onSuccess(result: String?)
//
//    fun onError(e: Exception?)
//
//    fun onFinish()
//}