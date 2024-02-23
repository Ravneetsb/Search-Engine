package edu.usfca.cs272;

import org.checkerframework.checker.units.qual.A;

import java.io.BufferedReader;
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

    public void readDirectory() {
        try (DirectoryStream<Path> listing = Files.newDirectoryStream(input)) {
            for (Path path: listing) {
                if (Files.isDirectory(path)) {
                    readDirectory();
                } else {
                    if (fileIsTXT(path)) {
                        readFile();
                        try {
                            JsonWriter.writeObject(index.getCounts(), countsOutput);
                        } catch (IOException e) {
                            System.out.printf("Unable to build counts from path: %s\n", countsOutput);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readFile() {
        try (BufferedReader br = Files.newBufferedReader(input)) {
            String text;
            int iter = 0;
            while ((text = br.readLine()) != null) {
                if (!text.isEmpty()) {
                    iter = index.index(input, text, iter);
                }
            }
        } catch (IOException e) {
            System.out.printf("Unable to load data from path: %s\n", input);
        }
    }

    private boolean fileIsTXT(Path path) {
        return path.toString().toLowerCase().endsWith(".txt")
                || path.toString().toLowerCase().endsWith(".text");
    }

    private boolean index(String text, int arrSize) {
        ArrayList<String> stems = FileStemmer.listStems(text);
        int nextVal = 0;
        for (int i = 0; i < stems.size(); i++) {
            String stem = stems.get(i);
            this.index.add(, stem, i);
        }
    }
}
