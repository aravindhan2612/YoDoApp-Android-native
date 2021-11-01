package com.example.ytsample.ui.bottomsheet

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.evgenii.jsevaluator.JsEvaluator
import com.evgenii.jsevaluator.interfaces.JsCallback
import com.example.ytsample.entities.*
import com.example.ytsample.utils.YouTubeUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Matcher
import java.util.regex.Pattern

class YtBottomSheetViewModel : ViewModel() {
    var responseResult = MutableLiveData<String>()
    var _responseResult: LiveData<String>? = responseResult
    var encSignatures = ArrayList<Signature>()
    private var decipherJsFileName: String? = null
    private var decipherFunctions: String? = null
    private var decipherFunctionName: String? = null
    private val lock: Lock = ReentrantLock()
    private val jsExecuting = lock.newCondition()

    private var _text = MutableLiveData<YTMetaData>()
    var text: LiveData<YTMetaData>? = _text
    var formatModelList = ArrayList<FormatsModel>()

    @Volatile
    private var decipheredSignature: String? = null
    private var cacheDirPath: String? = null
    private val CACHE_FILE_NAME = "decipher_js_funct"
    var LOGGING = false
    var CACHING = true
    private val LOG_TAG = "YTSample"

    fun getRequest(context: Context, ytUrl: String?, fragment: YtBottomSheetFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            val queue = Volley.newRequestQueue(context)
            var downnloadUrl = "https://youtube.com/watch?v="
            downnloadUrl += extractYTId(ytUrl)

            val stringRequest = object : StringRequest(
                Request.Method.GET, downnloadUrl,
                Response.Listener<String> { response ->
                    responseResult.value = response

                },
                Response.ErrorListener { responseResult.value = "Error" }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["User-agent"] = YouTubeUtils.USER_AGENT
                    return headers
                }
            }
            queue.add(stringRequest)
        }
    }

    fun extractUrl(response: String, context: Context) {
        viewModelScope.launch {
            cacheDirPath = context.cacheDir.absolutePath
            var videoMeta: VideoMeta? = null
            val result = withContext(Dispatchers.IO) {
                var mat: Matcher = YouTubeUtils.patPlayerResponse.matcher(
                    response
                )
                if (mat.find()) {
                    val ytPlayerResponse = JSONObject(mat.group(1))
                    val streamingData = ytPlayerResponse.getJSONObject("streamingData")
                    val formats = streamingData.getJSONArray("formats")
                    val videoDetails = ytPlayerResponse.getJSONObject("videoDetails")
                    val adaptiveFormats = streamingData.getJSONArray("adaptiveFormats")
                    videoMeta = getVideMetaData(videoDetails)
                    appendFormats(mat, formats)
                    appendAdaptiveFormats(mat, adaptiveFormats)
                } else {
                    Log.d(
                        LOG_TAG,
                        "ytPlayerResponse was not found"
                    )
                }
                test(response, context)
            }
            _text.value = YTMetaData(formatModelList, videoMeta)
        }
    }

    private fun appendFormats(matcher: Matcher, formats: JSONArray) {
        var mat: Matcher = matcher
        var gson = Gson()
        for (i in 0 until formats.length()) {
            val format = formats.getJSONObject(i)
            val ytFormat = gson.fromJson<YTFormat>(format.toString(), YTFormat::class.java)
            // FORMAT_STREAM_TYPE_OTF(otf=1) requires downloading the init fragment (adding
            // `&sq=0` to the URL) and parsing emsg box to determine the number of fragment that
            // would subsequently requested with (`&sq=N`) (cf. youtube-dl)
            val type = format.optString("type")
            if (type != null && type == "FORMAT_STREAM_TYPE_OTF") continue
            val itag = format.getInt("itag")
            if (format.has("url")) {
                ytFormat.url = format.getString("url").replace("\\u0026", "&")
            } else if (format.has("signatureCipher")) {
                mat =
                    YouTubeUtils.patSigEncUrl.matcher(
                        format.getString("signatureCipher")
                    )
                val matSig: Matcher =
                    YouTubeUtils.patSignature.matcher(
                        format.getString("signatureCipher")
                    )
                if (mat.find() && matSig.find()) {
                    ytFormat.url = URLDecoder.decode(mat.group(1), "UTF-8")
                    val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                    encSignatures.add(Signature(itag, signature))
                }
            }
            formatModelList.add(FormatsModel(ytFormat, null, ytFormat.itag))
        }
    }

    private fun appendAdaptiveFormats(matcher: Matcher, adaptiveFormats: JSONArray) {
        var mat: Matcher = matcher
        val gson = Gson()
        for (i in 0 until adaptiveFormats.length()) {
            val adaptiveFormat: JSONObject = adaptiveFormats.getJSONObject(i)
            val ytFormat = gson.fromJson(adaptiveFormat.toString(), YTAdaptiveFormats::class.java)
            val type = adaptiveFormat.optString("type")
            if (type != null && type == "FORMAT_STREAM_TYPE_OTF") continue
            val itag = adaptiveFormat.getInt("itag")
            if (adaptiveFormat.has("url")) {
                ytFormat.url = adaptiveFormat.getString("url").replace("\\u0026", "&")
            } else if (adaptiveFormat.has("signatureCipher")) {
                mat = YouTubeUtils.patSigEncUrl.matcher(
                    adaptiveFormat.getString("signatureCipher")
                )
                val matSig: Matcher =
                    YouTubeUtils.patSignature.matcher(
                        adaptiveFormat.getString("signatureCipher")
                    )
                if (mat.find() && matSig.find()) {
                    ytFormat.url = URLDecoder.decode(mat.group(1), "UTF-8")
                    val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                    encSignatures.add(Signature(itag, signature))
                }
            }
            formatModelList.add(FormatsModel(null, ytFormat, ytFormat.itag))
        }
    }

    private fun extractYTId(ytUrl: String?): String? {
        var vId: String? = null
        val pattern: Pattern = Pattern.compile(
            "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
            Pattern.CASE_INSENSITIVE
        )
        val matcher: Matcher = pattern.matcher(ytUrl)
        if (matcher.matches()) {
            vId = matcher.group(1)
        }
        return vId
    }

    //code for optional
    private fun getVideoId(ytUrl: String?): String? {
        var videoID: String? = null
        var mat: Matcher = YouTubeUtils.patYouTubePageLink.matcher(ytUrl)
        if (mat.find()) {
            videoID = mat.group(3)
        } else {
            mat = YouTubeUtils.patYouTubeShortLink.matcher(ytUrl)
            val mat2 = YouTubeUtils.graph.matcher(ytUrl)
            if (mat.find()) {
                videoID = mat.group(3)
            } else if (mat2.find()) {
                videoID = ytUrl
            }
        }
        return videoID
    }

    private fun getVideMetaData(videoDetails: JSONObject): VideoMeta? {
        return VideoMeta(
            videoDetails.getString("videoId"),
            videoDetails.getString("title"),
            videoDetails.getString("author"),
            videoDetails.getString("channelId"),
            videoDetails.getString("lengthSeconds").toLong(),
            videoDetails.getString("viewCount").toLong(),
            videoDetails.getBoolean("isLiveContent"),
            videoDetails.getString("shortDescription")
        )

    }

    private fun test(response: String, context: Context) {
        if (encSignatures.size > 0) {
            val curJsFileName: String
            if (CACHING
                && (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)
            ) {
                readDecipherFunctFromCache()
            }
            var mat: Matcher = YouTubeUtils.patDecryptionJsFile.matcher(response)
            if (!mat.find())
                mat =
                    YouTubeUtils.patDecryptionJsFileWithoutSlash.matcher(
                        response
                    )
            if (mat.find()) {
                curJsFileName = mat.group(0).replace("\\/", "/")
                if (decipherJsFileName == null || decipherJsFileName != curJsFileName) {
                    decipherFunctions = null
                    decipherFunctionName = null
                }
                decipherJsFileName = curJsFileName
            }
//            if (LOGGING) Log.d(
//                "*****",
//                "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size()
//            )
            val signature: String?
            decipheredSignature = null
            if (decipherSignature(encSignatures, context)) {
                lock.lock()
                try {
                    jsExecuting.await(7, TimeUnit.SECONDS)
                } finally {
                    lock.unlock()
                }
            }
            signature = decipheredSignature
            if (signature == null) {
            } else {
                val sigs = signature.split("\n".toRegex()).toTypedArray()
                var i = 0
                while (i < encSignatures.size && i < sigs.size) {
                    val key = encSignatures[i].itag
                    formatModelList.forEachIndexed { index, value ->
                        if (value.itag == key) {
                            if (value.adaptive?.url != null) {
                                value.adaptive?.url += "&sig=" + sigs[i]
                                formatModelList[index].adaptive = value.adaptive
                            } else if (value.format?.url != null) {
                                value.format?.url += "&sig=" + sigs[i]
                                formatModelList[index].format = value.format
                            }
                        }
                    }
//                    url += "&sig=" + sigs[i]
//                    val newFile =
//                        YtFile(FORMAT_MAP.get(key), url)
//                    ytFiles.put(key, newFile)
                    i++
                }
            }
        }
    }

    private fun readDecipherFunctFromCache() {
        val cacheFile: File =
            File("$cacheDirPath/$CACHE_FILE_NAME")
        // The cached functions are valid for 2 weeks
        if (cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() < 1209600000) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(FileInputStream(cacheFile), "UTF-8"))
                decipherJsFileName = reader.readLine()
                decipherFunctionName = reader.readLine()
                decipherFunctions = reader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun decipherSignature(
        encSignatures: ArrayList<Signature>,
        context: Context
    ): Boolean {
        // Assume the functions don't change that much
        if (decipherFunctionName == null || decipherFunctions == null) {
            val decipherFunctUrl =
                "https://youtube.com$decipherJsFileName"
            var reader: BufferedReader? = null
            val javascriptFile: String
            val url = URL(decipherFunctUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty(
                "User-Agent",
                YouTubeUtils.USER_AGENT
            )
            try {
                reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                    sb.append(" ")
                }
                javascriptFile = sb.toString()
            } finally {
                reader?.close()
                urlConnection.disconnect()
            }
            if (LOGGING) Log.d(
                "****",
                "Decipher FunctURL: $decipherFunctUrl"
            )
            var mat: Matcher =
                YouTubeUtils.patSignatureDecFunction.matcher(
                    javascriptFile
                )
            if (mat.find()) {
                decipherFunctionName = mat.group(1)
                if (LOGGING) Log.d(
                    LOG_TAG,
                    "Decipher Functname: $decipherFunctionName"
                )
                val patMainVariable = Pattern.compile(
                    "(var |\\s|,|;)" + decipherFunctionName?.replace(
                        "$",
                        "\\$"
                    ) +
                            "(=function\\((.{1,3})\\)\\{)"
                )
                var mainDecipherFunct: String
                mat = patMainVariable.matcher(javascriptFile)
                if (mat.find()) {
                    mainDecipherFunct =
                        "var $decipherFunctionName" + mat.group(
                            2
                        )
                } else {
                    val patMainFunction = Pattern.compile(
                        ("function " + decipherFunctionName?.replace(
                            "$",
                            "\\$"
                        ) +
                                "(\\((.{1,3})\\)\\{)")
                    )
                    mat = patMainFunction.matcher(javascriptFile)
                    if (!mat.find()) return false
                    mainDecipherFunct =
                        "function $decipherFunctionName" + mat.group(
                            2
                        )
                }
                var startIndex = mat.end()
                var braces = 1
                var i = startIndex
                while (i < javascriptFile.length) {
                    if (braces == 0 && startIndex + 5 < i) {
                        mainDecipherFunct += javascriptFile.substring(startIndex, i) + ";"
                        break
                    }
                    if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                    i++
                }
                decipherFunctions = mainDecipherFunct
                // Search the main function for extra functions and variables
                // needed for deciphering
                // Search for variables
                mat = YouTubeUtils.patVariableFunction.matcher(
                    mainDecipherFunct
                )
                while (mat.find()) {
                    val variableDef = "var " + mat.group(2) + "={"
                    if (decipherFunctions?.contains(
                            variableDef
                        ) == true
                    ) {
                        continue
                    }
                    startIndex = javascriptFile.indexOf(variableDef) + variableDef.length
                    var braces = 1
                    var i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0) {
                            decipherFunctions += variableDef + javascriptFile.substring(
                                startIndex,
                                i
                            ) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                        i++
                    }
                }
                // Search for functions
                mat =
                    YouTubeUtils.patFunction.matcher(mainDecipherFunct)
                while (mat.find()) {
                    val functionDef = "function " + mat.group(2) + "("
                    if (decipherFunctions?.contains(
                            functionDef
                        ) == true
                    ) {
                        continue
                    }
                    startIndex = javascriptFile.indexOf(functionDef) + functionDef.length
                    var braces = 0
                    var i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0 && startIndex + 5 < i) {
                            decipherFunctions += functionDef + javascriptFile.substring(
                                startIndex,
                                i
                            ) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                        i++
                    }
                }
                if (LOGGING) Log.d(
                    LOG_TAG,
                    "Decipher Function: " + decipherFunctions
                )
                decipherViaWebView(encSignatures, context)
                if (CACHING) {
                    writeDeciperFunctToChache()
                }
            } else {
                return false
            }
        } else {
            decipherViaWebView(encSignatures, context)
        }
        return true
    }

    private fun decipherViaWebView(encSignatures: ArrayList<Signature>, ctxt: Context) {
        val context: Context = ctxt.applicationContext
        val stb: StringBuilder =
            StringBuilder("$decipherFunctions function decipher(")
        stb.append("){return ")
        for (i in 0 until encSignatures.size) {
            if (i < encSignatures.size - 1) stb.append(decipherFunctionName)
                .append("('").append(
                    encSignatures[i].signature
                )
                .append("')+\"\\n\"+") else stb.append(decipherFunctionName)
                .append("('").append(
                    encSignatures[i].signature
                ).append("')")
        }
        stb.append("};decipher();")
        Handler(Looper.getMainLooper()).post {
            JsEvaluator(context).evaluate(stb.toString(), object : JsCallback {
                override fun onResult(result: String) {
                    lock.lock()
                    try {
                        decipheredSignature = result
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }

                override fun onError(errorMessage: String?) {
                    lock.lock()
                    try {
                        if (LOGGING) Log.e(
                            LOG_TAG,
                            errorMessage!!
                        )
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }
            })
        }
    }

    private fun writeDeciperFunctToChache() {
        val cacheFile =
            File("$cacheDirPath/$CACHE_FILE_NAME")
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(cacheFile), "UTF-8"))
            writer.write(decipherJsFileName + "\n")
            writer.write(decipherFunctionName + "\n")
            writer.write(decipherFunctions)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


}