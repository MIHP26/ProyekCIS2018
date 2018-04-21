import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

public class Decrypt
{
    public static void decrypt(String cipherFilePath, String keyFilePath, String tweakI, String messageFilePath)
            throws IOException {
        // Read file and convert to array of byte
        Path cipherPath = Paths.get(cipherFilePath);
        byte[] ciphers = Files.readAllBytes(cipherPath); // per byte

        int blocksOfCiphers = (ciphers.length / 16);
        boolean needStealing = false;
        int unusedLastBlockSpace = 0;
        if (ciphers.length % 16 != 0) {
            blocksOfCiphers = (ciphers.length / 16) + 1;
            needStealing = true;
            unusedLastBlockSpace = 16 - (ciphers.length % 16);
        }

        // Group the message to 2d array
        // column (first dimension) is per block
        // row (second dimension) is per byte in one block
        // note: 1 block = 16 byte
        int cipherIndex = 0;
        byte[][] blockCipher = new byte[blocksOfCiphers][16];
        for (int i = 0; i < blocksOfCiphers; i++) {
            for (int k = 0; k < 16; k++) {
                if (cipherIndex < ciphers.length) {
                    blockCipher[i][k] = ciphers[cipherIndex];
                    cipherIndex++;
                }
            }
        }

        // Read key
        BufferedReader inputKey = new BufferedReader(new FileReader(new File(keyFilePath)));
        // Key still in HEX
        String keyStr = inputKey.readLine();
//        int keyLength = keyStr.length();
        String keyHex1 = keyStr.substring(0, keyStr.length() / 2);
        String keyHex2 = keyStr.substring(keyStr.length() / 2, keyStr.length());

        // Convert each key to its char/ascii
//        int a = 0;
//        String key1 = "";
//        while (a < keyHex1.length()) {
//            String temp = keyHex1.substring(a, a + 2);
//            int hex = Integer.parseInt(temp, 16);
//            key1 += (char) hex;
//            a = a + 2;
//        }
//        byte[] key1arr = key1.getBytes();
        byte[] key1arr = DatatypeConverter.parseHexBinary(keyHex1);

//        a = 0;
//        String key2 = "";
//        while (a < keyHex2.length()) {
//            String temp = keyHex2.substring(a, a + 2);
//            int hex = Integer.parseInt(temp, 16);
//            key2 += (char) hex;
//            a = a + 2;
//        }
//        byte[] key2arr = key2.getBytes();
        byte[] key2arr = DatatypeConverter.parseHexBinary(keyHex2);

        // Tweak
        byte[] tweakArr = tweakI.getBytes();
        byte[] reversedTweakArr = new byte[tweakArr.length];
        // Make it little-endian
        for (int i = 0; i < tweakArr.length; i++) {
            reversedTweakArr[tweakArr.length - (i+1)] = tweakArr[i];
        }

        // Decrypt
        byte[][] plaintextArray = xtsAES(blockCipher, blocksOfCiphers, key1arr, key2arr, reversedTweakArr,
                needStealing, unusedLastBlockSpace);

//        printTwo2DByte(blockMessage, decryptedArray);

        // write decrypted file
//        writeByteArrayToFile(decryptedArray, decryptedFilename, message.length);

        byte[] message = new byte[ciphers.length];
        int messageIndex = 0;
        for (int x = 0; x < plaintextArray.length; x++) {
            for (int y = 0; y < plaintextArray[x].length; y++) {
                if (messageIndex < ciphers.length) {
                    message[messageIndex] = (plaintextArray[x][y]);
                    messageIndex++;
                }
            }
        }

        // write ciphertext file
        byteArrayToFile(message, messageFilePath);

        // Close files
        inputKey.close();
    }

