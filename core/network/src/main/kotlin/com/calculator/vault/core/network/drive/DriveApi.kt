package com.calculator.vault.core.network.drive

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single
import java.io.File

@Serializable
data class DriveFile(
    val id: String,
    val name: String,
    val size: String? = null,
    val modifiedTime: String? = null,
    val appProperties: Map<String, String>? = null,
)

/** Minimal Google Drive v3 REST client scoped to the hidden appDataFolder. */
@Single
class DriveApi(private val client: HttpClient) {

    suspend fun quota(token: String): Pair<Long, Long> {
        val about = client.get("$API/about") {
            bearerAuth(token)
            parameter("fields", "storageQuota")
        }.body<About>()
        val used = about.storageQuota.usage?.toLongOrNull() ?: 0L
        val limit = about.storageQuota.limit?.toLongOrNull() ?: 0L
        return used to limit
    }

    suspend fun listAppData(token: String): List<DriveFile> {
        val files = mutableListOf<DriveFile>()
        var pageToken: String? = null
        do {
            val page = client.get("$API/files") {
                bearerAuth(token)
                parameter("spaces", "appDataFolder")
                parameter("pageSize", 1000)
                parameter("fields", "nextPageToken,files(id,name,size,modifiedTime,appProperties)")
                pageToken?.let { parameter("pageToken", it) }
            }.body<FileList>()
            files += page.files
            pageToken = page.nextPageToken
        } while (pageToken != null)
        return files
    }

    /** Resumable upload streamed from disk so large videos never load into memory. */
    suspend fun upload(token: String, file: File, name: String, relativePath: String): String {
        val session = client.post("$UPLOAD/files") {
            bearerAuth(token)
            parameter("uploadType", "resumable")
            contentType(ContentType.Application.Json)
            header("X-Upload-Content-Type", "application/octet-stream")
            header("X-Upload-Content-Length", file.length().toString())
            setBody(NewFile(name = name, parents = listOf("appDataFolder"), appProperties = mapOf("path" to relativePath)))
        }
        val location = session.headers[HttpHeaders.Location] ?: error("Drive did not return an upload session")
        val created = client.put(location) {
            bearerAuth(token)
            setBody(FileContent(file))
        }.body<DriveFile>()
        return created.id
    }

    suspend fun download(token: String, fileId: String, target: File) {
        client.prepareGet("$API/files/$fileId") {
            bearerAuth(token)
            parameter("alt", "media")
        }.execute { response ->
            val channel = response.bodyAsChannel()
            target.parentFile?.mkdirs()
            target.outputStream().use { out ->
                val buffer = ByteArray(DEFAULT_BUFFER)
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read == -1) break
                    if (read > 0) out.write(buffer, 0, read)
                }
            }
        }
    }

    suspend fun delete(token: String, fileId: String) {
        client.delete("$API/files/$fileId") { bearerAuth(token) }
    }

    private class FileContent(private val file: File) : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType = ContentType.Application.OctetStream
        override val contentLength: Long = file.length()

        override suspend fun writeTo(channel: ByteWriteChannel) {
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    channel.writeFully(buffer, 0, read)
                }
            }
        }
    }

    @Serializable private data class NewFile(val name: String, val parents: List<String>, val appProperties: Map<String, String>)

    @Serializable private data class FileList(val files: List<DriveFile> = emptyList(), val nextPageToken: String? = null)

    @Serializable private data class About(val storageQuota: Quota)

    @Serializable private data class Quota(val limit: String? = null, val usage: String? = null)

    private companion object {
        const val API = "https://www.googleapis.com/drive/v3"
        const val UPLOAD = "https://www.googleapis.com/upload/drive/v3"
        const val DEFAULT_BUFFER = 64 * 1024
    }
}
