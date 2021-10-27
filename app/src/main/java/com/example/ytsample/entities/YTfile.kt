package com.example.ytsample.entities

import androidx.annotation.NonNull

class YtFile internal constructor(
    /**
     * Format data for the specific file.
     */
    val meta: Format?,
    /**
     * The url to download the file.
     */
    val url: String?
) {
    /**
     * Format data for the specific file.
     */

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val ytFile = o as YtFile
        if (if (meta != null) !meta.equals(ytFile.meta) else ytFile.meta != null) return false
        return if (url != null) url == ytFile.url else ytFile.url == null
    }

    override fun hashCode(): Int {
        var result = if (meta != null) meta.hashCode() else 0
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }

    @NonNull
    override fun toString(): String {
        return "YtFile{" +
                "format=" + meta +
                ", url='" + url + '\'' +
                '}'
    }
}