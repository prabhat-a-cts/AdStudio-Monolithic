# AdStudio (Monolith)

This is the AdStudio advertisement-management backend, consolidated from its
original microservice layout into a **single Spring Boot application**. Every
former service now lives as a sub-package under `com.cts.adstudio`, so the code
stays organized by domain while running as one deployable unit on one port.

## Modules

All under `com.cts.adstudio`:

- `iam` — authentication (JWT login/register), users, roles, audit log
- `advertiser` — advertisers, brands, campaign briefs, target audiences
- `mediaplan` — media plans, line items, insertion orders, delivery records, pacing alerts (scheduled)
- `finance` — client & publisher invoices, reconciliation, payment tracking, billing calendar
- `creative` — creative assets, approvals, asset-line-item links
- `notification` — notifications
- `delivery` — campaign delivery & performance: delivery records, accepted-only spend/impression rollups, pacing alerts (under/over-delivery, budget-exhausted, flight-end), and the delivered-spend/delivered-value endpoints the finance module reconciles against
- `config` — the single `SecurityConfig` and `OpenApiConfig` shared by the whole app

The original infrastructure services (Eureka discovery server, Spring Cloud
Config server, API gateway, and the standalone `security`/`common` libraries)
were dropped — a monolith doesn't need service discovery, a gateway, or
cross-service config distribution.

## Requirements

- Java 21
- MySQL running locally (or update the datasource settings below)
- Maven (a wrapper, `./mvnw`, is included — no separate install needed)

## Configuration

All settings live in `src/main/resources/application.properties`. The defaults are:

- Port `8181`
- Database `adstudio_monolith` on `localhost:3306`, user `root` / password `root`.
  The URL uses `createDatabaseIfNotExist=true`, so the schema is created on first run.
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate generates/updates tables from
  the entity mappings. (Flyway is not used in the monolith.)

Edit the `spring.datasource.*` lines to match your MySQL username/password, and
change `app.jwt.secret` before using this anywhere but your own machine.

## Running

```bash
./mvnw spring-boot:run
```

or build a jar and run it:

```bash
./mvnw clean package
java -jar target/adstudio-0.0.1-SNAPSHOT.jar
```

On first start a default admin user is seeded:

- **email:** `admin@adstudio.com`
- **password:** `Admin@123`

## Swagger / API docs

- Swagger UI: http://localhost:8181/swagger-ui.html
- OpenAPI JSON: http://localhost:8181/v3/api-docs (per module: `/v3/api-docs/{group}`)

The Swagger UI has a **group dropdown** (top-right) to switch between the IAM,
Advertiser, Media Plan, Finance, Creative, Notification, and Delivery &
Performance APIs.

### Authentication & authorization

Security is enforced application-wide. **Every endpoint requires a valid JWT**
except a small public allowlist:

- `POST /api/auth/register`, `POST /api/auth/login` — you can't present a token to obtain one
- the Swagger UI and OpenAPI docs (`/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`)
- the `health` and `info` actuator endpoints
- the two internal delivered-figure lookups
  (`GET /api/delivery/campaigns/{briefId}/delivered-spend` and
  `GET /api/delivery/insertion-orders/{ioId}/delivered-value`), which the finance
  module calls server-to-server without a token during invoice generation

A missing or invalid token returns **401 Unauthorized**; a valid token whose role
isn't permitted returns **403 Forbidden**.

To call secured endpoints from Swagger:

1. In the **Identity & Access** group, call `POST /api/auth/login` with the admin
   credentials above. Copy the `token` from the response.
2. Click **Authorize** (top-right of Swagger UI), paste the token, and confirm.
3. Your requests now carry `Authorization: Bearer <token>` and will be authorized
   according to your role.

Authentication is stateless JWT (HMAC-SHA256). The token carries the user's email
(subject), role, and id; the `JwtAuthenticationFilter` validates it on every
request and populates the security context. The signing secret and token lifetime
are configured via `app.jwt.secret` / `app.jwt.expiration-ms` in
`application.properties` — **change the secret before any non-local use** (it must
be at least 32 characters for HS256).

**Roles** (`com.cts.adstudio.iam.enums.Role`, mapped to Spring authorities as
`ROLE_<NAME>`): `BRAND_ADVERTISER`, `MEDIA_PLANNER`, `CREATIVE_MANAGER`,
`DELIVERY_PUBLISHER`, `FINANCE_EXECUTIVE`, `ADMIN`.

**Role-protected endpoints** (method-level `@PreAuthorize`, on top of the
authentication requirement above):

