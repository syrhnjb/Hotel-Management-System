package com.abhiram.hotel.config;

import java.nio.file.Path;

/**
 * Application configuration. Only the storage file and invoice-history file locations are
 * externally configurable (via the {@code HOTEL_STORAGE_FILE} and {@code HOTEL_HISTORY_FILE}
 * environment variables)--everything else (room counts, prices, menu) is genuine, stable business
 * data and lives in {@link com.abhiram.hotel.domain.RoomType} and
 * {@link com.abhiram.hotel.domain.MenuItem} rather than being pulled out into configuration for
 * its own sake.
 */
public final class HotelConfig {

    private static final String STORAGE_FILE_ENV_VAR = "HOTEL_STORAGE_FILE";
    private static final String HISTORY_FILE_ENV_VAR = "HOTEL_HISTORY_FILE";
    private static final String DEFAULT_STORAGE_FILE = "hotel-state.txt";
    private static final String DEFAULT_HISTORY_FILE = "hotel-history.txt";

    private final Path storageFile;
    private final Path historyFile;

    private HotelConfig(Path storageFile, Path historyFile) {
        this.storageFile = storageFile;
        this.historyFile = historyFile;
    }

    public static HotelConfig fromEnvironment() {
        String storageOverride = System.getenv(STORAGE_FILE_ENV_VAR);
        String historyOverride = System.getenv(HISTORY_FILE_ENV_VAR);
        Path storagePath = (storageOverride != null && !storageOverride.isBlank())
                ? Path.of(storageOverride)
                : Path.of(DEFAULT_STORAGE_FILE);
        Path historyPath = (historyOverride != null && !historyOverride.isBlank())
                ? Path.of(historyOverride)
                : Path.of(DEFAULT_HISTORY_FILE);
        return new HotelConfig(storagePath, historyPath);
    }

    public Path storageFile() {
        return storageFile;
    }

    public Path historyFile() {
        return historyFile;
    }
}