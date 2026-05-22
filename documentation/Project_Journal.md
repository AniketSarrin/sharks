# Project Journal — Sharks EventBrite Platform

**Team:** CMPE202-02-Spring2026-Sharks
**Course:** CMPE 202-02 · Spring 2026
**Members:** Ankush Makhijani · Aniket Sarrin · Maurin Baroi
**Sprint Cadence:** 2-week sprints · 6 sprints total · Feb 9 – May 7, 2026

---

## Team Contributions Summary

| Member | Component Ownership |
|---|---|
| **Ankush Makhijani** | Auth Service Lead (Spring Boot + Supabase GoTrue), Event & Ticketing Service Lead (Event/Ticket APIs, Payment Listener, Admin Moderation), Spring Security & JWT validation, Supabase Migrations & Row Level Security, RabbitMQ Event Publishing, Unit Tests, Containerization with Docker, Terraform scripts, k8s, API gateway, End-to-End integration. |
| **Aniket Sarrin** | User & Organizations Service Lead (Spring Boot), RabbitMQ Pub/Sub Topology, Docker Compose Infrastructure, Notification Consumer Stub, JWT extraction utilities, Bean Validation, End-to-End API testing. |
| **Maurin Baroi** | Frontend Lead (React/TypeScript), Mock Payment Service, AWS Production Deployment (Elastic Beanstalk + ALB + API Gateway), Custom Domain & DNS, UI Wireframes, Architecture Diagrams, Documentation, Database migrations. |

---

## XP Core Values

### 1. Communication

We held weekly Scrum meetings to align on progress, blockers, and priorities, and used Discord for async coordination between meetings. We agreed on API contracts early — the shared `api.ts` interface defined the JSON input/output for every endpoint before implementation began, which prevented integration issues between the frontend and the three backend services. RabbitMQ exchange and queue naming conventions were documented in `MESSAGING.md` and agreed upon before any service started publishing or consuming messages. Design decisions like using Supabase for auth, RabbitMQ for async coordination, and AWS Elastic Beanstalk for deployment were all discussed as a team rather than imposed by any one member. When one of us hit a wall — Ankush's BOM encoding bug breaking Docker builds, Maurin's CORS issues across AWS domains, Aniket's RabbitMQ connection retries warning the health check — the others pitched in to debug, even when it was outside their owned component.

### 2. Feedback

Every pull request was reviewed by at least one other team member before merging, with discussion on the PR or in our weekly sprint meeting. This caught real issues throughout the project — Ankush's BOM encoding bug breaking Docker builds, RabbitMQ connection retries causing health-check warnings, and the CORS misconfiguration between the frontend and the auth service when we eventually deployed to AWS in the final sprints. Each team member tested the others' components end-to-end against Docker Compose locally before any cloud deployment: Ankush's auth service was driven through the frontend by Maurin, and Aniket's messaging pipeline was verified by following messages across all services. We reviewed the professor's demo rubric at the end of each sprint as a feedback checklist, and the gap between what the rubric expected and what we'd built drove the priorities for the following sprint.

---

## Sprint Velocity Summary

> All planned story points were completed across all 6 sprints (100% velocity). Sprints 3 and 4 were intentionally light (totaling 19 pts combined) due to midterms and other course conflicts; Sprint 5 was a catch-up sprint where the Event & Ticketing service, organizer/admin dashboards, mock payment flow, and full AWS deployment were delivered in parallel.

| Sprint | Dates | Planned | Completed | Velocity |
|---|---|---|---|---|
| 1 | Feb 9 – Feb 22 | 40 | 40 | 100% |
| 2 | Feb 23 – Mar 8 | 45 | 45 | 100% |
| 3 | Mar 9 – Mar 22 *(light)* | 9 | 9 | 100% |
| 4 | Mar 23 – Apr 5 *(light)* | 10 | 10 | 100% |
| 5 | Apr 6 – Apr 19 *(catch-up)* | 56 | 56 | 100% |
| 6 | Apr 20 – May 7 | 42 | 42 | 100% |
| **TOTAL** | **Feb 9 – May 7, 2026** | **202** | **202** | **100%** |

---

# Sprint 1 — Feb 9 – Feb 22, 2026

