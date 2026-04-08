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
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetPassDBeaverGUI extends JFrame {

    private JTextArea textArea;
    private JButton openButton;
    private JButton autoSelectButton;
    private JLabel statusLabel;
    private JComboBox<String> connectionComboBox;
    private Map<String, String> connectionMap;

    private static final byte[] LOCAL_KEY_CACHE = new byte[] { -70, -69, 74, -97, 119, 74, -72, 83, -55, 108, 45, 101, 61, -2, 84, 74 };

    public GetPassDBeaverGUI() {
        setTitle("DBeaver Password Decryptor");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize connection map
        connectionMap = new HashMap<>();

        // Create components
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        openButton = new JButton("Open credentials-config.json");
        autoSelectButton = new JButton("Auto-Select Encrypted File");
        statusLabel = new JLabel("Select the credentials-config.json file to decrypt or use Auto-Select.");

        // Connection selector panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.add(new JLabel("Connection:"));
        connectionComboBox = new JComboBox<>();
        connectionComboBox.addItem("-- Select Connection --");
        connectionPanel.add(connectionComboBox);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(openButton);
        buttonPanel.add(autoSelectButton);

        // Set layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        add(connectionPanel, BorderLayout.EAST);

        // Add action listener to the open button
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

        // Add action listener to the auto-select button
        autoSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoSelectAndDecrypt();
            }
        });

        // Add action listener to connection combo box
        connectionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedId = connectionComboBox.getSelectedItem().toString();
                if (!selectedId.equals("-- Select Connection --") && connectionMap.containsKey(selectedId)) {
                    String connectionInfo = connectionMap.get(selectedId);
                    textArea.setText(connectionInfo);
                    statusLabel.setText("Showing connection: " + selectedId);
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
            
            // Parse and populate connection selector
            parseConnections(decryptedContent);
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException ex) {
            textArea.setText("");
            statusLabel.setText("Error decrypting file: " + ex.getMessage());
        }
    }

    private void parseConnections(String decryptedContent) {
        try {
            JSONObject json = new JSONObject(decryptedContent);
            JSONArray datasources = json.getJSONArray("datasources");
            
            // Clear existing items except the default
            connectionComboBox.removeAllItems();
            connectionComboBox.addItem("-- Select Connection --");
            connectionMap.clear();
            
            for (int i = 0; i < datasources.length(); i++) {
                JSONObject datasource = datasources.getJSONObject(i);
                String id = datasource.getString("id");
                String name = datasource.optString("name", id);
                String host = datasource.optString("host", "N/A");
                String port = datasource.optString("port", "N/A");
                String database = datasource.optString("database", "N/A");
                String driver = datasource.optString("driver", "N/A");
                
                // Build connection info string
                StringBuilder info = new StringBuilder();
                info.append("Connection Name: ").append(name).append("\n");
                info.append("ID: ").append(id).append("\n");
                info.append("Host: ").append(host).append("\n");
                info.append("Port: ").append(port).append("\n");
                info.append("Database: ").append(database).append("\n");
                info.append("Driver: ").append(driver).append("\n");
                
                if (datasource.has("password")) {
                    info.append("Password: ").append(datasource.getString("password")).append("\n");
                }
                if (datasource.has("username")) {
                    info.append("Username: ").append(datasource.getString("username")).append("\n");
                }
                
                connectionMap.put(id, info.toString());
                connectionComboBox.addItem(id + " - " + name);
            }
            
            statusLabel.setText("File decrypted successfully! Found " + datasources.length() + " connection(s).");
        } catch (Exception e) {
            statusLabel.setText("Warning: Could not parse connections: " + e.getMessage());
        }
    }

    private void autoSelectAndDecrypt() {
        Path credentialsPath = getDefaultCredentialsPath();
        
        if (credentialsPath == null || !Files.exists(credentialsPath)) {
            JOptionPane.showMessageDialog(this, 
                "Could not find credentials-config.json at default locations.\n" +
                "Please use 'Open credentials-config.json' button to select manually.",
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Auto-selected: " + credentialsPath.toString());
        decryptFile(credentialsPath);
    }

    private Path getDefaultCredentialsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        // Try different DBeaver data paths based on OS
        if (os.contains("win")) {
            // Windows
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                Path[] winPaths = {
                    Paths.get(appData, "DBeaverData", "workspace6", "General", ".dbeaver", "credentials-config.json"),
                    Paths.get(appData, "DBeaver", "workspace6", "General", ".dbeaver", "credentials-config.json"),
                    Paths.get(userHome, "AppData", "Roaming", "DBeaverData", "workspace6", "General", ".dbeaver", "credentials-config.json")
                };
                for (Path p : winPaths) {
                    if (Files.exists(p)) {
                        return p;
                    }
                }
            }
        } else if (os.contains("mac")) {
            // macOS
            Path[] macPaths = {
                Paths.get(userHome, "Library", "DBeaverData", "workspace6", "General", ".dbeaver", "credentials-config.json"),
                Paths.get(userHome, "Library", "Application Support", "DBeaver", "workspace6", "General", ".dbeaver", "credentials-config.json")
            };
            for (Path p : macPaths) {
                if (Files.exists(p)) {
                    return p;
                }
            }
        } else {
            // Linux and other Unix-like systems
            Path[] linuxPaths = {
                Paths.get(userHome, ".local", "share", "DBeaverData", "workspace6", "General", ".dbeaver", "credentials-config.json"),
                Paths.get(userHome, ".dbeaver", "workspace6", "General", ".dbeaver", "credentials-config.json"),
                Paths.get(userHome, ".local", "share", "DBeaver", "workspace6", "General", ".dbeaver", "credentials-config.json")
            };
            for (Path p : linuxPaths) {
                if (Files.exists(p)) {
                    return p;
                }
            }
        }
        
        return null;
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
