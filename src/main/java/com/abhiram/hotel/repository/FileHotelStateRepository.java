package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.PersistenceException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based {@link HotelStateRepository} using a simple, human-readable, line-oriented text
 * format--deliberately <em>not</em> Java's built-in object serialization.
 *
 * <p>The original application persisted its entire object graph with
 * {@code ObjectOutputStream}/{@code ObjectInputStream}. Native Java deserialization of
 * data from disk is a well-known security risk (a corrupted or tampered file can trigger
 * arbitrary code execution via gadget chains in the classpath), so this implementation instead
 * writes one plain-text line per active booking and parses it back field by field. A malformed
 * or tampered file can, at worst, cause a {@link PersistenceException}--never arbitrary
 * deserialization.
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>Only <em>active</em> (not yet checked-out) bookings are persisted; historical/checked-out
 *       bookings and past invoices are not retained. That matches the original application's
 *       scope, which had no booking history either.</li>
 *   <li>Guest names and menu item names must not contain the field/line delimiters ({@code |},
 *       {@code ;}, {@code ,}, {@code :}, or a newline); {@link #saveActiveBookings} rejects such
 *       input rather than silently corrupting the file.</li>
 *   <li>Writes are synchronous and whole-file (read-modify-write), which is appropriate for a
 *       single-user console application but would not scale to concurrent writers.</li>
 * </ul>
 */
public final class FileHotelStateRepository implements HotelStateRepository {

    private static final String FIELD_DELIMITER = "|";
    private static final String FIELD_DELIMITER_REGEX = "\\|";
    private static final String GUEST_DELIMITER = ";";
    private static final String LINE_ITEM_DELIMITER = ",";
    private static final String ITEM_QTY_DELIMITER = ":";

    private final Path storageFile;

    public FileHotelStateRepository(Path storageFile) {
        this.storageFile = storageFile;
    }

    @Override
    public void saveActiveBookings(List<Booking> activeBookings) {
        try {
            List<String> lines = new ArrayList<>();
            for (Booking booking : activeBookings) {
                lines.add(toLine(booking));
            }
            Path parent = storageFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(storageFile, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to save hotel state to " + storageFile, e);
        }
    }

    @Override
    public List<Booking> loadActiveBookings() {
        List<Booking> bookings = new ArrayList<>();
        if (!Files.exists(storageFile)) {
            return bookings;
        }
        try (BufferedReader reader = Files.newBufferedReader(storageFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                bookings.add(fromLine(line));
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to load hotel state from " + storageFile, e);
        }
        return bookings;
    }

    private String toLine(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append(booking.roomNumber()).append(FIELD_DELIMITER);
        List<String> guestFields = new ArrayList<>();
        for (Guest guest : booking.guests()) {
            requireSafe(guest.name(), "Guest name");
            requireSafe(guest.gender(), "Guest gender");
            guestFields.add(guest.name() + GUEST_DELIMITER + guest.contactNumber().value()
                    + GUEST_DELIMITER + guest.gender());
        }
        sb.append(String.join(FIELD_DELIMITER, guestFields));
        sb.append(FIELD_DELIMITER);
        List<String> foodFields = new ArrayList<>();
        for (FoodOrderLine foodLine : booking.foodOrder().lines()) {
            foodFields.add(foodLine.menuItem().name() + ITEM_QTY_DELIMITER + foodLine.quantity());
        }
        sb.append(String.join(LINE_ITEM_DELIMITER, foodFields));
        return sb.toString();
    }

    private void requireSafe(String value, String fieldName) {
        if (value.contains(FIELD_DELIMITER) || value.contains(GUEST_DELIMITER)
                || value.contains(LINE_ITEM_DELIMITER) || value.contains(ITEM_QTY_DELIMITER)
                || value.contains("\n")) {
            throw new PersistenceException(
                    fieldName + " contains a character reserved by the storage format: " + value);
        }
    }

    private Booking fromLine(String line) {
        try {
            String[] fields = line.split(FIELD_DELIMITER_REGEX, -1);
            int roomNumber = Integer.parseInt(fields[0]);
            RoomType roomType = RoomType.forRoomNumber(roomNumber);

            List<Guest> guests = new ArrayList<>();
            int guestFieldCount = roomType.occupancy();
            for (int i = 0; i < guestFieldCount; i++) {
                String[] guestParts = fields[1 + i].split(GUEST_DELIMITER, -1);
                guests.add(new Guest(guestParts[0], ContactNumber.of(guestParts[1]), guestParts[2]));
            }

            Booking booking = new Booking(roomNumber, roomType, guests);

            String foodField = fields[1 + guestFieldCount];
            if (!foodField.isBlank()) {
                for (String item : foodField.split(LINE_ITEM_DELIMITER)) {
                    String[] itemParts = item.split(ITEM_QTY_DELIMITER);
                    MenuItem menuItem = MenuItem.valueOf(itemParts[0]);
                    int quantity = Integer.parseInt(itemParts[1]);
                    booking.foodOrder().addLine(new FoodOrderLine(menuItem, quantity));
                }
            }
            return booking;
        } catch (RuntimeException e) {
            throw new PersistenceException("Corrupt or unreadable state line: " + line, e);
        }
    }
}
