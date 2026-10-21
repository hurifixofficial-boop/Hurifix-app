package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

/** Direct Android OAuth/Drive REST helper. The token must be obtained by the app's Google authorization flow. */
object GoogleDriveMediaHelper {
    private const val API = "https://www.googleapis.com/drive/v3/files"
    private const val UPLOAD_API = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,webViewLink,mimeType"
    private const val FOLDER_NAME = "Hurifix Order Media"
    private const val FOLDER_MIME = "application/vnd.google-apps.folder"
    private val client = OkHttpClient()

    data class UploadedMedia(val id: String, val name: String, val url: String, val mimeType: String)

    suspend fun uploadUris(context: Context, accessToken: String, uris: List<Uri>, orderLabel: String, category: String): List<UploadedMedia> = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext emptyList()
        val folderId = getOrCreateFolder(accessToken)
        uris.mapIndexed { index, uri ->
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val originalName = queryName(context, uri) ?: "media_${System.currentTimeMillis()}_$index"
            val safeName = "${orderLabel}_${category}_${originalName}".replace(Regex("[^A-Za-z0-9._-]"), "_")
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Could not read selected file: $originalName")
            uploadOne(accessToken, folderId, safeName, mime, bytes)
        }
    }

    private fun getOrCreateFolder(token: String): String {
        val query = "name = '$FOLDER_NAME' and mimeType = '$FOLDER_MIME' and trashed = false"
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val get = Request.Builder().url("$API?q=$encoded&fields=files(id,name)")
            .header("Authorization", "Bearer $token").get().build()
        client.newCall(get).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive folder lookup failed (${response.code})")
            val files = JSONObject(response.body?.string().orEmpty()).optJSONArray("files") ?: JSONArray()
            if (files.length() > 0) return files.getJSONObject(0).getString("id")
        }
        val metadata = JSONObject().put("name", FOLDER_NAME).put("mimeType", FOLDER_MIME)
            .toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val create = Request.Builder().url(API).header("Authorization", "Bearer $token").post(metadata).build()
        client.newCall(create).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive folder creation failed (${response.code}): ${response.body?.string().orEmpty()}")
            return JSONObject(response.body?.string().orEmpty()).getString("id")
        }
    }

    private fun uploadOne(token: String, folderId: String, name: String, mime: String, bytes: ByteArray): UploadedMedia {
        val boundary = "hurifix_${UUID.randomUUID()}"
        val metadata = JSONObject().put("name", name).put("mimeType", mime)
            .put("parents", JSONArray().put(folderId)).toString()
        val preamble = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$metadata\r\n--$boundary\r\nContent-Type: $mime\r\nContent-Transfer-Encoding: binary\r\n\r\n"
            .toByteArray(Charsets.UTF_8)
        val closing = "\r\n--$boundary--".toByteArray(Charsets.UTF_8)
        val body = object : RequestBody() {
            override fun contentType() = "multipart/related; boundary=$boundary".toMediaType()
            override fun contentLength() = preamble.size.toLong() + bytes.size + closing.size
            override fun writeTo(sink: okio.BufferedSink) {
                sink.write(preamble); sink.write(bytes); sink.write(closing)
            }
        }
        val request = Request.Builder().url(UPLOAD_API).header("Authorization", "Bearer $token").post(body).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Drive upload failed (${response.code}): $raw")
            val obj = JSONObject(raw)
            val id = obj.getString("id")
            val link = obj.optString("webViewLink").ifBlank { "https://drive.google.com/file/d/$id/view" }
            return UploadedMedia(id, obj.optString("name", name), link, obj.optString("mimeType", mime))
        }
    }

    private fun queryName(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: Exception) { null }
}
