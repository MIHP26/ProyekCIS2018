import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Encrypt
{


    public void keyProcessing(String keyPath) {
        try
        {
            FileInputStream fis = new FileInputStream(keyPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String key = reader.readLine ();
            String k1 = key.substring (0, (key.length()/2)-1);
            String k2 = key.substring (key.length ()/2, key.length ()-1);
            reader.close ();
            //System.out.println (k1);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}