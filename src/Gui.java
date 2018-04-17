import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JFileChooser;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import java.awt.Window.Type;

public class Gui
{

    private JFrame frame;
    private JTextField fileLocation;
    private JTextField keyLocation;
    private String filePath;
    private String keyPath;


    /**
     * Launch the application.
     */
    public static void main (String[] args)
    {
        EventQueue.invokeLater (new Runnable () {
            public void run ()
            {
                try {
                    Gui window = new Gui ();
                    window.frame.setVisible (true);
                } catch (Exception e) {
                    e.printStackTrace ();
                }
            }
        });
    }


    /**
     * Create the application.
     */
    public Gui ()
    {
        initialize ();
    }


    /**
     * Initialize the contents of the frame.
     */
    private void initialize ()
    {
        frame = new JFrame ("AES-Encryption and Decryption");
        frame.setBounds (100, 100, 450, 300);
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        
        JLabel lblEncryptionAndDecryption = new JLabel("Encryption and Decryption");
        lblEncryptionAndDecryption.setFont(new Font("Gadugi", Font.BOLD, 12));
        
        JLabel lblXtsaesMode = new JLabel("XTS-AES Mode (256 bits Key)");
        lblXtsaesMode.setFont(new Font("Gadugi", Font.PLAIN, 11));
        
        JButton btnOpenFile = new JButton("Open File");
        btnOpenFile.setFont(new Font("Tahoma", Font.PLAIN, 9));
        btnOpenFile.addActionListener (new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                // TODO Auto-generated method stub
                openFileActionPerformed(e);
                
            }         
        });
        
        
        JButton btnOpenKey = new JButton("Open Key");
        btnOpenKey.setFont(new Font("Tahoma", Font.PLAIN, 9));
        btnOpenKey.addActionListener (new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                // TODO Auto-generated method stub
                openKeyActionPerformed(e);  
            }         
        });
        
        
        JButton btnEncrypt = new JButton("Encrypt");
        btnEncrypt.setFont(new Font("Tahoma", Font.PLAIN, 10));
        btnEncrypt.addActionListener (new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                // TODO Auto-generated method stub
                Encrypt keyTest = new Encrypt ();   
                //tes kunci 
                keyTest.keyProcessing (keyPath);
            }
            
        });
        
        JButton btnDecrypt = new JButton("Decrypt");
        btnDecrypt.setFont(new Font("Tahoma", Font.PLAIN, 10));
        btnDecrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        
       
        fileLocation = new JTextField();
        fileLocation.setColumns(10);
        
        keyLocation = new JTextField();
        keyLocation.setColumns(10);
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGap(144)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(btnEncrypt)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(btnDecrypt))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(lblEncryptionAndDecryption, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGap(5)
                            .addComponent(lblXtsaesMode)))
                    .addContainerGap(140, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup()
                    .addGap(178)
                    .addComponent(btnOpenKey)
                    .addContainerGap(177, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap(114, Short.MAX_VALUE)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(fileLocation, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                        .addComponent(keyLocation, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))
                    .addGap(110))
                .addGroup(groupLayout.createSequentialGroup()
                    .addGap(179)
                    .addComponent(btnOpenFile)
                    .addContainerGap(178, Short.MAX_VALUE))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblEncryptionAndDecryption)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lblXtsaesMode)
                    .addPreferredGap(ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                    .addComponent(fileLocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnOpenFile)
                    .addGap(18)
                    .addComponent(keyLocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnOpenKey)
                    .addGap(24)
                    .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnEncrypt)
                        .addComponent(btnDecrypt))
                    .addGap(22))
        );
        frame.getContentPane().setLayout(groupLayout);

    }


    protected void openKeyActionPerformed (ActionEvent e)
    {
        // TODO Auto-generated method stub
        JFileChooser keyChooser = new JFileChooser();
        int returnVal = keyChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = keyChooser.getSelectedFile();
            try {
                //display file name (or path?) 
                keyLocation.setText(keyChooser.getSelectedFile().getName());
                // return the file path
                setKeyPath (file.getAbsolutePath ());
              } catch (Exception ex) {
                System.out.println("problem accessing file"+file.getAbsolutePath());
              }
        }
    }


    protected void openFileActionPerformed (ActionEvent e)
    {
        // TODO Auto-generated method stub
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
             //display file name (or path?)   
                fileLocation.setText(fileChooser.getSelectedFile().getName());
             // return the file path
                setFilePath (file.getAbsolutePath ());
              } catch (Exception ex) {
                System.out.println("problem accessing file"+file.getAbsolutePath());
              }
        }
    }


    public String getFilePath ()
    {
        return filePath;
    }


    public void setFilePath (String filePath)
    {
        this.filePath = filePath;
    }


    public String getKeyPath ()
    {
        return keyPath;
    }


    public void setKeyPath (String keyPath)
    {
        this.keyPath = keyPath;
    }
}
