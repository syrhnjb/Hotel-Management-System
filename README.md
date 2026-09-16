# Hotel Management System

A layered, test-covered Java console application for hotel room booking, food ordering, and
billing.

## Problem statement

The original version of this project was a single 400-line `Main.java` containing every class,
with business logic driven entirely by switch statements on integer codes, `float` used for
money, and persistence via unauthenticated Java object deserialization. This version keeps the
same feature set — book a room, order food, generate a bill, check out — rebuilt with a layered
architecture, a real domain model, automated tests, and safe persistence.

## Key features

- **Room management** — 60 rooms across 4 categories (Luxury/Deluxe × Single/Double), with
  live availability tracking.
- **Booking** — books a room for the correct number of guests for its type over a date range
  (one or more nights); rejects an already-occupied room, a non-existent room, the wrong guest
  count, overlapping dates, or a check-in in the past.
- **Advance reservations** — a room can hold several non-overlapping future bookings; each is
  `RESERVED` until check-in and `ACTIVE` once the guest is in house.
- **Prorated billing** — room charges are `daily rate × nights` (computed with `BigDecimal`,
  never `float` or `double`).
- **Food ordering** — a 4-item menu, added to a room's in-house booking; rejects zero/negative/
  unreasonably large quantities and orders against unbooked rooms.
- **Billing** — deterministic room + food charge calculation per night stayed.
- **Booking history** — every checked-out invoice is recorded in an append-only history file and
  can be listed from the menu.
