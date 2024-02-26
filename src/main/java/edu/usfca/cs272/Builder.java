package edu.usfca.cs272;

import org.eclipse.jetty.util.IO;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Builder {
    private final Path input;
    private final Path indexOutput;
    private final Path countsOutput;

    private final InvertedIndex index;
    public Builder(Path input, Path countsOutput, Path indexOutput, InvertedIndex index) {
        this.input = input;
        this.countsOutput = countsOutput;
        this.indexOutput = indexOutput;
        this.index = index;
    }

    public void readDirectory(Path directory) throws Exception {
        try (DirectoryStream<Path> listing = Files.newDirectoryStream(directory)) {
            for (Path path: listing) {
                if (Files.isDirectory(path)) {
                    readDirectory(path);
                } else {
                    if (fileIsTXT(path)) {
                        readFile(path);
                        writeOutput();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readDirectory() throws Exception {
        this.readDirectory(input);
    }

    public void readFile(Path file) throws Exception {
        ArrayList<String> stems;
        stems = FileStemmer.listStems(file);
        for (int i = 0; i < stems.size(); i++) {
            this.index.add(file, stems.get(i), i);
        }
        if (!stems.isEmpty())
            this.index.addCounts(file, stems.size());
        writeOutput();
    }

    private boolean fileIsTXT(Path path) {
        return path.toString().toLowerCase().endsWith(".txt")
                || path.toString().toLowerCase().endsWith(".text");
    }

    public void writeOutput() {
        if (countsOutput != null) {
            try {
                JsonWriter.writeObject(this.index.getCounts(), countsOutput);
            } catch (IOException e) {
                System.out.printf("Unable to build counts with path: %s\n", countsOutput);
            }
        }
        if (indexOutput != null) {
            try {
                JsonWriter.writeIndex(this.index, indexOutput);
            } catch (IOException e) {
                System.out.printf("Unable to build index with path: %s\n", indexOutput);
            }
        }
    }
}
