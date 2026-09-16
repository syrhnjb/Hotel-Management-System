package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.Money;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.PersistenceException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Append-only, line-oriented plain-text {@link InvoiceHistoryRepository}. Each line is one
 * checked-out invoice: {@code roomNumber|checkIn|checkOut|guestName|roomCharge|foodCharge|foodLines}.
 *
 * <p>History is written only ever by appending, so past invoices are never rewritten or lost--an
 * audit log rather than a mutable snapshot. A corrupted or tampered line can, at worst, raise a
 * {@link PersistenceException}; there is no object deserialization involved.
 */
public final class FileInvoiceHistoryRepository implements InvoiceHistoryRepository {

    private static final String FIELD_DELIMITER = "|";
    private static final String FIELD_DELIMITER_REGEX = "\\|";
    private static final String LINE_ITEM_DELIMITER = ",";
    private static final String ITEM_QTY_DELIMITER = ":";

    private final Path historyFile;

    public FileInvoiceHistoryRepository(Path historyFile) {
        this.historyFile = historyFile;
    }

    @Override
    public void save(Invoice invoice) {
        try {
            Path parent = historyFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(historyFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(toLine(invoice));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to save invoice history to " + historyFile, e);
        }
    }

    @Override
    public List<Invoice> loadAll() {
        List<Invoice> invoices = new ArrayList<>();
        if (!Files.exists(historyFile)) {
            return invoices;
        }
        try (BufferedReader reader = Files.newBufferedReader(historyFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                invoices.add(fromLine(line));
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to load invoice history from " + historyFile, e);
        }
        return invoices;
    }

    private String toLine(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append(invoice.roomNumber()).append(FIELD_DELIMITER);
        sb.append(invoice.checkInDate()).append(FIELD_DELIMITER);
        sb.append(invoice.checkOutDate()).append(FIELD_DELIMITER);
        requireSafe(invoice.guestName(), "Guest name");
        sb.append(invoice.guestName()).append(FIELD_DELIMITER);
        sb.append(invoice.roomCharge().asBigDecimal().toString()).append(FIELD_DELIMITER);
        sb.append(invoice.foodCharge().asBigDecimal().toString()).append(FIELD_DELIMITER);
        List<String> foodFields = new ArrayList<>();
        for (FoodOrderLine foodLine : invoice.foodLines()) {
            foodFields.add(foodLine.menuItem().name() + ITEM_QTY_DELIMITER + foodLine.quantity());
        }
        sb.append(String.join(LINE_ITEM_DELIMITER, foodFields));
        return sb.toString();
    }

    private void requireSafe(String value, String fieldName) {
        if (value.contains(FIELD_DELIMITER) || value.contains(LINE_ITEM_DELIMITER)
                || value.contains(ITEM_QTY_DELIMITER) || value.contains("\n")) {
            throw new PersistenceException(
                    fieldName + " contains a character reserved by the storage format: " + value);
        }
    }

    private Invoice fromLine(String line) {
        try {
            String[] fields = line.split(FIELD_DELIMITER_REGEX, -1);
            int roomNumber = Integer.parseInt(fields[0]);
            RoomType roomType = RoomType.forRoomNumber(roomNumber);
            LocalDate checkIn = LocalDate.parse(fields[1]);
            LocalDate checkOut = LocalDate.parse(fields[2]);
            String guestName = fields[3];
            Money roomCharge = Money.of(new BigDecimal(fields[4]));
            Money foodCharge = Money.of(new BigDecimal(fields[5]));

            List<FoodOrderLine> foodLines = new ArrayList<>();
            String foodField = fields[6];
            if (!foodField.isBlank()) {
                for (String item : foodField.split(LINE_ITEM_DELIMITER)) {
                    String[] itemParts = item.split(ITEM_QTY_DELIMITER);
                    MenuItem menuItem = MenuItem.valueOf(itemParts[0]);
                    int quantity = Integer.parseInt(itemParts[1]);
                    foodLines.add(new FoodOrderLine(menuItem, quantity));
                }
            }
            return new Invoice(roomNumber, roomType, checkIn, checkOut, guestName,
                    roomCharge, foodLines, foodCharge);
        } catch (RuntimeException e) {
            throw new PersistenceException("Corrupt or unreadable history line: " + line, e);
        }
    }
}