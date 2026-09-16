# Design decisions

Short explanations for choices that aren't self-evident from the code, and the trade-offs behind
them. Written so a reviewer doesn't have to guess why something is the way it is.

## Unchecked domain exceptions

All exceptions extend `HotelDomainException`, which extends `RuntimeException`, not `Exception`.
The original app had one checked exception (`NotAvailable`). Going unchecked here is a deliberate
choice, not an oversight: every one of these exceptions is caught at exactly one place — the CLI's
menu loop — and reported to the user. Checked exceptions would force every service method between
the throw site and that one catch site to either declare `throws` or wrap-and-rethrow, for no
safety benefit, since there is nothing the intermediate layers could usefully do besides propagate
them anyway. If this application grows a second caller (e.g. a REST layer) that needs to handle
specific failures differently, the exceptions are still distinguishable by type via multi-catch —
they just aren't compiler-enforced.

## File-based persistence instead of a database

Phase 18 of the brief asks us to evaluate whether a database would help. For an application whose
entire state is "which of 60 rooms are occupied, by whom, with what food order" and which has
exactly one user at a time, a database adds operational weight (a schema, migrations, a connection
to manage, a reason to explain in the README why a hotel CLI needs Postgres) without buying
anything a database is actually good at: concurrent access, complex queries, or referential
integrity across a large object graph. `RoomRepository` and `BookingRepository` are still proper
interfaces — a JDBC- or JPA-backed implementation could be dropped in later without touching the
service layer — but building one now would be complexity for its own sake.

## Custom plain-text format instead of Java serialization

The original app persisted its entire object graph with `ObjectOutputStream`/`ObjectInputStream`.
Deserializing untrusted or corrupted data with Java's native mechanism is a well-documented attack
vector (gadget-chain remote code execution) — treating it as production-safe persistence would be
exactly the kind of unaudited risk Phase 9/16 ask us to catch. `FileHotelStateRepository` and
`FileInvoiceHistoryRepository` instead write one delimited plain-text line per booking/invoice and
parse each back field by field. A tampered or corrupted file can, at worst, produce a
`PersistenceException` — never arbitrary code execution. The trade-off is that it's a bespoke
format rather than a standard one (JSON would also have worked, but pulling in a JSON library for
a handful of fields per line felt disproportionate — see the "no dependencies" note below).

## Append-only invoice history instead of a mutable snapshot

Checked-out bookings move out of the open-bookings file into a separate, *append-only* history
file. History is written only ever by appending, so past invoices are never rewritten or lost —
an audit log rather than a mutable snapshot — and the open-bookings file stays small (matching its
actual job: "which rooms are occupied/coming up"). The two files have distinct shapes and purposes
(open bookings are mutable state; history is an immutable ledger), which is why they are separate
repositories behind two interfaces that the service layer already depends on.

## Zero runtime dependencies

The application depends on nothing beyond the JDK standard library at runtime (JUnit is a
test-only dependency). For a project this size — a handful of domain concepts, no networking, no
web layer — every dependency is something a reviewer has to trust and a future maintainer has to
keep patched, for a tiny amount of convenience. If the persistence format grows more complex, or a
web/REST front end is added, that's the natural point to introduce something like Jackson.

## No authentication, no database, no microservices, no Docker

Explicitly out of scope. This is a single-user console application; adding auth, a service mesh,
or containerization would not make it more correct or more maintainable, only longer and less
honest about what it actually is. If a genuinely multi-user or networked version of this
application were built, all of those would be worth reconsidering from scratch — bolting them onto
the current shape would not be the same design exercise.

## JaCoCo only, not Checkstyle/PMD/SpotBugs

One coverage tool was added (JaCoCo, wired into `mvn test`) rather than a full static-analysis
stack. Checkstyle/PMD/SpotBugs are genuinely useful on larger, longer-lived codebases with
multiple contributors enforcing a shared style; for a ~2,000-line portfolio project maintained by
one person, adding three more plugins to configure and keep green would be tooling for the sake of
looking thorough rather than tooling that changes how the code gets written. They're easy to add
later (`docs/architecture.md` doesn't need to change to accommodate them).

## Multi-day reservations are implemented, not just shaped for

The original application only ever charged one day's room rate per booking, with no check-in/
check-out *dates* at all — `checkout()` just ended the stay whenever it was called. Multi-day
stays are now a first-class concept:

- `Booking` carries `checkInDate`/`checkOutDate` (half-open: check-in night inclusive, check-out
  day exclusive) and derives its status from them — `RESERVED` before check-in, `ACTIVE` while in
  house, `CHECKED_OUT` after. Persisted bookings restore into the right status automatically.
- A room can hold several *non-overlapping* bookings over time (e.g. back-to-back reservations),
  so availability is a date-range query against `BookingRepository`, not a single mutable occupied
  flag. The flag survives only as a "guest in house today" convenience for the room inventory.
- Billing is prorated: `daily rate × nights`, computed in `Money`/`BigDecimal`.

Deliberate, documented boundaries rather than over-modeling: check-in is not a separate
*event* (a reservation becomes `ACTIVE` when the app next loads persisted state, not mid-run),
and a stay whose check-out passes without a checkout is dropped on load rather than auto-billed.
Both are explicit in the README's known limitations; neither is worth a lifecycle engine for a
single-user console application.
