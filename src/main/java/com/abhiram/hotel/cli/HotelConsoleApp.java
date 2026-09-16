package com.abhiram.hotel.cli;

import com.abhiram.hotel.domain.Booking;
import com.abhiram.hotel.domain.ContactNumber;
import com.abhiram.hotel.domain.FoodOrderLine;
import com.abhiram.hotel.domain.Guest;
import com.abhiram.hotel.domain.Invoice;
import com.abhiram.hotel.domain.MenuItem;
import com.abhiram.hotel.domain.Room;
import com.abhiram.hotel.domain.RoomType;
import com.abhiram.hotel.exception.HotelDomainException;
import com.abhiram.hotel.exception.ValidationException;
import com.abhiram.hotel.service.BookingService;
import com.abhiram.hotel.service.FoodOrderService;
import com.abhiram.hotel.service.RoomService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * The console presentation layer. This class owns all {@code System.out}/{@code Scanner}
 * interaction and menu flow; it never touches {@code RoomRepository} or {@code BookingRepository}
 * directly--every state change goes through the service layer, so business rules stay
 * enforceable and testable independent of the console.
 */
public final class HotelConsoleApp {

    private final RoomService roomService;
    private final BookingService bookingService;
    private final FoodOrderService foodOrderService;
    private final ConsoleIO io;

