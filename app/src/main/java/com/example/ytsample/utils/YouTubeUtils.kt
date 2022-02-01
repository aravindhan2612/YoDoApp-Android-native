package com.example.ytsample.utils

import android.os.Build
import java.util.regex.Pattern

class YouTubeUtils {

    companion object {
         val USER_AGENT =
            "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MANUFACTURER} ${Build.MODEL}) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19"
        @JvmStatic
        val patYouTubePageLink =
            Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)")
        @JvmStatic
        val patYouTubeShortLink =
            Pattern.compile("(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)")
        @JvmStatic
        val graph =
            Pattern.compile("\\p{Graph}+?")
        @JvmStatic
        val patPlayerResponse =
            Pattern.compile("var ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\s*;")

        @JvmStatic
         val patSigEncUrl = Pattern.compile("url=(.+?)(\\u0026|$)")
        @JvmStatic
         val patSignature = Pattern.compile("s=(.+?)(\\u0026|$)")
        @JvmStatic
        val patDecryptionJsFile: Pattern =
            Pattern.compile("\\\\/s\\\\/player\\\\/([^\"]+?)\\.js")
        @JvmStatic
         val patDecryptionJsFileWithoutSlash: Pattern =
            Pattern.compile("/s/player/([^\"]+?).js")
        @JvmStatic
         val patSignatureDecFunction =
            Pattern.compile("(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{1,4})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")
        @JvmStatic
         val patVariableFunction: Pattern =
            Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(")
        @JvmStatic
        val patFunction: Pattern =
            Pattern.compile("([{; =])([a-zA-Z$\\_][a-zA-Z0-9$]{0,2})\\(")

    }

}