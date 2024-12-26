package com.filesync.foldersync;

import java.util.List;

public class ComparisonResult {
    private long processedFiles;
    private List<String> differences;

    public ComparisonResult(long processedFiles, List<String> differences) {
        this.processedFiles = processedFiles;
        this.differences = differences;
    }

    public long getProcessedFiles() {
        return processedFiles;
    }

    public List<String> getDifferences() {
        return differences;
    }
}