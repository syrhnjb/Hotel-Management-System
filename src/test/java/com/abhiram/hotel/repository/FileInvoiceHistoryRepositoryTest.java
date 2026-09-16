package com.abhiram.hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.Money;
import com.abhiram.hotel.domain.RoomType;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileInvoiceHistoryRepositoryTest {

    @TempDir
    Path tempDir;

    private final LocalDate today = LocalDate.now();

    private Invoice invoice(int roomNumber, RoomType type, String guest, int nights,
                            int roomCharge, List<FoodOrderLine> foodLines, int foodCharge) {
        return new Invoice(roomNumber, type, today, today.plusDays(nights), guest,
                Money.rupees(roomCharge), foodLines, Money.rupees(foodCharge));
    }

    @Test
    void loadingFromAMissingFileReturnsAnEmptyList() {
        FileInvoiceHistoryRepository repo = new FileInvoiceHistoryRepository(tempDir.resolve("no-history.txt"));

        assertTrue(repo.loadAll().isEmpty());
    }

    @Test
    void savedInvoicesRoundTripCorrectly() {
        FileInvoiceHistoryRepository repo = new FileInvoiceHistoryRepository(tempDir.resolve("history.txt"));

        Invoice stayOnly = invoice(31, RoomType.LUXURY_SINGLE, "Asha", 2, 4400, List.of(), 0);
        Invoice withFood = invoice(1, RoomType.LUXURY_DOUBLE, "Ravi", 1, 4000,
                List.of(new FoodOrderLine(MenuItem.PASTA, 2)), 120);

        repo.save(stayOnly);
        repo.save(withFood);

        List<Invoice> reloaded = repo.loadAll();
        assertEquals(2, reloaded.size());

        Invoice reloadedStay = reloaded.get(0);
        assertEquals(31, reloadedStay.roomNumber());
        assertEquals(RoomType.LUXURY_SINGLE, reloadedStay.roomType());
        assertEquals("Asha", reloadedStay.guestName());
        assertEquals(2, reloadedStay.nights());
        assertEquals(Money.rupees(4400), reloadedStay.roomCharge());
        assertTrue(reloadedStay.foodLines().isEmpty());
        assertEquals(Money.rupees(4400), reloadedStay.total());

        Invoice reloadedFood = reloaded.get(1);
        assertEquals(1, reloadedFood.foodLines().size());
        assertEquals(MenuItem.PASTA, reloadedFood.foodLines().get(0).menuItem());
        assertEquals(2, reloadedFood.foodLines().get(0).quantity());
        assertEquals(Money.rupees(120), reloadedFood.foodCharge());
        assertEquals(Money.rupees(4120), reloadedFood.total());
    }

    @Test
    void appendedInvoicesDoNotClobberEarlierOnes() {
        Path historyFile = tempDir.resolve("history.txt");
        FileInvoiceHistoryRepository repo = new FileInvoiceHistoryRepository(historyFile);
        repo.save(invoice(31, RoomType.LUXURY_SINGLE, "Asha", 1, 2200, List.of(), 0));

        FileInvoiceHistoryRepository secondSession = new FileInvoiceHistoryRepository(historyFile);
        secondSession.save(invoice(32, RoomType.LUXURY_SINGLE, "Ravi", 1, 2200, List.of(), 0));

        assertEquals(2, repo.loadAll().size());
    }
}