**Sprint Goal:** Bootstrap all three services, frontend, and infrastructure foundations. Establish shared API contracts and the RabbitMQ messaging topology before any service starts publishing.

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-15 | Set up dev environment (Java 17, Gradle, IDE) and initialize Spring Boot auth service | Ankush | 3 | ✓ |
| US-16 | Write Supabase DB migrations: `init_app_role_and_user_roles`, `user_roles_rls` (RLS policies) | Ankush | 5 | ✓ |
| US-01 | Design auth API contract (login/register/JWT) and implement `SupabaseAuthClient` | Ankush | 3 | ✓ |
| US-02 | Implement `AuthController` `POST /api/v1/auth/login` + `AppRole`, `UserRole`, `SupabaseProperties` | Ankush | 3 | ✓ |
| US-15 | Set up Docker dev environment and initialize User & Organizations Spring Boot service | Aniket | 3 | ✓ |
| US-14 | Design RabbitMQ exchange/queue topology and document `MESSAGING.md` | Aniket | 5 | ✓ |
| US-08 | Define `UserProfile` / `Organization` entities and DTOs | Aniket | 3 | ✓ |
| US-08 | Implement `UserProfileRepository` and `OrganizationRepository` (Spring Data JPA) | Aniket | 2 | ✓ |
| US-22 | Set up frontend dev env (Vite, React, TypeScript, Tailwind, shadcn/ui) and team GitHub repo | Maurin | 2 | ✓ |
| US-03 | Initialize React/TypeScript frontend with React Router and role-based routing scaffolding | Maurin | 2 | ✓ |
| US-04 | Build Login/Signup UI with form validation and create `api.ts` contract | Maurin | 3 | ✓ |
| US-22 | Design initial Component diagram (frontend, services, RabbitMQ, Supabase) | Maurin | 3 | ✓ |
| US-08 | Design initial ER diagram (User, Event, Ticket, Organization) | Maurin | 3 | ✓ |
| **TOTAL** | **13/13 tasks completed** |  | **40** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Set up dev environment (Java 17, Gradle, IntelliJ) and initialized the Spring Boot auth service [US-15]
- Wrote first batch of Supabase DB migrations: `init_app_role_and_user_roles`, `user_roles_rls` — established Row Level Security policies so users can only read their own role [US-16]
- Designed the auth API contract (login / register / JWT response shapes) and implemented `SupabaseAuthClient` using GoTrue password grant [US-01]
- Implemented `AuthController` with `POST /api/v1/auth/login`, plus `AppRole` enum (ATTENDEE / ORGANIZER / ADMIN), `UserRole` model, and `SupabaseProperties` for env-driven configuration [US-02]

**What am I planning to work on next?**
- Add JWT validation using the Supabase JWT issuer (`oauth2ResourceServer`)
- Configure Spring Security with role-based access control, CORS, and stateless sessions
- Add input validation and global exception handling

**What tasks are blocked?**
- None

#### Aniket Sarrin

**What did I work on / complete?**
- Set up the Docker dev environment and initialized the User & Organizations Spring Boot service [US-15]
- Designed the full RabbitMQ exchange/queue topology and documented it in `MESSAGING.md` so all three services agreed on naming conventions before publishing [US-14]
- Defined the data schema: `UserProfile` and `Organization` entities, plus `UserDto`, `OrganizationDto`, and `CreateOrganizationRequest` DTOs [US-08]
- Implemented `UserProfileRepository` and `OrganizationRepository` on Spring Data JPA with custom query methods [US-08]

**What am I planning to work on next?**
- Implement REST endpoints for users and organizations
- Set up the RabbitMQ Docker image with pre-loaded exchange/queue definitions via `definitions.json`
- Add `UserCreatedListener` to consume `user.provisioned` events from auth

**What tasks are blocked?**
- Waiting on Ankush to confirm the auth service will publish `user.provisioned` on registration — unblocked once API contract was finalized

#### Maurin Baroi

**What did I work on / complete?**
- Set up the frontend dev environment (Vite + React 18 + TypeScript + Tailwind + shadcn/ui) and the team GitHub repository [US-22]
- Initialized the React app with React Router v6 and role-based routing scaffolding [US-03]
- Built Login and Signup page UI with form validation and a role selector; wrote the shared `api.ts` file as the API contract for the team [US-04]
- Drafted the initial UML Component diagram showing service boundaries and the role of RabbitMQ + Supabase [US-22]
- Drafted the initial ER diagram covering User, Event, Ticket, and Organization entities [US-08]

**What am I planning to work on next?**
- Build event discovery page with search, filters, and category browsing
- Build event detail page with map and schedule
- Wire login/signup to the backend once endpoints are ready

