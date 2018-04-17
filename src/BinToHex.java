import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;

public class BinToHex
{
    public static String toHexString(byte [] array) {
        return DatatypeConverter.printHexBinary(array);
    }
   
    public static void main(final String[] args) throws IOException
    {
        try
        {
            FileInputStream fis = new FileInputStream("C:\\Users\\Tazkianida\\workspace\\TesCIS\\src\\gambar.jpg");
            BufferedWriter fos = new BufferedWriter(new FileWriter("C:\\Users\\Tazkianida\\workspace\\TesCIS\\src\\gambar1.txt"));

            byte[] bytes = new byte[800];
            int value = 0;
            int a = 0;
            do
            {
                value = fis.read(bytes);
                a += value;
                fos.write(toHexString(bytes));
            }while(value != -1);
            byte[] allbytes = new byte[a+1];
            fis.read(allbytes);
            fos.write(toHexString(allbytes));
            
            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}