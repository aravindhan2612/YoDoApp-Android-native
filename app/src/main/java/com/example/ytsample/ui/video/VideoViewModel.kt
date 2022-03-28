package com.example.ytsample.ui.video

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContentResolverCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ytsample.entities.video.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class VideoViewModel(savedStateHandle: SavedStateHandle):ViewModel() {

    var videoListLiveData = MutableLiveData<ArrayList<Video>>()


    fun fetchVideo(context: Context?){
        viewModelScope.launch {
            val videoList = ArrayList<Video>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.WIDTH
            )
// Display videos in alphabetical order based on their display name.
            val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

            val result = withContext(Dispatchers.IO) {
                val query = context?.contentResolver?.query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
                )
                query?.use { cursor ->
                    // Cache column indices.
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val width = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                    val height = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

                    while (cursor.moveToNext()) {
                        // Get values of columns for a given video.
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val duration = cursor.getInt(durationColumn)
                        val size = cursor.getInt(sizeColumn)

                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        // Stores column values and the contentUri in a local object
                        // that represents the media file.

                        videoList.add( Video(contentUri, name, duration, size,id))
                        println("***** videoList "+videoList.size)
                    }
                }
            }

            videoListLiveData.value = videoList
        }
    }
}