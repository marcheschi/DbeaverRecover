# DBeaver Password Recovery Tool

This Java utility allows you to decrypt stored database credentials from DBeaver's configuration files. It's particularly useful for recovering forgotten database passwords or for system administrators who need to manage DBeaver connections.

## Features

- Decrypts DBeaver stored credentials using AES/CBC/PKCS5Padding encryption.
- **Modern Graphical User Interface (GUI)** with an intuitive table view displaying Connection Name, User Name, and Password.
- **Auto-select encrypted file**: Automatically detects and selects the `credentials-config.json` file based on your operating system (Windows, Linux, or macOS).
- **Export to CSV**: Save all decrypted credentials to a CSV file for easy backup or migration.
- **Beautiful UI**: Styled buttons with hover effects, color-coded header, sortable table columns, and modern look-and-feel.
- Supports both old and new DBeaver configuration formats.

## Requirements

- Java Development Kit (JDK) 8 or higher
- Apache Maven
- DBeaver credentials configuration file (`credentials-config.json`)

## Build

To build the application and create an executable JAR file, run the following Maven command:

```bash
mvn package
```
This will generate the `getpassdbeaver-1.0-SNAPSHOT.jar` file in the `target` directory.

## Running the Application

There are two ways to run the application:

### Using Maven

You can run the application directly using the Maven `exec` plugin:

```bash
mvn exec:java
```

### Using the JAR file

Alternatively, you can run the generated JAR file from the `target` directory:

```bash
java -jar target/getpassdbeaver-1.0-SNAPSHOT.jar
```

Once the application is running, you have three options:

1. **Auto-Select**: Click the green "Auto-Select Encrypted File" button to automatically locate and decrypt the credentials file based on your operating system.

2. **Manual Selection**: Click the blue "Open credentials-config.json" button and select your DBeaver `credentials-config.json` file to view the decrypted content.

3. **Export to CSV**: After decryption, click the orange "Export to CSV" button to save all connection credentials to a CSV file.

The main window displays a clean table with three columns:
- **Connection Name**: The name of the database connection
- **User Name**: The username used for the connection
- **Password**: The decrypted password

You can click on column headers to sort the table.

### Default Credentials File Locations

The auto-select feature searches for the credentials file in these default locations:

- **Linux**: 
  - `~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`
  - `~/.dbeaver/workspace6/General/.dbeaver/credentials-config.json`
  
- **Windows**: 
  - `%APPDATA%\DBeaverData\workspace6\General\.dbeaver\credentials-config.json`
  - `%APPDATA%\DBeaver\workspace6\General\.dbeaver\credentials-config.json`
  
- **macOS**: 
  - `~/Library/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`
  - `~/Library/Application Support/DBeaver/workspace6/General/.dbeaver/credentials-config.json`

If the file is not found in standard locations, the tool performs a recursive search up to 10 levels deep from your home directory.

## Security Notice

This tool is intended for legitimate system administration and recovery purposes only. Always ensure you have proper authorization before accessing or decrypting database credentials.

**Warning**: The exported CSV file contains plaintext passwords. Store it securely and delete it when no longer needed.

## Technical Details

The tool uses AES encryption in CBC mode with PKCS5 padding to decrypt the stored credentials. The encryption key is derived from system-specific information to match DBeaver's encryption scheme.

The GUI automatically merges information from both `credentials-config.json` (containing encrypted passwords) and `data-sources.json` (containing connection metadata) to provide complete connection details.

## Error Handling

If the credentials file cannot be found or accessed, the program will display a user-friendly dialog with instructions.

## ORIGINAL CODE: https://github.com/regis-amaral/GetPassDBeaver
