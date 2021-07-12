package com.radziejewskig.todo.utils

import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.*
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.ERROR_BUCKET_NOT_FOUND
import com.radziejewskig.todo.R

object ErrorUtil {

    fun getStringResForException(e: Throwable?): Int {
        return when(e) {
            is FirebaseNetworkException -> R.string.error_check_internet_connection
            is FirebaseTooManyRequestsException -> R.string.error_too_many_requests
            is FirebaseApiNotAvailableException -> R.string.error_api_not_available
            is StorageException -> {
                when(e.errorCode) {
                    ERROR_BUCKET_NOT_FOUND -> R.string.error_storage_bucket_not_found
                    else -> R.string.an_unknown_storage_error_occurred
                }
            }
            is FirebaseFirestoreException -> {
                when(e.code) {
                    ALREADY_EXISTS -> R.string.error_document_already_exists
                    NOT_FOUND -> R.string.error_document_not_found
                    UNAVAILABLE -> R.string.error_check_internet_connection
                    else -> R.string.an_unknown_firestore_error_occurred
                }
            }
            else -> R.string.an_unknown_error_occurred
        }
    }

}