**What tasks are blocked?**
- Waiting on Ankush to finalize login/register response shape — unblocked once `api.ts` was agreed

---

# Sprint 2 — Feb 23 – Mar 8, 2026

**Sprint Goal:** Lock in security on the auth service, stand up the User & Organizations REST API, get RabbitMQ orchestrated under Docker Compose, and ship the first useful attendee-facing screens (event discovery, event detail, my-tickets).

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-02 | Implement JWT validation using Supabase JWT issuer (`oauth2ResourceServer`) | Ankush | 3 | ✓ |
| US-21 | Configure `SecurityConfig` CORS, `InternalApiKeyAuthFilter`, `RequestIdFilter` | Ankush | 3 | ✓ |
| US-21 | Implement `SupabaseAdminClient` and `UserProvisioningService` for role assignment on signup | Ankush | 3 | ✓ |
| US-16 | Write Supabase DB migrations: `seed_user_roles`, `jwt_custom_claims`, `auto_role_on_signup` | Ankush | 4 | ✓ |
| US-01 | Implement `/api/v1/auth/register` + `UserEventPublisher` for `user.provisioned` events | Ankush | 3 | ✓ |
| US-21 | Add `RabbitMqConfig` + `Jackson2JsonMessageConverter`; write unit tests | Ankush | 3 | ✓ |
| US-08 | Implement REST endpoints for user profile (`GET/PUT/PATCH /api/v1/users/me`, password change) | Aniket | 3 | ✓ |
| US-08 | Implement REST endpoints for organizations (`GET/POST /api/v1/organizations`, GET by id) | Aniket | 3 | ✓ |
| US-15 | Set up RabbitMQ Docker image with pre-loaded exchange/queue definitions via `definitions.json` | Aniket | 3 | ✓ |
| US-15 | Configure `docker-compose.yml` for local dev orchestrating Postgres + RabbitMQ + services | Aniket | 2 | ✓ |
| US-14 | Implement `UserCreatedListener` + `OrganizationEventPublisher` + `RabbitMqConfig` | Aniket | 4 | ✓ |
| US-04 | Build event discovery page with search bar, category filters, date and location filters | Maurin | 3 | ✓ |
| US-05 | Build event detail page (description, schedule, organizer, Google Maps) + Calendar link | Maurin | 3 | ✓ |
| US-06 | Build My Tickets page with ticket codes, quantities, and status badges | Maurin | 2 | ✓ |
| US-22 | Finalize UML Component diagram (service boundaries, integrations, exchanges) | Maurin | 3 | ✓ |
| **TOTAL** | **15/15 tasks completed** |  | **45** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Implemented JWT validation against the Supabase issuer via Spring's `oauth2ResourceServer` [US-02]
- Configured `SecurityConfig` with the CORS allowlist, stateless sessions, plus `InternalApiKeyAuthFilter` and `RequestIdFilter` for service-to-service auth and correlation IDs [US-21]
- Implemented `SupabaseAdminClient` (service-role provisioning) and `UserProvisioningService` for role assignment on signup [US-21]
- Wrote three more Supabase migrations: `seed_user_roles`, `jwt_custom_claims`, `auto_role_on_signup` — so role gets injected into the JWT `app_metadata` automatically on user creation [US-16]
- Implemented `POST /api/v1/auth/register` and `UserEventPublisher` publishing `user.provisioned` events to the `sharks.auth` exchange [US-01]
- Added `RabbitMqConfig` with `Jackson2JsonMessageConverter` and unit tests: `AuthControllerTest`, `SupabaseAuthClientTest`, `UserProvisioningServiceTest` [US-21]

**What am I planning to work on next?**
- Light sprint planned — code review, address feedback, fix any issues uncovered in integration

**What tasks are blocked?**
- None

#### Aniket Sarrin

**What did I work on / complete?**
- Implemented REST endpoints for user profile: `GET/PUT/PATCH /api/v1/users/me` and password change [US-08]
- Implemented REST endpoints for organizations: `GET/POST /api/v1/organizations` and `GET /api/v1/organizations/{id}` [US-08]
- Set up the RabbitMQ Docker image with `definitions.json` so the broker boots with all exchanges and queues already declared — avoids race conditions between services starting [US-15]
- Configured `docker-compose.yml` for local dev orchestrating Postgres (auth + user) + RabbitMQ + the services with healthcheck-based ordering [US-15]
- Implemented `UserCreatedListener` consuming `user.provisioned`, `OrganizationEventPublisher` publishing `organization.created`, and a `RabbitMqConfig` with Jackson converter [US-14]

