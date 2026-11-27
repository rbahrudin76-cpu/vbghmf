package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class IsoVaultRepository(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val vaultDir: File
        get() = File(context.filesDir, "game_vault").apply { mkdirs() }

    private val artDir: File
        get() = File(vaultDir, "art").apply { mkdirs() }

    fun importIsoFromUri(uri: Uri): Flow<ImportProgressState> = flow {
        val resolver = context.contentResolver
        var displayName = "assassins_creed_bloodlines.iso"
        var totalSize = -1L

        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) totalSize = cursor.getLong(sizeIdx)
                }
            }

        val lowerName = displayName.lowercase()
        if (lowerName.endsWith(".zip") || lowerName.endsWith(".7z") || lowerName.endsWith(".rar")) {
            emit(
                ImportProgressState.Error(
                    "File masih terkompresi ($displayName). Ekstrak dulu menjadi file .iso atau .cso lalu import kembali."
                )
            )
            return@flow
        }

        if (!lowerName.endsWith(".iso") && !lowerName.endsWith(".cso")) {
            emit(
                ImportProgressState.Error(
                    "Format file tidak didukung ($displayName). Pilih file disc PSP berekstensi .iso atau .cso."
                )
            )
            return@flow
        }

        val ext = if (lowerName.endsWith(".cso")) "cso" else "iso"
        val tempFile = File(vaultDir, "import_temp.$ext")
        val finalFile = File(vaultDir, "bloodlines_disc.$ext")

        // Clear any old temp file or previous disc extension
        tempFile.delete()
        File(vaultDir, "bloodlines_disc.iso").takeIf { ext != "iso" }?.delete()
        File(vaultDir, "bloodlines_disc.cso").takeIf { ext != "cso" }?.delete()
        artDir.listFiles()?.forEach { it.delete() }

        emit(ImportProgressState.Copying(copiedBytes = 0L, totalBytes = totalSize, speedMbps = 0f))

        val startTime = System.currentTimeMillis()
        var lastEmitTime = startTime
        var copied = 0L

        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(256 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    copied += read

                    val now = System.currentTimeMillis()
                    if (now - lastEmitTime >= 120L) {
                        val elapsedSec = ((now - startTime).coerceAtLeast(1L)) / 1000f
                        val speedMbps = (copied / (1024f * 1024f)) / elapsedSec
                        emit(
                            ImportProgressState.Copying(
                                copiedBytes = copied,
                                totalBytes = if (totalSize > 0L) totalSize else copied,
                                speedMbps = speedMbps
                            )
                        )
                        lastEmitTime = now
                    }
                }
                output.flush()
            }
        } ?: run {
            emit(ImportProgressState.Error("Gagal membuka file dari penyimpanan. Coba pilih ulang file ISO."))
            return@flow
        }

        if (copied < 1024L * 1024L) {
            tempFile.delete()
            emit(
                ImportProgressState.Error(
                    "Ukuran file terlalu kecil (${copied / 1024} KB). Pastikan Anda memilih file ISO game PSP lengkap (~500–600 MB)."
                )
            )
            return@flow
        }

        emit(ImportProgressState.Verifying)

        val parsed = PspIsoParser.parseAndExtractArt(
            discFile = tempFile,
            artOutputDir = artDir,
            originalFileName = displayName
        )

        if (!parsed.isValidImage) {
            tempFile.delete()
            emit(
                ImportProgressState.Error(
                    "Header disc tidak valid. Pastikan file merupakan ISO-9660 atau CSO PSP yang tidak rusak."
                )
            )
            return@flow
        }

        if (finalFile.exists()) finalFile.delete()
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()
        }

        val info = ImportedIsoInfo(
            isInstalled = true,
            fileName = displayName,
            internalFilePath = finalFile.absolutePath,
            sizeBytes = finalFile.length(),
            format = parsed.format,
            discId = parsed.discId,
            discTitle = parsed.discTitle.ifEmpty { "Assassin's Creed: Bloodlines" },
            volumeLabel = parsed.volumeLabel,
            isVerifiedPspDisc = parsed.isPspDisc,
            isBloodlinesMatch = parsed.isBloodlinesMatch,
            iconPath = parsed.extractedIconPath,
            pic1Path = parsed.extractedPic1Path,
            importedAtMillis = System.currentTimeMillis()
        )
        settingsRepository.saveImportedIso(info)
        emit(ImportProgressState.Idle)
    }.flowOn(Dispatchers.IO)

    suspend fun deleteVaultIso() {
        withContext(Dispatchers.IO) {
            vaultDir.listFiles()?.forEach { file ->
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
            settingsRepository.clearImportedIso()
        }
    }

    fun findInstalledExternalPspCore(): String? {
        val candidates = listOf("org.ppsspp.ppssppgold", "org.ppsspp.ppsspp")
        val pm = context.packageManager
        for (pkg in candidates) {
            val installed = runCatching {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                true
            }.recoverCatching {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
                true
            }.getOrDefault(false)
            if (installed) return pkg
        }
        return null
    }

    fun buildExternalCoreLaunchIntent(packageName: String, internalPath: String): Intent? {
        val discFile = File(internalPath)
        if (!discFile.exists()) return null
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            discFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/octet-stream")
            setPackage(packageName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
