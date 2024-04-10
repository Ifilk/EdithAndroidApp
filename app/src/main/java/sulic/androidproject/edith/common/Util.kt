package sulic.androidproject.edith.common

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.ConnectException


//
//private val handler: Handler = Handler()
//
//fun get(strMap: Map<*, *>, strUrl: String?, callBack: HttpCallBack) {
//    val thread: Thread = object : Thread() {
//        override fun run() {
//            var connection: HttpURLConnection? = null
//            var `is`: InputStream? = null
//            try {
//                val stringBuffer = StringBuilder(strUrl)
//                stringBuffer.append("?")
//                for (key in strMap.keys) {
//                    stringBuffer.append(key.toString() + "=" + strMap[key] + "&")
//                }
//                stringBuffer.deleteCharAt(stringBuffer.length - 1)
//                val url = URL(stringBuffer.toString())
//                connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "GET"
//                connection!!.connectTimeout = 10 * 1000
//                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
//                    `is` = connection.inputStream
//                    val result = InputStreamToString(`is`)
//                    handler.post(Runnable { callBack.onSuccess(result) })
//                } else {
//                    throw Exception("ResponseCode:" + connection.responseCode)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                handler.post(Runnable { callBack.onError(e) })
//            } finally {
//                connection?.disconnect()
//                if (`is` != null) try {
//                    `is`.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//                handler.post(Runnable { callBack.onFinish() })
//            }
//        }
//    }
//    thread.start()
//}
//
//fun post(strMap: Map<*, *>, strUrl: String?, callBack: HttpCallBack) {
//    val thread: Thread = object : Thread() {
//        override fun run() {
//            var connection: HttpURLConnection? = null
//            var os: OutputStream? = null
//            var `is`: InputStream? = null
//            try {
//                val stringBuilder = StringBuilder()
//                for (key in strMap.keys) {
//                    stringBuilder.append(key.toString() + "=" + strMap[key] + "&")
//                }
//                stringBuilder.deleteCharAt(stringBuilder.length - 1)
//                val url = URL(strUrl)
//                connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "POST"
//                connection.connectTimeout = 10 * 1000
//                connection.doOutput = true
//                connection.doInput = true
//                connection.useCaches = false
//                connection.setRequestProperty("Charset", "utf-8")
//                connection.connect()
//                os = connection.outputStream
//                os.write(stringBuilder.toString().toByteArray())
//                os.flush()
//                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
//                    `is` = connection.inputStream
//                    val result = InputStreamToString(`is`)
//                    handler.post(Runnable { callBack.onSuccess(result) })
//                } else {
//                    throw Exception("ResponseCode:" + connection.responseCode)
//                }
//            } catch (e: Exception) {
//                handler.post(Runnable { callBack.onError(e) })
//            } finally {
//                connection?.disconnect()
//                try {
//                    `is`?.close()
//                    os?.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//                handler.post(Runnable { callBack.onFinish() })
//            }
//        }
//    }
//    thread.start()
//}

//@Throws(Exception::class)
//fun upload(url: String, fileName: String, data: Map<String, String>?, bytesData: ByteArray?, callback: (String) -> Unit){
//    val buffer = ByteArray(1024)
//    val inputStream = upload(url, fileName, data, bytesData)
//    var len: Int
//    do {
//        len = inputStream.read(buffer)
//        val line = String(buffer, 0, len, Charset.forName("utf-8"))
//        callback(line)
//    } while(len != -1)
//}

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