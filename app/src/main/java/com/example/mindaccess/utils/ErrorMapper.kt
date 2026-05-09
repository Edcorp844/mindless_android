package com.example.mindaccess.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

object ErrorMapper {
    fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException, is ConnectException -> 
                "Unable to connect to the server. Please check your internet connection."
            is SocketTimeoutException -> 
                "The connection timed out. Please try again later."
            is HttpException -> {
                when (exception.code()) {
                    401 -> "Unauthorized. Please sign in again."
                    403 -> "You don't have permission to perform this action."
                    404 -> "The requested resource was not found."
                    in 500..599 -> "Server error. We're working on fixing it."
                    else -> "Something went wrong. Please try again."
                }
            }
            else -> {
                val message = exception.message ?: ""
                when {
                    message.contains("network", ignoreCase = true) -> 
                        "Network error. Please check your connection."
                    message.contains("timeout", ignoreCase = true) -> 
                        "The request timed out."
                    else -> "An unexpected error occurred."
                }
            }
        }
    }
}
