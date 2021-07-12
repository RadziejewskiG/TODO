package com.radziejewskig.todo.data.source.network

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.radziejewskig.todo.data.StorageRepository
import com.radziejewskig.todo.utils.FileUtils
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val fileUtils: FileUtils
): StorageRepository {

    override suspend fun uploadImage(bitmap: Bitmap): String? = try {
        val generatedImageName = fileUtils.generateImageName()
        val imageStorageRef = storage.reference.child("$REF_IMAGES$generatedImageName")
        val scaledBitmap = fileUtils.resizeBitmap(bitmap)

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 97, baos)
        val imageData = baos.toByteArray()

        imageStorageRef.putBytes(imageData).await()

        imageStorageRef.downloadUrl.await()?.toString()
    } catch (e: Exception) {
        throw e
    }

    override suspend fun loadImages(): List<String> = try {
        val storageRef = storage.reference.child(REF_IMAGES)
        val imagesRefs = storageRef.listAll().await()

        val imagesUrls = mutableListOf<String>()

        imagesRefs.items.forEach {
            it.downloadUrl.await()?.let { url ->
                imagesUrls.add(url.toString())
            }
        }

        imagesUrls
    } catch (e: Exception) {
        throw e
    }

}

const val REF_IMAGES = "images/"
