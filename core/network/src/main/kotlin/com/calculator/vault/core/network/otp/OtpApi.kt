package com.calculator.vault.core.network.otp

import com.calculator.vault.core.network.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

/**
 * Client for our own OTP backend (to be built with Ktor server later).
 *
 * Contract:
 * - `POST {OTP_BASE_URL}otp/send   {"email": "..."}`                 → 204
 * - `POST {OTP_BASE_URL}otp/verify {"email": "...", "code": "123456"}` → `{"valid": true}`
 */
@Single
class OtpApi(private val client: HttpClient) {
    private val baseUrl = BuildConfig.OTP_BASE_URL.trimEnd('/') + "/"

    suspend fun send(email: String) {
        client.post(baseUrl + "otp/send") {
            contentType(ContentType.Application.Json)
            setBody(SendRequest(email))
        }
    }

    suspend fun verify(email: String, code: String): Boolean =
        client.post(baseUrl + "otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(VerifyRequest(email, code))
        }.body<VerifyResponse>().valid

    @Serializable private data class SendRequest(val email: String)

    @Serializable private data class VerifyRequest(val email: String, val code: String)

    @Serializable private data class VerifyResponse(val valid: Boolean)
}
