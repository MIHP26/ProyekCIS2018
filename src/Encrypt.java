import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Encrypt
{


    public void preProcessing(String filePath, String keyPath) {
        try
        {
            //read file
            byte[] file = Files.readAllBytes(Paths.get(filePath)); // per byte
            
          /*  int messageCounter = 0;
            int j = 0;
            boolean needStealing = true;
            int unusedLastBlockSpace = 0;
            if(message.length % 16 == 0) {
                    j = message.length/16;
                    needStealing = false;
            }
            else {
                    j = (message.length/16)+1;
                    needStealing = true;
                    unusedLastBlockSpace = 16 - (message.length % 16);
            }*/
            
            //divide message into blocks
            
            //key processing
            FileInputStream fis = new FileInputStream(keyPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String key = reader.readLine ();
            String k1 = key.substring (0, (key.length()/2));
            String k2 = key.substring (key.length ()/2, key.length ());
            reader.close ();
            System.out.println (k1);
            System.out.println (k2);
        }
        
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
