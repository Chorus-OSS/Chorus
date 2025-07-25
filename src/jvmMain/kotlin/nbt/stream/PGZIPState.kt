package org.chorus_oss.chorus.nbt.stream

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class PGZIPState(parent: PGZIPOutputStream) {
    val str: DeflaterOutputStream
    val buf: ByteArrayOutputStream = ByteArrayOutputStream(PGZIPBlock.Companion.SIZE)

    val def: Deflater = parent.newDeflater()

    init {
        this.str = PGZIPOutputStream.Companion.newDeflaterOutputStream(buf, def)
    }
}