**What am I planning to work on next?**
- Add `JwtUtils` for extracting user ID from bearer token in REST controllers (Sprint 3)

**What tasks are blocked?**
- None

#### Maurin Baroi

**What did I work on / complete?**
- Built the event discovery page with a search bar, category filters, date filter, and location filter — wired to the `GET /api/events` endpoint [US-04]
- Built the event detail page with description, schedule, organizer card, and an embedded Google Maps iframe; added the Google Calendar template URL integration [US-05]
- Built the My Tickets page with ticket codes, quantities, status badges, and refund button [US-06]
- Finalized the UML Component diagram showing the three services, RabbitMQ topic exchanges, Supabase, and external integrations [US-22]

**What am I planning to work on next?**
- Build Profile page skeleton (tabs scaffolding, avatar)
- Minor UI polish and navigation refinements

**What tasks are blocked?**
- Some user/org endpoints still being polished by Aniket — handled by stubbing with mocks until ready

---

# Sprint 3 — Mar 9 – Mar 22, 2026 *(light sprint)*

**Sprint Goal:** Light sprint due to midterms across all three members. Focus on bug fixes, code review, and small JWT/profile groundwork — keep momentum without committing to large stories.

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-21 | Fix BOM (Byte Order Mark) encoding issue in `SecurityConfig.java` that broke Docker builds | Ankush | 1 | ✓ |
| US-21 | Code review and minor refactoring of auth service | Ankush | 2 | ✓ |
| US-08 | Add `JwtUtils` for extracting user ID from bearer token in REST controllers | Aniket | 2 | ✓ |
| US-22 | Build Profile page skeleton (tabs scaffolding, avatar) | Maurin | 2 | ✓ |
| US-22 | Minor UI polish, navigation refinements, and bug fixes | Maurin | 2 | ✓ |
| **TOTAL** | **5/5 tasks completed** |  | **9** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Fixed the BOM (Byte Order Mark) encoding issue in `SecurityConfig.java` that was causing Docker builds to fail with a UTF-8 parse error — re-saved the file as UTF-8 without BOM and added a note in the README troubleshooting table [US-21]
- Code review pass on the auth service: cleaned up unused imports, tightened exception handling, and verified all endpoints still pass the Sprint 2 unit tests [US-21]

**What am I planning to work on next?**
- Add `/actuator/health` endpoint configuration in preparation for load balancer health checks (Sprint 4)
- Add Javadoc and inline comments to auth service classes (Sprint 4)

**What tasks are blocked?**
- Midterms — light sprint by design

#### Aniket Sarrin

**What did I work on / complete?**
- Added `JwtUtils` to the user & organizations service for extracting the user ID (sub claim) from the bearer token in REST controllers — keeps the controller layer thin and JWT parsing in one place [US-08]

**What am I planning to work on next?**
- Add Bean Validation and `UserNotFoundException` across user/org endpoints (Sprint 4)
- Test endpoints via Postman (Sprint 4)

**What tasks are blocked?**
- Midterms — light sprint by design

#### Maurin Baroi

**What did I work on / complete?**
- Built the Profile page skeleton — tabs scaffolding (Profile, Attending, Created, History) and avatar placeholder [US-22]
- Minor UI polish: navigation refinements, button styling consistency, mobile-responsive fixes on the discovery page [US-22]

**What am I planning to work on next?**
- Build Admin dashboard skeleton (Sprint 4)
- Continue frontend bug fixes

**What tasks are blocked?**
- Midterms — light sprint by design

---

# Sprint 4 — Mar 23 – Apr 5, 2026 *(light sprint)*

**Sprint Goal:** Continued light cadence (assignments and other coursework). Add health check infrastructure, validation polish on user/org APIs, and the Admin dashboard skeleton in preparation for the Sprint 5 catch-up.

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-21 | Add `/actuator/health` endpoint configuration (preparation for load balancer health checks) | Ankush | 1 | ✓ |
| US-21 | Add Javadoc and inline comments to auth service classes | Ankush | 2 | ✓ |
| US-08 | Add input validation (Bean Validation) and `UserNotFoundException` across user/org endpoints | Aniket | 2 | ✓ |
| US-08 | Test user and organization endpoints via Postman | Aniket | 1 | ✓ |
| US-12 | Build Admin dashboard skeleton (tabs scaffolding, stat card placeholders) | Maurin | 2 | ✓ |
| US-22 | Frontend bug fixes and minor responsive polish | Maurin | 2 | ✓ |
| **TOTAL** | **6/6 tasks completed** |  | **10** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Added `/actuator/health` endpoint configuration — required for the upcoming AWS Application Load Balancer health checks; verified locally via curl [US-21]
- Added Javadoc and inline comments to all auth service classes (controllers, clients, filters, config) for code-clarity and grading [US-21]

