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
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class Gui
{

    private JFrame frame;
    private JTextField fileLocation;
    private JTextField keyLocation;


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
        frame = new JFrame ();
        frame.setBounds (100, 100, 450, 300);
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        
        JLabel lblEncryptionAndDecryption = new JLabel("Encryption and Decryption");
        lblEncryptionAndDecryption.setFont(new Font("Gadugi", Font.BOLD, 12));
        
        JLabel lblXtsaesMode = new JLabel("XTS-AES Mode (256 bits Key)");
        lblXtsaesMode.setFont(new Font("Gadugi", Font.PLAIN, 11));
        
        JButton btnEncrypt = new JButton("Decrypt");
        btnEncrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        
        JButton button = new JButton("Encrypt");
        
        JButton btnOpenFile = new JButton("Open File");
        
        JButton btnOpenKey = new JButton("Open Key");
        
        fileLocation = new JTextField();
        fileLocation.setColumns(10);
        
        keyLocation = new JTextField();
        keyLocation.setColumns(10);
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                    .addGap(144)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(button, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addComponent(btnEncrypt))
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                            .addGroup(groupLayout.createSequentialGroup()
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(lblEncryptionAndDecryption, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE))
                            .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                                .addGap(5)
                                .addComponent(lblXtsaesMode))))
                    .addContainerGap(140, Short.MAX_VALUE))
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
                    .addGap(178)
                    .addComponent(btnOpenKey)
                    .addContainerGap(177, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap(181, Short.MAX_VALUE)
                    .addComponent(btnOpenFile)
                    .addGap(176))
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap(308, Short.MAX_VALUE)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(fileLocation, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                        .addComponent(keyLocation, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))
                    .addGap(110))
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
                        .addComponent(button)
                        .addComponent(btnEncrypt))
                    .addGap(22))
        );
        frame.getContentPane().setLayout(groupLayout);

    }
}
