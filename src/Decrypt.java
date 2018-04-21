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

        int blocksOfCiphers = ciphers.length /16;
        boolean needStealing = false;
        int unusedLastBlockSpace = 0;
        if (ciphers.length % 16 != 0) {
            blocksOfCiphers = (ciphers.length / 16) + 1;
            needStealing = true;
            unusedLastBlockSpace = 16 - (ciphers.length % 16);
        }

        // convert the cipher into 2d array
        // column represents block
        // row represents byte in one block
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

        // write message file
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

        // 2. Create PP
        if (blocksOfCiphers > 2) { // jumlah block harus minimal 2
            // For all block except index j-2 and j-1 (last)
            // Calculate PP for all blocks except block index j-1
            for (int i = 0; i < blocksOfCiphers - 2; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }

            byte[][] ppx = new byte[blocksOfCiphers][16];
            for (int i = 0; i < blocksOfCiphers - 2; i++) {
                ppx[i] = (byte) (blockCipher[i] ^ tweakXORAlpha [i+1]);
            }

            // 3. Create CC
            // Calculate CC for all blocks except block index j-1
            for (int i = 0; i < blocksOfCiphers - 2; i++) { // i represent block number
                    cc[i] = key1AES.decrypt(pp[i]);
            }

            // 4. Calculate cipher text
            // Calculate cipher text for all blocks except block index j-1
            for (int i = 0; i < blocksOfCiphers - 1; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }

            byte[][] pt = new byte[blocksOfCiphers][16];
            for (int i = 0; i < blocksOfCiphers - 1; i++) {
                pt[i] = (byte) (cc[i] ^ tweakXORAlpha[i+1]);
            }

            // ==== Special treatment for block index j-2 & j-1 (last block)
            // ====
            // evaluate block index j-2

            // PP

            int i = blocksOfCiphers - 2;
            for (int p = 0; p < 16; p++) {
                pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 2][p]); // menggunakan
                // T
                // ke
                // m
            }

            int lastTwo = blocksOfCiphers - 2;
            ppx[lastTwo] = (byte) (blockCipher[lastTwo] ^ tweakXORAlpha[lastTwo + 2])

            // CC
            cc[i] = key1AES.decrypt(pp[i]);

            // ciphertext
            for (int p = 0; p < 16; p++) {
                plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 2][p]); // menggunakan
                // T
                // ke
                // m
            }

            pt[lastTwo] = (byte) (cc[lastTwo] ^ tweakXORAlpha[lastTwo + 2])


            // evaluate block index j - 1
            // Append Last Block Plaintext with Ciphertext (size:
            // unusedLastBlockSpace) block number j-2
            int startIndex = 16 - unusedLastBlockSpace;
            byte[] modifiedLastBlock = new byte[16];
            // copy original last block to modifiedLastBlock
            for (int idx = 0; idx < startByteID; idx++) {
                modifiedLastBlock[idx] = blockCipher[j - 1][idx];
            }

            for (int idx = startByteID; idx < 16; byteID++) {
                modifiedLastBlock[idx] = plaintextArray[j - 2][idx];
            }

            int i = blocksOfCiphers - 1;
            // Calculate PP
            for (int p = 0; p < 16; p++) {


                    pp[i][p] = (byte) (modifiedLastBlock[p] ^ tweakXORAlpha[i][p]); // menggunakan
                    // T
                    // ke
                    // m-1
            }

            int lastOne = blocksOfCiphers - 1;
            ppx[lastOne] = (byte) (modifiedLastBlock ^ tweakXORAlpha[lastOne]);

            // Calculate CC
                cc[i] = key1AES.decrypt(pp[i]);

            // Calculate ciphertext
            for (int p = 0; p < 16; p++) {
                int i = j - 1;

                    plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i][p]); // menggunakan
                    // T
                    // ke
                    // m-1
            }

            pt[lastOne] = (byte) ^ tweakXORAlpha[lastOne];

            // Swap j-1 ciphertext with cropped j-2 ciphertext
            byte[] lastCiphertextMaster = new byte[16];
            for (int byteID = 0; byteID <= 15; byteID++) {
                lastCiphertextMaster[byteID] = ciphertextArray[j - 1][byteID];
            }
            // copy cropped block j-2 to last block
            for (int byteID = 0; byteID <= 15; byteID++) {

                if (byteID < startByteID) {
                    ciphertextArray[j - 1][byteID] = ciphertextArray[j - 2][byteID];
                } else {
                    ciphertextArray[j - 1][byteID] = (byte) 0;
                }
            }
            // copy last block (original) to block j - 2
            for (int byteID = 0; byteID <= 15; byteID++) {
                ciphertextArray[j - 2][byteID] = lastCiphertextMaster[byteID];
            }
        } else { // jumlah block kurang dari 2
            System.out.println("Jumlah block tidak lebih dari 1");
        }

        return ciphertextArray;
    }

    public static void byteArrayToFile(byte[] bytes, String filepath) throws IOException {
        // FileInputStream fis = new
        // FileInputStream("C:\\Users\\Tazkianida\\workspace\\TesCIS\\src\\gambar1.jpg");
        FileOutputStream fos = new FileOutputStream(filepath);
        // BufferedReader reader = new BufferedReader(new
        // InputStreamReader(fis));
        // String s;
        // while((s = reader.readLine ()) != null) {
        fos.write(bytes);
        // }
        // reader.close ();
        fos.flush();
        fos.close();
    }
}
