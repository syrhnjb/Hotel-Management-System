package com.abhiram.hotel;

import com.abhiram.hotel.cli.HotelConsoleApp;
import com.abhiram.hotel.config.HotelConfig;
import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.exception.HotelDomainException;
import com.abhiram.hotel.repository.BookingRepository;
import com.abhiram.hotel.repository.FileHotelStateRepository;
import com.abhiram.hotel.repository.FileInvoiceHistoryRepository;
import com.abhiram.hotel.repository.HotelStateRepository;
import com.abhiram.hotel.repository.InMemoryBookingRepository;
import com.abhiram.hotel.repository.InMemoryRoomRepository;
import com.abhiram.hotel.repository.InvoiceHistoryRepository;
import com.abhiram.hotel.repository.RoomRepository;
import com.abhiram.hotel.service.BillingService;
import com.abhiram.hotel.service.BookingService;
import com.abhiram.hotel.service.FoodOrderService;
import com.abhiram.hotel.service.RoomService;
import java.util.List;
import java.util.Scanner;

/**
 * Composition root: wires repositories and services together, restores persisted state, runs the
 * CLI, and saves state back on exit. Contains no business logic of its own.
 */
public final class App {

    public static void main(String[] args) {
        HotelConfig config = HotelConfig.fromEnvironment();

        RoomRepository roomRepository = new InMemoryRoomRepository();
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        HotelStateRepository stateRepository = new FileHotelStateRepository(config.storageFile());
        InvoiceHistoryRepository historyRepository = new FileInvoiceHistoryRepository(config.historyFile());

        BillingService billingService = new BillingService();
        BookingService bookingService = new BookingService(roomRepository, bookingRepository,
                billingService, historyRepository);
        RoomService roomService = new RoomService(roomRepository);
        FoodOrderService foodOrderService = new FoodOrderService(bookingService);

        restoreState(stateRepository, bookingService);

        HotelConsoleApp app = new HotelConsoleApp(roomService, bookingService, foodOrderService, new Scanner(System.in));
        app.run();

        stateRepository.saveOpenBookings(bookingService.openBookings());
    }

    private static void restoreState(HotelStateRepository stateRepository, BookingService bookingService) {
        try {
            List<Booking> restored = stateRepository.loadOpenBookings();
            for (Booking booking : restored) {
                if (booking.isPastStay()) {
                    continue;
                }
                bookingService.restoreBooking(booking);
            }
            if (!restored.isEmpty()) {
                System.out.println("Restored " + restored.size() + " open booking(s) from previous session.");
            }
        } catch (HotelDomainException e) {
            System.out.println("Warning: could not restore previous session state (" + e.getMessage() + "). Starting fresh.");
        }
    }
}