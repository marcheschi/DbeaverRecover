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

public class GetPassDBeaver {

    private static final byte[] LOCAL_KEY_CACHE = new byte[] { -70, -69, 74, -97, 119, 74, -72, 83, -55, 108, 45, 101, 61, -2, 84, 74 };

    /*
    public static void main(String[] args) {
        String pathFileString = "";
        Path pathFile = null;
        String OS = System.getProperty("os.name").toLowerCase();
        if (args.length == 1) {
            pathFile = Paths.get(args[0]);
        }else{
            if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0){
                pathFileString = "/home/" + System.getProperty("user.name") + "/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json";
                pathFile = Paths.get(pathFileString);
            }
        }

        try{
            System.out.println(decryptCredentialsFromFile(pathFile.toString()));
            // System.exit(1); // Removed
        }catch(Exception e){
            //e.printStackTrace();
            System.err.println("Error processing file: " + e.getMessage());
        }
        // finally block removed as it's not suitable for a library method
    }
    */

    public static String decryptCredentialsFromFile(String filePath) throws Exception {
        byte[] contents = Files.readAllBytes(Paths.get(filePath));
        return decrypt(contents);
    }

    public static String getDefaultCredentialsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String pathString = null;

        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            pathString = userHome + "/.local/share/DBeaverData/workspace6/General/.dbeaver/credentials-config.json";
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                pathString = appData + "/DBeaverData/workspace6/General/.dbeaver/credentials-config.json";
            }
        } else if (os.contains("mac")) {
            pathString = userHome + "/Library/DBeaverData/workspace6/General/.dbeaver/credentials-config.json";
        }

        if (pathString != null && Paths.get(pathString).toFile().exists()) {
            return pathString;
        }
        return null;
    }

    // showData method removed as its functionality is now in decryptCredentialsFromFile

    private static String inputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static String decrypt(byte[] contents) throws InvalidAlgorithmParameterException, InvalidKeyException,
            IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        // Ensure this method is private if it's a helper, or public if called directly elsewhere.
        // For now, keeping it as it was, but typically it would be private if showData was the public entry point.
        // With decryptCredentialsFromFile being the new public entry, this can remain private.
        try (InputStream byteStream = new ByteArrayInputStream(contents)) {
            byte[] fileIv = new byte[16];
            int bytesRead = byteStream.read(fileIv);
            if (bytesRead < 16) {
                throw new IOException("Could not read IV from file");
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aes = new SecretKeySpec(LOCAL_KEY_CACHE, "AES");
            cipher.init(Cipher.DECRYPT_MODE, aes, new IvParameterSpec(fileIv));
            try (CipherInputStream cipherIn = new CipherInputStream(byteStream, cipher)) {
                return inputStreamToString(cipherIn);
            }
        }
    }

}
