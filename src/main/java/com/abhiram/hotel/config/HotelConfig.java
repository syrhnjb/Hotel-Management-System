package com.abhiram.hotel.config;

import java.nio.file.Path;

/**
 * Application configuration. Only the storage file location is externally configurable (via the
 * {@code HOTEL_STORAGE_FILE} environment variable)--everything else (room counts, prices, menu)
 * is genuine, stable business data and lives in {@link com.abhiram.hotel.domain.RoomType} and
 * {@link com.abhiram.hotel.domain.MenuItem} rather than being pulled out into configuration for
 * its own sake.
 */
public final class HotelConfig {

    private static final String STORAGE_FILE_ENV_VAR = "HOTEL_STORAGE_FILE";
    private static final String DEFAULT_STORAGE_FILE = "hotel-state.txt";

    private final Path storageFile;

    private HotelConfig(Path storageFile) {
        this.storageFile = storageFile;
    }

    public static HotelConfig fromEnvironment() {
        String override = System.getenv(STORAGE_FILE_ENV_VAR);
        Path path = (override != null && !override.isBlank())
                ? Path.of(override)
                : Path.of(DEFAULT_STORAGE_FILE);
        return new HotelConfig(path);
    }

    public Path storageFile() {
        return storageFile;
    }
}
