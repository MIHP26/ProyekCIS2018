import javax.xml.bind.DatatypeConverter;
public class CobaTes {
    public static void main(String[] args) {
        String keyHex1 = "12345678901234567890123456789011";
        int a = 0;
        String key1 = "";
        while(a < keyHex1.length()) {
            String temp = keyHex1.substring(a,a+2);
            int hex = Integer.parseInt(temp, 16);
            key1 += (char)hex;
            a = a+2;
        }
        System.out.println(key1);
        byte[] key1arr = key1.getBytes();
        DatatypeConverter.printHexBinary(key1arr);
    }
}