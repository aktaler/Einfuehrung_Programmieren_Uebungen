// Florian Böhme 23.01.2024
//TODO write the program so it can accept CLI arguments

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.System.exit;

public class cryptography {
    // current working directory is the repo root directory
    private static final String ASSET_PATH = "Modul Komplexe Datentypen\\Übungsaufgabe Verschlüsselung\\Java\\Uebungsaufgabe_Verschluesselung_FB\\assets";
    private static final Predicate<Path> TXT_FILE_FILTER = path -> path.toString().endsWith(".txt");

    private enum STATE_TAGS {ENCRYPT, DECRYPT}

    private static STATE_TAGS currentState;

    public static void main(String[] args) throws IOException {
        // uncomment this to check for the current working directory
        /*String currentDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDirectory);*/

        //First talk to the user via console
        Scanner scanner = new Scanner(System.in);

        System.out.println("#".repeat(60));
        System.out.print("""
                Welcome to your cryptography service. This service is only able to process .txt-Files located in the assets folder.
                It will make a copy of all .txt files inside the assets folder and apply your chosen method to it.
                The program will handle files with their name ending with "_encrypted" as encrypted files. Everything else
                as unencrypted.
                Do you wish to encrypt or decrypt? (e/d)                
                """);
        System.out.println("#".repeat(60));

        // fetch the input and fetch the files accordingly
        String input = scanner.next();

        Map<String, String> fileContents = null;

        if (input.equalsIgnoreCase("e")) {
            currentState = STATE_TAGS.ENCRYPT;
            fileContents = readTxtFiles(fetchEncryptFiles());
        } else if (input.equalsIgnoreCase("d")) {
            currentState = STATE_TAGS.DECRYPT;
            fileContents = readTxtFiles(fetchDecryptFiles());
        } else {
            System.out.println("#".repeat(60));
            System.out.print("""
                    Your input does not match the intended format. Better luck next time!              
                    """);
            System.out.println("#".repeat(60));
            exit(666);
        }

        //Talk to user again about the encryption key
        System.out.println("#".repeat(60));
        System.out.print("""
                Great! You have chosen to {{state}}!
                Please insert you encryption key. Currently the service accepts only single integer values
                ranging from 32-255.
                """.replace("{{state}}", String.valueOf(currentState)));
        System.out.println("#".repeat(60));

        // fetch the input from the user
        int cryptKey = 0;
        try {
            cryptKey = Integer.parseInt(scanner.next());
        } catch (NumberFormatException e) {
            System.out.println("You picked the wrong format! Better luck next time!");
            exit(666);
        }

        //negate the key if we want to decrypt for easier traversing over the ascii table
        if (currentState == STATE_TAGS.DECRYPT) {
            cryptKey = -cryptKey;
        }

        // iterate over the files to perform encryption and writeback
        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            transformFileContent(entry.getKey(), entry.getValue(), cryptKey);
        }

        System.out.println("\u001B[32m" + "Process finished!");
    }

    private static void transformFileContent(String fileName, String fileContent, int cryptKey) {
        String enctyptedContent = "";
        //sloppy casting and string concatenation but should work
        for (char character : fileContent.toCharArray()) {
            int rep = character;
            rep = rep + cryptKey;
            enctyptedContent += (char) (rep);
        }

        String newFileName = null;

        // slice the fileName according to the selected method, then add the right suffix to it
        switch (currentState) {
            case ENCRYPT -> {
                newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_encrypted.txt";
            }
            case DECRYPT -> {
                newFileName = fileName.substring(0, fileName.lastIndexOf("_")) + "_decrypted.txt";
            }
        }

        try {
            File file = new File(ASSET_PATH, newFileName);
            FileWriter writer = new FileWriter(file);
            writer.write(enctyptedContent);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        System.out.print("""
                \u001B[33m{{state}} was successfull. {{fileName}} was created inside the assets folder.
                """.replace("{{state}}", String.valueOf(currentState)).replace("{{fileName}}", newFileName));
    }

    private static void letsDecrypt(String fileName, String fileContent, int cryptKey) {
    }

    /**
     * Fetches the files inside the asset directory. Filters the file list for file names ending with _encrypted.
     *
     * @return List<Path>
     * @throws IOException
     */
    private static List<Path> fetchEncryptFiles() throws IOException {
        List<Path> files;

        files = fetchTxtFiles(ASSET_PATH);
        // remove files which were created by this program
        files.removeIf(path -> path.toString().endsWith("_encrypted.txt"));
        files.removeIf(path -> path.toString().endsWith("_decrypted.txt"));

        if (files.isEmpty()) {
            throw new IOException("no matching files found");
        }

        return files;
    }

    /**
     * Fetches the files inside the asset directory. Filters the file list for file names ending with _encrypted.
     *
     * @return List<Path>
     * @throws IOException
     */
    private static List<Path> fetchDecryptFiles() throws IOException {
        List<Path> files;

        files = fetchTxtFiles(ASSET_PATH);
        // remove files which were not created by this programs encryption service
        files.removeIf(path -> !path.toString().endsWith("_encrypted.txt"));

        if (files.isEmpty()) {
            throw new IOException("no matching files found");
        }

        return files;
    }

    /**
     * Returns a list of .txt-files in a given directory path.
     *
     * @param directoryPath
     * @return List<Path>
     * @throws IOException
     */
    public static List<Path> fetchTxtFiles(String directoryPath) throws IOException {

        if (!Files.isDirectory(Paths.get(directoryPath))) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        List<Path> files = new ArrayList<>();

        Files.list(Paths.get(directoryPath)).forEach(file -> {
            if (Files.isRegularFile(file) && TXT_FILE_FILTER.test(file)) {
                files.add(file);
            }
        });

        return files;
    }

    /**
     * Returns a list of Strings, each String resembling the contents of one .txt File.
     *
     * @param txtFiles
     * @return List<String>
     * @throws IOException
     */
    public static Map<String, String> readTxtFiles(List<Path> txtFiles) throws IOException {
        Map<String, String> contents = new HashMap<>();
        for (Path file : txtFiles) {
            contents.put(file.getFileName().toString(), Files.readString(file));
        }
        return contents;
    }
}
