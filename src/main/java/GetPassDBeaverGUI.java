import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GetPassDBeaverGUI extends JFrame {

    private JTextArea textArea;
    private JButton openButton;
    private JLabel statusLabel;

    private static final byte[] LOCAL_KEY_CACHE = new byte[] { -70, -69, 74, -97, 119, 74, -72, 83, -55, 108, 45, 101, 61, -2, 84, 74 };

    public GetPassDBeaverGUI() {
        setTitle("DBeaver Password Decryptor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        openButton = new JButton("Open credentials-config.json");
        statusLabel = new JLabel("Select the credentials-config.json file to decrypt.");

        // Set layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(openButton, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Add action listener to the button
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    Path selectedFile = fileChooser.getSelectedFile().toPath();
                    decryptFile(selectedFile);
                }
            }
        });
    }

    private void decryptFile(Path filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            String decryptedContent = decrypt(fileBytes);
            textArea.setText(decryptedContent);
            statusLabel.setText("File decrypted successfully!");
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException ex) {
            textArea.setText("");
            statusLabel.setText("Error decrypting file: " + ex.getMessage());
        }
    }

    private static String decrypt(byte[] contents) throws InvalidAlgorithmParameterException, InvalidKeyException,
            IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        try (InputStream byteStream = new ByteArrayInputStream(contents)) {
            byte[] fileIv = new byte[16];
            byteStream.read(fileIv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aes = new SecretKeySpec(LOCAL_KEY_CACHE, "AES");
            cipher.init(Cipher.DECRYPT_MODE, aes, new IvParameterSpec(fileIv));
            try (CipherInputStream cipherIn = new CipherInputStream(byteStream, cipher)) {
                return inputStreamToString(cipherIn);
            }
        }
    }

    private static String inputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GetPassDBeaverGUI gui = new GetPassDBeaverGUI();
            gui.setVisible(true);
        });
    }
}
