package com.example.data

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ParsedPspDisc(
    val format: String,
    val isValidImage: Boolean,
    val isPspDisc: Boolean,
    val volumeLabel: String = "",
    val discId: String = "",
    val discTitle: String = "",
    val isBloodlinesMatch: Boolean = false,
    val extractedIconPath: String? = null,
    val extractedPic1Path: String? = null
)

object PspIsoParser {
    private const val SECTOR_SIZE = 2048L
    private const val MAX_DIR_BYTES = 512 * 1024
    private const val MAX_SFO_BYTES = 256 * 1024
    private const val MAX_ART_BYTES = 2 * 1024 * 1024

    fun parseAndExtractArt(
        discFile: File,
        artOutputDir: File,
        originalFileName: String
    ): ParsedPspDisc {
        if (!discFile.exists() || discFile.length() < 32) {
            return ParsedPspDisc(format = "UNKNOWN", isValidImage = false, isPspDisc = false)
        }

        return runCatching {
            RandomAccessFile(discFile, "r").use { raf ->
                val header4 = ByteArray(4)
                raf.seek(0L)
                raf.readFully(header4)
                val magicStr = String(header4, Charsets.US_ASCII)

                if (magicStr == "CISO") {
                    val isNameMatch = originalFileName.contains("bloodlines", ignoreCase = true) ||
                        originalFileName.contains("assassin", ignoreCase = true)
                    return ParsedPspDisc(
                        format = "CSO",
                        isValidImage = true,
                        isPspDisc = true,
                        volumeLabel = "CSO Compressed UMD",
                        discId = if (isNameMatch) "ULES-01367 (CSO)" else "PSP-CSO",
                        discTitle = if (isNameMatch) "Assassin's Creed: Bloodlines" else originalFileName.substringBeforeLast('.'),
                        isBloodlinesMatch = isNameMatch
                    )
                }

                // Check ISO-9660 Primary Volume Descriptor at sector 16 (0x8000)
                if (discFile.length() < 17 * SECTOR_SIZE) {
                    return ParsedPspDisc(format = "ISO", isValidImage = false, isPspDisc = false)
                }

                val pvd = ByteArray(SECTOR_SIZE.toInt())
                raf.seek(16L * SECTOR_SIZE)
                raf.readFully(pvd)

                val pvdType = pvd[0].toInt() and 0xFF
                val cd001 = String(pvd, 1, 5, Charsets.US_ASCII)
                if (pvdType != 1 || cd001 != "CD001") {
                    return ParsedPspDisc(format = "ISO", isValidImage = false, isPspDisc = false)
                }

                val volumeLabel = String(pvd, 40, 32, Charsets.US_ASCII).trim()
                val rootLba = readUInt32LE(pvd, 156 + 2)
                val rootLen = readUInt32LE(pvd, 156 + 10)

                val rootEntries = readDirectory(raf, rootLba, rootLen)
                val pspGameDir = rootEntries.firstOrNull {
                    it.isDirectory && it.name.equals("PSP_GAME", ignoreCase = true)
                }

                var discId = ""
                var discTitle = ""
                var iconPath: String? = null
                var pic1Path: String? = null

                if (pspGameDir != null) {
                    val pspGameEntries = readDirectory(raf, pspGameDir.extentLba, pspGameDir.dataLength)

                    val paramSfo = pspGameEntries.firstOrNull {
                        !it.isDirectory && it.name.equals("PARAM.SFO", ignoreCase = true)
                    }
                    if (paramSfo != null && paramSfo.dataLength in 20..MAX_SFO_BYTES) {
                        val sfoBytes = readFileRange(raf, paramSfo.extentLba, paramSfo.dataLength.toInt())
                        val sfoMap = parseSfoBytes(sfoBytes)
                        discId = sfoMap["DISC_ID"].orEmpty().trim()
                        discTitle = sfoMap["TITLE"].orEmpty().trim()
                    }

                    artOutputDir.mkdirs()

                    val icon0 = pspGameEntries.firstOrNull {
                        !it.isDirectory && it.name.equals("ICON0.PNG", ignoreCase = true)
                    }
                    if (icon0 != null && icon0.dataLength in 64..MAX_ART_BYTES) {
                        val iconFile = File(artOutputDir, "icon0.png")
                        iconFile.writeBytes(readFileRange(raf, icon0.extentLba, icon0.dataLength.toInt()))
                        iconPath = iconFile.absolutePath
                    }

                    val pic1 = pspGameEntries.firstOrNull {
                        !it.isDirectory && it.name.equals("PIC1.PNG", ignoreCase = true)
                    }
                    if (pic1 != null && pic1.dataLength in 64..MAX_ART_BYTES) {
                        val pic1File = File(artOutputDir, "pic1.png")
                        pic1File.writeBytes(readFileRange(raf, pic1.extentLba, pic1.dataLength.toInt()))
                        pic1Path = pic1File.absolutePath
                    }
                }

                // Fallback check for UMD_DATA.BIN in root if PARAM.SFO wasn't found
                if (discId.isEmpty()) {
                    val umdData = rootEntries.firstOrNull {
                        !it.isDirectory && it.name.equals("UMD_DATA.BIN", ignoreCase = true)
                    }
                    if (umdData != null && umdData.dataLength in 10..4096) {
                        val umdBytes = readFileRange(raf, umdData.extentLba, umdData.dataLength.toInt())
                        val rawId = String(umdBytes, 0, minOf(10, umdBytes.size), Charsets.US_ASCII)
                            .replace("-", "")
                            .trim()
                        if (rawId.length >= 9) discId = rawId
                    }
                }

                val isPsp = pspGameDir != null || discId.isNotEmpty()
                val normalizedId = discId.replace("-", "").uppercase()
                val knownBloodlinesIds = setOf("ULES01367", "ULES01368", "ULUS10455", "ULJM05571", "NPEH00018", "NPUH10025")
                val isBloodlines = normalizedId in knownBloodlinesIds ||
                    discTitle.contains("bloodlines", ignoreCase = true) ||
                    (discTitle.contains("assassin", ignoreCase = true) && discTitle.contains("creed", ignoreCase = true)) ||
                    originalFileName.contains("bloodlines", ignoreCase = true)

                ParsedPspDisc(
                    format = "ISO",
                    isValidImage = true,
                    isPspDisc = isPsp,
                    volumeLabel = volumeLabel,
                    discId = discId.ifEmpty { if (isPsp) "PSP-ISO" else "ISO-9660" },
                    discTitle = discTitle.ifEmpty { volumeLabel.ifEmpty { originalFileName.substringBeforeLast('.') } },
                    isBloodlinesMatch = isBloodlines,
                    extractedIconPath = iconPath,
                    extractedPic1Path = pic1Path
                )
            }
        }.getOrElse {
            ParsedPspDisc(format = "UNKNOWN", isValidImage = false, isPspDisc = false)
        }
    }

