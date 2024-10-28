package com.despread.snapshothelper.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream

class BufferUtil {
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192  // Typically default buffer size would be 8KB

        fun calculateBufferSize(fileSize: Long): Int {
            return when {
                fileSize > 1024 * 1024 * 1024 -> 64 * 1024  // If it's over 1GB, better to use 64KB
                fileSize > 1024 * 1024 -> 32 * 1024         // If it's over 1MB, better to use 32KB
                else -> DEFAULT_BUFFER_SIZE                 // Default
            }
        }

        fun channelOutputStream(channel: Channel<ByteArray>, bufferSize: Int) = object : OutputStream() {
            private val buffer = ByteArray(bufferSize)
            private var position = 0

            override fun write(b: Int) {
                buffer[position++] = b.toByte()
                if (position == bufferSize) flush()
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                var offset = off
                var remaining = len
                while (remaining > 0) {
                    val count = minOf(remaining, bufferSize - position)
                    System.arraycopy(b, offset, buffer, position, count)
                    position += count
                    offset += count
                    remaining -= count
                    if (position == bufferSize) flush()
                }
            }

            override fun flush() {
                if (position > 0) {
                    runBlocking { channel.send(buffer.copyOfRange(0, position)) }
                    position = 0
                }
            }

            override fun close() {
                flush()
            }
        }

        fun channelInputStream(channel: Channel<ByteArray>) = object : InputStream() {
            private var currentBuffer: ByteArray? = null
            private var position = 0

            override fun read(): Int {
                if (currentBuffer == null || position >= currentBuffer!!.size) {
                    currentBuffer = runBlocking { channel.receiveCatching().getOrNull() } ?: return -1
                    position = 0
                }
                return currentBuffer!![position++].toInt() and 0xFF
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                var bytesRead = 0
                var offset = off
                var remaining = len

                while (remaining > 0) {
                    if (currentBuffer == null || position >= currentBuffer!!.size) {
                        currentBuffer = runBlocking { channel.receiveCatching().getOrNull() } ?: break
                        position = 0
                    }

                    val count = minOf(remaining, currentBuffer!!.size - position)
                    System.arraycopy(currentBuffer!!, position, b, offset, count)
                    position += count
                    offset += count
                    remaining -= count
                    bytesRead += count
                }

                return if (bytesRead == 0 && len > 0) -1 else bytesRead
            }
        }
    }
}