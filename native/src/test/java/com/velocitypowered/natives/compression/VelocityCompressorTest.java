package com.velocitypowered.natives.compression;

import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import java.util.Random;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;

class VelocityCompressorTest {
    @BeforeAll
    static void checkNatives() {
        Natives.compressor.getLoadedVariant();
    }

    @Test
    @EnabledOnOs({ MAC, LINUX })
    void nativeIntegrityCheck() throws DataFormatException {
        VelocityCompressor compressor = Natives.compressor.get();
        if (compressor instanceof JavaVelocityCompressor) {
            fail("Loaded regular compressor");
        }
        check(compressor, Unpooled::directBuffer);
    }

    @Test
    void javaIntegrityCheck() throws DataFormatException {
        JavaVelocityCompressor compressor = new JavaVelocityCompressor();
        check(compressor, Unpooled::buffer);
    }

    private void check(VelocityCompressor compressor, Supplier<ByteBuf> supplier) throws DataFormatException {
        ByteBuf source = supplier.get();
        ByteBuf dest = supplier.get();
        ByteBuf decompressed = supplier.get();

        Random random = new Random(1);
        byte[] randomBytes = new byte[1 << 16];
        random.nextBytes(randomBytes);
        source.writeBytes(randomBytes);

        try {
            compressor.deflate(source, dest);
            compressor.inflate(dest, decompressed);
            source.readerIndex(0);
            assertTrue(ByteBufUtil.equals(source, decompressed));
        } finally {
            source.release();
            dest.release();
            compressor.dispose();
        }
    }
}