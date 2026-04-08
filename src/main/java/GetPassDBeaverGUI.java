import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private JTable connectionTable;
    private DefaultTableModel tableModel;
    private JButton openButton;
    private JButton autoSelectButton;
    private JButton exportButton;
    private JLabel statusLabel;
    private List<ConnectionInfo> connectionsList;
    private Path currentConfigFolder;

    private static final byte[] LOCAL_KEY_CACHE = new byte[] { -70, -69, 74, -97, 119, 74, -72, 83, -55, 108, 45, 101, 61, -2, 84, 74 };

    // Inner class to hold connection information
    private static class ConnectionInfo {
        String folder;
        String connectionName;
        String id;
        String host;
        String port;
        String database;
        String driver;
        String username;
        String password;

        public Object[] toTableRow() {
            return new Object[]{connectionName, username, password};
        }
    }

    public GetPassDBeaverGUI() {
        setTitle("DBeaver Password Decryptor");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize connections list
        connectionsList = new ArrayList<>();

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create components
        String[] columnNames = {"Connection Name", "User Name", "Password"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        connectionTable = new JTable(tableModel);
        connectionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        connectionTable.setRowHeight(28);
        connectionTable.setForeground(Color.BLACK);
        connectionTable.setBackground(Color.WHITE);
        connectionTable.setGridColor(new Color(220, 220, 220));
        connectionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        connectionTable.getTableHeader().setBackground(new Color(60, 90, 140));
        connectionTable.getTableHeader().setForeground(Color.WHITE);
        connectionTable.setSelectionBackground(new Color(200, 220, 240));
        connectionTable.setSelectionForeground(Color.BLACK);
        connectionTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(connectionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1));
        scrollPane.setBackground(Color.WHITE);

        openButton = createStyledButton("Open credentials-config.json", new Color(70, 130, 180));
        autoSelectButton = createStyledButton("Auto-Select Encrypted File", new Color(60, 179, 113));
        exportButton = createStyledButton("Export to CSV", new Color(255, 140, 0));
        exportButton.setEnabled(false);
        
        statusLabel = new JLabel("Select the credentials-config.json file to decrypt or use Auto-Select.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(openButton);
        buttonPanel.add(autoSelectButton);
        buttonPanel.add(exportButton);

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(60, 90, 140));
        titlePanel.setPreferredSize(new Dimension(getWidth(), 50));
        JLabel titleLabel = new JLabel("DBeaver Password Decryptor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // Set layout
        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.EAST);

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

        // Add action listener to the export button
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(220, 35));
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
        });
        
        return button;
    }

    private void decryptFile(Path filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            String decryptedContent = decrypt(fileBytes);
            statusLabel.setText("File decrypted successfully!");
            
            // Store the config folder path for later use
            currentConfigFolder = filePath.getParent();
            
            // Parse and populate table with connections
            parseConnectionsAndPopulateTable(decryptedContent);
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException ex) {
            statusLabel.setText("Error decrypting file: " + ex.getMessage());
        }
    }

    private void parseConnectionsAndPopulateTable(String decryptedContent) {
        try {
            JSONObject json = new JSONObject(decryptedContent);
            
            // Clear existing data
            connectionsList.clear();
            tableModel.setRowCount(0);
            
            // Try the newer DBeaver format with "connections" object
            if (json.has("connections")) {
                JSONObject connections = json.getJSONObject("connections");
                JSONArray keys = connections.names();
                
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String connId = keys.getString(i);
                        JSONObject connData = connections.getJSONObject(connId);
                        
                        ConnectionInfo info = new ConnectionInfo();
                        info.id = connId;
                        info.connectionName = connData.optString("name", connId);
                        info.host = connData.optString("host", "N/A");
                        info.port = connData.optString("port", "N/A");
                        info.database = connData.optString("database", "N/A");
                        info.driver = connData.optString("driver", "N/A");
                        info.folder = connData.optString("folder", "N/A");
                        info.username = "N/A";
                        info.password = "N/A";
                        
                        connectionsList.add(info);
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
                    
                    ConnectionInfo info = new ConnectionInfo();
                    info.id = datasource.getString("id");
                    info.connectionName = datasource.optString("name", info.id);
                    info.host = datasource.optString("host", "N/A");
                    info.port = datasource.optString("port", "N/A");
                    info.database = datasource.optString("database", "N/A");
                    info.driver = datasource.optString("driver", "N/A");
                    info.folder = datasource.optString("folder", "N/A");
                    
                    if (datasource.has("password")) {
                        info.password = datasource.getString("password");
                    }
                    if (datasource.has("username")) {
                        info.username = datasource.getString("username");
                    } else {
                        info.username = "N/A";
                    }
                    
                    connectionsList.add(info);
                }
                
                statusLabel.setText("File decrypted successfully! Found " + datasources.length() + " connection(s).");
            }
            
            // Now try to merge with credentials from credentials-config.json
            if (currentConfigFolder != null) {
                Path dataSourcesPath = currentConfigFolder.resolve("data-sources.json");
                if (Files.exists(dataSourcesPath)) {
                    String dataSourcesContent = new String(Files.readAllBytes(dataSourcesPath));
                    mergeWithCredentials(dataSourcesContent);
                }
            }
            
            // Populate table
            updateTableFromConnectionsList();
            
            // Enable export button
            exportButton.setEnabled(!connectionsList.isEmpty());
            
        } catch (Exception e) {
            statusLabel.setText("Warning: Could not parse connections: " + e.getMessage());
        }
    }

    private void mergeWithCredentials(String dataSourcesContent) {
        try {
            JSONObject json = new JSONObject(dataSourcesContent);
            Map<String, JSONObject> credentialsMap = new HashMap<>();
            
            // Extract credentials from the decrypted content
            if (json.has("connections")) {
                JSONObject connections = json.getJSONObject("connections");
                JSONArray keys = connections.names();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String key = keys.getString(i);
                        credentialsMap.put(key, connections.getJSONObject(key));
                    }
                }
            }
            
            // Update connectionsList with credentials
            for (ConnectionInfo info : connectionsList) {
                if (credentialsMap.containsKey(info.id)) {
                    JSONObject cred = credentialsMap.get(info.id);
                    JSONObject connectionCred = cred.optJSONObject("#connection");
                    if (connectionCred != null) {
                        if (connectionCred.has("user")) {
                            info.username = connectionCred.getString("user");
                        }
                        if (connectionCred.has("password")) {
                            info.password = connectionCred.getString("password");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors in merging
        }
    }

    private void updateTableFromConnectionsList() {
        tableModel.setRowCount(0);
        for (ConnectionInfo info : connectionsList) {
            tableModel.addRow(info.toTableRow());
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
        
        if (!Files.exists(credentialsPath)) {
            JOptionPane.showMessageDialog(this, 
                "credentials-config.json not found at: " + credentialsPath,
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        statusLabel.setText("Auto-selected: " + credentialsPath.toString());
        decryptFile(credentialsPath);
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

    private void exportToCSV() {
        if (connectionsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No connections to export.",
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setSelectedFile(new java.io.File("dbeaver_connections.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                // Write header
                writer.append("Connection Name,User Name,Password\n");
                
                // Write data rows
                for (ConnectionInfo info : connectionsList) {
                    writer.append(escapeCsv(info.connectionName)).append(",");
                    writer.append(escapeCsv(info.username)).append(",");
                    writer.append(escapeCsv(info.password)).append("\n");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Successfully exported " + connectionsList.size() + " connection(s) to CSV.",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Exported " + connectionsList.size() + " connection(s) to CSV.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting to CSV: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // If the value contains comma, quote, or newline, wrap it in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
