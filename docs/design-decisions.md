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
exactly the kind of unaudited risk Phase 9/16 ask us to catch. `FileHotelStateRepository` instead
writes one delimited plain-text line per active booking and parses it back field by field. A
tampered or corrupted file can, at worst, produce a `PersistenceException` — never arbitrary code
execution. The trade-off is that it's a bespoke format rather than a standard one (JSON would also
have worked, but pulling in a JSON library for four fields per line felt disproportionate — see
the "no dependencies" note below).

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

## Multi-day stays are not implemented

The original application only ever charged one day's room rate per booking, with no check-in/
check-out *dates* at all — `checkout()` just ends the stay whenever it's called. This rebuild
preserves that scope rather than pretending multi-day reservations exist: `Booking` records a
`checkInTime` (useful for the persisted-state restoration message and for a future feature) but
billing is still a flat one-stay room charge. Implementing real multi-day pricing properly (date
ranges, overlapping-booking checks, prorated billing) is a large enough feature that it belongs in
its own change with its own tests, not folded silently into a "modernization."