    internal fun parseSfoBytes(bytes: ByteArray): Map<String, String> {
        if (bytes.size < 20) return emptyMap()
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        if (bytes[0] != 0.toByte() || bytes[1] != 'P'.code.toByte() || bytes[2] != 'S'.code.toByte() || bytes[3] != 'F'.code.toByte()) {
            return emptyMap()
        }
        val keyTableStart = buf.getInt(8)
        val dataTableStart = buf.getInt(12)
        val entryCount = buf.getInt(16).coerceIn(0, 128)
        val result = mutableMapOf<String, String>()

        for (i in 0 until entryCount) {
            val entryPos = 20 + i * 16
            if (entryPos + 16 > bytes.size) break
            val keyOffset = buf.getShort(entryPos).toInt() and 0xFFFF
            val dataFormat = buf.getShort(entryPos + 2).toInt() and 0xFFFF
            val dataLen = buf.getInt(entryPos + 4)
            val dataOffset = buf.getInt(entryPos + 12)

            val absKeyStart = keyTableStart + keyOffset
            if (absKeyStart !in bytes.indices) continue
            var absKeyEnd = absKeyStart
            while (absKeyEnd < bytes.size && bytes[absKeyEnd] != 0.toByte()) {
                absKeyEnd++
            }
            val key = String(bytes, absKeyStart, absKeyEnd - absKeyStart, Charsets.UTF_8)

            // 0x0204 (516) = UTF-8 string
            if (dataFormat == 0x0204 && dataLen > 0) {
                val absDataStart = dataTableStart + dataOffset
                if (absDataStart in bytes.indices) {
                    val safeLen = minOf(dataLen, bytes.size - absDataStart)
                    var actualLen = 0
                    while (actualLen < safeLen && bytes[absDataStart + actualLen] != 0.toByte()) {
                        actualLen++
                    }
                    result[key] = String(bytes, absDataStart, actualLen, Charsets.UTF_8)
                }
            }
        }
        return result
    }

