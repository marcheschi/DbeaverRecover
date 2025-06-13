import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DBeaverRecoveryGUI extends JFrame {

    private JTextField filePathField;
    private JTextArea resultsArea;
    private JLabel statusLabel;
    private JButton browseButton;
    private JButton decryptButton;

    public DBeaverRecoveryGUI() {
        // Set up the main window
        setTitle("DBeaver Password Recovery");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // File Selection Panel (NORTH)
        JPanel fileSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fileLabel = new JLabel("Credentials File:");
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        browseButton = new JButton("Browse...");
        decryptButton = new JButton("Decrypt");

        fileSelectionPanel.add(fileLabel);
        fileSelectionPanel.add(filePathField);
        fileSelectionPanel.add(browseButton);
        fileSelectionPanel.add(decryptButton);
        add(fileSelectionPanel, BorderLayout.NORTH);

        // Results Area (CENTER)
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        add(scrollPane, BorderLayout.CENTER);

        // Status Bar (SOUTH)
        statusLabel = new JLabel("Ready.");
        add(statusLabel, BorderLayout.SOUTH);

        // Load default path
        loadDefaultCredentialsPath();

        // Add action listeners
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files (*.json)", "json");
                fileChooser.setFileFilter(filter);
                // Suggest current directory based on existing field, or user's home directory
                File currentDir = null;
                String currentPath = filePathField.getText();
                if (currentPath != null && !currentPath.isEmpty()) {
                    File f = new File(currentPath);
                    if (f.exists()) {
                        currentDir = f.isFile() ? f.getParentFile() : f;
                    }
                }
                if (currentDir == null) {
                    currentDir = new File(System.getProperty("user.home"));
                }
                fileChooser.setCurrentDirectory(currentDir);


                int returnValue = fileChooser.showOpenDialog(DBeaverRecoveryGUI.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                    statusLabel.setText("File selected: " + selectedFile.getName());
                }
            }
        });

        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathField.getText();

                if (filePath == null || filePath.trim().isEmpty()) {
                    resultsArea.setText("Error: No file selected. Please select a credentials file.");
                    statusLabel.setText("Error: No file selected.");
                    return;
                }

                resultsArea.setText(""); // Clear previous results
                statusLabel.setText("Decrypting...");

                try {
                    String decryptedJson = GetPassDBeaver.decryptCredentialsFromFile(filePath);
                    resultsArea.setText(decryptedJson);
                    statusLabel.setText("Decryption successful.");
                } catch (Exception ex) {
                    resultsArea.setText("Error during decryption: " + ex.getMessage() +
                                      "\n\nMake sure the selected file is a valid DBeaver credentials file " +
                                      "and the application has permission to read it.");
                    statusLabel.setText("Decryption failed.");
                    // For developer debugging, consider uncommenting:
                    // ex.printStackTrace();
                }
            }
        });
    }

    private void loadDefaultCredentialsPath() {
        try {
            String defaultPath = GetPassDBeaver.getDefaultCredentialsPath();
            if (defaultPath != null && !defaultPath.isEmpty()) {
                filePathField.setText(defaultPath);
                statusLabel.setText("Default credentials file loaded.");
            } else {
                statusLabel.setText("Ready. Select a credentials file or use Browse.");
            }
        } catch (Exception ex) {
            // Handle any unexpected exceptions from getDefaultCredentialsPath, though it's designed to return null.
            statusLabel.setText("Error loading default path. Please browse manually.");
            // Log error if necessary: ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DBeaverRecoveryGUI().setVisible(true);
            }
        });
    }
}
