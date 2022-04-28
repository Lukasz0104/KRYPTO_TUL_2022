package pl.lodz.p.it.krypto.aes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AES {
    private final byte[] key;
    private final int numberOfRounds = 10;

    public AES(byte[] key) {
        this.key = key;
    }

    public void changeKey(byte[] newKey) {
        if (newKey != null && newKey.length == 16 && !Arrays.equals(key, newKey)) {
            System.arraycopy(newKey, 0, key, 0, 16);
        }
    }

    public byte[] encryptBlock(byte[] block) {
        byte[] encrypted = block.clone();

        int[][] expandedKey = expandKey();

        // region AddRoundKey
        addRoundKey(encrypted, expandedKey, 0);
        // endregion

        for (int round = 1; round < numberOfRounds; round++) {
            // region SubBytes
            subBytes(encrypted);
            // endregion

            // region ShiftRows
            shiftRows(encrypted);
            // endregion

            // region MixColumns
            mixColumns(encrypted);
            // endregion

            // region AddRoundKey
            addRoundKey(encrypted, expandedKey, round);
            // endregion
        }

        // region SubBytes
        subBytes(encrypted);
        // endregion

        // region ShiftRows
        shiftRows(encrypted);
        // endregion

        // region AddRoundKey
        addRoundKey(encrypted, expandedKey, numberOfRounds);
        // endregion

        return encrypted;
    }

    private void mixColumns(byte[] encrypted) {
        byte byteTwo = (byte) 2;
        byte byteThree = (byte) 3;

        for (int i = 0; i < 4; i++) {
            byte s0 = encrypted[4 * i];
            byte s1 = encrypted[4 * i + 1];
            byte s2 = encrypted[4 * i + 2];
            byte s3 = encrypted[4 * i + 3];

            encrypted[4 * i] = (byte) (mul(byteTwo, s0) ^ mul(byteThree, s1) ^ s2 ^ s3);
            encrypted[4 * i + 1] = (byte) (s0 ^ mul(byteTwo, s1) ^ mul(byteThree, s2) ^ s3);
            encrypted[4 * i + 2] = (byte) (s0 ^ s1 ^ mul(byteTwo, s2) ^ mul(byteThree, s3));
            encrypted[4 * i + 3] = (byte) (mul(byteThree, s0) ^ s1 ^ s2 ^ mul(byteTwo, s3));
        }
    }

    private void subBytes(byte[] encrypted) {
        for (int i = 0; i < 16; i++) {
            encrypted[i] = (byte) Constants.SBox[(encrypted[i] >> 4) & 0xf][encrypted[i] & 0xf];
        }
    }

    private void shiftRows(byte[] encrypted) {
        byte t = encrypted[1];
        encrypted[1] = encrypted[5];
        encrypted[5] = encrypted[9];
        encrypted[9] = encrypted[13];
        encrypted[13] = t;

        for (int i = 0; i < 2; i++) {
            t = encrypted[2];
            encrypted[2] = encrypted[6];
            encrypted[6] = encrypted[10];
            encrypted[10] = encrypted[14];
            encrypted[14] = t;
        }

        t = encrypted[15];
        encrypted[15] = encrypted[11];
        encrypted[11] = encrypted[7];
        encrypted[7] = encrypted[3];
        encrypted[3] = t;
    }

    public int[][] expandKey() {
        int[][] expanded = new int[numberOfRounds + 1][4];

        for (int i = 0; i < 4; i++) {
            expanded[0][i] = (0xff000000 & (key[4 * i] << 24))
                    | (0x00ff0000 & (key[4 * i + 1] << 16))
                    | (0x0000ff00 & (key[4 * i + 2] << 8))
                    | (0x000000ff & (key[4 * i + 3]));
        }

        for (int round = 1; round < numberOfRounds + 1; round++) {
            for (int b = 0; b < 4; b++) {
                if (b % 4 == 0) {
                    expanded[round][b] = (expanded[round - 1][3] >> 24) & 0x000000ff;
                    expanded[round][b] |= (expanded[round - 1][3] << 8) & 0xffffff00;

                    short a0 = (short) ((expanded[round][b] >> 24) & 0xff);
                    short a1 = (short) ((expanded[round][b] >> 16) & 0xff);
                    short a2 = (short) ((expanded[round][b] >> 8) & 0xff);
                    short a3 = (short) (expanded[round][b] & 0xff);

                    short b0 = Constants.SBox[a0 >> 4][a0 & 0b1111];
                    short b1 = Constants.SBox[a1 >> 4][a1 & 0b1111];
                    short b2 = Constants.SBox[a2 >> 4][a2 & 0b1111];
                    short b3 = Constants.SBox[a3 >> 4][a3 & 0b1111];

                    expanded[round][b] = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;

                    expanded[round][b] ^= Constants.rcon[round - 1] << 24;

                    expanded[round][b] ^= expanded[round - 1][0];

                } else {
                    expanded[round][b] = expanded[round - 1][b] ^ expanded[round][b - 1];
                }
            }
        }

        return expanded;
    }

    private byte xtime(byte x) {
        byte b7 = (byte) 0x80;
        byte temp = (byte) (x << 1);
        if ((b7 & x) == b7) {
            temp ^= 0x1b;
        }
        return temp;
    }

    public byte mul(byte a, byte b) {
        byte output = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                output ^= a;
            }
            a = xtime(a);
            b = (byte) (b >> 1);
        }
        return output;
    }

    public byte[] encryptAllBytes(byte[] bytes) throws IOException {
        byte[] encrypted;
        int size;
        if (bytes.length % 16 == 0) {
            size = bytes.length;
        } else {
            size = 16 * (bytes.length / 16 + 1);
        }
        encrypted = new byte[size];
        byte[] encryptedBuffer;
        byte[] buffer = new byte[16];
        int count = 0;
        int offset = 0;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            do {
                count = bais.readNBytes(buffer, 0, 16);
                if (count < 16) {
                    buffer[count] = (byte) count;
                }

                encryptedBuffer = encryptBlock(buffer);

                System.arraycopy(encryptedBuffer, 0, encrypted, offset * 16, 16);
                offset++;

                Arrays.fill(buffer, (byte) 0);
            } while (bais.available() > 0);

        }

        return encrypted;
    }

    public void encryptFile(File inputFile, String outFile)
            throws IOException {

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outFile)) {

            byte[] encrypted = encryptAllBytes(fis.readAllBytes());
            fos.write(encrypted);
        }
    }

    public byte[] decryptBlock(byte[] block) {
        byte[] decrypted = block.clone();

        int[][] expandedKey = expandKey();

        // region AddRoundKey
        addRoundKey(decrypted, expandedKey, numberOfRounds);
        // endregion

        for (int round = numberOfRounds - 1; round > 0; round--) {
            // region InvShiftRows
            invShiftRows(decrypted);
            // endregion

            // region InvSubBytes
            invSubBytes(decrypted);
            // endregion

            // region AddRoundKey
            addRoundKey(decrypted, expandedKey, round);
            // endregion

            // region InvMixColumns
            invMixColumns(decrypted);
            // endregion
        }

        // region InvShiftRows
        invShiftRows(decrypted);
        // endregion

        // region InvSubBytes
        invSubBytes(decrypted);
        // endregion

        // region AddRoundKey
        addRoundKey(decrypted, expandedKey, 0);
        // endregion

        return decrypted;
    }

    private void addRoundKey(byte[] block, int[][] expandedKey, int round) {
        for (int i = 0; i < 16; i++) {
            int offset = 8 * (3 - (i % 4));
            block[i] ^= (byte) (((expandedKey[round][i / 4]) >> (offset)) & 0xff);
        }
    }

    private void invMixColumns(byte[] block) {
        byte byteFourteen = (byte) 0xe;
        byte byteEleven = (byte) 0xb;
        byte byteThirteen = (byte) 0xd;
        byte byteNine = (byte) 0x9;

        for (int i = 0; i < 4; i++) {
            byte s0 = block[4 * i];
            byte s1 = block[4 * i + 1];
            byte s2 = block[4 * i + 2];
            byte s3 = block[4 * i + 3];

            block[4 * i] = (byte) (mul(byteFourteen, s0) ^ mul(byteEleven, s1)
                    ^ mul(byteThirteen, s2) ^ mul(byteNine, s3));
            block[4 * i + 1] = (byte) (mul(byteNine, s0) ^ mul(byteFourteen, s1)
                    ^ mul(byteEleven, s2) ^ mul(byteThirteen, s3));
            block[4 * i + 2] = (byte) (mul(byteThirteen, s0) ^ mul(byteNine, s1)
                    ^ mul(byteFourteen, s2) ^ mul(byteEleven, s3));
            block[4 * i + 3] = (byte) (mul(byteFourteen, s3) ^ mul(byteEleven, s0)
                    ^ mul(byteThirteen, s1) ^ mul(byteNine, s2));
        }
    }

    private void invSubBytes(byte[] block) {
        for (int i = 0; i < 16; i++) {
            block[i] = (byte) Constants.InvSBox[(block[i] >> 4) & 0xf][block[i] & 0xf];
        }
    }

    private void invShiftRows(byte[] block) {
        byte t = block[13];
        block[13] = block[9];
        block[9] = block[5];
        block[5] = block[1];
        block[1] = t;

        for (int i = 0; i < 2; i++) {
            t = block[14];
            block[14] = block[10];
            block[10] = block[6];
            block[6] = block[2];
            block[2] = t;
        }

        t = block[3];
        block[3] = block[7];
        block[7] = block[11];
        block[11] = block[15];
        block[15] = t;
    }

    public byte[] decryptAllBytes(byte[] bytes) throws IOException {
        ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[16];
        byte[] decryptedBuffer;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            while (bais.available() > 16) {
                bais.readNBytes(buffer, 0, 16);
                decryptedBuffer = decryptBlock(buffer);
                decryptedStream.write(decryptedBuffer);
                Arrays.fill(buffer, (byte) 0);
            }

            // reading last block
            bais.readNBytes(buffer, 0, 16);
            decryptedBuffer = decryptBlock(buffer);
            int count = 16;
            if (decryptedBuffer[15] == 0) {
                for (int pos = 15; pos >= 0; pos--) {
                    if (decryptedBuffer[pos] != 0) {
                        count = decryptedBuffer[pos];
                        break;
                    }
                }
            }
            decryptedStream.write(decryptedBuffer, 0, count);
        }

        return decryptedStream.toByteArray();
    }

    public void decryptFile(File encryptedFile, String decryptedFilePath)
            throws IOException {
        try (FileInputStream fis = new FileInputStream(encryptedFile);
             FileOutputStream fos = new FileOutputStream(decryptedFilePath)) {

            fos.write(decryptAllBytes(fis.readAllBytes()));
        }
    }
}
