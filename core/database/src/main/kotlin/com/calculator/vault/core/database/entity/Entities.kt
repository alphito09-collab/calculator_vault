package com.calculator.vault.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.calculator.vault.core.model.IntruderAttempt
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultItem

@Entity(
    tableName = "vault_items",
    indices = [Index("type"), Index("trashed_at"), Index("date_taken")],
)
data class VaultItemEntity(
    @PrimaryKey val id: String,
    val type: MediaType,
    val name: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "original_path") val originalPath: String?,
    val album: String?,
    @ColumnInfo(name = "date_taken") val dateTaken: Long,
    @ColumnInfo(name = "date_added") val dateAdded: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long?,
    val width: Int?,
    val height: Int?,
    @ColumnInfo(name = "trashed_at") val trashedAt: Long?,
    @ColumnInfo(name = "has_thumbnail") val hasThumbnail: Boolean,
)

fun VaultItemEntity.asModel() = VaultItem(
    id = id,
    type = type,
    name = name,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    originalPath = originalPath,
    album = album,
    dateTaken = dateTaken,
    dateAdded = dateAdded,
    durationMs = durationMs,
    width = width,
    height = height,
    trashedAt = trashedAt,
    hasThumbnail = hasThumbnail,
)

fun VaultItem.asEntity() = VaultItemEntity(
    id = id,
    type = type,
    name = name,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    originalPath = originalPath,
    album = album,
    dateTaken = dateTaken,
    dateAdded = dateAdded,
    durationMs = durationMs,
    width = width,
    height = height,
    trashedAt = trashedAt,
    hasThumbnail = hasThumbnail,
)

@Entity(tableName = "intruder_attempts", indices = [Index("timestamp")])
data class IntruderAttemptEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    @ColumnInfo(name = "failed_attempts") val failedAttempts: Int,
    @ColumnInfo(name = "has_photo") val hasPhoto: Boolean,
)

fun IntruderAttemptEntity.asModel() = IntruderAttempt(id, timestamp, failedAttempts, hasPhoto)

/** What has already been uploaded to Drive, so backups are incremental. */
@Entity(tableName = "backup_files")
data class BackupFileEntity(
    @PrimaryKey @ColumnInfo(name = "relative_path") val relativePath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long,
    @ColumnInfo(name = "drive_file_id") val driveFileId: String,
    @ColumnInfo(name = "uploaded_at") val uploadedAt: Long,
)

data class TypeCount(val type: MediaType, val count: Int)
