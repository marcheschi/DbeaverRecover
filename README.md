# DBeaver Password Recovery Tool

This utility allows you to decrypt stored database credentials from DBeaver's configuration files. It's particularly useful for recovering forgotten database passwords or for system administrators who need to manage DBeaver connections.

**This tool now includes a user-friendly Desktop GUI application (recommended) and the original command-line interface.**

## Desktop GUI Application (Recommended)

The Desktop GUI application provides an easy-to-use interface for decrypting your DBeaver credentials.

### Features (GUI)

-   User-friendly interface for selecting the credentials file.
-   Automatic detection of default `credentials-config.json` paths for Linux, Windows, and macOS.
-   Clear display of decrypted credentials or error messages.
-   Status updates for ongoing operations.

### Requirements (GUI)

-   Java Runtime Environment (JRE) 8 or higher.
-   The `DBeaverPasswordRecovery.jar` file (created from the compilation & packaging step).

### Running the GUI Application

1.  **Ensure you have Java JRE 8 or higher installed.** You can check by opening a terminal or command prompt and typing `java -version`.
2.  **Download or package the `DBeaverPasswordRecovery.jar` file.** (If you're building from source, see Compilation and Packaging below).
3.  **Run the application using the following command:**

    ```bash
    java -jar DBeaverPasswordRecovery.jar
    ```

### Using the GUI

1.  **Launch the Application:** Execute the `java -jar DBeaverPasswordRecovery.jar` command. The GUI window will appear.
2.  **Credentials File:**
    *   **Automatic Detection:** Upon startup, the application will attempt to locate the default `credentials-config.json` file for your operating system. If found, its path will be automatically filled in the "Credentials File:" text field.
    *   **Manual Selection:** If the path is not detected, or if you wish to use a file from a different location:
        *   Click the "**Browse...**" button.
        *   A file dialog will open. Navigate to and select your `credentials-config.json` file.
        *   Click "Open." The path will appear in the text field.
3.  **Decrypt Credentials:**
    *   Once a file path is shown in the text field, click the "**Decrypt**" button.
4.  **View Results:**
    *   **Success:** If decryption is successful, the decrypted data (usually in JSON format) will appear in the main text area. The status bar at the bottom will indicate "Decryption successful."
    *   **Failure:** If an error occurs (e.g., file not found, incorrect file format, decryption error), an error message will be displayed in the main text area, and the status bar will show "Decryption failed."
5.  **Status Bar:** The label at the bottom of the window provides feedback on the application's status (e.g., "Ready," "File selected," "Decrypting...").

---

## Command-Line Interface (CLI)

This section details the original command-line usage.

### Features (CLI)

- Decrypts DBeaver stored credentials using AES/CBC/PKCS5Padding encryption
- Supports automatic detection of credentials file location on Linux systems (original version)
- Command-line interface for easy integration with scripts

### Requirements (CLI)

- Java Runtime Environment (JRE) 8 or higher
- DBeaver credentials configuration file (credentials-config.json)

### Compilation (CLI)

To compile the original CLI program:

```bash
javac GetPassDBeaver.java
```

### Usage (CLI)

```bash
java GetPassDBeaver [path_to_credentials_file]
```

### Arguments (CLI)

- `path_to_credentials_file`: (Optional) Path to the DBeaver credentials configuration file. If not provided, the program will attempt to locate the file automatically on Linux systems.

### Default Credentials File Locations

*(These locations are used by both the GUI for auto-detection and the CLI's original Linux auto-detection)*
- Linux: `~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`
- Windows: `%APPDATA%\DBeaverData\workspace6\General\.dbeaver\credentials-config.json` (Note: The GUI uses this path; the original CLI might have used `.dbeaver4` as per an earlier note, but `workspace6` is now consistently used).
- macOS: `~/Library/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`

### Example (CLI)

```bash
java GetPassDBeaver ~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json
```

---

## General Information

### Security Notice

This tool is intended for legitimate system administration and recovery purposes only. Always ensure you have proper authorization before accessing or decrypting database credentials.

### Technical Details

The tool uses AES encryption in CBC mode with PKCS5 padding to decrypt the stored credentials. The encryption key (`LOCAL_KEY_CACHE` in the code) is part of DBeaver's scheme. The Initialization Vector (IV) is read from the beginning of the credentials file.

### Error Handling

-   **GUI:** Error messages are displayed in the main text area and the status bar.
-   **CLI:** If the credentials file cannot be found or accessed, the program will display usage instructions and an example command.

### Compilation and Packaging (for GUI JAR)

If you have the source code (`GetPassDBeaver.java` and `DBeaverRecoveryGUI.java`):

1.  **Create `manifest.txt`:**
    This file tells Java where the main method for the JAR is. Create a file named `manifest.txt` with the following content (ensure there's a newline after the line):
    ```
    Main-Class: DBeaverRecoveryGUI
    ```

2.  **Compile:**
    ```bash
    javac GetPassDBeaver.java DBeaverRecoveryGUI.java
    ```

3.  **Package into Executable JAR:**
    ```bash
    jar cfm DBeaverPasswordRecovery.jar manifest.txt DBeaverRecoveryGUI.class GetPassDBeaver.class
    ```
    This will create `DBeaverPasswordRecovery.jar`, which you can then run.

## ORIGINAL CODE: https://github.com/regis-amaral/GetPassDBeaver
