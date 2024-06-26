package com.example.ytsample.entities

import androidx.annotation.NonNull

class Format {

    enum class VCodec {
        H263, H264, MPEG4, VP8, VP9, NONE
    }

    enum class ACodec {
        MP3, AAC, VORBIS, OPUS, NONE
    }

    /**
     * An identifier used by youtube for different formats.
     */
    var itag = 0

    /**
     * The file extension and conainer format like "mp4"
     */
    var ext: String? = null

    /**
     * The pixel height of the video stream or -1 for audio files.
     */
    var height = 0

    /**
     * Get the frames per second
     */
    var fps = 0
    val videoCodec: VCodec? = null
    val audioCodec: ACodec? = null

    /**
     * Audio bitrate in kbit/s or -1 if there is no audio track.
     */
    var audioBitrate = 0
    var isDashContainer = false
    var isHlsContent = false

    constructor(
        itag: Int,
        ext: String?,
        height: Int,
        vCodec: VCodec?,
        aCodec: ACodec?,
        isDashContainer: Boolean
    ) {
        this.itag = itag
        this.ext = ext
        this.height = height
        fps = 30
        audioBitrate = -1
        this.isDashContainer = isDashContainer
        isHlsContent = false
    }

    constructor(
        itag: Int,
        ext: String?,
        vCodec: VCodec?,
        aCodec: ACodec?,
        audioBitrate: Int,
        isDashContainer: Boolean
    ) {
        this.itag = itag
        this.ext = ext
        height = -1
        fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        isHlsContent = false
    }

    constructor(
        itag: Int, ext: String?, height: Int, vCodec: VCodec?, aCodec: ACodec?, audioBitrate: Int,
        isDashContainer: Boolean
    ) {
        this.itag = itag
        this.ext = ext
        this.height = height
        fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        isHlsContent = false
    }

    constructor (
        itag: Int, ext: String?, height: Int, vCodec: VCodec?, aCodec: ACodec?, audioBitrate: Int,
        isDashContainer: Boolean, isHlsContent: Boolean
    ) {
        this.itag = itag
        this.ext = ext
        this.height = height
        fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        this.isHlsContent = isHlsContent
    }

    constructor (
        itag: Int,
        ext: String?,
        height: Int,
        vCodec: VCodec?,
        fps: Int,
        aCodec: ACodec?,
        isDashContainer: Boolean
    ) {
        this.itag = itag
        this.ext = ext
        this.height = height
        audioBitrate = -1
        this.fps = fps
        this.isDashContainer = isDashContainer
        isHlsContent = false
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val format: Format = o as Format
        if (itag != format.itag) return false
        if (height != format.height) return false
        if (fps != format.fps) return false
        if (audioBitrate != format.audioBitrate) return false
        if (isDashContainer != format.isDashContainer) return false
        if (isHlsContent != format.isHlsContent) return false
        if (if (ext != null) ext != format.ext else format.ext != null) return false
        return if (videoCodec != format.videoCodec) false else audioCodec == format.audioCodec
    }

    override fun hashCode(): Int {
        var result = itag
        result = 31 * result + (ext?.hashCode() ?: 0)
        result = 31 * result + height
        result = 31 * result + fps
        result = 31 * result + if (videoCodec != null) videoCodec.hashCode() else 0
        result = 31 * result + if (audioCodec != null) audioCodec.hashCode() else 0
        result = 31 * result + audioBitrate
        result = 31 * result + if (isDashContainer) 1 else 0
        result = 31 * result + if (isHlsContent) 1 else 0
        return result
    }

    @NonNull
    override fun toString(): String {
        return "Format{" +
                "itag=" + itag +
                ", ext='" + ext + '\'' +
                ", height=" + height +
                ", fps=" + fps +
                ", vCodec=" + videoCodec +
                ", aCodec=" + audioCodec +
                ", audioBitrate=" + audioBitrate +
                ", isDashContainer=" + isDashContainer +
                ", isHlsContent=" + isHlsContent +
                '}'
    }
}