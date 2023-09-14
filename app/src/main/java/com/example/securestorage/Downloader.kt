package com.example.securestorage

interface Downloader {
    fun downloadFile(url: String): Long
}