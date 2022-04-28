package pl.lodz.p.it.krypto.aes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AESTest {
    AES aes;
    final String parentDir = Paths.get("src", "test", "resources").toString();

    byte[] key1 = new byte[] {
            0x1, 0x2, 0x3, 0x4,
            0x5, 0x6, 0x7, 0x8,
            0x9, 0xa, 0xb, 0xc,
            0xd, 0xe, 0xf, 0x0
    };

    byte[] key2 = new byte[] {
            0x01, 0x23, 0x45, 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
            (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98,
            0x76, 0x54, 0x32, 0x10
    };

    byte[] block1 = new byte[] {
            0x00, 0x00, 0x01, 0x01,
            0x03, 0x03, 0x07, 0x07,
            0x0f, 0x0f, 0x1f, 0x1f,
            0x3f, 0x3f, 0x7f, 0x7f
    };

    byte[] block2 = new byte[] {
            0x11, 0x28, (byte) 0xea, 0x62,
            (byte) 0xfa, (byte) 0xde, 0x2b, 0x6b,
            (byte) 0xa8, (byte) 0x9d, 0x27, 0x32,
            0x6d, (byte) 0x94, 0x7f, 0x12
    };

    @Test
    public void expandKey1Test() {
        aes = new AES(key1);

        int[][] expanded1 = aes.expandKey();

        int[][] expected1 = new int[][] {
                { 0x01020304, 0x05060708, 0x090a0b0c, 0x0d0e0f00 },
                { 0xab7460d3, 0xae7267db, 0xa7786cd7, 0xaa7663d7 },
                { 0x918f6e7f, 0x3ffd09a4, 0x98856573, 0x32f306a4 },
                { 0x98e0275c, 0xa71d2ef8, 0x3f984b8b, 0x0d6b4d2f },
                { 0xef03328b, 0x481e1c73, 0x778657f8, 0x7aed1ad7 },
                { 0xaaa13c51, 0xe2bf2022, 0x953977da, 0xefd46d0d },
                { 0xc29deb8e, 0x2022cbac, 0xb51bbc76, 0x5acfd17b },
                { 0x08a3ca30, 0x2881019c, 0x9d9abdea, 0xc7556c91 },
                { 0x74f34bf6, 0x5c724a6a, 0xc1e8f780, 0x06bd9b11 },
                { 0x15e7c999, 0x499583f3, 0x887d7473, 0x8ec0ef62 },
                { 0x99386380, 0xd0ade073, 0x58d09400, 0xd6107b62 }
        };

        assertArrayEquals(expected1, expanded1);
    }

    @Test
    public void expandKey2Test() {
        aes = new AES(key2);

        int[][] expanded2 = aes.expandKey();

        int[][] expected2 = new int[][] {
                { 0x01234567, 0x89abcdef, 0xfedcba98, 0x76543210 },
                { 0x20008f5f, 0xa9ab42b0, 0x5777f828, 0x2123ca38 },
                { 0x047488a2, 0xaddfca12, 0xfaa8323a, 0xdb8bf802 },
                { 0x3d35ff1b, 0x90ea3509, 0x6a420733, 0xb1c9ff31 },
                { 0xe82338d3, 0x78c90dda, 0x128b0ae9, 0xa342f5d8 },
                { 0xd4c559d9, 0xac0c5403, 0xbe875eea, 0x1dc5ab32 },
                { 0x52a77a7d, 0xfeab2e7e, 0x402c7094, 0x5de9dba6 },
                { 0x0c1e5e31, 0xf2b5704f, 0xb29900db, 0xef70db7d },
                { 0xdda7a1ee, 0x2f12d1a1, 0x9d8bd17a, 0x72fb0a07 },
                { 0xc9c064ae, 0xe6d2b50f, 0x7b596475, 0x09a26e72 },
                { 0xc55f24af, 0x238d91a0, 0x58d4f5d5, 0x51769ba7 },
        };

        assertArrayEquals(expected2, expanded2);
    }

    @Test
    @Tag("block1")
    @Tag("key1")
    public void encrypt11Test() {
        aes = new AES(key1);

        byte[] encrypted = aes.encryptBlock(block1);

        byte[] expected = {
                (byte) 0x94, 0x77, (byte) 0xdf, 0x11,
                0x66, 0x68, 0x39, (byte) 0xb7,
                0x46, 0x34, 0x18, (byte) 0xd8,
                0x06, 0x1a, (byte) 0xb3, 0x17
        };

        assertArrayEquals(expected, encrypted);
    }

    @Test
    @Tag("block1")
    @Tag("key2")
    public void encrypt12Test() {
        aes = new AES(key2);
        byte[] expected = {
                0x57, (byte) 0xef, 0x57, 0x60,
                (byte) 0x95, (byte) 0xa2, (byte) 0x97, 0x49,
                (byte) 0xc5, (byte) 0xd1, 0x48, (byte) 0xed,
                (byte) 0xb4, 0x6c, (byte) 0xd5, 0x5f
        };

        assertArrayEquals(expected, aes.encryptBlock(block1));
    }

    @Test
    @Tag("block2")
    @Tag("key1")
    public void encrypt21Test() {
        aes = new AES(key1);
        byte[] expected = {
                (byte) 0x91, (byte) 0xa7, (byte) 0xa2, 0x51,
                (byte) 0xee, 0x0a, (byte) 0x82, (byte) 0xc9,
                0x5c, 0x47, 0x54, (byte) 0xd0,
                0x5b, 0x5b, 0x3c, (byte) 0xfe
        };

        assertArrayEquals(expected, aes.encryptBlock(block2));
    }

    @Test
    @Tag("block2")
    @Tag("key2")
    public void encrypt22Test() {
        aes = new AES(key2);
        byte[] expected = {
                0x11, 0x14, 0x27, 0x49,
                (byte) 0x99, 0x3f, 0x21, (byte) 0x95,
                0x5b, 0x07, (byte) 0xf5, (byte) 0xa2,
                (byte) 0xed, (byte) 0x9c, (byte) 0x8e, 0x68
        };

        assertArrayEquals(expected, aes.encryptBlock(block2));
    }

    @Test
    public void encryptSmallerBlockTest() {
        aes = new AES(key1);
        byte[] block = Arrays.copyOf(block1, 10);
        byte[] expected = {
                (byte) 0xf6, 0x1b, 0x47, 0x04,
                (byte) 0x8c, (byte) 0xe0, 0x2e, 0x3e,
                (byte) 0xf6, (byte) 0xc2, (byte) 0x9d, (byte) 0xb0,
                0x25, (byte) 0xad, (byte) 0xa0, 0x05
        };
        try {
            assertArrayEquals(expected, aes.encryptAllBytes(block));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void encryptAllBytesTest() {
        aes = new AES(key1);
        byte[] block = {
                0x00, 0x00, 0x01, 0x01,
                0x03, 0x03, 0x07, 0x07,
                0x0f, 0x0f, 0x1f, 0x1f,
                0x3f, 0x3f, 0x7f, 0x7f,
                0x11, 0x28, (byte) 0xea, 0x62,
                (byte) 0xfa, (byte) 0xde, 0x2b, 0x6b,
                (byte) 0xa8, (byte) 0x9d, 0x27, 0x32,
                0x6d, (byte) 0x94, 0x7f, 0x12
        };

        byte[] expected = {
                (byte) 0x94, 0x77, (byte) 0xdf, 0x11,
                0x66, 0x68, 0x39, (byte) 0xb7,
                0x46, 0x34, 0x18, (byte) 0xd8,
                0x06, 0x1a, (byte) 0xb3, 0x17,
                (byte) 0x91, (byte) 0xa7, (byte) 0xa2, 0x51,
                (byte) 0xee, 0x0a, (byte) 0x82, (byte) 0xc9,
                0x5c, 0x47, 0x54, (byte) 0xd0,
                0x5b, 0x5b, 0x3c, (byte) 0xfe
        };
        try {
            assertArrayEquals(expected, aes.encryptAllBytes(block));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void decryptFirstBlockWithFirstTest() {
        aes = new AES(key1);
        byte[] encryptedBlock = {
                (byte) 0x94, 0x77, (byte) 0xdf, 0x11,
                0x66, 0x68, 0x39, (byte) 0xb7,
                0x46, 0x34, 0x18, (byte) 0xd8,
                0x06, 0x1a, (byte) 0xb3, 0x17
        };

        assertArrayEquals(block1, aes.decryptBlock(encryptedBlock));
    }

    @Test
    public void decryptSecondBlockWithFirstKeyTest() {
        aes = new AES(key1);
        byte[] encryptedBlock = {
                (byte) 0x91, (byte) 0xa7, (byte) 0xa2, 0x51,
                (byte) 0xee, 0x0a, (byte) 0x82, (byte) 0xc9,
                0x5c, 0x47, 0x54, (byte) 0xd0,
                0x5b, 0x5b, 0x3c, (byte) 0xfe
        };

        assertArrayEquals(block2, aes.decryptBlock(encryptedBlock));
    }

    @Test
    public void decryptFirstBlockWithSecondKeyTest() {
        aes = new AES(key2);
        byte[] encrypted = {
                0x57, (byte) 0xef, 0x57, 0x60,
                (byte) 0x95, (byte) 0xa2, (byte) 0x97, 0x49,
                (byte) 0xc5, (byte) 0xd1, 0x48, (byte) 0xed,
                (byte) 0xb4, 0x6c, (byte) 0xd5, 0x5f
        };

        assertArrayEquals(block1, aes.decryptBlock(encrypted));
    }

    @Test
    public void decryptSecondBlockWithSecondKeyTest() {
        aes = new AES(key2);
        byte[] encrypted = {
                0x11, 0x14, 0x27, 0x49,
                (byte) 0x99, 0x3f, 0x21, (byte) 0x95,
                0x5b, 0x07, (byte) 0xf5, (byte) 0xa2,
                (byte) 0xed, (byte) 0x9c, (byte) 0x8e, 0x68
        };

        assertArrayEquals(block2, aes.decryptBlock(encrypted));
    }

    @Test
    public void decryptAllBytesTest() {
        aes = new AES(key1);
        byte[] encrypted = {
                (byte) 0x94, 0x77, (byte) 0xdf, 0x11,
                0x66, 0x68, 0x39, (byte) 0xb7,
                0x46, 0x34, 0x18, (byte) 0xd8,
                0x06, 0x1a, (byte) 0xb3, 0x17,
                (byte) 0x91, (byte) 0xa7, (byte) 0xa2, 0x51,
                (byte) 0xee, 0x0a, (byte) 0x82, (byte) 0xc9,
                0x5c, 0x47, 0x54, (byte) 0xd0,
                0x5b, 0x5b, 0x3c, (byte) 0xfe
        };
        byte[] decrypted = {
                0x00, 0x00, 0x01, 0x01,
                0x03, 0x03, 0x07, 0x07,
                0x0f, 0x0f, 0x1f, 0x1f,
                0x3f, 0x3f, 0x7f, 0x7f,
                0x11, 0x28, (byte) 0xea, 0x62,
                (byte) 0xfa, (byte) 0xde, 0x2b, 0x6b,
                (byte) 0xa8, (byte) 0x9d, 0x27, 0x32,
                0x6d, (byte) 0x94, 0x7f, 0x12
        };
        try {
            assertArrayEquals(decrypted, aes.decryptAllBytes(encrypted));
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void decryptNotFullBlockTest() {
        aes = new AES(key1);
        byte[] block = {
                0x00, 0x00, 0x01, 0x01,
                0x03, 0x03, 0x07, 0x07,
                0x0f, 0x0f, 0x1f
        };
        try {
            byte[] encrypted = aes.encryptAllBytes(block);
            byte[] decrypted = aes.decryptAllBytes(encrypted);

            assertArrayEquals(block, decrypted);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    @Order(1)
    public void encryptFileTest() {
        aes = new AES(key1);
        File originalFile = new File(parentDir, "original.txt");
        String encryptedFilePath = Paths.get(parentDir, "encrypted").toString();

        try {
            aes.encryptFile(originalFile, encryptedFilePath);

            File encryptedFile = new File(encryptedFilePath);
            assertTrue(encryptedFile.exists());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(2)
    public void decryptFileTest() {
        aes = new AES(key1);
        File encrypted = new File(parentDir, "encrypted");
        String decryptedFilePath = Paths.get(parentDir, "decrypted.txt").toString();

        try {
            aes.decryptFile(encrypted, decryptedFilePath);
            assertTrue((new File(decryptedFilePath)).exists());
            assertEquals(-1L, Files.mismatch(
                    Paths.get(parentDir, "original.txt"),
                    Paths.get(parentDir, "decrypted.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
