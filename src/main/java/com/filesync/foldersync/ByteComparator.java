package com.filesync.foldersync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ByteComparator {

    public boolean areFilesIdentical(File file1, File file2) {
        try {
            if (file1.length() != file2.length()) {
                return false;
            }
            return Files.mismatch(file1.toPath(), file2.toPath()) == -1L;
        } catch (IOException e) {
            System.err.println("Error comparing file contents: " + e.getMessage());
            return false;
        }
    }
}