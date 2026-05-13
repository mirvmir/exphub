package io.github.mirvmir.media.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class RangedFileInputStream extends InputStream {

    private final InputStream delegate;
    private long remaining;

    public RangedFileInputStream(Path path, long start, long end) throws IOException {
        this.delegate = Files.newInputStream(path);
        this.delegate.skip(start);
        this.remaining = end - start + 1;
    }

    @Override
    public int read() throws IOException {
        if (remaining <= 0) {
            return -1;
        }

        int value = delegate.read();

        if (value != -1) {
            remaining--;
        }

        return value;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (remaining <= 0) {
            return -1;
        }

        int bytesToRead = (int) Math.min(length, remaining);
        int read = delegate.read(buffer, offset, bytesToRead);

        if (read != -1) {
            remaining -= read;
        }

        return read;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}