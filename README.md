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
- **Booking** — books a room for the correct number of guests for its type; rejects booking an
  already-occupied room, a non-existent room, or the wrong guest count.
- **Food ordering** — a 4-item menu, added to a room's active booking; rejects zero/negative/
  unreasonably large quantities and orders against unbooked rooms.
- **Billing** — deterministic room + food charge calculation using `BigDecimal`, never `float`
  or `double`.
- **Persistence** — active bookings survive an application restart, via a safe plain-text file
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
| `Booking` | A room + its guest(s) + `FoodOrder`, from check-in to checkout |
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

Runs the full JUnit 5 suite (46 tests) and generates a JaCoCo coverage report at
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

The only externally-configurable setting is where booking state is persisted, via the
`HOTEL_STORAGE_FILE` environment variable:

```bash
HOTEL_STORAGE_FILE=/tmp/my-hotel-state.txt java -jar target/hotel-management-system.jar
```

Defaults to `hotel-state.txt` in the working directory. Room counts, prices, and the food menu
are stable business data, not configuration — they live in `RoomType` and `MenuItem`.

## Persistence

Active (not yet checked-out) bookings are saved to a plain-text file when the application exits
and restored on the next startup. Each line is one booking: room number, guest details, and food
order lines, pipe/semicolon/comma-delimited. See
[`docs/design-decisions.md`](docs/design-decisions.md#custom-plain-text-format-instead-of-java-serialization)
for why this replaces the original's `ObjectOutputStream`-based approach, and its documented
limitations (booking history/past invoices are not retained — only active bookings).

## Example usage

```
1. Display room details
2. Display room availability
3. Book a room
4. Order food
5. Checkout
6. Exit
Enter your choice: 3

Choose room type:
1. Luxury Double Room
2. Deluxe Double Room
3. Luxury Single Room
4. Deluxe Single Room
Enter choice: 3
Available room numbers: 31, 32, 33, 34, 35, 36, 37, 38, 39, 40
Enter room number: 31
Guest name: Asha
Contact number: 9876543210
Gender: F
Room 31 booked successfully.
```

### Sample workflow

1. Book Luxury Single Room #31 for "Asha".
2. Order food: 2× Pasta.
3. Checkout room 31 →

   ```
   ******* Bill *******
   Room 31 (LUXURY_SINGLE)
   Room charge: ₹2200.00

   Food charges:
   Item        Qty       Price
   Pasta       2         ₹120.00

   Total: ₹2320.00
   ```

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

46 JUnit 5 tests, focused on business behavior rather than trivial getters/setters:

- `BookingServiceTest` — booking, double-booking prevention, invalid room/guest-count rejection,
  checkout, double-checkout rejection, and that a rejected booking leaves no partial state.
- `FoodOrderServiceTest` — valid orders, zero/negative/excessive quantity rejection, ordering
  against an unbooked room.
- `BillingServiceTest` — room-only, room+food, multiple food lines, zero food orders, and
  single-unit boundary pricing — as pure arithmetic against a `Booking`, with no I/O involved.
- `RoomServiceTest`, `RoomTypeTest`, `BookingTest`, `MoneyTest` — domain invariants and room-type
  boundary resolution.
- `ValidatorTest`, `ContactNumberTest` — input validation edge cases.
- `FileHotelStateRepositoryTest` — persistence round-trips, including bookings with no food
  orders and loading from a missing file.

## CI/CD

`.github/workflows/ci.yml` runs on every push/PR to `main`: checks out the code, sets up JDK 17,
and runs `mvn clean verify` (compile + test), uploading the JaCoCo report as a build artifact.
The build fails if any test fails. No deployment step exists — none is claimed.

## Known limitations

- **Single-day stays only.** There is no check-in/check-out date range or multi-night pricing —
  matching the original application's scope. See
  [`docs/design-decisions.md`](docs/design-decisions.md#multi-day-stays-are-not-implemented).
- **No booking history.** Only active bookings are persisted; once a booking is checked out, its
  invoice is shown once and not retained.
- **Single-user, single-process.** The in-memory repositories are not thread-safe; this is a
  single-threaded console application by design (see
  [`docs/design-decisions.md`](docs/design-decisions.md#no-authentication-no-database-no-microservices-no-docker)).
- **No authentication.** Not required for a local console tool; would need a real design pass if
  this became a networked/multi-user application.
- **In-sandbox verification note:** during development, `mvn clean test` itself could not be run
  in the isolated build sandbox used to prepare this change (no access to Maven Central). The
  exact same source and test suite were compiled and executed instead with a local `javac`/JUnit
  toolchain — all 46 tests pass. `mvn clean test` is expected to work normally in any environment
  with standard internet access; if you hit an issue running it, please open an issue.

## Future improvements

- Multi-day reservations with date-range validation and prorated billing.
- Booking/invoice history retained across checkouts.
- A JDBC- or JPA-backed `RoomRepository`/`BookingRepository` implementation, if the project ever
  needs concurrent/multi-user access (the interfaces are already in place for this).
- Checkstyle/PMD/SpotBugs, if the project grows enough contributors to need enforced style
  consistency (see [`docs/design-decisions.md`](docs/design-decisions.md#jacoco-only-not-checkstylepmdspotbugs)).

## Contributing

Contributions are welcome — please open an issue or pull request.

## License

[MIT License](LICENSE).

## Author

ABHIRAM — [GitHub Profile](https://github.com/AbhiramMandala)
