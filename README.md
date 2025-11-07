# DBeaver Password Recovery Tool

This Java utility allows you to decrypt stored database credentials from DBeaver's configuration files. It's particularly useful for recovering forgotten database passwords or for system administrators who need to manage DBeaver connections.

## Features

- Decrypts DBeaver stored credentials using AES/CBC/PKCS5Padding encryption
- Graphical User Interface (GUI) for an intuitive user experience.
- Decrypts DBeaver stored credentials using AES/CBC/PKCS5Padding encryption.

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

Once the application is running, click the "Open credentials-config.json" button and select your DBeaver `credentials-config.json` file to view the decrypted content.

### Default Credentials File Locations

- Linux: `~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`
- Windows: `%APDATA%\DBeaverData\workspace6\General\.dbeaver\credentials-config.json`
- macOS: `~/Library/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`

## Security Notice

This tool is intended for legitimate system administration and recovery purposes only. Always ensure you have proper authorization before accessing or decrypting database credentials.

## Technical Details

The tool uses AES encryption in CBC mode with PKCS5 padding to decrypt the stored credentials. The encryption key is derived from system-specific information to match DBeaver's encryption scheme.

## Error Handling

If the credentials file cannot be found or accessed, the program will display usage instructions and an example command.

## ORIGINAL CODE: https://github.com/regis-amaral/GetPassDBeaver
