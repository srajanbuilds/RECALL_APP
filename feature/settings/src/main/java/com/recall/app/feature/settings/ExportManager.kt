package com.recall.app.feature.settings

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encrypted export/import per spec Section 7.
 * Key derivation: PBKDF2WithHmacSHA256, 310,000 iterations, random 16-byte salt.
 * File format: [16-byte salt][12-byte IV][ciphertext+GCM tag]
 */
object ExportManager {

    private const val ITERATIONS   = 310_000
    private const val KEY_LEN      = 256
    private const val SALT_LEN     = 16
    private const val IV_LEN       = 12
    private const val GCM_TAG_BITS = 128

    @Serializable
    data class ExportPayload(
        val version: Int = 1,
        val exportedAt: Long,
        val notes: List<ExportNote>,
        val reminders: List<ExportReminder>
    )

    @Serializable
    data class ExportNote(
        val id: String, val title: String, val body: String,
        val createdAt: Long, val updatedAt: Long, val isPrivate: Boolean,
        val isPinned: Boolean, val isArchived: Boolean, val tags: String, val colorHex: String?
    )

    @Serializable
    data class ExportReminder(
        val id: String, val noteId: String, val label: String,
        val triggerAt: Long, val isCompleted: Boolean
    )

    sealed class ExportResult {
        object Success : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    // ── Export ────────────────────────────────────────────────────────

    suspend fun export(
        context: Context,
        uri: Uri,
        passphrase: String,
        payload: ExportPayload
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.encodeToString(payload)
            val plainBytes = json.toByteArray(Charsets.UTF_8)

            val salt = SecureRandom().generateSeed(SALT_LEN)
            val iv   = SecureRandom().generateSeed(IV_LEN)
            val key  = deriveKey(passphrase, salt)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            val cipherBytes = cipher.doFinal(plainBytes)

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(salt)
                out.write(iv)
                out.write(cipherBytes)
            } ?: return@withContext ExportResult.Error("Could not open output stream")

            ExportResult.Success
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Export failed")
        }
    }

    // ── Import ────────────────────────────────────────────────────────

    sealed class ImportResult {
        data class Success(val payload: ExportPayload) : ImportResult()
        object WrongPassphrase : ImportResult()
        object CorruptFile : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun import(
        context: Context,
        uri: Uri,
        passphrase: String
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@withContext ImportResult.CorruptFile

            if (bytes.size < SALT_LEN + IV_LEN + 1) return@withContext ImportResult.CorruptFile

            val salt       = bytes.copyOfRange(0, SALT_LEN)
            val iv         = bytes.copyOfRange(SALT_LEN, SALT_LEN + IV_LEN)
            val cipherText = bytes.copyOfRange(SALT_LEN + IV_LEN, bytes.size)

            val key = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))

            val plainBytes = try {
                cipher.doFinal(cipherText)
            } catch (e: javax.crypto.AEADBadTagException) {
                return@withContext ImportResult.WrongPassphrase
            } catch (e: Exception) {
                return@withContext ImportResult.CorruptFile
            }

            val payload = try {
                Json.decodeFromString<ExportPayload>(String(plainBytes, Charsets.UTF_8))
            } catch (e: Exception) {
                return@withContext ImportResult.CorruptFile
            }

            ImportResult.Success(payload)
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Import failed")
        }
    }

    // ── Key derivation ────────────────────────────────────────────────

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LEN)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
