import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

public class Encrypt
{
    public static byte[] messageToByteArray (String filepath) throws IOException
    {
        FileInputStream fis = new FileInputStream (filepath);
        byte[] bytes = new byte[800];
        int value = 0;
        do {
            value = fis.read (bytes);
            // fos.write(toHexString(bytes));

        } while (value != -1);

        return bytes;
    }


    public static void byteArrayToFile (byte[] bytes, String filepath)
            throws IOException
    {

        FileOutputStream fos = new FileOutputStream (filepath);
        fos.write (bytes);
        fos.flush ();
        fos.close ();
    }


    public static void encryption (String filePath, String keyPath, String tweakValue,
            String cipherPath)
    {
        try {

            // read file
            byte[] messages = Files.readAllBytes (Paths.get (filePath)); // per
                                                                         // byte

            // divide message into blocks
            int blocksOfMessages = messages.length / 16;
            boolean needStealing = false;

            int unusedLastBlockSpace = 0;
            if (messages.length % 16 != 0) {
                blocksOfMessages = (messages.length / 16) + 1;
                needStealing = true;
                unusedLastBlockSpace = 16 - (messages.length % 16);
            }

            // key processing
            FileInputStream fis = new FileInputStream (keyPath);
            BufferedReader reader = new BufferedReader (
                    new InputStreamReader (fis));
            String key = reader.readLine ();
            String k1 = key.substring (0, (key.length () / 2));
            String k2 = key.substring (key.length () / 2, key.length ());

            // Create 2d array to group the message
            int messageIndex = 0;
            byte[][] blockMessage = new byte[blocksOfMessages][16];
            for (int i = 0; i < blocksOfMessages; i++) {
                for (int k = 0; k < 16; k++) {
                    if (messageIndex < messages.length) {
                        blockMessage[i][k] = messages[messageIndex];
                        messageIndex++;
                    }
                }
            }

            byte[] key1arr = DatatypeConverter.parseHexBinary (k1);
            byte[] key2arr = DatatypeConverter.parseHexBinary (k2);

            // Tweak
            byte[] tweakArr = tweakValue.getBytes ();
            byte[] reversedTweakArr = new byte[tweakArr.length];

            // Make it little-endian
            for (int idx = 0; idx < tweakArr.length; idx++) {
                reversedTweakArr[tweakArr.length - (idx + 1)] = tweakArr[idx];
            }

            // Encrypt
            byte[][] ciphertextArray = xtsAES (blockMessage, blocksOfMessages,
                    key1arr, key2arr, reversedTweakArr, needStealing,
                    unusedLastBlockSpace);

            byte[] cipher = new byte[messages.length];
            int cipherIndex = 0;
            for (int x = 0; x < ciphertextArray.length; x++) {
                for (int y = 0; y < ciphertextArray[x].length; y++) {
                    if (cipherIndex < messages.length) {
                        cipher[cipherIndex] = (ciphertextArray[x][y]);
                        cipherIndex++;
                    }
                }
            }

            // write ciphertext file
            byteArrayToFile (cipher, cipherPath);

            // Close files
            reader.close ();

        }

        catch (Exception e) {
            e.printStackTrace ();
        }
    }