| Area | Endpoints | Allowed roles |
|------|-----------|---------------|
| IAM audit log | `/api/audit-logs/**` | `ADMIN` |
| Client invoices | `/api/client-invoices/**` | `FINANCE_EXECUTIVE`, `ADMIN` (read also `BRAND_ADVERTISER`) |
| Publisher invoices | `/api/publisher-invoices/**` | `DELIVERY_PUBLISHER`, `FINANCE_EXECUTIVE`, `ADMIN` |
| Billing calendar | `/api/invoices/**` | `FINANCE_EXECUTIVE`, `ADMIN`, `BRAND_ADVERTISER` |
| Delivery records & summaries | `/api/delivery/**` (except the two open lookups) | `DELIVERY_PUBLISHER`, `ADMIN` |
| Pacing alerts | `/api/delivery/pacing-alerts/**` | `MEDIA_PLANNER`, `ADMIN` |

All other authenticated endpoints (advertiser, media plan, creative, notification)
require a valid token but are not restricted by role; tighten them the same way
(`@PreAuthorize` per controller/method) if your team needs finer access control.

## Testing

Run the test suite with:

```bash
./mvnw test
```

The `delivery` module ships a full test suite (`src/test/java/com/cts/adstudio/delivery`):

- **`DeliveryServiceImplTest`** / **`PacingAlertServiceImplTest`** — pure Mockito
  unit tests (no Spring context). They mock the repositories and exercise the
  service logic directly: accepted-only aggregation, CTR derivation, status
  validation, the two finance-facing rollups, the summary reducers, and every
  pacing branch (under/over-delivery, budget-exhausted, flight-end-approaching,
  plus open-alert de-duplication).
- **`DeliveryControllerTest`** — standalone-MockMvc tests wired with the module's
  `@RestControllerAdvice`, so they cover HTTP status codes (201/400/404), request
  validation, and the JSON shape of the delivered-spend / delivered-value
  responses without starting a server.
- **`DeliveryRepositoryTest`** — a `@DataJpaTest` slice that boots a real
  persistence unit against an in-memory **H2 database in MySQL mode**
  (`src/test/resources/application.properties`). It persists a fixture across
  statuses, insertion orders and campaigns, then asserts the hand-written
  `COALESCE`/`SUM` JPQL returns correct accepted-only totals and `0` (never
  `null`) when nothing matches.

H2 is included as a `test`-scoped dependency, so the JPA slice test needs no
running MySQL; the unit and controller tests need no database at all.

## Notes

- The finance module calls the delivery module during invoice generation, hitting
  `GET /api/delivery/campaigns/{briefId}/delivered-spend` (client billing) and
  `GET /api/delivery/insertion-orders/{ioId}/delivered-value` (publisher
  reconciliation). Both are now implemented and return the **Accepted**-only spend
  rolled up for that scope, so finance reconciliation works end to end. These two
  endpoints are intentionally left open (no `@PreAuthorize`) to match the
  unauthenticated `RestClient` call finance already makes. The target URL is
  configurable via `adstudio.services.delivery.base-url` and points at this same
  application by default.
- Only delivery records in **Accepted** status count toward delivered totals,
  pacing, billing and summaries; `Disputed` and `PendingVerification` records are
  excluded until accepted. New records default to `Accepted`.
- Pacing thresholds are configurable under `adstudio.delivery.pacing.*` in
  `application.properties`: `under-delivery-threshold` (default 80),
  `over-delivery-threshold` (default 110), and `flight-end-warning-days`
  (default 3). The pacing engine compares accepted actuals against the planned
  targets supplied on each evaluate call and raises at most one open alert per
  type per line item.
- Entity-naming note: the `mediaplan` module ships temporary `DeliveryRecord` /
  `PacingAlert` entities (tables `delivery_record` / `pacing_alert`). To let both
  live in one persistence unit, the authoritative delivery entities keep their
  Java class names but use distinct JPA entity names / tables
  (`DeliveryServiceRecord` → `delivery_service_record`, `DeliveryServicePacingAlert`
  → `delivery_service_pacing_alert`). This mirrors the pattern the monolith
  already uses for its duplicated `AuditLog` entity.
- Security is unified into one filter chain (`config/SecurityConfig`): sessions are
  stateless, the `JwtAuthenticationFilter` validates the bearer token and populates
  the security context, URL access is locked down (every request authenticated
  except the public allowlist documented above), and method-level `@PreAuthorize`
  layers per-role authorization on top. See **Authentication & authorization** for
  the full matrix.