**What am I planning to work on next?**
- Heavy catch-up sprint planned: bootstrap the Event & Ticketing service, build `EventController` + `TicketController`, write `EventPublisher` + `PaymentListener`, finish internal endpoints (Sprint 5)

**What tasks are blocked?**
- None

#### Aniket Sarrin

**What did I work on / complete?**
- Added Bean Validation across all user and organization endpoints (`@NotBlank`, `@Email`, `@Size`) and a `UserNotFoundException` with proper HTTP 404 mapping via the global exception handler [US-08]
- Tested all user and organization endpoints via Postman — round-tripped through JWT auth, profile updates, password change, organization create/list/get [US-08]

**What am I planning to work on next?**
- Verify the full auth → user → organization RabbitMQ message flow end-to-end (Sprint 5)
- Implement notification consumer stub for ticket confirmation emails (Sprint 5)

**What tasks are blocked?**
- None

#### Maurin Baroi

**What did I work on / complete?**
- Built the Admin dashboard skeleton — 4 tabs (Overview / Users / Events / User Tickets) with stat card placeholders ready to wire to real data [US-12]
- Frontend bug fixes and minor responsive polish: discovery page card grid, login form spacing, dark-mode token pass [US-22]

**What am I planning to work on next?**
- Sprint 5 is the big push: organizer dashboard, mock payment, full Profile page, Forgot/Reset/Verify, Organizations browser, AWS deployment (EB + ALB + API Gateway), demo accounts

**What tasks are blocked?**
- None

---

# Sprint 5 — Apr 6 – Apr 19, 2026 *(catch-up sprint)*

**Sprint Goal:** Heavy catch-up sprint after midterms. Stand up the entire Event & Ticketing service (event/ticket APIs + admin moderation + EventPublisher + PaymentListener), build out the organizer/admin/profile UI, ship the mock payment flow, and deploy the full backend to AWS Elastic Beanstalk behind a dedicated Application Load Balancer with **AWS API Gateway** in front for HTTPS termination, CORS, and request throttling. **56 points — the largest sprint of the project.**

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-08 | Bootstrap Event & Ticketing service: Event/Ticket entities, repositories, services | Ankush | 3 | ✓ |
| US-08 | `EventController` CRUD: `GET/POST/PUT/DELETE /api/events` with `@Future`, `@Positive`, `@NotBlank` validation | Ankush | 4 | ✓ |
| US-06 | `TicketController` (purchase, my-tickets, refund) + admin moderation endpoints | Ankush | 4 | ✓ |
| US-14 | `EventPublisher` (event.created/updated/cancelled) + `PaymentListener` (payment.completed/failed) | Ankush | 3 | ✓ |
| US-21 | Implement Internal endpoints (JWKS, UserCreated) + final security review of all auth endpoints | Ankush | 3 | ✓ |
| US-14 | Verify full RabbitMQ message flow end-to-end: auth → user → organization service | Aniket | 3 | ✓ |
| US-20 | Implement notification consumer stub for ticket confirmation emails | Aniket | 3 | ✓ |
| US-15 | Verify `definitions.json` correctly pre-loads exchanges/queues; end-to-end testing of user/org service | Aniket | 3 | ✓ |
| US-08 | Build organizer dashboard (event list, stats, create event button) + event creation form | Maurin | 4 | ✓ |
| US-11 | Build admin dashboard with platform stats, pending approvals, event moderation panel (4 tabs) | Maurin | 3 | ✓ |
| US-06 | Implement mock payment flow with deterministic test cards + Payment Successful page | Maurin | 4 | ✓ |
| US-22 | Build full Profile page (4 tabs) + Forgot/Reset/Verify Email pages | Maurin | 4 | ✓ |
| US-09 | Build Organizations browser (2x2 grid) + Create Organization modal + attendee management | Maurin | 3 | ✓ |
| US-01 | Update register and deleteMe to call Supabase user_metadata + delete API with JWT | Maurin | 2 | ✓ |
| US-17 | Create IAM roles + deploy auth JAR to AWS Elastic Beanstalk (sharks-auth-prod, Corretto 17) | Maurin | 4 | ✓ |
| US-17 | Configure ALB (multi-AZ, min 2 / max 4 t3.micro) + set EB environment variables | Maurin | 3 | ✓ |
| US-18 | Set up AWS API Gateway HTTPS reverse proxy + update `api.ts` + create demo accounts + test login E2E | Maurin | 3 | ✓ |
| **TOTAL** | **17/17 tasks completed** |  | **56** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Bootstrapped the Event & Ticketing service: Spring Boot + Gradle, `Event` and `Ticket` entities (with category enum, capacity tracking, price, status), repositories, and service skeletons [US-08]
- Implemented full `EventController` CRUD: `GET /api/events` (list/search with filters), `GET/POST/PUT/DELETE /api/events/{id}` — Bean Validation with `@Future` on event date, `@Positive` on capacity/price, `@NotBlank` on required fields [US-08]
- Implemented `TicketController`: `POST /api/v1/tickets/purchase`, `GET /api/v1/tickets/my-tickets`, `PUT /api/v1/tickets/{id}/refund`. Wired admin moderation endpoints: `GET /api/v1/admin/dashboard`, `GET /api/v1/admin/users`, `GET /api/v1/admin/events`, `PATCH /api/v1/admin/events/{id}/approve|reject` [US-06]
- Built `EventPublisher` emitting `event.created` / `event.updated` / `event.cancelled` to the `sharks.event` exchange, and `PaymentListener` consuming `payment.completed` / `payment.failed` to confirm or release ticket reservations [US-14]
- Implemented Internal endpoints: `InternalJwksController` (JWKS for cross-service JWT validation) and `InternalUserCreatedController` (receives user creation hooks). Did the final security audit of all auth endpoints — verified RLS, CORS, internal API key checks [US-21]

