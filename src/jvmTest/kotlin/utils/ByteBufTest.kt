package org.chorus_oss.chorus.utils

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Test

class ByteBufTest {
    @Test
    fun testReadRetainedSlice() {
        val originalBuf = Unpooled.wrappedBuffer(byteArrayOf(1, 2, 3, 4, 5))
        val slice = originalBuf.readRetainedSlice(3)

        println("Original Buf refCnt: " + originalBuf.refCnt())
        println("Original Buf readerIndex: " + originalBuf.readerIndex())
        println("Slice refCnt: " + slice.refCnt())
        println("Slice readerIndex: " + slice.readerIndex())

        println("read value :" + slice.readByte())
        println("Original Buf readerIndex after slice read: " + originalBuf.readerIndex())
        println("Slice readerIndex after slice read: " + slice.readerIndex())

        slice.release()

        println("Original Buf refCnt after slice release: " + originalBuf.refCnt())
        println("Slice Buf refCnt after slice release: " + slice.refCnt())
    }
}
