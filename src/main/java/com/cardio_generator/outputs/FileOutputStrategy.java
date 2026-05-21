package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements OutputStrategy by writing patient health data to label-specific text files.
 *
 * Each distinct data label (e.g., "HeartRate", "Saturation") is mapped to its own file inside
 * the configured base directory. Files are created on first write and subsequent records are
 * appended. The mapping from label to file path is cached in a thread-safe hashmap
 * so that path resolution happens only once per label.
 */
// class name from fileOutputStrategy to FileOutputStrategy class names must useUpperCamelCase 
public class FileOutputStrategy implements OutputStrategy {

    // field name from 'BaseDirectory' to baseDirectory non-constant field names must use lowerCamelCase 
    private String baseDirectory;

    // field name from 'file_map' to 'fileMap', field names must use lowerCamelCase
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructs a FileOutputStrategy that writes output files into the given directory.
     *
     * @param baseDirectory the path to the directory where label-specific output files will be
     *                      created, the directory is created automatically on first write if absent
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes a single patient data record to the file associated with the given label.
     * @param patientId the unique identifier of the patient whose data is being recorded
     * @param timestamp the time of the measurement in milliseconds since the Unix epoch
     * @param label     the category of health data (e.g., "HeartRate", "BloodPressure")
     * @param data      the measured value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // local variable name from FilePath to filePath, use lowecase
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
