package com.example.ytsample.ui.downloads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
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
import com.example.ytsample.entities.Format
import com.example.ytsample.entities.VideoMeta
import com.example.ytsample.entities.YTMetaData
import com.example.ytsample.entities.YtFile
import com.example.ytsample.utils.YouTubeUtils
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

class DownloadsViewModel : ViewModel() {
    private var _text = MutableLiveData<YTMetaData>()
    private var _meta = MutableLiveData<VideoMeta>()
    val text: LiveData<YTMetaData> = _text
    private val FORMAT_MAP: SparseArray<Format> = SparseArray<Format>()
    var ytFiles: SparseArray<YtFile> = SparseArray<YtFile>()
    var encSignatures = SparseArray<String>()
    var CACHING = true
    private var decipherJsFileName: String? = null
    private var decipherFunctions: String? = null
    private var decipherFunctionName: String? = null
    private val lock: Lock = ReentrantLock()
    private val jsExecuting = lock.newCondition()

    @Volatile
    private var decipheredSignature: String? = null
    private var cacheDirPath: String? = null
    private val CACHE_FILE_NAME = "decipher_js_funct"
    var LOGGING = false
    private val LOG_TAG = "YTSample"
    var responseResult = MutableLiveData<String>()


    fun extractUrl(response: String, context: Context) {
        viewModelScope.launch {
            cacheDirPath = context.cacheDir.absolutePath
            var videoMeta: VideoMeta? = null
            initFormatMap()
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
                test(mat, response, context)
            }

            _text.value = YTMetaData(ytFiles,videoMeta)
        }

    }

    fun getRequest(context: Context, ytUrl: String, downloadsFragment: DownloadsFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            val queue = Volley.newRequestQueue(context)
            var downnloadUrl = "https://youtube.com/watch?v="
            downnloadUrl += extractYTId(ytUrl)

            val stringRequest = object : StringRequest(Request.Method.GET, downnloadUrl,
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

    private fun test(mat: Matcher, response: String, context: Context) {
        if (encSignatures.size() > 0) {
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
            if (LOGGING) Log.d(
                "*****",
                "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size()
            )
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
                while (i < encSignatures.size() && i < sigs.size) {
                    val key = encSignatures.keyAt(i)
                    var url: String? = ytFiles[key].url
                    url += "&sig=" + sigs[i]
                    val newFile =
                        YtFile(FORMAT_MAP.get(key), url)
                    ytFiles.put(key, newFile)
                    i++
                }
            }
        }
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

    private fun appendAdaptiveFormats(matcher: Matcher, adaptiveFormats: JSONArray) {
        var mat: Matcher = matcher
        for (i in 0 until adaptiveFormats.length()) {
            val adaptiveFormat: JSONObject = adaptiveFormats.getJSONObject(i)
            val type = adaptiveFormat.optString("type")
            if (type != null && type == "FORMAT_STREAM_TYPE_OTF") continue
            val itag = adaptiveFormat.getInt("itag")
            if (FORMAT_MAP.get(itag) != null) {
                if (adaptiveFormat.has("url")) {
                    val url = adaptiveFormat.getString("url").replace("\\u0026", "&")
                    ytFiles.append(
                        itag,
                        YtFile(FORMAT_MAP.get(itag), url)
                    )
                } else if (adaptiveFormat.has("signatureCipher")) {
                    mat = YouTubeUtils.patSigEncUrl.matcher(
                        adaptiveFormat.getString("signatureCipher")
                    )
                    val matSig: Matcher =
                        YouTubeUtils.patSignature.matcher(
                            adaptiveFormat.getString("signatureCipher")
                        )
                    if (mat.find() && matSig.find()) {
                        val url = URLDecoder.decode(mat.group(1), "UTF-8")
                        val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                        ytFiles.append(
                            itag,
                            YtFile(
                                FORMAT_MAP.get(itag),
                                url
                            )
                        )
                        encSignatures.append(itag, signature)
                    }
                }
            }
        }
    }

    private fun appendFormats(matcher: Matcher, formats: JSONArray) {
        var mat: Matcher = matcher
        for (i in 0 until formats.length()) {
            val format = formats.getJSONObject(i)

            // FORMAT_STREAM_TYPE_OTF(otf=1) requires downloading the init fragment (adding
            // `&sq=0` to the URL) and parsing emsg box to determine the number of fragment that
            // would subsequently requested with (`&sq=N`) (cf. youtube-dl)
            val type = format.optString("type")
            if (type != null && type == "FORMAT_STREAM_TYPE_OTF") continue
            val itag = format.getInt("itag")
            if (FORMAT_MAP.get(itag) != null) {
                if (format.has("url")) {
                    val url = format.getString("url").replace("\\u0026", "&")
                    ytFiles.append(
                        itag,
                        YtFile(
                            FORMAT_MAP.get(
                                itag
                            ), url
                        )
                    )
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
                        val url = URLDecoder.decode(mat.group(1), "UTF-8")
                        val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                        ytFiles.append(
                            itag,
                            YtFile(
                                FORMAT_MAP.get(
                                    itag
                                ), url
                            )
                        )
                        encSignatures.append(itag, signature)
                    }
                }
            }
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
        encSignatures: SparseArray<String>,
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

    private fun decipherViaWebView(encSignatures: SparseArray<String>, ctxt: Context) {
        val context: Context = ctxt.applicationContext
        val stb: StringBuilder =
            StringBuilder("$decipherFunctions function decipher(")
        stb.append("){return ")
        for (i in 0 until encSignatures.size()) {
            val key = encSignatures.keyAt(i)
            if (i < encSignatures.size() - 1) stb.append(decipherFunctionName)
                .append("('").append(
                    encSignatures[key]
                )
                .append("')+\"\\n\"+") else stb.append(decipherFunctionName)
                .append("('").append(
                    encSignatures[key]
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

    private fun initFormatMap() {
        FORMAT_MAP.put(
            17,
            Format(17, "3gp", 144, Format.VCodec.MPEG4, Format.ACodec.AAC, 24, false)
        );
        FORMAT_MAP.put(
            36,
            Format(36, "3gp", 240, Format.VCodec.MPEG4, Format.ACodec.AAC, 32, false)
        );
        FORMAT_MAP.put(
            5,
            Format(5, "flv", 240, Format.VCodec.H263, Format.ACodec.MP3, 64, false)
        );
        FORMAT_MAP.put(
            43,
            Format(43, "webm", 360, Format.VCodec.VP8, Format.ACodec.VORBIS, 128, false)
        );
        FORMAT_MAP.put(
            18,
            Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false)
        );
        FORMAT_MAP.put(
            22,
            Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false)
        );

        // Dash Video
        FORMAT_MAP.put(
            160,
            Format(160, "mp4", 144, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            133,
            Format(133, "mp4", 240, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            134,
            Format(134, "mp4", 360, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            135,
            Format(135, "mp4", 480, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            136,
            Format(136, "mp4", 720, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            137,
            Format(137, "mp4", 1080, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            264,
            Format(264, "mp4", 1440, Format.VCodec.H264, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            266,
            Format(266, "mp4", 2160, Format.VCodec.H264, Format.ACodec.NONE, true)
        );

        FORMAT_MAP.put(
            298,
            Format(298, "mp4", 720, Format.VCodec.H264, 60, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            299,
            Format(299, "mp4", 1080, Format.VCodec.H264, 60, Format.ACodec.NONE, true)
        );

        // Dash Audio
        FORMAT_MAP.put(
            140,
            Format(140, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 128, true)
        );
        FORMAT_MAP.put(
            141,
            Format(141, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 256, true)
        );
        FORMAT_MAP.put(
            256,
            Format(256, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 192, true)
        );
        FORMAT_MAP.put(
            258,
            Format(258, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 384, true)
        );

        // WEBM Dash Video
        FORMAT_MAP.put(
            278,
            Format(278, "webm", 144, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            242,
            Format(242, "webm", 240, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            243,
            Format(243, "webm", 360, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            244,
            Format(244, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            247,
            Format(247, "webm", 720, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            248,
            Format(248, "webm", 1080, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            271,
            Format(271, "webm", 1440, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            313,
            Format(313, "webm", 2160, Format.VCodec.VP9, Format.ACodec.NONE, true)
        );

        FORMAT_MAP.put(
            302,
            Format(302, "webm", 720, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            308,
            Format(308, "webm", 1440, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            303,
            Format(303, "webm", 1080, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        );
        FORMAT_MAP.put(
            315,
            Format(315, "webm", 2160, Format.VCodec.VP9, 60, Format.ACodec.NONE, true)
        );

        // WEBM Dash Audio
        FORMAT_MAP.put(
            171,
            Format(171, "webm", Format.VCodec.NONE, Format.ACodec.VORBIS, 128, true)
        );

        FORMAT_MAP.put(
            249,
            Format(249, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 48, true)
        );
        FORMAT_MAP.put(
            250,
            Format(250, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 64, true)
        );
        FORMAT_MAP.put(
            251,
            Format(251, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 160, true)
        );

        // HLS Live Stream
        FORMAT_MAP.put(
            91,
            Format(91, "mp4", 144, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true)
        );
        FORMAT_MAP.put(
            92,
            Format(92, "mp4", 240, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true)
        );
        FORMAT_MAP.put(
            93,
            Format(93, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true)
        );
        FORMAT_MAP.put(
            94,
            Format(94, "mp4", 480, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true)
        );
        FORMAT_MAP.put(
            95,
            Format(95, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true)
        );
        FORMAT_MAP.put(
            96,
            Format(96, "mp4", 1080, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true)
        );
    }

}