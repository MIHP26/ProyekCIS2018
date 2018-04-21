import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.sun.nio.file.SensitivityWatchEventModifier;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
    private String resultPath;
    private String tweakValue = "c7192a71054bfeda";
    private String fileName;


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
        frame.setBounds (100, 100, 450, 213);
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

        JLabel lblEncryptionAndDecryption = new JLabel (
                "Encryption and Decryption");
        lblEncryptionAndDecryption.setFont (new Font ("Gadugi", Font.BOLD, 12));

        JLabel lblXtsaesMode = new JLabel ("XTS-AES Mode (256 bits Key)");
        lblXtsaesMode.setFont (new Font ("Gadugi", Font.PLAIN, 11));

        JButton btnOpenFile = new JButton ("Open File");
        btnOpenFile.setFont (new Font ("Tahoma", Font.PLAIN, 9));
        btnOpenFile.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                // TODO Auto-generated method stub
                openFileActionPerformed (e);

            }
        });

        JButton btnOpenKey = new JButton ("Open Key");
        btnOpenKey.setFont (new Font ("Tahoma", Font.PLAIN, 9));
        btnOpenKey.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                // TODO Auto-generated method stub
                openKeyActionPerformed (e);
            }
        });

        JButton btnEncrypt = new JButton ("Encrypt");
        btnEncrypt.setFont (new Font ("Tahoma", Font.PLAIN, 10));
        btnEncrypt.addActionListener (new ActionListener () {

            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                if (filePath == null || keyPath == null) {
                    JOptionPane.showMessageDialog (null,
                            "File or Key Cannot be Empty", "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    // dibuat default
                    setResultPath (resultPath + "cipher_of_" + fileName);
                   
                    Encrypt.encryption (filePath, keyPath, tweakValue,
                            resultPath);
                    JOptionPane.showMessageDialog (null,
                            "File is successfully encrypted.\n Go check " + resultPath, "SUCCESS",
                            JOptionPane.INFORMATION_MESSAGE);
                    
                }
            }
        });

        JButton btnDecrypt = new JButton ("Decrypt");
        btnDecrypt.setFont (new Font ("Tahoma", Font.PLAIN, 10));
        btnDecrypt.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent arg0)
            {
                if (filePath == null || keyPath == null) {
                    JOptionPane.showMessageDialog (null,
                            "File or Key Cannot be Empty", "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    // TODO Auto-generated method stub
                    // dibuat default
                    setResultPath (resultPath + "messages_in_" + fileName);
                    try {
                        Decrypt.decryption (filePath, keyPath, tweakValue,
                                resultPath);
                        JOptionPane.showMessageDialog (null,
                                "File is successfully decrypted.\n Go check " + resultPath, "SUCCESS",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                }
            }
        });

        fileLocation = new JTextField ();
        fileLocation.setColumns (10);

        keyLocation = new JTextField ();
        keyLocation.setColumns (10);

        GroupLayout groupLayout = new GroupLayout (frame.getContentPane ());
        groupLayout.setHorizontalGroup (
                groupLayout.createParallelGroup (Alignment.LEADING).addGroup (
                        groupLayout
                                .createSequentialGroup ()
                                .addGroup (groupLayout
                                        .createParallelGroup (Alignment.LEADING)
                                        .addGroup (groupLayout
                                                .createSequentialGroup ()
                                                .addGap (144)
                                                .addGroup (groupLayout
                                                        .createParallelGroup (
                                                                Alignment.LEADING)
                                                        .addGroup (
                                                                groupLayout
                                                                        .createSequentialGroup ()
                                                                        .addPreferredGap (
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent (
                                                                                lblEncryptionAndDecryption,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                147,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addGroup (groupLayout
                                                                .createSequentialGroup ()
                                                                .addGap (5)
                                                                .addComponent (
                                                                        lblXtsaesMode))))
                                        .addGroup (groupLayout
                                                .createSequentialGroup ()
                                                .addGap (66)
                                                .addGroup (groupLayout
                                                        .createParallelGroup (
                                                                Alignment.TRAILING,
                                                                false)
                                                        .addComponent (
                                                                keyLocation,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                210,
                                                                Short.MAX_VALUE)
                                                        .addComponent (
                                                                fileLocation,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                210,
                                                                Short.MAX_VALUE))
                                                .addPreferredGap (
                                                        ComponentPlacement.RELATED)
                                                .addGroup (groupLayout
                                                        .createParallelGroup (
                                                                Alignment.LEADING)
                                                        .addComponent (
                                                                btnOpenFile)
                                                        .addComponent (
                                                                btnOpenKey)))
                                        .addGroup (groupLayout
                                                .createSequentialGroup ()
                                                .addGap (131)
                                                .addComponent (btnEncrypt)
                                                .addPreferredGap (
                                                        ComponentPlacement.RELATED)
                                                .addComponent (btnDecrypt)))
                                .addContainerGap (86, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup (groupLayout
                .createParallelGroup (Alignment.LEADING)
                .addGroup (groupLayout.createSequentialGroup ()
                        .addContainerGap ()
                        .addComponent (lblEncryptionAndDecryption)
                        .addPreferredGap (ComponentPlacement.RELATED)
                        .addComponent (lblXtsaesMode).addGap (18)
                        .addGroup (groupLayout
                                .createParallelGroup (Alignment.BASELINE)
                                .addComponent (fileLocation,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent (btnOpenFile))
                        .addPreferredGap (ComponentPlacement.RELATED)
                        .addGroup (groupLayout
                                .createParallelGroup (Alignment.BASELINE)
                                .addComponent (keyLocation,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent (btnOpenKey))
                        .addPreferredGap (ComponentPlacement.UNRELATED)
                        .addGroup (groupLayout
                                .createParallelGroup (Alignment.BASELINE)
                                .addComponent (btnDecrypt)
                                .addComponent (btnEncrypt))
                        .addContainerGap (190, Short.MAX_VALUE)));
        frame.getContentPane ().setLayout (groupLayout);

    }


    protected void openKeyActionPerformed (ActionEvent e)
    {
        // TODO Auto-generated method stub
        JFileChooser keyChooser = new JFileChooser ();
        int returnVal = keyChooser.showOpenDialog (null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = keyChooser.getSelectedFile ();
            try {
                // display the file name
                keyLocation.setText (keyChooser.getSelectedFile ().getName ());
                // return the file path
                setKeyPath (file.getAbsolutePath ());
            } catch (Exception ex) {
                System.out.println (
                        "problem accessing file" + file.getAbsolutePath ());
            }
        }
    }


    protected void openFileActionPerformed (ActionEvent e)
    {
        // TODO Auto-generated method stub
        JFileChooser fileChooser = new JFileChooser ();
        int returnVal = fileChooser.showOpenDialog (null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile ();
            try {
                // display file name
                fileLocation
                        .setText (fileChooser.getSelectedFile ().getName ());
                // return the file path
                setFilePath (file.getAbsolutePath ());
                // set default cipherpath
                setResultPath (
                        file.getAbsoluteFile ().getParent () + File.separator);
                // System.out.println(file.getCanonicalPath());
                // System.out.println(file.getCanonicalFile().getParent());
                System.out.println (resultPath);
                fileName = file.getName ();

            } catch (Exception ex) {
                System.out.println (
                        "problem accessing file" + file.getAbsolutePath ());
            }
        }
    }


    public void setFilePath (String filePath)
    {
        this.filePath = filePath;
    }


    public void setKeyPath (String keyPath)
    {
        this.keyPath = keyPath;
    }


    public void setResultPath (String resultPath)
    {
        this.resultPath = resultPath;
    }
}
