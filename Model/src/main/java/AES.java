public class AES {
    private final byte[] key;
    // private final int Nb = 4;
    private final int numberOfRounds = 10;
    // private final int Nk = 4;

    public AES(byte[] key) {
        this.key = key;
    }

    public byte[] encrypt(byte[] block) {
        byte byteTwo = (byte) 2;
        byte byteThree = (byte) 3;

        byte[] encrypted = block.clone();

        int[][] expandedKey = expandKey();

        // region AddRoundKey
        for (int i = 0; i < 16; i++) {
            encrypted[i] ^= key[i];
        }
        // endregion

        for (int round = 1; round < numberOfRounds; round++) {
            // region SubBytes
            for (int i = 0; i < 16; i++) {
                encrypted[i] = (byte) Constants.SBox[(encrypted[i] >> 4) & 0xf][encrypted[i] & 0xf];
            }
            // endregion

            // region ShiftRows
            // TODO rewrite it with loops
            {
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

                for (int i = 0; i < 3; i++) {
                    t = encrypted[3];
                    encrypted[3] = encrypted[7];
                    encrypted[7] = encrypted[11];
                    encrypted[11] = encrypted[15];
                    encrypted[15] = t;
                }
            }

            // endregion

            // region MixColumns
            for (int i = 0; i < 4; i++) {
                byte s0 = encrypted[4 * i];
                byte s1 = encrypted[4 * i + 1];
                byte s2 = encrypted[4 * i + 2];
                byte s3 = encrypted[4 * i + 3];


                encrypted[4 * i] = (byte) (mul(byteTwo, s0) ^ mul(byteThree, s1) ^ s2 ^ s3);
                encrypted[4 * i + 1]  = (byte) (s0 ^ mul(byteTwo, s1) ^ mul(byteThree, s2) ^ s3);
                encrypted[4 * i + 2]  = (byte) (s0 ^ s1 ^ mul(byteTwo, s2) ^ mul(byteThree, s3));
                encrypted[4 * i + 3]  = (byte) (mul(byteThree, s0) ^ s1 ^ s2 ^ mul(byteTwo, s3));


                // encrypted[4 * i] = (byte) ((2 * s0) ^ (3 * s1) ^ (s2) ^ (s3));
                // encrypted[4 * i + 1] = (byte) ((s0) ^ (2 * s1) ^ (3 * s2) ^ (s3));
                // encrypted[4 * i + 2] = (byte) ((s0) ^ (s1) ^ (2 * s2) ^ (3 * s3));
                // encrypted[4 * i + 3] = (byte) ((3 * s0) ^ (s1) ^ (s2) ^ (2 * s3));

                // encrypted[4 * i] = (byte) ((((byte) 2) * s0) ^ (((byte) (3)) * s1) ^ s2 ^ s3);
                // encrypted[4 * i + 1] = (byte) (s0 ^ (byte) (2 * s1) ^ (byte) (3 * s2) ^ s3);
                // encrypted[4 * i + 2] = (byte) (s0 ^ s1 ^ (byte) (2 * s2) ^ (byte) (3 * s3));
                // encrypted[4 * i + 3] = (byte) ((byte) (3 * s0) ^ s1 ^ s2 ^ (byte) (2 * s3));
            }
            // endregion

            // region AddRoundKey
            for (int i = 0; i < 16; i++) {
                encrypted[i] ^= (byte) (((expandedKey[round][i / 4]) >> (8 * (3 - (i % 4)))) & 0xff);
            }
            // endregion
        }

        // region SubBytes
        for (int i = 0; i < 16; i++) {
            encrypted[i] = (byte) Constants.SBox[(encrypted[i] >> 4) & 0xf][encrypted[i] & 0xf];
        }
        // endregion

        // region ShiftRows
        // TODO rewrite it with loops
        {
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

            for (int i = 0; i < 3; i++) {
                t = encrypted[3];
                encrypted[3] = encrypted[7];
                encrypted[7] = encrypted[11];
                encrypted[11] = encrypted[15];
                encrypted[15] = t;
            }
        }

        // endregion

        // region AddRoundKey
        for (int i = 0; i < 16; i++) {
            encrypted[i] ^= (byte) (((expandedKey[numberOfRounds][i / 4]) >> (8 * (3 - (i % 4)))) & 0xff);
        }
        // endregion

        return encrypted;
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

                    expanded[round][b] ^= (Constants.rcon[round - 1] << 24);

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
            if ((b & 1) != 0)  {
                output ^= a;
            }
            a = xtime(a);
            b = (byte) (b >> 1);
        }
        return output;
    }
}