    private data class IsoDirEntry(
        val name: String,
        val extentLba: Long,
        val dataLength: Long,
        val isDirectory: Boolean
    )

    private fun readDirectory(raf: RandomAccessFile, lba: Long, length: Long): List<IsoDirEntry> {
        val clampedLen = length.coerceIn(0L, MAX_DIR_BYTES.toLong()).toInt()
        if (clampedLen == 0) return emptyList()
        val data = readFileRange(raf, lba, clampedLen)
        val entries = mutableListOf<IsoDirEntry>()
        var pos = 0

        while (pos < data.size) {
            val recordLen = data[pos].toInt() and 0xFF
            if (recordLen == 0) {
                val nextSector = ((pos / SECTOR_SIZE.toInt()) + 1) * SECTOR_SIZE.toInt()
                if (nextSector <= pos || nextSector >= data.size) break
                pos = nextSector
                continue
            }
            if (pos + recordLen > data.size || recordLen < 34) break

            val extentLba = readUInt32LE(data, pos + 2)
            val dataLength = readUInt32LE(data, pos + 10)
            val flags = data[pos + 25].toInt() and 0xFF
            val nameLen = data[pos + 32].toInt() and 0xFF

            if (pos + 33 + nameLen <= data.size && nameLen > 0) {
                val rawName = String(data, pos + 33, nameLen, Charsets.US_ASCII)
                // Skip '.' (0x00) and '..' (0x01)
                if (!(nameLen == 1 && (data[pos + 33] == 0.toByte() || data[pos + 33] == 1.toByte()))) {
                    val cleanName = rawName.substringBefore(';')
                    entries.add(
                        IsoDirEntry(
                            name = cleanName,
                            extentLba = extentLba,
                            dataLength = dataLength,
                            isDirectory = (flags and 0x02) != 0
                        )
                    )
                }
            }
            pos += recordLen
        }
        return entries
    }

    private fun readFileRange(raf: RandomAccessFile, lba: Long, length: Int): ByteArray {
        val offset = lba * SECTOR_SIZE
        val fileLen = raf.length()
        if (offset < 0 || offset >= fileLen) return ByteArray(0)
        val safeLen = minOf(length.toLong(), fileLen - offset).toInt()
        val out = ByteArray(safeLen)
        raf.seek(offset)
        raf.readFully(out)
        return out
    }

    private fun readUInt32LE(buf: ByteArray, offset: Int): Long {
        if (offset + 4 > buf.size) return 0L
        return ((buf[offset].toLong() and 0xFF)) or
            ((buf[offset + 1].toLong() and 0xFF) shl 8) or
            ((buf[offset + 2].toLong() and 0xFF) shl 16) or
            ((buf[offset + 3].toLong() and 0xFF) shl 24)
    }
}