- **Persistence** — open bookings survive an application restart, via a safe plain-text file
  format (not Java's native object serialization — see [Security](#security)).

## Architecture

```
Presentation (cli)
        ↓
Application/Service (service)
        ↓
Domain (domain)
        ↓
Repository/Persistence (repository)
```

Full details, including sequence diagrams for the booking and checkout flows, are in
[`docs/architecture.md`](docs/architecture.md). Rationale for specific choices (unchecked
exceptions, no database, no framework dependencies, custom persistence format) is in
[`docs/design-decisions.md`](docs/design-decisions.md).

## Technology stack

- Java 17
- Maven
- JUnit 5 (test-only dependency — the application has **zero** runtime dependencies beyond the
  JDK)
- JaCoCo for test coverage reporting
- GitHub Actions for CI

## Project structure

```
src/
├── main/java/com/abhiram/hotel/
│   ├── App.java              # composition root
│   ├── cli/                  # console presentation layer
│   ├── domain/                # Room, RoomType, Booking, Guest, Money, ...
│   ├── service/               # RoomService, BookingService, FoodOrderService, BillingService
│   ├── repository/            # in-memory + file-based persistence
│   ├── exception/             # domain exception hierarchy
│   ├── validation/            # shared input-validation helpers
│   └── config/                 # HotelConfig
└── test/java/com/abhiram/hotel/
    ├── domain/
    ├── service/
    ├── repository/
    └── validation/
docs/
├── architecture.md
└── design-decisions.md
.github/workflows/ci.yml
```

## Domain model

| Type | Role |
|---|---|
| `Room` | A physical room: number, type, `AVAILABLE`/`OCCUPIED` status |
| `RoomType` | Enum: `LUXURY_DOUBLE`, `DELUXE_DOUBLE`, `LUXURY_SINGLE`, `DELUXE_SINGLE` — carries number range, occupancy, daily rate, amenities |
| `Guest` | Name, `ContactNumber`, gender |
| `Booking` | A room + its guest(s) + `FoodOrder` + check-in/check-out dates, from `RESERVED`/`ACTIVE` to `CHECKED_OUT` |
| `MenuItem` | Enum: `SANDWICH`, `PASTA`, `NOODLES`, `COKE`, each with a unit price |
| `FoodOrder` / `FoodOrderLine` | Line items ordered against a booking |
| `Invoice` | The computed room + food charges for a checked-out booking |
| `Money` | `BigDecimal`-backed value object — no floating-point currency arithmetic anywhere |
| `ContactNumber` | Validated phone-number value object |

## Setup

**Prerequisites:** JDK 17+ and Maven 3.8+.

```bash
git clone https://github.com/AbhiramMandala/Hotel-Management-System.git
cd Hotel-Management-System
```

## Build

```bash
mvn clean compile
```

## Test

```bash
mvn clean test
```

Runs the full JUnit 5 suite (62 tests) and generates a JaCoCo coverage report at
`target/site/jacoco/index.html`.

## Run

```bash
mvn clean package
java -jar target/hotel-management-system.jar
```

or, without packaging a jar:

```bash
mvn compile exec:java -Dexec.mainClass=com.abhiram.hotel.App
```

## Configuration

The externally-configurable settings are the two file locations, via the `HOTEL_STORAGE_FILE` and
`HOTEL_HISTORY_FILE` environment variables:

```bash
HOTEL_STORAGE_FILE=/tmp/my-hotel-state.txt \
HOTEL_HISTORY_FILE=/tmp/my-hotel-history.txt \
java -jar target/hotel-management-system.jar
```

They default to `hotel-state.txt` and `hotel-history.txt` in the working directory. Room counts,
prices, and the food menu are stable business data, not configuration — they live in `RoomType`
and `MenuItem`.

## Persistence

Two plain-text files are read and written on application exit and startup:

- **`hotel-state.txt`** — open bookings (reserved or in-house, not yet checked out). Each line is
  one booking: room number, check-in/check-out dates, guest details, and food order lines,
  pipe/semicolon/comma-delimited. A stay whose check-out date has passed without a checkout is
  dropped on load.
- **`hotel-history.txt`** — invoices of checked-out bookings. Append-only, so past invoices are
  never rewritten or lost.

See [`docs/design-decisions.md`](docs/design-decisions.md#custom-plain-text-format-instead-of-java-serialization)
for why this replaces the original's `ObjectOutputStream`-based approach.

## Example usage

```
1. Display room details
2. Display room availability
3. Book a room
4. Order food
5. Checkout
6. View booking history
7. Exit
Enter your choice: 3

Choose room type:
1. Luxury Double Room
2. Deluxe Double Room
3. Luxury Single Room
4. Deluxe Single Room
Enter choice: 3
Check-in date (yyyy-mm-dd): 2026-09-16
Check-out date (yyyy-mm-dd): 2026-09-18
Available room numbers: 31, 32, 33, 34, 35, 36, 37, 38, 39, 40
Enter room number: 31
Guest name: Asha
Contact number: 9876543210
Gender: F
Room 31 booked for 2 night(s) from 2026-09-16 to 2026-09-18.
```

### Sample workflow

1. Book Luxury Single Room #31 for "Asha" for 2 nights (2026-09-16 → 2026-09-18).
2. Order food: 2× Pasta.
3. Checkout room 31 →

   ```
   ******* Bill *******
   Room 31 (LUXURY_SINGLE)
   Guest: Asha
   Stay: 2026-09-16 to 2026-09-18 (2 night(s))
   Room charge: ₹4400.00

   Food charges:
   Item        Qty       Price
   Pasta       2         ₹120.00

   Total: ₹4520.00
   ```
4. View booking history → lists the invoice above and every other past checkout.

## Error handling

User input is never allowed to crash the application. Non-numeric input where a number is
expected re-prompts rather than throwing; all business-rule violations (booking an occupied
room, ordering food for an unbooked room, an invalid quantity, and so on) raise a specific
`HotelDomainException` subtype that the CLI's single top-level catch reports as a plain error
message. See [`docs/design-decisions.md`](docs/design-decisions.md#unchecked-domain-exceptions)
for why these are unchecked exceptions.

## Security

- **No unsafe deserialization.** Persistence uses a custom plain-text format, not Java's
  `ObjectInputStream` — see [Persistence](#persistence).
- **No secrets or credentials** anywhere in the codebase or configuration.
- **Input validation** at the domain boundary (`ContactNumber`, `Guest`, `FoodOrderLine`, and
  `Validator`) rejects malformed input before it reaches business logic.
- **No authentication/authorization** is implemented — this remains a single-user local console
  application, and adding auth without a real multi-user requirement would be security theater,
  not a security improvement.

## Testing strategy

62 JUnit 5 tests, focused on business behavior rather than trivial getters/setters:

- `BookingServiceTest` — booking, double-booking/overlap prevention across date ranges, invalid
  room/guest-count rejection, past-date rejection, advance (non-overlapping future)
  reservations, checkout, double-checkout rejection, that a rejected booking leaves no partial
  state, and that checkout records the invoice in history.
- `FoodOrderServiceTest` — valid orders, zero/negative/excessive quantity rejection, ordering
  against an unbooked room.
- `BillingServiceTest` — single-night and multi-night (prorated) room charges, room+food, multiple
  food lines, zero food orders, and single-unit boundary pricing — as pure arithmetic against a
  `Booking`, with no I/O involved.
- `RoomServiceTest`, `RoomTypeTest`, `BookingTest`, `MoneyTest` — domain invariants, date-range
  validation, status derivation (reserved/in-house), and room-type boundary resolution.
- `ValidatorTest`, `ContactNumberTest` — input validation edge cases.
- `FileHotelStateRepositoryTest` — open-booking round-trips including check-in/check-out dates,
  reserved bookings, bookings with no food orders, and loading from a missing file.
- `FileInvoiceHistoryRepositoryTest` — invoice round-trips and append-only behaviour.

## CI/CD

`.github/workflows/ci.yml` runs on every push/PR to `main`: checks out the code, sets up JDK 17,
and runs `mvn clean verify` (compile + test), uploading the JaCoCo report as a build artifact.
The build fails if any test fails. No deployment step exists — none is claimed.

## Known limitations

- **Date-bound availability, single process.** The room's occupied flag only reflects whether a
  guest is in house *today*; a booking matures from `RESERVED` to `ACTIVE` when the application
  next loads persisted state, not mid-run while the app stays open.
- **Stale stays are dropped, not billed.** If a guest's check-out passes without a checkout, the
  booking is silently dropped on the next startup rather than auto-billed.
- **Single-user, single-process.** The in-memory repositories are not thread-safe; this is a
  single-threaded console application by design (see
  [`docs/design-decisions.md`](docs/design-decisions.md#no-authentication-no-database-no-microservices-no-docker)).
- **No authentication.** Not required for a local console tool; would need a real design pass if
  this became a networked/multi-user application.
- **In-sandbox verification note:** during the original modernization, `mvn clean test` itself
  could not be run in the isolated build sandbox used to prepare that change (no access to Maven
  Central). The exact same source and test suite were compiled and executed instead with a local
  `javac`/JUnit toolchain — all tests passed. The project now verifiably builds here with
  `mvn clean verify` (62 tests, all passing).

## Future improvements

- A JDBC- or JPA-backed `RoomRepository`/`BookingRepository` implementation, if the project ever
  needs concurrent/multi-user access (the interfaces are already in place for this).
- Reporting and analytics over the invoice history (occupancy rates, revenue per room type, food
  sales) now that history is retained.
- Checkin/checkout signalling so a running application marks a reservation `ACTIVE` when its
  check-in date arrives, without needing a restart.
- Interrupted-stay handling (e.g. dropping a stale stay with a logged warning rather than
  silently).
- Checkstyle/PMD/SpotBugs, if the project grows enough contributors to need enforced style
  consistency (see [`docs/design-decisions.md`](docs/design-decisions.md#jacoco-only-not-checkstylepmdspotbugs)).

## Contributing

Contributions are welcome — please open an issue or pull request.

## License

[MIT License](LICENSE).

## Author

ABHIRAM — [GitHub Profile](https://github.com/AbhiramMandala)
