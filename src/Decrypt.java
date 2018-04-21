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
    public static void decryption(String cipherFilePath, String keyFilePath, String tweakI, String messageFilePath)
            throws IOException {

        // Membaca file menjadi array yang terdiri dari byte
        Path cipherPath = Paths.get(cipherFilePath);
        byte[] ciphers = Files.readAllBytes(cipherPath);

        // Menghitung jumlah block dengan membagi panjang array dengan 16
        // Menentukan perlunya stealing atau tidak
        // Menentukan sisa blok yang kosong pada block terakhir
        int blocksOfCiphers = (ciphers.length / 16);
        boolean needStealing = false;
        int unusedLastBlockSpace = 0;
        if (ciphers.length % 16 != 0) {
            blocksOfCiphers = (ciphers.length / 16) + 1;
            needStealing = true;
            unusedLastBlockSpace = 16 - (ciphers.length % 16);
        }

        // Mengelompokkan array menjadi block yang masing-masing terdiri dari 16 blok
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

        // Membaca kunci
        BufferedReader inputKey = new BufferedReader(new FileReader(new File(keyFilePath)));
        String keyStr = inputKey.readLine();

        // Membagi kunci menjadi 2 yang sama panjang
        String keyHex1 = keyStr.substring(0, keyStr.length() / 2);
        String keyHex2 = keyStr.substring(keyStr.length() / 2, keyStr.length());

        // Mengubah kunci menjadi array of byte
        byte[] key1arr = DatatypeConverter.parseHexBinary(keyHex1);
        byte[] key2arr = DatatypeConverter.parseHexBinary(keyHex2);

        // Mengubah tweak value menjadi array of byte
        byte[] tweakArr = tweakI.getBytes();

        // Membalik tweakvalue
        byte[] reversedTweakArr = new byte[tweakArr.length];
        for (int i = 0; i < tweakArr.length; i++) {
            reversedTweakArr[tweakArr.length - (i+1)] = tweakArr[i];
        }

        // Melakukan Dekripsi
        byte[][] plaintextArray = xtsAES(blockCipher, blocksOfCiphers, key1arr, key2arr, reversedTweakArr,
                needStealing, unusedLastBlockSpace);

        // Mengubah kembali block menjadi array of byte
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

        // Menulis kembali array of byte ke dalam bentuk file
        byteArrayToFile(message, messageFilePath);
        inputKey.close();
    }

    public static byte[][] xtsAES(byte[][] blockCipher, int blocksOfCiphers, byte[] key1arr, byte[] key2arr,
                                  byte[] reversedTweakArr, boolean needStealing, int unusedLastBlockSpace) {
        // Alpha
        // 135 adalah sisa modulus GF(2^128)
        int alpha = 135;

        // Buat objek AES dengan kunci 1
        AES key1AES = new AES();
        key1AES.setKey(key1arr);

        // Buat objek AES dengan kunci 1
        AES key2AES = new AES();
        key2AES.setKey(key2arr);

        // inisialisasi pp, cc dan plaintext
        byte[][] pp = new byte[blocksOfCiphers][16];
        byte[][] cc = new byte[blocksOfCiphers][16];
        byte[][] plaintextArray = new byte[blocksOfCiphers][16];

        // Enkripsi tweak value awal dengan kunci 2
        byte[] tweakEncrypted = key2AES.encrypt(reversedTweakArr);

        // Membuat  tweak value tiap block dengan tweak value awal
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
                // Isi pp dengan hasil bitwise xor cipher dengan tweak value
                // kecuali 2 block terakhir
                int lastTwo = blocksOfCiphers - 2;
                for (int i = 0; i < lastTwo; i++) {
                    for (int p = 0; p < 16; p++) {
                        pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 1][p]);
                    }
                }

                // Isi cc dengan hasil dekripsi pp menggunakan kunci 1
                // kecuali 2 block terakhir
                for (int i = 0; i < lastTwo; i++) {
                    cc[i] = key1AES.decrypt(pp[i]);
                }

                // Isi plaintext dengan hasil bitwise xor cc dengan tweak value
                // kecuali 2 block terakhir
                for (int i = 0; i < lastTwo; i++) {
                    for (int p = 0; p < 16; p++) {
                        plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 1][p]);
                    }
                }

                // Kalkulasi pp untuk block yang sebelum block terakhir menggunakan tweak value block terakhir
                for (int p = 0; p < 16; p++) {
                    pp[lastTwo][p] = (byte) (blockCipher[lastTwo][p] ^ tweakXORAlpha[lastTwo + 2][p]);
                }

                // Kalkulasi cc untuk block yang sebelum block terakhir
                cc[lastTwo] = key1AES.decrypt(pp[lastTwo]);

                // Kalkulasi plaintext untuk block yang sebelum block terakhir
                for (int p = 0; p < 16; p++) {
                    plaintextArray[lastTwo][p] = (byte) (cc[lastTwo][p] ^ tweakXORAlpha[lastTwo + 2][p]);
                }

                // Stealing block terakhir
                int lastOne = blocksOfCiphers - 1;
                int startIndex = 16 - unusedLastBlockSpace;
                byte[] modifiedLastBlock = new byte[16];
                // Isi blok kosong pada block terakhir dengan plaintext dari block sebelum terakhir
                for (int idx = 0; idx < startIndex; idx++) {
                    modifiedLastBlock[idx] = blockCipher[lastOne][idx];
                }
                for (int idx = startIndex; idx < 16; idx++) {
                    modifiedLastBlock[idx] = plaintextArray[lastTwo][idx];
                }

                // Kalkulasi pp untuk block terakhir menggunakan tweak value sebelum block terakhir
                for (int p = 0; p < 16; p++) {
                    pp[lastOne][p] = (byte) (modifiedLastBlock[p] ^ tweakXORAlpha[lastOne][p]); // menggunakan
                }

                // Kalkulasi cc untuk block terakhir
                cc[lastOne] = key1AES.decrypt(pp[lastOne]);

                // Kalkulasi plaintext untuk block terakhir
                for (int p = 0; p < 16; p++) {
                    plaintextArray[lastOne][p] = (byte) (cc[lastOne][p] ^ tweakXORAlpha[lastOne][p]);
                }

                // tukar plaintext dari block terakhir dengan block sebelum terakhir
                byte[] lastPlaintext = new byte[16];
                for (int idx = 0; idx < 16; idx++) {
                    lastPlaintext[idx] = plaintextArray[lastOne][idx];
                }
                for (int idx = 0; idx < 16; idx++) {

                    if (idx < startIndex) {
                        plaintextArray[lastOne][idx] = plaintextArray[lastTwo][idx];
                    } else {
                        plaintextArray[lastOne][idx] = (byte) 0;
                    }
                }
                for (int idx = 0; idx < 16; idx++) {
                    plaintextArray[lastTwo][idx] = lastPlaintext[idx];
                }
            }
        } else {
            // Isi pp dengan hasil bitwise xor cipher dengan tweak value
            for (int i = 0; i < blocksOfCiphers; i++) {
                for (int p = 0; p < 16; p++) {
                    pp[i][p] = (byte) (blockCipher[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }

            // Isi cc dengan hasil dekripsi pp menggunakan kunci 1
            for (int i = 0; i < blocksOfCiphers; i++) {
                cc[i] = key1AES.decrypt(pp[i]);
            }

            // Isi plaintext dengan hasil bitwise xor cc dengan tweak value
            for (int i = 0; i < blocksOfCiphers; i++) {
                for (int p = 0; p < 16; p++) {
                    plaintextArray[i][p] = (byte) (cc[i][p] ^ tweakXORAlpha[i + 1][p]);
                }
            }
        }

        //kembalikan hasil deksripsi
        return plaintextArray;
    }

    // Fungsi untuk menulikan array of byte ke dalam file
    public static void byteArrayToFile(byte[] bytes, String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
}