package com.example

import com.example.data.PerformancePreset
import com.example.data.PerformanceSettings
import com.example.data.PspIsoParser
import com.example.data.RenderBackend
import com.example.data.ResolutionScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ExampleUnitTest {

    @Test
    fun `ultra anti lag preset configures performance flags`() {
        val base = PerformanceSettings()
        val ultra = PerformanceSettings.forPreset(PerformancePreset.ULTRA_ANTI_LAG, base)
        assertEquals(RenderBackend.VULKAN, ultra.renderBackend)
        assertEquals(ResolutionScale.X1_NATIVE, ultra.resolutionScale)
        assertTrue(ultra.disableHeavyEffects)
        assertTrue(ultra.lazyTextureCaching)
        assertTrue(ultra.hardwareTransform)
    }

    @Test
    fun `parseSfoBytes extracts DISC_ID and TITLE`() {
        val keyTable = "DISC_ID\u0000TITLE\u0000".toByteArray(Charsets.UTF_8)
        val discIdBytes = "ULES01367\u0000".toByteArray(Charsets.UTF_8)
        val titleBytes = "Assassin's Creed: Bloodlines\u0000".toByteArray(Charsets.UTF_8)

        val headerSize = 20
        val indexSize = 2 * 16
        val keyTableStart = headerSize + indexSize
        val dataTableStart = keyTableStart + keyTable.size

        val totalSize = dataTableStart + discIdBytes.size + titleBytes.size
        val buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        // SFO Header: \0PSF, version 1.1
        buf.put(0.toByte()).put('P'.code.toByte()).put('S'.code.toByte()).put('F'.code.toByte())
        buf.putInt(0x00000101)
        buf.putInt(keyTableStart)
        buf.putInt(dataTableStart)
        buf.putInt(2)

        // Entry 0: DISC_ID
        buf.putShort(0) // keyOffset
        buf.putShort(0x0204) // UTF-8
        buf.putInt(discIdBytes.size)
        buf.putInt(discIdBytes.size)
        buf.putInt(0) // dataOffset

        // Entry 1: TITLE
        buf.putShort("DISC_ID\u0000".length.toShort())
        buf.putShort(0x0204)
        buf.putInt(titleBytes.size)
        buf.putInt(titleBytes.size)
        buf.putInt(discIdBytes.size)

        buf.put(keyTable)
        buf.put(discIdBytes)
        buf.put(titleBytes)

        val parsed = PspIsoParser.parseSfoBytes(buf.array())
        assertEquals("ULES01367", parsed["DISC_ID"])
        assertEquals("Assassin's Creed: Bloodlines", parsed["TITLE"])
    }

    @Test
    fun `invalid small file fails iso validation`() {
        val tmp = File.createTempFile("invalid_test", ".iso")
        try {
            tmp.writeText("not an iso")
            val result = PspIsoParser.parseAndExtractArt(tmp, tmp.parentFile!!, "invalid.iso")
            assertFalse(result.isValidImage)
        } finally {
            tmp.delete()
        }
    }
}