**What am I planning to work on next?**
- Final end-to-end testing of all auth + event endpoints through the AWS stack
- Verify `EventPublisher` and `PaymentListener` round-trip messaging in the deployed environment
- Final GitHub push with clean commit history

**What tasks are blocked?**
- None

#### Aniket Sarrin

**What did I work on / complete?**
- Verified the full RabbitMQ message flow end-to-end: auth service publishes `user.provisioned` → user service consumes and creates profile → `organization.created` propagates to event service. Walked the messages through the RabbitMQ management UI for each step [US-14]
- Implemented the notification consumer stub for ticket confirmation emails — listens for `ticket.purchased` and logs/queues a notification (full email send is the Sprint 6 in-progress story) [US-20]
- Verified `definitions.json` correctly pre-loads all exchanges and queues on RabbitMQ container startup (auth/user/event/payment topic exchanges + their bound queues). Ran full end-to-end testing of the user & organizations service against the integrated stack [US-15]

**What am I planning to work on next?**
- Final end-to-end testing of user/org APIs and the full RabbitMQ pipeline
- Document RabbitMQ messaging topology in `MESSAGING.md` with exchange/queue reference table
- Demo dry run preparation

**What tasks are blocked?**
- None

#### Maurin Baroi

**What did I work on / complete?**
- Built the organizer dashboard with event list, stats (events / RSVPs / revenue), create event button — and the full event creation form with all fields (title, description, date/time, location, capacity, price, category) [US-08]
- Built the admin dashboard with 4 tabs (Overview / Users / Events / User Tickets), stat cards wired to `/api/v1/admin/dashboard`, role-assignment dropdowns, and the event approval table [US-11]
- Implemented the mock payment flow with deterministic test cards (`4242` → success, `4000 0002` → declined, `4000 9995` → insufficient funds, `4000 0069` → expired) and a dedicated Payment Successful page with transaction ID, card last 4, and amount [US-06]
- Built the full Profile page (4 tabs: Profile / Attending / Created / History) with name+email update, change password, delete account; plus Forgot Password, Reset Password, and Verify Email flows handling Supabase email-link redirects [US-22]
- Built the Organizations browser (2x2 grid with logo, name, description, member count) and the Create Organization modal; built the organizer attendee management page [US-09]
- Updated the register flow to call Supabase directly storing name+role in `user_metadata`; updated deleteMe to call the Supabase delete API using the user's JWT [US-01]
- Created IAM roles (Elastic Beanstalk service role + EC2 instance profile with least-privilege policies) and deployed the auth JAR to AWS Elastic Beanstalk (`sharks-auth-prod`, `us-east-1`, Corretto 17) [US-17]
- Configured the dedicated public Application Load Balancer (multi-AZ, min 2 / max 4 t3.micro instances, `/actuator/health` checks) and set all EB environment variables (Supabase URL, anon key, service-role key, JWT issuer, internal API key, RabbitMQ host) [US-17]
- Set up **AWS API Gateway** as the HTTPS reverse proxy in front of the ALB (HTTPS termination, CORS, and request throttling), updated `api.ts` to use the API Gateway endpoint, created Supabase demo accounts (alice / bob / carol = admin / organizer / attendee), and verified login end-to-end through the full AWS stack — Network tab shows POST to AWS URL with 200 + JWT [US-18]

