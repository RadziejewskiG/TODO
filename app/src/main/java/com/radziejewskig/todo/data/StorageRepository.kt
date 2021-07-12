package com.radziejewskig.todo.data

import android.graphics.Bitmap

interface StorageRepository {

    suspend fun uploadImage(bitmap: Bitmap): String?

    suspend fun loadImages(): List<String>
}