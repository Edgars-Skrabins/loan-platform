# Loan Platform — Backend

A REST API for a small consumer-loan workflow: customers register and apply for a loan, loan
officers review and decide on it. Built with Spring Boot 3.5 and Java 21 on PostgreSQL.
The [Known limitations](#known-limitations--next-steps) section is an honest list of what is not
finished yet.

---

## Stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 3.5, Java 21 
| Database | PostgreSQL 17 
| Schema | Flyway, `ddl-auto: validate` 
| Auth | JWT (jjwt), BCrypt, stateless sessions 
| Testing | JUnit 5, Mockito, AssertJ, H2 for the API test 

## Running it

```bash
docker compose up -d          # PostgreSQL 17 on :5432
./mvnw spring-boot:run        # Flyway applies V1__init.sql on startup
```

The API is then on `http://localhost:8080`.

```bash
./mvnw test                   # 95 tests, no database required
```

## Data model

Five tables, defined in [`V1__init.sql`](src/main/resources/db/migration/V1__init.sql).

```
users ──1:1── customer_profiles ──1:N── loan_applications ──1:N── loan_decisions
  │                                                                     │
  └──────────────────── audit_logs                    officer_id ───────┘
```

- **`users`** — one row per login account. `role` is `CUSTOMER | LOAN_OFFICER | ADMIN`.
- **`customer_profiles`** — the financial picture for a customer (income, employment, credit
  score). Split from `users` because authentication identity and financial data have different
  lifecycles and different access rules.
- **`loan_applications`** — a request for money. Starts `PENDING`; staff move it to `IN_REVIEW`,
  `APPROVED` or `REJECTED`.
- **`loan_decisions`** — intended as an append-only record of who decided what, and why. The table
  exists and is mapped; **it is not written to yet** (see below).
- **`audit_logs`** — intended for cross-platform action tracking. Also mapped but not written to
  yet.

Money is `NUMERIC(12,2)` and `BigDecimal` in Java — never `double`.

## API

All errors share one JSON shape:

```json
{ "timestamp": "2026-08-19T10:15:30Z", "status": 409, "error": "Conflict", "message": "..." }
```

Validation failures add a `fieldErrors` object keyed by field name.

### `POST /api/auth/register` — public

Creates a `CUSTOMER` account plus its (empty) customer profile in one transaction. Staff accounts
are provisioned out of band.

```bash
curl -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email": "ada@example.com", "password": "longenough"}'
```

```json
{ "id": 1, "email": "ada@example.com", "role": "CUSTOMER", "createdAt": "2026-08-19T10:15:30Z" }
```

`409` if the email is taken. `400` with `fieldErrors` if the email is malformed or the password is
under 8 characters.

### `POST /api/auth/login` — public

```bash
curl -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email": "ada@example.com", "password": "longenough"}'
```

```json
{ "id": 1, "token": "eyJhbGciOiJIUzI1NiJ9...", "email": "ada@example.com", "role": "CUSTOMER" }
```

`401` on bad credentials — the message deliberately does not reveal whether it was the email or the
password that was wrong.

Send the token as `Authorization: Bearer <token>` on everything below.

### `POST /api/loans/loan-application` — customer

```bash
curl -X POST localhost:8080/api/loans/loan-application \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"amount": 5000.00, "termMonths": 24}'
```

```json
{ "id": 1, "status": "PENDING" }
```

→ `201`.

### `PUT /api/loans/loan-application` — loan officer / admin

```bash
curl -X PUT localhost:8080/api/loans/loan-application \
  -H "Authorization: Bearer $OFFICER_TOKEN" -H 'Content-Type: application/json' \
  -d '{"id": 1, "newStatus": "APPROVED"}'
```

`403` if a customer tries it. `404` if the application does not exist.

### `DELETE /api/loans/loan-application`

Withdraws an application. Only while `PENDING`, and a customer may only delete their own.

```bash
curl -X DELETE localhost:8080/api/loans/loan-application \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id": 1}'
```

→ `204`. `403` if it belongs to someone else, `409` if it has already been decided.

## How it is put together

Packages are organised by **feature**, not by layer — `auth`, `user`, `customer`, `loanApplication`,
`audit`, `security` — each with its own `controller` / `service` / `repository` / `dto` / `entity`
as needed. Everything about loan applications is in one place instead of smeared across four
top-level layer packages.

A few decisions worth calling out:

- **Requests and responses are `record` DTOs, one pair per operation.** Entities never cross the
  API boundary, so a change to the schema does not silently change the public contract.
- **`User` implements `UserDetails`.** Spring Security resolves `Authentication.getName()` through
  `getUsername()`; without this it silently falls back to `Object.toString()` and every lookup by
  username breaks. The JWT filter also copies `role` into a `ROLE_`-prefixed authority so
  role-based rules can match.
- **All error translation lives in one `@RestControllerAdvice`.** Services throw domain exceptions
  (`ForbiddenOperationException`, `InvalidLoanStateException`, …) and know nothing about HTTP; the
  advice maps them to 403 / 409 / 404 and keeps the response shape consistent. The catch-all logs
  the stack trace but returns a generic message so internals never leak.
- **Write operations are `@Transactional`.** Registration in particular creates a user *and* a
  customer profile; a half-completed signup would leave an account that cannot apply for anything.

## Testing

Two layers:

**Unit tests** (Mockito) for service logic, JWT handling, the security filter, the exception
mapping, and the bean-validation rules on every request DTO.

**`ApiIntegrationTest`** boots the whole application and drives the real endpoints with MockMvc:
register → log in → apply → approve, plus the authorization and error paths. This is the layer
that catches what unit tests structurally cannot — a missing `@Autowired`, a security matcher that
does not match the controller's path, a principal the rest of the app cannot read, a `NOT NULL`
column nobody populates. Every one of those was a real bug in this repo before the test existed.

It runs against H2 so `./mvnw test` needs no Docker. The trade-off: it exercises the entity
mappings but not `V1__init.sql` itself. Testcontainers would cover both and is the next step.

The 3 skipped tests are deliberate. Each is `@Disabled` with the reason in the annotation, and each
asserts the behaviour I *want* — a specification for work not yet done, not a test that was
switched off because it failed. They correspond to the first three items below.

## Known limitations / next steps

Roughly in the order I would tackle them:

1. **Decisions and audit logging are not written.** `loan_decisions` and `audit_logs` are designed,
   migrated and mapped, but `updateLoanApplicationStatus` only overwrites the status — it records
   no decision row and no audit entry. This is the biggest gap and the most interesting remaining
   work, since the schema was designed around an append-only decision history.
2. **No status state machine.** Any status can currently move to any other, so an `APPROVED`
   application can be walked back to `PENDING`.
3. **The JWT signing key is generated at startup.** Every restart invalidates all outstanding
   tokens, and two instances could not validate each other's. The fix is to read it from
   configuration — the constructor is already written and commented out in `JwtService`.
4. **No read endpoints.** There is no way to list your own applications or an officer's review
   queue. `findByCustomerId` and `findByStatus` exist on the repository and are unused.
5. **Testcontainers instead of H2**, so the integration test runs against real Postgres and
   actually exercises the Flyway migration.
6. **`customer_profiles` is never populated with financial data.** Registration creates an empty
   profile; there is no endpoint to fill in income, employment status or credit score, and nothing
   uses them to make a decision.
7. **Path variables instead of ids in request bodies** for update and delete, which would be more
   conventionally RESTful.
8. **Config hygiene** — the database password is committed in `application.yaml` and there are no
   per-environment profiles.