**What am I planning to work on next?**
- Connect custom domain `sharks-eventbrite.live` with DNS + SSL
- Update CORS for the new domain and redeploy the JAR
- Write all project documentation (UI Wireframes, UML diagrams, README, Project Journal, Scrum backlog spreadsheet)

**What tasks are blocked?**
- None

---

# Sprint 6 — Apr 20 – May 7, 2026

**Sprint Goal:** Land the custom domain, finalize CORS for production, complete all required documentation (UI wireframes, UML diagrams, README, project journal, scrum backlog), and run a full demo dry-run across all 3 roles. Final end-to-end verification across the deployed AWS stack.

### Sprint Backlog

| Story | Task | Assignee | Pts | Done |
|---|---|---|---|---|
| US-21 | Final E2E testing of all auth endpoints through AWS stack (login, register, JWT validation) | Ankush | 2 | ✓ |
| US-08 | Final E2E testing of Event & Ticketing: event creation, ticket purchase, refund, admin approval | Ankush | 2 | ✓ |
| US-14 | Verify `EventPublisher` and `PaymentListener` round-trip messaging across RabbitMQ in deployed env | Ankush | 2 | ✓ |
| US-17 | Verify ALB routes correctly across both EC2 instances + Supabase RLS policies | Ankush | 2 | ✓ |
| US-21 | Final GitHub push (auth + event service code, migrations, tests) with clean commit history | Ankush | 2 | ✓ |
| US-15 | Final E2E testing of user/org APIs and full RabbitMQ pipeline | Aniket | 2 | ✓ |
| US-14 | Document RabbitMQ messaging topology in `MESSAGING.md` with exchange/queue reference table | Aniket | 2 | ✓ |
| US-15 | Final cleanup and documentation of user & organizations service + final GitHub push | Aniket | 2 | ✓ |
| US-22 | Demo preparation and dry run of all 3 role logins (admin, organizer, attendee) | Aniket | 2 | ✓ |
| US-19 | Connect custom domain `sharks-eventbrite.live` (Route 53 DNS + SSL certificate) | Maurin | 3 | ✓ |
| US-17 | Update CORS for `sharks-eventbrite.live` + www subdomain; rebuild and redeploy auth JAR (v1-2) | Maurin | 3 | ✓ |
| US-22 | Create UI wireframes for all 15 screens across 3 user roles | Maurin | 4 | ✓ |
| US-22 | Create final UML Component diagram and Deployment diagram (AWS architecture) | Maurin | 3 | ✓ |
| US-22 | Write README with full architecture, deployment instructions, demo accounts, troubleshooting | Maurin | 3 | ✓ |
| US-22 | Create Scrum backlog spreadsheet with product backlog, sprint backlogs, and burndown chart | Maurin | 2 | ✓ |
| US-22 | Write Project Journal with all 6 sprint scrum reports, XP values, team contributions | Maurin | 3 | ✓ |
| US-22 | Add Procfile and `.ebextensions` AWS deployment configs to repository | Maurin | 1 | ✓ |
| US-20 | Final demo prep + end-to-end dry run across all 3 role logins; polish remaining edges | Maurin | 2 | ✓ |
| **TOTAL** | **18/18 tasks completed** |  | **42** | **100%** |

### Weekly Scrum Reports

#### Ankush Makhijani

