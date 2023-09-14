package com.example.securestorage

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File

class AndroidDownloader(private val context: Context) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val fileName = "fileName"
        val folderName = "folderName"
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            File.separator + folderName + File.separator + fileName
        )
        if (file.exists()) {
            deleteDownloadedFile(context, folderName, fileName)
        }
        var request = DownloadManager.Request(Uri.parse(url))
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                File.separator + folderName + File.separator + fileName
            )
//            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "test.mp4")
        return downloadManager.enqueue(request)
    }
}

fun deleteDownloadedFile(context: Context, folderName: String, fileName: String) {
    val downloadPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val filePath = File(downloadPath, File.separator + folderName + File.separator + fileName)

    if (filePath.exists()) {
        if (filePath.delete()) {
            // File deleted successfully
            val downloadId = getDownloadIdForFile(context, fileName)

            // Remove the download from the Download Manager
            if (downloadId != -1L) {
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.remove(downloadId)
            }

            // Clear traces from the MediaStore if it's a media file
            val contentResolver = context.contentResolver
            val contentUri = MediaStore.Files.getContentUri("external")
            contentResolver.delete(
                contentUri,
                "${MediaStore.MediaColumns.DATA}=?",
                arrayOf(filePath.absolutePath)
            )
        } else {
            // Failed to delete the file
        }
    }
}

fun getDownloadIdForFile(context: Context, fileName: String): Long {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query()
    query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)

    val cursor: Cursor = downloadManager.query(query)
    try {
        while (cursor.moveToNext()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            if (columnIndex != -1) {
                val localUri = Uri.parse(cursor.getString(columnIndex))
                val localFile = File(localUri.path)
                if (localFile.name == fileName) {
                    val idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                    if (idIndex != -1) {
                        return cursor.getLong(idIndex)
                    }
                }
            }
        }
    } finally {
        cursor.close()
    }
    return -1L
}