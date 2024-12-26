package com.filesync.foldersync;

import javafx.concurrent.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FolderComparatorTask extends Task<List<String>> {

    private final String folderPath1;
    private final String folderPath2;
    private final ByteComparator byteComparator = new ByteComparator();

    public FolderComparatorTask(String folderPath1, String folderPath2) {
        this.folderPath1 = folderPath1;
        this.folderPath2 = folderPath2;
    }

    @Override
    protected List<String> call() throws Exception {
        List<String> differences = new ArrayList<>();
        
        File folder1 = new File(folderPath1);
        File folder2 = new File(folderPath2);
        
        if (!folder1.isDirectory() || !folder2.isDirectory()) {
            throw new Exception("One or both paths are not valid directories.");
        }
        
        long totalFiles = countFiles(folder1);
        long processedFiles = 0;

        try {
            ComparisonResult result = compareAndMirrorDirectoryContents(folder1, folder2, differences, "", totalFiles, 0);
            processedFiles = result.getProcessedFiles();
            differences = result.getDifferences();
            System.out.println("Processed completed");
        } catch (Exception e) {
            differences.add("Error processing files: " + e.getMessage());
            throw e;
        }
        
        if (totalFiles != processedFiles) {
            differences.add("Error processing files.");
            throw new Exception("Error processing files.");
        } else {
            differences.add("Comparison completed.");
            System.out.println("Comparison completed");
        }

        // Check if all elements in differences are of type String
        for (Object difference : differences) {
            if (!(difference instanceof String)) {
                throw new Exception("Differences list contains non-string elements.");
            }
        }

        // Create the folderPath2 directory if it doesn't exist (if needed)
        File outputDir = new File(folderPath2);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Write differences to a text file in folderPath2
        File outputFile = new File(outputDir, "differences.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String difference : differences) {
                writer.write(difference);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new Exception("Error writing differences to file: " + e.getMessage());
        }
        
        return differences;
    }

    private long countFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }
        long count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else {
                count++;
            }
        }
        return count;
    }

    private ComparisonResult compareAndMirrorDirectoryContents(File folder1, File folder2, List<String> differences, String relativePath, long totalFiles, long processedFiles) throws IOException {
        File[] files1 = folder1.listFiles();
        File[] files2 = folder2.listFiles();

        if (files1 == null || files2 == null) {
            differences.add("Error reading directories.");
            return new ComparisonResult(processedFiles, differences);
        }

        // Compare files and directories in folder1 with folder2
        for (File file1 : files1) {
            File file2 = new File(folder2, file1.getName());
            String currentRelativePath = relativePath + "/" + file1.getName();

            if (file1.isDirectory()) {
                if (!file2.exists()) {
                    differences.add("Directory missing in folder2: " + currentRelativePath);
                    file2.mkdirs();
                    differences.add("Directory created in folder2: " + currentRelativePath);
                }
                ComparisonResult result = compareAndMirrorDirectoryContents(file1, file2, differences, currentRelativePath, totalFiles, processedFiles);
                processedFiles = result.getProcessedFiles();
                differences = result.getDifferences();
            } else {
                if (!file2.exists()) {
                    differences.add("File missing in folder2: " + currentRelativePath);
                    try {
                        Files.copy(file1.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        differences.add("File copied to folder2: " + currentRelativePath);
                    } catch (IOException e) {
                        differences.add("Error copying file " + currentRelativePath + ": " + e.getMessage());
                        throw e;
                    }
                } else if (!byteComparator.areFilesIdentical(file1, file2)) {
                    differences.add("File differs: " + currentRelativePath);
                    try {
                        Files.copy(file1.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        differences.add("File updated in folder2: " + currentRelativePath);
                    } catch (IOException e) {
                        differences.add("Error updating file " + currentRelativePath + ": " + e.getMessage());
                        throw e;
                    }
                }
                processedFiles++;
                System.out.println("Processed files: " + processedFiles + "/" + totalFiles);
                updateProgress(processedFiles, totalFiles);
            }
        }
        System.out.println("Exited loop");
        return new ComparisonResult(processedFiles, differences);
    }
}