    public static byte[][] xtsAES(byte[][] blockCipher, int blocksOfCiphers, byte[] key1arr, byte[] key2arr,
                                  byte[] reversedTweakArr, boolean needStealing, int unusedLastBlockSpace) {
        // Alpha
        int alpha = 135;

        // Make AES object to encrypt plain text with key 1
        AES key1AES = new AES();
        key1AES.setKey(key1arr);

        // Make AES object to encrypt tweak with key 2
        AES key2AES = new AES();
        key2AES.setKey(key2arr);

        // Encryption process start here
        // initialization
        byte[][] pp = new byte[blocksOfCiphers][16];
        byte[][] cc = new byte[blocksOfCiphers][16];
        byte[][] plaintextArray = new byte[blocksOfCiphers][16];

        // 1. Create T
        // Encrypt Key2 + i with AES Encrypt = tweakEncrypted
        byte[] tweakEncrypted = key2AES.encrypt(reversedTweakArr);

        // Multiplication alpha^j + tweakEncrypted = T = mul
        // Calculate T FOR ALL BLOCKS
        byte[][] tweakXORAlpha = new byte[blocksOfCiphers + 1][16];
        tweakXORAlpha[0] = tweakEncrypted;
        for (int i = 0; i < blocksOfCiphers; i++) {
            for (int k = 0; k < 16; k++) {
                if (k == 0) {
                    tweakXORAlpha[i + 1][k] = (byte) ((2 * (tweakXORAlpha[i][k] % 128)) ^ (alpha * (tweakXORAlpha[i][15] / 128)));
                } else {
                    tweakXORAlpha[i + 1][k] = (byte) ((2 * (tweakXORAlpha[i][k] % 128)) ^ ((tweakXORAlpha[i][k - 1] / 128)));
                }
            }
        }


        if (needStealing) {
            // jumlah block harus minimal 2
            if (blocksOfCiphers < 2) {
                System.out.println("Jumlah block tidak lebih dari 1");
            } else {
                // 2. Create PP
                // For all block except index j-2 and j-1 (last)
                // Calculate PP for all blocks except block index j-1
                int lastTwo = blocksOfCiphers - 2;
                for (int i = 0; i < lastTwo; i++) { // i represent block number
                    for (int p = 0; p < 16; p++) {
                        pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 1][p]);
                    }
                }

                // 3. Create CC
                // Calculate CC for all blocks except block index j-1
                for (int i = 0; i < lastTwo; i++) { // i represent block number
                    cc[i] = key1AES.decrypt(pp[i]);
                }

                // 4. Calculate cipher text
                // Calculate cipher text for all blocks except block index j-1
                for (int i = 0; i < lastTwo; i++) { // i represent block number
                    for (int p = 0; p < 16; p++) {
                        plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 1][p]);
                    }
                }

                // ==== Special treatment for block index j-2 & j-1 (last block)
                // ====
                // evaluate block index j-2

                // PP


                for (int p = 0; p < 16; p++) {
                    pp[lastTwo][p] = (byte) (blockCipher[lastTwo][p] ^ tweakXORAlpha[lastTwo + 2][p]); // menggunakan
                    // T
                    // ke
                    // m
                }

                // CC
                cc[lastTwo] = key1AES.decrypt(pp[lastTwo]);

                // ciphertext
                for (int p = 0; p < 16; p++) {
                    plaintextArray[lastTwo][p] = (byte) (cc[lastTwo][p] ^ tweakXORAlpha[lastTwo + 2][p]); // menggunakan
                    // T
                    // ke
                    // m
                }

                // evaluate block index j - 1
                // Append Last Block Plaintext with Ciphertext (size:
                // unusedLastBlockSpace) block number j-2
                int lastOne = blocksOfCiphers - 1;
                int startIndex = 16 - unusedLastBlockSpace;
                byte[] modifiedLastBlock = new byte[16];
                // copy original last block to modifiedLastBlock
                for (int idx = 0; idx < startIndex; idx++) {
                    modifiedLastBlock[idx] = blockCipher[lastOne][idx];
                }

                for (int idx = startIndex; idx < 16; idx++) {
                    modifiedLastBlock[idx] = plaintextArray[lastTwo][idx];
                }

                int i = blocksOfCiphers - 1;
                // Calculate PP
                for (int p = 0; p < 16; p++) {
                    pp[i][p] = (byte) (modifiedLastBlock[p] ^ tweakXORAlpha[i][p]); // menggunakan
                    // T
                    // ke
                    // m-1
                }

                // Calculate CC
                cc[i] = key1AES.decrypt(pp[i]);

                // Calculate ciphertext
                for (int p = 0; p < 16; p++) {
                    plaintextArray[lastOne][p] = (byte) (cc[i][p] ^ tweakXORAlpha[lastOne][p]); // menggunakan
                    // T
                    // ke
                    // m-1
                }

                // Swap j-1 ciphertext with cropped j-2 ciphertext
                byte[] lastCiphertext = new byte[16];
                for (int idx = 0; idx < 16; idx++) {
                    lastCiphertext[idx] = plaintextArray[lastOne][idx];
                }
                // copy cropped block j-2 to last block
                for (int idx = 0; idx < 16; idx++) {

                    if (idx < startIndex) {
                        plaintextArray[lastOne][idx] = plaintextArray[lastTwo][idx];
                    } else {
                        plaintextArray[lastOne][idx] = (byte) 0;
                    }
                }
                // copy last block (original) to block j - 2
                for (int idx = 0; idx < 16; idx++) {
                    plaintextArray[lastTwo][idx] = lastCiphertext[idx];
                }
            }
        } else {
            // 2. Create PP
            // jumlah block kurang dari 2
            for (int i = 0; i < blocksOfCiphers; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }

            // 3. Create CC
            // Calculate CC for all blocks except block index j-1
            for (int i = 0; i < blocksOfCiphers; i++) { // i represent block number
                cc[i] = key1AES.decrypt(pp[i]);
            }

            // 4. Calculate cipher text
            // Calculate cipher text for all blocks except block index j-1
            for (int i = 0; i < blocksOfCiphers; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }
        }

        return plaintextArray;
    }

    public static void byteArrayToFile(byte[] bytes, String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
}