**What did I work on / complete?**
- Final end-to-end testing of all auth endpoints through the AWS stack (login, register, JWT validation, /me) — all green [US-21]
- Final end-to-end testing of the Event & Ticketing service: event creation by an organizer, ticket purchase via the mock payment flow, refund flow, and the admin approval workflow [US-08]
- Verified `EventPublisher` and `PaymentListener` round-trip messaging across RabbitMQ in the deployed environment — followed `event.created` and `payment.completed` messages through the management UI [US-14]
- Verified the Application Load Balancer correctly routes traffic across both EC2 instances and that Supabase Row Level Security policies still enforce per-user data isolation in production [US-17]
- Final GitHub push with all auth + event service code, migrations, and tests — clean commit history with descriptive messages [US-21]

**What am I planning to work on next?**
- Demo Day presentation (May 7, 2026)

**What tasks are blocked?**
- None

#### Aniket Sarrin

**What did I work on / complete?**
- Final end-to-end testing of user and organization API endpoints against the integrated stack [US-15]
- Documented the full RabbitMQ messaging topology in `MESSAGING.md` with an exchange/queue/binding reference table so any future contributor can see the topic-routing scheme at a glance [US-14]
- Final cleanup and documentation of the user & organizations service; final GitHub push with descriptive commit messages [US-15]
- Demo preparation and dry run of all 3 role logins (admin, organizer, attendee) — verified the full flows work as expected [US-22]

**What am I planning to work on next?**
- Demo Day presentation (May 7, 2026)

**What tasks are blocked?**
- None

#### Maurin Baroi

**What did I work on / complete?**
- Connected the custom domain `sharks-eventbrite.live` with Route 53 DNS configuration and SSL certificate provisioning — site loads on the custom domain over HTTPS [US-19]
- Updated `SecurityConfig` CORS to include `sharks-eventbrite.live` and `www.sharks-eventbrite.live`; rebuilt and redeployed the auth JAR to Elastic Beanstalk (version v1-2) [US-17]
- Created UI wireframes for all 15 screens across 3 user roles (Public, Attendee, Organizer, Admin) and packaged them as a single PDF deliverable [US-22]
- Created the final UML Component diagram and Deployment diagram showing the AWS architecture (API Gateway → ALB → backend services on EB → RabbitMQ + Supabase) [US-22]
- Wrote the README with full architecture overview, deployment instructions, demo account credentials, and a troubleshooting table [US-22]
- Created the Scrum backlog spreadsheet with the product backlog, per-sprint backlogs, sprint velocity table, and a daily burndown chart for Sprint 6 [US-22]
- Wrote this Project Journal with all 6 sprint scrum reports, XP values, and per-member contribution tables [US-22]
- Added Procfile and `.ebextensions` AWS deployment configs to the repository for reproducible EB deployments [US-22]
- Final demo preparation and end-to-end dry run across all 3 role logins; polished the remaining UI edge cases (loading states, empty states, error toasts) [US-20]

**What am I planning to work on next?**
- Demo Day presentation (May 7, 2026)

**What tasks are blocked?**
- None

---

## Burndown Summary

All 202 planned story points across all 6 sprints were completed (100% velocity per sprint). The full sprint-by-sprint burndown chart, daily burndown for Sprint 6, and product backlog with priorities/owners/acceptance criteria are maintained in the team's Google Sheet.

📊 [Scrum Backlog & Burndown Chart (Google Sheet)](https://docs.google.com/spreadsheets/d/1d1vWJW_v5CGMG9sdQAqgiE5aPG8JOLfR/edit?gid=1083467952#gid=1083467952)

📋 [Task Board (GitHub Projects)](https://github.com/orgs/gopinathsjsu/projects/155)

---

## Artifact Links

| Artifact | Location |
|---|---|
| UI Wireframes | `documentation/UI Wireframes/` |
| Component Diagram | `documentation/Component Diagram.jpg` |
| Deployment Diagram | `documentation/Deployment Diagram.jpg` |
| ER Diagram | `documentation/ER-diagram.png` |
| RabbitMQ Topology | `backend/infrastructure/MESSAGING.md` |
| API Endpoints Overview | `documentation/API Endpoints Overview.txt` |
| Scrum Backlog (Google Sheet) | [Open in Google Sheets](https://docs.google.com/spreadsheets/d/1d1vWJW_v5CGMG9sdQAqgiE5aPG8JOLfR/edit?gid=1083467952#gid=1083467952) |
| Task Board (GitHub Projects) | [Open in GitHub](https://github.com/orgs/gopinathsjsu/projects/155) |
| README | `README.md` |