    public static byte[][] xtsAES (byte[][] blockMessage, int j, byte[] key1arr,
            byte[] key2arr, byte[] LittleEndianTweak, boolean needStealing,
            int unusedLastBlockSpace)
    {

        int alpha = 135;
        // 135 is modulus of Galois Field (2^128)

        // Make AES object to encrypt plain text with key 1
        AES keyAES1 = new AES ();
        keyAES1.setKey(key1arr);

        // Make AES object to encrypt tweak with key 2
        AES keyAES2 = new AES ();
        keyAES2.setKey(key2arr);

        // Initialize
        byte[][] ciphertextArray = new byte[j][16];
        byte[][] PP = new byte[j][16];
        byte[][] CC = new byte[j][16];

        // 1. Calculate T
        // First, encrypt Key2 + i with AES Encrypt
        byte[] encryptedTweak = keyAES2.encrypt (LittleEndianTweak);

        // T = Multiplication alpha^j and encrypted tweak
        // Calculate T for each blocks
        byte[][] t = new byte[j + 1][16];
        t[0] = encryptedTweak;
        for (int i = 0; i < j; i++) {
            for (int k = 0; k < 16; k++) {
                if (k == 0) {
                    t[i + 1][k] = (byte) ((2 * (t[i][k] % 128))
                            ^ (alpha * (t[i][15] / 128)));
                } else {
                    t[i + 1][k] = (byte) ((2 * (t[i][k] % 128))
                            ^ ((t[i][k - 1] / 128)));
                }
            }
        }

        int lastOne = j - 1;
        int lastTwo = j - 2;

        if (needStealing == false) {
            // 2. Calculate PP
            for (int i = 0; i < j; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    PP[i][p] = (byte) (blockMessage[i][p] ^ t[i + 1][p]);
                }
            }

            // 3. Calculate CC for all blocks
            for (int i = 0; i < j; i++) { // i represents block number
                CC[i] = keyAES1.encrypt (PP[i]);
            }

            // 4. Calculate cipher text
            for (int i = 0; i < j; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
                }
            }
        } else if (needStealing == true && j >= 2) {
            // 2. Calculate PP for all blocks except two last blocks (index j-2
            // & j-1)
            for (int i = 0; i < lastTwo; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    PP[i][p] = (byte) (blockMessage[i][p] ^ t[i + 1][p]);
                }
            }

            // 3. Calculate CC for all blocks except two last blocks (index j-2
            // & j-1)
            for (int i = 0; i < lastTwo; i++) { // i represents block number
                CC[i] = keyAES1.encrypt (PP[i]);
            }

            // 4. Calculate cipher text
            // Calculate cipher text for all blocks except the last block
            for (int i = 0; i < lastOne; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
                }
            }

            // ===Treatment for the last two blocks===
            // evaluate block index j-2

            // PP
            for (int p = 0; p < 16; p++) {
                int i = j - 2;
                PP[i][p] = (byte) (blockMessage[i][p] ^ t[i + 1][p]);
            }
            // CC
            CC[j - 2] = keyAES1.encrypt (PP[j - 2]);

            // ciphertext
            for (int p = 0; p < 16; p++) {
                int i = j - 2;
                ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
            }

            // evaluate last block (index j - 1)
            // Append Last Block Plaintext with Ciphertext

            int startBlockSpace = 16 - unusedLastBlockSpace;
            int endBlockSpace = 16 - 1;
            byte[] lastBlock = new byte[16];
            // copy original last block to modified last block
            for (int index = 0; index <= 15; index++) {
                lastBlock[index] = blockMessage[lastOne][index];
            }

            // stealing from one block before
            for (int index = startBlockSpace; index <= endBlockSpace; index++) {
                lastBlock[index] = ciphertextArray[lastTwo][index];
            }

            // Calculate PP
            for (int p = 0; p < 16; p++) {
                int i = j - 1;

                PP[i][p] = (byte) (lastBlock[p] ^ t[i + 1][p]);

            }
            // Calculate CC
            CC[j - 1] = keyAES1.encrypt (PP[lastOne]);

            // Calculate ciphertext
            for (int p = 0; p < 16; p++) {
                int i = lastOne;
                ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
            }

            // Swap j-1 ciphertext with cropped j-2 ciphertext
            byte[] lastCiphertextMaster = new byte[16];
            for (int byteIdx = 0; byteIdx <= 15; byteIdx++) {
                lastCiphertextMaster[byteIdx] = ciphertextArray[lastOne][byteIdx];
            }

            // copy cropped block j-2 to last block
            for (int byteIdx = 0; byteIdx <= 15; byteIdx++) {

                if (byteIdx < startBlockSpace) {
                    ciphertextArray[lastOne][byteIdx] = ciphertextArray[lastTwo][byteIdx];
                } else {
                    ciphertextArray[lastOne][byteIdx] = (byte) 0;
                }
            }
            // copy the original last block to the second last block
            for (int byteIdx = 0; byteIdx <= 15; byteIdx++) {
                ciphertextArray[lastTwo][byteIdx] = lastCiphertextMaster[byteIdx];
            }
        } else {
            // Jika blok kurang dari 2
            System.out.println ("Jumlah block tidak lebih dari 1");
        }

        return ciphertextArray;
    }

}