    public HotelConsoleApp(RoomService roomService, BookingService bookingService,
                            FoodOrderService foodOrderService, Scanner scanner) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.foodOrderService = foodOrderService;
        this.io = new ConsoleIO(scanner);
    }

    public void run() {
        io.println("\nWelcome to the Hotel Management System");
        try {
            boolean continueRunning = true;
            while (continueRunning) {
                printMainMenu();
                int choice = io.readInt("Enter your choice: ");
                try {
                    continueRunning = handleMenuChoice(choice);
                } catch (HotelDomainException e) {
                    io.println("Error: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            io.println("\nInput closed. Exiting.");
        }
        io.println("Goodbye!");
    }

    private void printMainMenu() {
        io.println("\n1. Display room details");
        io.println("2. Display room availability");
        io.println("3. Book a room");
        io.println("4. Order food");
        io.println("5. Checkout");
        io.println("6. View booking history");
        io.println("7. Exit");
    }

    /** @return {@code false} if the user chose to exit. */
    private boolean handleMenuChoice(int choice) {
        switch (choice) {
            case 1 -> displayRoomDetails();
            case 2 -> displayAvailability();
            case 3 -> bookRoom();
            case 4 -> orderFood();
            case 5 -> checkout();
            case 6 -> viewBookingHistory();
            case 7 -> {
                return false;
            }
            default -> io.println("Please choose an option between 1 and 7.");
        }
        return true;
    }

    private void displayRoomDetails() {
        RoomType type = chooseRoomType();
        io.println("\nBeds: " + type.occupancy());
        io.println("AC: " + (type.isAirConditioned() ? "Yes" : "No"));
        io.println("Free breakfast: " + (type.isBreakfastIncluded() ? "Yes" : "No"));
        io.println("Charge per day: " + type.dailyRate());
    }

    private void displayAvailability() {
        RoomType type = chooseRoomType();
        LocalDate checkIn = io.readDate("Check-in date (yyyy-mm-dd): ");
        LocalDate checkOut = io.readDate("Check-out date (yyyy-mm-dd): ");
        List<Room> available = bookingService.availableRoomsOfType(type, checkIn, checkOut);
        io.println("Rooms available: " + available.size());
        if (!available.isEmpty()) {
            io.print("Room numbers: ");
            io.println(available.stream().map(r -> String.valueOf(r.roomNumber()))
                    .reduce((a, b) -> a + ", " + b).orElse(""));
        }
    }

    private void bookRoom() {
        RoomType type = chooseRoomType();
        LocalDate checkIn = io.readDate("Check-in date (yyyy-mm-dd): ");
        LocalDate checkOut = io.readDate("Check-out date (yyyy-mm-dd): ");
        List<Room> available = bookingService.availableRoomsOfType(type, checkIn, checkOut);
        if (available.isEmpty()) {
            io.println("No rooms of this type are available for that stay.");
            return;
        }
        io.print("Available room numbers: ");
        io.println(available.stream().map(r -> String.valueOf(r.roomNumber()))
                .reduce((a, b) -> a + ", " + b).orElse(""));

        int roomNumber = io.readInt("Enter room number: ");
        List<Guest> guests = new ArrayList<>();
        for (int i = 1; i <= type.occupancy(); i++) {
            String label = type.occupancy() > 1 ? " (guest " + i + ")" : "";
            String name = io.readLine("Guest name" + label + ": ");
            String contact = io.readLine("Contact number" + label + ": ");
            String gender = io.readLine("Gender" + label + ": ");
            guests.add(new Guest(name, ContactNumber.of(contact), gender));
        }

        Booking booking = bookingService.bookRoom(roomNumber, guests, checkIn, checkOut);
        io.println("Room " + booking.roomNumber() + " booked for "
                + booking.nights() + " night(s) from " + booking.checkInDate() + " to " + booking.checkOutDate() + ".");
    }

    private void orderFood() {
        int roomNumber = io.readInt("Room number: ");
        boolean more = true;
        while (more) {
            io.println("\nMenu:");
            int i = 1;
            for (MenuItem item : MenuItem.values()) {
                io.println((i++) + ". " + item.displayName() + " - " + item.unitPrice());
            }
            int choice = io.readInt("Choose item number: ");
            MenuItem[] items = MenuItem.values();
            if (choice < 1 || choice > items.length) {
                io.println("Invalid item number.");
            } else {
                int quantity = io.readInt("Quantity: ");
                foodOrderService.addFoodOrder(roomNumber, items[choice - 1], quantity);
                io.println("Added.");
            }
            more = io.readYesNo("Order anything else? (y/n): ");
        }
    }

    private void checkout() {
        int roomNumber = io.readInt("Room number: ");
        Booking booking = bookingService.getInHouseBooking(roomNumber);
        io.println("Room used by " + booking.primaryGuest().name());
        if (!io.readYesNo("Proceed with checkout? (y/n): ")) {
            return;
        }
        Invoice invoice = bookingService.checkout(roomNumber);
        printInvoice(invoice);
    }

    private void viewBookingHistory() {
        List<Invoice> history = bookingService.bookingHistory();
        if (history.isEmpty()) {
            io.println("\nNo past bookings on record yet.");
            return;
        }
        io.println("\n******* Booking history *******");
        for (Invoice invoice : history) {
            io.println("- Room " + invoice.roomNumber() + " (" + invoice.roomType() + "), guest "
                    + invoice.guestName() + ", " + invoice.checkInDate() + " to " + invoice.checkOutDate()
                    + " (" + invoice.nights() + " night(s)) -- " + invoice.total());
        }
    }

    private void printInvoice(Invoice invoice) {
        io.println("\n******* Bill *******");
        io.println("Room " + invoice.roomNumber() + " (" + invoice.roomType() + ")");
        io.println("Guest: " + invoice.guestName());
        io.println("Stay: " + invoice.checkInDate() + " to " + invoice.checkOutDate()
                + " (" + invoice.nights() + " night(s))");
        io.println("Room charge: " + invoice.roomCharge());
        if (!invoice.foodLines().isEmpty()) {
            io.println("\nFood charges:");
            io.println(String.format("%-12s%-10s%-10s", "Item", "Qty", "Price"));
            for (FoodOrderLine line : invoice.foodLines()) {
                io.println(String.format("%-12s%-10d%-10s",
                        line.menuItem().displayName(), line.quantity(), line.lineTotal()));
            }
        }
        io.println("\nTotal: " + invoice.total());
    }

    private RoomType chooseRoomType() {
        io.println("\nChoose room type:");
        io.println("1. Luxury Double Room");
        io.println("2. Deluxe Double Room");
        io.println("3. Luxury Single Room");
        io.println("4. Deluxe Single Room");
        int choice = io.readInt("Enter choice: ");
        return switch (choice) {
            case 1 -> RoomType.LUXURY_DOUBLE;
            case 2 -> RoomType.DELUXE_DOUBLE;
            case 3 -> RoomType.LUXURY_SINGLE;
            case 4 -> RoomType.DELUXE_SINGLE;
            default -> throw new ValidationException("Invalid room type choice: " + choice);
        };
    }
}