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
    // Fungsi untuk menulikan array of byte ke dalam file
    public static void byteArrayToFile(byte[] bytes, String filepath)
            throws IOException {
        FileOutputStream fos = new FileOutputStream (filepath);
        fos.write (bytes);
        fos.flush ();
        fos.close ();
    }

    public static void encryption(String filePath, String keyPath, String tweakValue,
            String cipherPath) {
        try {
            // Membaca file menjadi array yang terdiri dari byte
            byte[] messages = Files.readAllBytes(Paths.get(filePath));

            // Menghitung jumlah block dengan membagi panjang array dengan 16
            // Menentukan perlunya stealing atau tidak
            // Menentukan sisa blok yang kosong pada block terakhir
            int blocksOfMessages = messages.length / 16;
            boolean needStealing = false;
            int unusedLastBlockSpace = 0;
            if (messages.length % 16 != 0) {
                blocksOfMessages = (messages.length / 16) + 1;
                needStealing = true;
                unusedLastBlockSpace = 16 - (messages.length % 16);
            }

            // Membaca kunci
            FileInputStream fis = new FileInputStream(keyPath);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(fis));
            String key = reader.readLine();

            // Membagi kunci menjadi 2 yang sama panjang
            String k1 = key.substring (0, (key.length() / 2));
            String k2 = key.substring (key.length() / 2, key.length());

            // Mengelompokkan array menjadi block yang masing-masing terdiri dari 16 blok
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

            // Mengubah kunci menjadi array of byte
            byte[] key1arr = DatatypeConverter.parseHexBinary (k1);
            byte[] key2arr = DatatypeConverter.parseHexBinary (k2);

            // Mengubah tweak value menjadi array of byte
            byte[] tweakArr = tweakValue.getBytes ();

            // Membalik tweakvalue
            byte[] reversedTweakArr = new byte[tweakArr.length];
            for (int idx = 0; idx < tweakArr.length; idx++) {
                reversedTweakArr[tweakArr.length - (idx + 1)] = tweakArr[idx];
            }

            // Melakukan Enkripsi
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

            // Mengubah kembali block menjadi array of byte
            byteArrayToFile(cipher, cipherPath);
            reader.close();
        }

        catch (Exception e) {
            e.printStackTrace ();
        }
    }


    public static byte[][] xtsAES (byte[][] blockMessage, int j, byte[] key1arr,
            byte[] key2arr, byte[] LittleEndianTweak, boolean needStealing,
            int unusedLastBlockSpace)
    {
        // Alpha
        // 135 adalah sisa modulus GF(2^128)
        int alpha = 135;

        // Buat objek AES dengan kunci 1
        AES keyAES1 = new AES ();
        keyAES1.setKey(key1arr);

        // Buat objek AES dengan kunci 1
        AES keyAES2 = new AES ();
        keyAES2.setKey(key2arr);

        // inisialisasi pp, cc dan ciphertext
        byte[][] ciphertextArray = new byte[j][16];
        byte[][] PP = new byte[j][16];
        byte[][] CC = new byte[j][16];

        // Enkripsi tweak value awal dengan kunci 2
        byte[] encryptedTweak = keyAES2.encrypt (LittleEndianTweak);

        // Membuat  tweak value tiap block dengan tweak value awal
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

        if (needStealing == false) {
            // Isi pp dengan hasil bitwise xor plaintext dengan tweak value
            for (int i = 0; i < j; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    PP[i][p] = (byte) (blockMessage[i][p] ^ t[i + 1][p]);
                }
            }

            // Isi cc dengan hasil dekripsi pp menggunakan kunci 1
            for (int i = 0; i < j; i++) { // i represents block number
                CC[i] = keyAES1.encrypt (PP[i]);
            }

            // Isi ciphertext dengan hasil bitwise xor cc dengan tweak value
            for (int i = 0; i < j; i++) { // i represent block number
                for (int p = 0; p < 16; p++) {
                    ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
                }
            }
        } else if (needStealing == true && j >= 2) {
            int lastOne = j - 1;
            int lastTwo = j - 2;

            // Isi pp dengan hasil bitwise xor cipher dengan tweak value
            // kecuali 2 block terakhir
            for (int i = 0; i < lastTwo; i++) {
                for (int p = 0; p < 16; p++) {
                    PP[i][p] = (byte) (blockMessage[i][p] ^ t[i + 1][p]);
                }
            }

            // Isi cc dengan hasil dekripsi pp menggunakan kunci 1
            // kecuali 2 block terakhir
            for (int i = 0; i < lastTwo; i++) {
                CC[i] = keyAES1.encrypt (PP[i]);
            }

            // Isi plaintext dengan hasil bitwise xor cc dengan tweak value
            // kecuali 2 block terakhir
            for (int i = 0; i < lastTwo; i++) {
                for (int p = 0; p < 16; p++) {
                    ciphertextArray[i][p] = (byte) (CC[i][p] ^ t[i + 1][p]);
                }
            }

            // Kalkulasi pp untuk block yang sebelum block terakhir menggunakan tweak value block terakhir
            for (int p = 0; p < 16; p++) {
                PP[lastTwo][p] = (byte) (blockMessage[lastTwo][p] ^ t[lastTwo + 1][p]);
            }

            // Kalkulasi cc untuk block yang sebelum block terakhir
            CC[lastTwo] = keyAES1.encrypt (PP[lastTwo]);

            // Kalkulasi ciphertext untuk block yang sebelum block terakhir
            for (int p = 0; p < 16; p++) {
                ciphertextArray[lastTwo][p] = (byte) (CC[lastTwo][p] ^ t[lastTwo + 1][p]);
            }

            // Stealing block terakhir
            int startBlockSpace = 16 - unusedLastBlockSpace;
            byte[] lastBlock = new byte[16];
            // Isi blok kosong pada block terakhir dengan plaintext dari block sebelum terakhir
            for (int index = 0; index < startBlockSpace; index++) {
                lastBlock[index] = blockMessage[lastOne][index];
            }
            for (int index = startBlockSpace; index < 16; index++) {
                lastBlock[index] = ciphertextArray[lastTwo][index];
            }

            // Kalkulasi pp untuk block terakhir menggunakan tweak value sebelum block terakhir
            for (int p = 0; p < 16; p++) {
                PP[lastOne][p] = (byte) (lastBlock[p] ^ t[lastOne + 1][p]);

            }

            // Kalkulasi cc untuk block terakhir
            CC[lastOne] = keyAES1.encrypt (PP[lastOne]);

            // Kalkulasi plaintext untuk block terakhir
            for (int p = 0; p < 16; p++) {
                ciphertextArray[lastOne][p] = (byte) (CC[lastOne][p] ^ t[lastOne + 1][p]);
            }

            // tukar ciphertext dari block terakhir dengan block sebelum terakhir
            byte[] lastCiphertextMaster = new byte[16];
            for (int byteIdx = 0; byteIdx <= 15; byteIdx++) {
                lastCiphertextMaster[byteIdx] = ciphertextArray[lastOne][byteIdx];
            }
            for (int byteIdx = 0; byteIdx <= 15; byteIdx++) {
                if (byteIdx < startBlockSpace) {
                    ciphertextArray[lastOne][byteIdx] = ciphertextArray[lastTwo][byteIdx];
                } else {
                    ciphertextArray[lastOne][byteIdx] = (byte) 0;
                }
            }
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