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
import java.util.stream.Stream;
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
    private Path currentConfigFolder;

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
            
            // Store the config folder path for later use
            currentConfigFolder = filePath.getParent();
            
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
            
            // Clear existing items except the default
            connectionComboBox.removeAllItems();
            connectionComboBox.addItem("-- Select Connection --");
            connectionMap.clear();
            
            // Try the newer DBeaver format with "connections" object
            if (json.has("connections")) {
                JSONObject connections = json.getJSONObject("connections");
                JSONArray keys = connections.names();
                
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String connId = keys.getString(i);
                        JSONObject connData = connections.getJSONObject(connId);
                        
                        String name = connData.optString("name", connId);
                        String host = connData.optString("host", "N/A");
                        String port = connData.optString("port", "N/A");
                        String database = connData.optString("database", "N/A");
                        String driver = connData.optString("driver", "N/A");
                        
                        // Build connection info string
                        StringBuilder info = new StringBuilder();
                        info.append("Connection Name: ").append(name).append("\n");
                        info.append("ID: ").append(connId).append("\n");
                        info.append("Host: ").append(host).append("\n");
                        info.append("Port: ").append(port).append("\n");
                        info.append("Database: ").append(database).append("\n");
                        info.append("Driver: ").append(driver).append("\n");
                        
                        connectionMap.put(connId, info.toString());
                        connectionComboBox.addItem(connId + " - " + name);
                    }
                    
                    statusLabel.setText("File decrypted successfully! Found " + keys.length() + " connection(s).");
                    return;
                }
            }
            
            // Fallback to older format with "datasources" array
            if (json.has("datasources")) {
                JSONArray datasources = json.getJSONArray("datasources");
                
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
            }
        } catch (Exception e) {
            statusLabel.setText("Warning: Could not parse connections: " + e.getMessage());
        }
    }

    private void autoSelectAndDecrypt() {
        // First, try to find any DBeaver config folder using the bash script logic
        Path configFolder = findDBeaverConfigFolder();
        
        if (configFolder == null) {
            JOptionPane.showMessageDialog(this, 
                "Could not find DBeaver configuration files.\n" +
                "Please use 'Open credentials-config.json' button to select manually.",
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Path credentialsPath = configFolder.resolve("credentials-config.json");
        Path dataSourcesPath = configFolder.resolve("data-sources.json");
        
        if (!Files.exists(credentialsPath)) {
            JOptionPane.showMessageDialog(this, 
                "credentials-config.json not found at: " + credentialsPath,
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Auto-selected: " + credentialsPath.toString());
        decryptFile(credentialsPath);
        
        // Also try to load and display data-sources.json if it exists
        if (Files.exists(dataSourcesPath)) {
            try {
                String dataSourcesContent = new String(Files.readAllBytes(dataSourcesPath));
                parseConnectionsFromDataSources(dataSourcesContent);
            } catch (IOException e) {
                statusLabel.setText("Loaded credentials but could not read data-sources.json: " + e.getMessage());
            }
        }
    }

    private Path findDBeaverConfigFolder() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        
        // Define search paths based on OS
        java.util.List<Path> searchPaths = new java.util.ArrayList<>();
        
        if (os.contains("win")) {
            // Windows
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                searchPaths.add(Paths.get(appData, "DBeaverData", "workspace6", "General", ".dbeaver"));
                searchPaths.add(Paths.get(appData, "DBeaver", "workspace6", "General", ".dbeaver"));
                searchPaths.add(Paths.get(userHome, "AppData", "Roaming", "DBeaverData", "workspace6", "General", ".dbeaver"));
                searchPaths.add(Paths.get(userHome, "AppData", "Local", "DBeaverData", "workspace6", "General", ".dbeaver"));
            }
        } else if (os.contains("mac")) {
            // macOS
            searchPaths.add(Paths.get(userHome, "Library", "DBeaverData", "workspace6", "General", ".dbeaver"));
            searchPaths.add(Paths.get(userHome, "Library", "Application Support", "DBeaver", "workspace6", "General", ".dbeaver"));
        } else {
            // Linux and other Unix-like systems
            searchPaths.add(Paths.get(userHome, ".local", "share", "DBeaverData", "workspace6", "General", ".dbeaver"));
            searchPaths.add(Paths.get(userHome, ".dbeaver", "workspace6", "General", ".dbeaver"));
            searchPaths.add(Paths.get(userHome, ".local", "share", "DBeaver", "workspace6", "General", ".dbeaver"));
            searchPaths.add(Paths.get(userHome, "DBeaverData", "workspace6", "General", ".dbeaver"));
        }
        
        // First check if any of the standard paths exist
        for (Path p : searchPaths) {
            if (Files.exists(p) && Files.exists(p.resolve("credentials-config.json"))) {
                return p;
            }
        }
        
        // If not found in standard paths, search recursively from home directory with more depth
        try (Stream<Path> stream = Files.walk(Paths.get(userHome), 10)) {
            return stream
                .filter(Files::isDirectory)
                .filter(p -> p.endsWith(".dbeaver"))
                .filter(p -> Files.exists(p.resolve("credentials-config.json")))
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            // Ignore and return null
        }
        
        return null;
    }

    private void parseConnectionsFromDataSources(String dataSourcesContent) {
        try {
            JSONObject json = new JSONObject(dataSourcesContent);
            
            // Try the newer DBeaver format with "connections" object
            if (json.has("connections")) {
                JSONObject connections = json.getJSONObject("connections");
                JSONArray keys = connections.names();
                
                if (keys != null) {
                    // Update existing connection map with additional info from data-sources.json
                    for (int i = 0; i < keys.length(); i++) {
                        String connId = keys.getString(i);
                        JSONObject connData = connections.getJSONObject(connId);
                        
                        String name = connData.optString("name", connId);
                        String host = connData.optString("host", "N/A");
                        String port = connData.optString("port", "N/A");
                        String database = connData.optString("database", "N/A");
                        String driver = connData.optString("driver", "N/A");
                        String folder = connData.optString("folder", "N/A");
                        
                        // Build or update connection info string
                        StringBuilder info = new StringBuilder();
                        info.append("Folder: ").append(folder).append("\n");
                        info.append("Connection Name: ").append(name).append("\n");
                        info.append("ID: ").append(connId).append("\n");
                        info.append("Host: ").append(host).append("\n");
                        info.append("Port: ").append(port).append("\n");
                        info.append("Database: ").append(database).append("\n");
                        info.append("Driver: ").append(driver).append("\n");
                        
                        // If we already have this connection from credentials, merge the info
                        if (connectionMap.containsKey(connId)) {
                            String existingInfo = connectionMap.get(connId);
                            // Extract username and password from existing info if present
                            if (existingInfo.contains("Username:")) {
                                String[] lines = existingInfo.split("\n");
                                for (String line : lines) {
                                    if (line.startsWith("Username:") || line.startsWith("Password:")) {
                                        info.append(line).append("\n");
                                    }
                                }
                            }
                        }
                        
                        connectionMap.put(connId, info.toString());
                    }
                    
                    // Refresh combo box if needed
                    if (connectionComboBox.getItemCount() <= 1) {
                        connectionComboBox.removeAllItems();
                        connectionComboBox.addItem("-- Select Connection --");
                        for (String key : connectionMap.keySet()) {
                            connectionComboBox.addItem(key + " - " + connectionMap.get(key).split("\n")[1].replace("Connection Name: ", ""));
                        }
                    }
                    
                    statusLabel.setText("Found " + keys.length() + " connection(s) from data-sources.json");
                    return;
                }
            }
            
            // Fallback to older format with "datasources" array
            if (json.has("datasources")) {
                JSONArray datasources = json.getJSONArray("datasources");
                
                for (int i = 0; i < datasources.length(); i++) {
                    JSONObject datasource = datasources.getJSONObject(i);
                    String id = datasource.getString("id");
                    String name = datasource.optString("name", id);
                    String host = datasource.optString("host", "N/A");
                    String port = datasource.optString("port", "N/A");
                    String database = datasource.optString("database", "N/A");
                    String driver = datasource.optString("driver", "N/A");
                    String folder = datasource.optString("folder", "N/A");
                    
                    // Build connection info string
                    StringBuilder info = new StringBuilder();
                    info.append("Folder: ").append(folder).append("\n");
                    info.append("Connection Name: ").append(name).append("\n");
                    info.append("ID: ").append(id).append("\n");
                    info.append("Host: ").append(host).append("\n");
                    info.append("Port: ").append(port).append("\n");
                    info.append("Database: ").append(database).append("\n");
                    info.append("Driver: ").append(driver).append("\n");
                    
                    // If we already have this connection from credentials, merge the info
                    if (connectionMap.containsKey(id)) {
                        String existingInfo = connectionMap.get(id);
                        // Extract username and password from existing info if present
                        if (existingInfo.contains("Username:") || existingInfo.contains("Password:")) {
                            String[] lines = existingInfo.split("\n");
                            for (String line : lines) {
                                if (line.startsWith("Username:") || line.startsWith("Password:")) {
                                    info.append(line).append("\n");
                                }
                            }
                        }
                    }
                    
                    connectionMap.put(id, info.toString());
                }
                
                // Refresh combo box if needed
                if (connectionComboBox.getItemCount() <= 1) {
                    connectionComboBox.removeAllItems();
                    connectionComboBox.addItem("-- Select Connection --");
                    for (String key : connectionMap.keySet()) {
                        connectionComboBox.addItem(key + " - " + connectionMap.get(key).split("\n")[1].replace("Connection Name: ", ""));
                    }
                }
                
                statusLabel.setText("Found " + datasources.length() + " connection(s) from data-sources.json");
            }
        } catch (Exception e) {
            statusLabel.setText("Warning: Could not parse data-sources.json: " + e.getMessage());
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
