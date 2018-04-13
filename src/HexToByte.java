import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.xml.bind.DatatypeConverter;

public class HexToByte
{
    
    public static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }
    
    public static void main(final String[] args) throws IOException
    {
        try
        {
            FileInputStream fis = new FileInputStream("C:\\Users\\Tazkianida\\workspace\\TesCIS\\src\\gambar1.jpg");
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Tazkianida\\workspace\\TesCIS\\src\\gambar2.jpg");

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String s;
            while((s = reader.readLine ()) != null) {
                fos.write(toByteArray(s));
            }
            reader.close ();
            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
