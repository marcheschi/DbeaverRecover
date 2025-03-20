# DBeaver Password Recovery Tool

This Java utility allows you to decrypt stored database credentials from DBeaver's configuration files. It's particularly useful for recovering forgotten database passwords or for system administrators who need to manage DBeaver connections.

## Features

- Decrypts DBeaver stored credentials using AES/CBC/PKCS5Padding encryption
- Supports automatic detection of credentials file location on Linux systems
- Command-line interface for easy integration with scripts

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- DBeaver credentials configuration file (credentials-config.json)

## Compilation

To compile the program:

```bash
javac GetPassDBeaver.java
```

## Usage

```bash
java GetPassDBeaver [path_to_credentials_file]
```

### Arguments

- `path_to_credentials_file`: (Optional) Path to the DBeaver credentials configuration file. If not provided, the program will attempt to locate the file automatically on Linux systems.

### Default Credentials File Locations

- Linux: `~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`
- Windows: `%APPDATA%\.dbeaver4\credentials-config.json`
- macOS: `~/Library/DBeaverData/workspace6/General/.dbeaver/credentials-config.json`

## Example

```bash
java GetPassDBeaver ~/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json
```

## Security Notice

This tool is intended for legitimate system administration and recovery purposes only. Always ensure you have proper authorization before accessing or decrypting database credentials.

## Technical Details

The tool uses AES encryption in CBC mode with PKCS5 padding to decrypt the stored credentials. The encryption key is derived from system-specific information to match DBeaver's encryption scheme.

## Error Handling

If the credentials file cannot be found or accessed, the program will display usage instructions and an example command.

## ORIGINAL CODE: https://github.com/regis-amaral/GetPassDBeaver
