package com.data_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads patient health data from text files produced by FileOutputStrategy and loads
 * every record into the provided DataStorage.
 *
 * Expected line format (one record per line):
 *   Patient ID: id, Timestamp: ts, Label: label, Data: value
 *
 * Numeric values are parsed directly. Saturation percentages (e.g. "97.0%") have the
 * trailing "%" stripped before conversion. The string tokens "triggered" and "resolved"
 * from the Alert generator are mapped to 1.0 and 0.0 respectively so they can be stored
 * as a double measurement value.
 */
public class FileDataReader implements DataReader {

    private final String outputDirectory;

    /**
     * Constructs a FileDataReader that will read from the given directory.
     *
     * @param outputDirectory path to the directory containing the simulator output files
     */
    public FileDataReader(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Reads all .txt files in the configured directory and stores each parsed record in
     * the provided DataStorage. Files that cannot be read are skipped with an error message.
     *
     * @param dataStorage the storage instance that will receive the parsed records
     * @throws IOException if the configured directory does not exist or is not a directory
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dirPath = Paths.get(outputDirectory);
        if (!Files.isDirectory(dirPath)) {
            throw new IOException("Output directory not found: " + outputDirectory);
        }
        Files.list(dirPath)
            .filter(p -> p.toString().endsWith(".txt"))
            .forEach(p -> {
                try {
                    readFile(p, dataStorage);
                } catch (IOException e) {
                    System.err.println("Error reading " + p + ": " + e.getMessage());
                }
            });
    }

    private void readFile(Path filePath, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    parseLine(line, dataStorage);
                }
            }
        }
    }

    /**
     * Parses a single line and adds the resulting record to storage.
     * Malformed lines are skipped with a warning message.
     *
     * @param line        the raw text line to parse
     * @param dataStorage the storage to receive the record
     */
    void parseLine(String line, DataStorage dataStorage) {
        try {
            // Format: "Patient ID: 1, Timestamp: 123456, Label: HeartRate, Data: 72.0"
            // Split on ", " with a limit of 4 to keep any commas inside the Data field intact.
            String[] parts = line.split(", ", 4);
            if (parts.length < 4) {
                System.err.println("Skipping malformed line: " + line);
                return;
            }
            int patientId    = Integer.parseInt(parts[0].replace("Patient ID: ", "").trim());
            long timestamp   = Long.parseLong(parts[1].replace("Timestamp: ", "").trim());
            String label     = parts[2].replace("Label: ", "").trim();
            String dataStr   = parts[3].replace("Data: ", "").trim();

            double value = parseValue(dataStr);
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (Exception e) {
            System.err.println("Failed to parse line: " + line);
        }
    }

    /**
     * Converts a raw data string to a double.
     *
     * @param dataStr the raw value string (e.g. "72.0", "97.0%", "triggered", "resolved")
     * @return the numeric representation of the value
     */
    private double parseValue(String dataStr) {
        if (dataStr.endsWith("%")) {
            return Double.parseDouble(dataStr.substring(0, dataStr.length() - 1));
        }
        if ("triggered".equals(dataStr)) return 1.0;
        if ("resolved".equals(dataStr))  return 0.0;
        try {
            return Double.parseDouble(dataStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
