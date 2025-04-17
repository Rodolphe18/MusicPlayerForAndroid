package com.example.contentproviderformusic

import android.media.MediaMetadataRetriever

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}