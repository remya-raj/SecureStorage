package com.example.securestorage

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

class AndroidDownloader(private val context: Context): Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        var request = DownloadManager.Request(Uri.parse(url))
            .setDestinationInExternalFilesDir(context,
                Environment.DIRECTORY_DOWNLOADS, File.separator + "folderName" + File.separator + "fileName")
//            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "test.mp4")

        return downloadManager.enqueue(request)
    }
}