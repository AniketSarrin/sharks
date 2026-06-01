# Sharks — EventBrite Platform

**CMPE 202-02 (Spring 2026) — Team Project**
A multi-service event management platform: attendees discover and book events, organizers create and manage them, admins moderate the platform.

🌐 **Live:** [sharks-eventbrite.live](https://sharks-eventbrite.live)
📦 **Repo:** [gopinathsjsu/team-project-cmpe202-02-spring2026-sharks](https://github.com/gopinathsjsu/team-project-cmpe202-02-spring2026-sharks)

## Team

| Team member | Summary of contribution areas |
| --- | --- |
| **Ankush Makhijani** | Auth Service Lead (Spring Boot + Supabase GoTrue), Event & Ticketing Service Lead (Event/Ticket APIs, Payment Listener, Admin Moderation).Supabase Migrations & Row Level Security, RabbitMQ Event Publishing, Unit Tests |
| **Aniket Sarrin** | User & Organizations Service, Admin Service, JWT Authentication, RabbitMQ Pub sub between User and Org service to auth service |
| **Maurin Baroi** | Frontend Lead (React/TypeScript), Mock Payment Service, AWS Production Deployment (Elastic Beanstalk + ALB + API Gateway), Custom Domain & DNS, UI Wireframes, Architecture Diagrams, Documentation, Database migrations |

[**Sprint Journal + Backlog + Burndown Chart (Google Sheet)**](https://docs.google.com/spreadsheets/d/1d1vWJW_v5CGMG9sdQAqgiE5aPG8JOLfR/edit?gid=1083467952#gid=1083467952) [**Project Journal**](./project_journal.md) [**Task Board (GitHub Projects)**](https://github.com/orgs/gopinathsjsu/projects/155) [**UI Wireframes**](./documentation/uiwireframes/UI%20Wireframes.pdf)

The team followed Scrum practices over six 2-week sprints (Feb 9 – May 7, 2026) to develop the Sharks platform.

## What is Sharks?

Sharks is an event-management platform used by:

- **Attendees** — discover, view, and book free or paid events
- **Organizers** — create and manage events, track RSVPs, view stats
- **Admins** — approve/reject events, manage users, oversee platform analytics

## Functional Requirements

- User authentication and role-based access (attendee, organizer, admin)
- Event creation and management with date, time, location, and capacity
- Event discovery with search, filters, and category-based browsing
- Event detail pages with description, schedule, and organizer information
- Ticketing and registration with mock payment flow
- Google Calendar integration for saving events
- Google Maps integration for in-person events
- RSVP tracking and attendee management for organizers
- Email notifications via Supabase GoTrue (verification, password reset)
- Admin moderation and event approval workflow
- Secure API design with input validation and structured error responses
- Responsive React UI for web devices

## Non-Functional Requirements

- **Availability** — load-balanced backend on AWS with health checks and auto-scaling
- **Scalability** — stateless services behind an ALB; horizontal scaling via Elastic Beanstalk
- **Security** — JWT auth, Row Level Security, stateless sessions, CORS allowlist, internal API keys
- **Reliability** — async coordination via RabbitMQ so a single service outage doesn't cascade
- **Maintainability** — Infrastructure-as-Code (Terraform), Docker Compose for local dev parity

## Tech Stack

Java 17 (Spring Boot 4.0.5 microservices), React 18 + Vite 5 + TypeScript 5.8, PostgreSQL (Supabase managed), RabbitMQ, Tailwind CSS + shadcn/ui, Docker Compose, Terraform, AWS Ec2 + EKS + ALB + API Gateway, Supabase (auth + Postgres + RLS), Google Maps embed, Bun (frontend package manager), Gradle (backend build), Git, Google Sheets (sprint backlog), GitHub Projects (task board), Discord + weekly Scrum meetings (team comms).

## Design Decisions

### Tech Stack Rationale

- **React + Vite + TypeScript** for the frontend — fast dev server, strict typing, and the team's existing familiarity. SWC over Babel for build speed.
- **Spring Boot 4 (Java 17)** for backend microservices — picked over Express for stricter typing, mature dependency injection, and first-class Spring Security + Spring AMQP support.
- **Supabase** for authentication and Postgres — generous free tier, managed GoTrue auth, built-in Row Level Security, and JWT custom claims out of the box. Considered AWS Cognito + RDS but the operational overhead and cost were not worth it for a class project.
- **RabbitMQ** for inter-service messaging — chosen over SQS so the broker runs locally in Docker (no AWS account required for development) and supports topic exchanges for fan-out patterns. Topology is pre-loaded via definitions.json so the broker comes up consistent every time.
- **AWS Elastic Beanstalk + Application Load Balancer + API Gateway** for deployment — simpler than ECS or Kubernetes for a class project, gives auto-scaling and HTTPS termination out of the box, and the dedicated ALB satisfies the project's load-balancer requirement.
- **Google Maps embed** for in-person event location — easy iframe embed, no API key required for the read-only embed view.
- **Tailwind CSS + shadcn/ui** for the design system — Radix UI primitives wrapped with Tailwind utility classes; consistent look without writing component CSS from scratch.
- **Terraform** alongside Docker Compose — Compose for local dev convenience, Terraform for reproducible AWS infrastructure across environments.

### Functional Decisions

- **Three explicit roles** (attendee, organizer, admin) stored in Supabase user_metadata and surfaced as JWT custom claims via a Supabase migration. Frontend reads app_metadata.roles from the decoded JWT to gate role-specific UI; backends check the same claim via Spring Security.
- **Public registration cannot self-assign ADMIN.** The signup flow only offers Attendee or Organizer; admin role assignment requires a service-role API call via SupabaseAdminClient, which is locked behind an admin-only endpoint.
- **Async event flow over RabbitMQ** rather than direct REST calls between services. Four topic exchanges (sharks.auth, sharks.user, sharks.event, sharks.payment) decouple services so an outage in one doesn't cascade — the auth service can publish user.provisioned even if the user service is restarting, and the message will be delivered when it comes back.
- **Mock payment flow** instead of integrating Stripe — the project rubric allows free events or mock payments, and a mocked payment service kept the demo focused on platform mechanics rather than payment-provider plumbing. The mock service still emits payment.completed/payment.failed events on RabbitMQ so the full async flow is exercised end-to-end.
- **Demo accounts on the login page** (alice@example.com / bob@example.com / carol@example.com — admin / organizer / attendee) so graders can switch between roles instantly without registering. Gated off in production builds.

## Feature Set

- ✅ User authentication and role-based access (attendee, organizer, admin)
- ✅ Event creation and management with date, time, location, and capacity
- ✅ Event discovery with search, filters, and category-based browsing
- ✅ Event detail pages with description, schedule, and organizer information
- ✅ Ticketing and registration with mock payment flow (test cards: 4242 → success, 4000 0002 → declined)
- ✅ Google Calendar integration via prefilled template URLs
- ✅ Google Maps integration for in-person events
- ✅ RSVP tracking and attendee management for organizers
- ✅ Email notifications via Supabase GoTrue (verification, password reset)
- ✅ Admin moderation and event approval workflow
- ✅ Secure API design with input validation (Bean Validation @NotBlank, @Future, @Positive)
- ✅ Deployed to AWS in a cluster with a dedicated Application Load Balancer
- ✅ Responsive React UI for web devices

## Architecture Diagrams

| Diagram | File |
| --- | --- |
| Component Diagram | [documentation/Component Diagram.jpg](./documentation/Component%20Diagram.jpg) |
| Deployment Diagram | [documentation/Deployment Diagram.png](./documentation/Deployment%20Diagram.png) |
| ER Diagram | [documentation/ER-diagram.png](./documentation/ER-diagram.png) |
| RabbitMQ Topology | [backend/infrastructure/MESSAGING.md](./backend/infrastructure/MESSAGING.md) |
| API Endpoints Overview | [documentation/API Endpoints Overview.txt](./documentation/API%20Endpoints%20Overview.txt) |

The platform is three Spring Boot microservices behind an AWS API Gateway, communicating asynchronously through a RabbitMQ topic-exchange topology, with Supabase providing managed authentication and Postgres storage.

## Two XP Values

**1. Communication** — We held weekly Scrum meetings to align on progress, blockers, and priorities, and used Discord for async coordination between meetings. We agreed on API contracts early — the shared api.ts interface defined the JSON input/output for every endpoint before implementation began, which prevented integration issues between the frontend and the three backend services. RabbitMQ exchange and queue naming conventions were documented in MESSAGING.md and agreed upon before any service started publishing or consuming messages. Design decisions like using Supabase for auth, RabbitMQ for async coordination, and AWS Elastic Beanstalk for deployment were all discussed as a team rather than imposed by any one member. When one of us hit a wall — Ankush's BOM encoding bug breaking Docker builds, Maurin's CORS issues across AWS domains, Aniket's RabbitMQ connection retries warning the health check — the others pitched in to debug, even when it was outside their owned component.

**2. Feedback** — Every pull request was reviewed by at least one other team member before merging, with discussion on the PR or in our weekly sprint meeting. This caught real issues throughout the project — Ankush's BOM encoding bug breaking Docker builds, RabbitMQ connection retries causing health-check warnings, and the CORS misconfiguration between the frontend and the auth service when we eventually deployed to AWS in the final sprints. Each team member tested the others' components end-to-end against Docker Compose locally before any cloud deployment: Ankush's auth service was driven through the frontend by Maurin, and Aniket's messaging pipeline was verified by following messages across all services. We reviewed the professor's demo rubric at the end of each sprint as a feedback checklist, and the gap between what the rubric expected and what we'd built drove the priorities for the following sprint.

# Team Contributions

## Ankush Makhijani

Primary ownership: **Auth Service, Supabase Migrations & RLS, Spring Security, Auth Event Publishing**

### 1) Auth Service (backend/auth/)

| Area | What was delivered |
| --- | --- |
| Service bootstrap | Spring Boot 4 service with Gradle build, Java 17 |
| Authentication | SupabaseAuthClient using GoTrue password grant for login; AuthController exposing register, login, verify-email, resend-verification, forgot-password, reset-password, and me endpoints |
| Roles & domain | AppRole enum (ATTENDEE, ORGANIZER, ADMIN), UserRole model |
| Configuration | SupabaseProperties binding Supabase URL, anon key, service-role key, and JWT issuer via environment variables |
| Admin operations | SupabaseAdminClient for service-role admin user provisioning and role assignment |
| User provisioning | UserProvisioningService orchestrates Supabase signup + role assignment + downstream notification on first login |
| API design | Versioned REST APIs under /api/v1/auth; consistent JSON responses; structured error handling |

### 2) Event Service and Ticketing Service (backend/event and backend/ticketing/)

| Area | What was delivered |
| --- | --- |
| Event APIs | GET /api/events (list/search with filters), GET /api/events/{id}, POST /api/events, PUT /api/events/{id}, DELETE /api/events/{id} |
| Ticket APIs | POST /api/v1/tickets/purchase, GET /api/v1/tickets/my-tickets, PUT /api/v1/tickets/{id}/refund |
| Admin APIs | GET /api/v1/admin/dashboard, GET /api/v1/admin/users, GET /api/v1/admin/events, PATCH /api/v1/admin/events/{id}/approve |
| Entities | Event, Ticket with category enum, capacity tracking, price, and status fields |
| Validation | @Future for event date, @Positive for capacity/price, @NotBlank for required fields |
| Messaging | EventPublisher for event.created, ticket.purchase.initiated, etc.; PaymentListener for payment.completed/payment.failed |

### 3) Spring Security & JWT

| Area | What was delivered |
| --- | --- |
| JWT validation | oauth2ResourceServer configured with the Supabase JWT issuer URI for stateless validation |
| SecurityConfig | CORS allowlist (sharks-eventbrite.live + localhost), SessionCreationPolicy.STATELESS, role-based access rules |
| Custom filters | InternalApiKeyAuthFilter for /internal/** service-to-service auth; RequestIdFilter attaching correlation IDs to every request |
| Internal endpoints | InternalJwksController (JWKS for cross-service validation), InternalUserCreatedController for receiving user creation hooks |
| Anti-enumeration | forgot-password always returns 200 to prevent email enumeration |

### 4) Supabase Migrations & Row Level Security

| Area | What was delivered |
| --- | --- |
| Schema migrations | init_app_role_and_user_roles, user_roles_rls, seed_user_roles, auto_role_on_signup, jwt_custom_claims |
| RLS policies | Postgres Row Level Security ensuring users can only read/write their own profile and ticket history |
| Custom JWT claims | Migration that injects user role into JWT app_metadata so frontend + backends can read role from a single source of truth |
| Seed data | Seeded user roles for development and demo accounts |

### 5) RabbitMQ Event Publishing

| Area | What was delivered |
| --- | --- |
| Message converter | RabbitMqConfig with Jackson2JsonMessageConverter for typed message serialization |
| Publishers | UserEventPublisher publishing user.provisioned events on successful registration |
| Consumers | UserCreatedListener consuming user.created events from the user service |
| Configuration | AuthRabbitProperties binding exchange and queue names via environment variables |
| Topology contribution | Owns the sharks.auth exchange and auth.user.created queue bindings |

### 6) Tests & Cross-cutting

| Area | What was delivered |
| --- | --- |
| Unit tests | AuthControllerTest, SupabaseAuthClientTest, UserProvisioningServiceTest |
| Bug fixes | Resolved BOM (Byte Order Mark) encoding issue in SecurityConfig.java that was causing Docker builds to fail |
| Documentation | Auth service README, JWT flow diagrams, demo account credentials |

## Aniket Sarrin

Primary ownership: **User & Organizations Service, Event & Ticketing Service, RabbitMQ Pub/Sub Topology, Docker Compose Infrastructure**

### 1) User & Organizations Service (backend/user_and_organizations/)

| Area | What was delivered |
| --- | --- |
| Service bootstrap | Spring Boot 4 service with Spring Data JPA, jjwt 0.12.5, Postgres driver |
| User APIs | GET /api/v1/users/me, PUT /api/v1/users/me, PATCH /api/v1/users/me, PATCH /api/v1/users/me/password, DELETE /api/v1/users/me |
| Organization APIs | GET /api/v1/organizations (list), POST /api/v1/organizations (create), GET /api/v1/organizations/{id} (detail) |
| Admin APIs | PATCH /api/v1/users/{id}/role, PATCH /api/v1/users/{id}/status for admin role/status management |
| DTOs & entities | UserDto, UserUpdateRequest, OrganizationDto, CreateOrganizationRequest, UserProfile, UserRole, Organization |
| Repositories | UserProfileRepository, OrganizationRepository with JPA query methods |
| Services | UserService, OrganizationService with full CRUD and business rules |
| Security | JwtUtils for extracting user ID from bearer token, JwtAuthenticationFilter, custom SecurityConfig |
| Validation | Bean Validation across all endpoints, UserNotFoundException with HTTP 404 |

## Maurin Baroi

Primary ownership: **Frontend Application, Mock Payment Service, AWS Production Deployment, Custom Domain, UI Wireframes, Documentation**

### 1) Frontend Application (frontend/)

| Area | What was delivered |
| --- | --- |
| Architecture | Vite 5.4 + React 18 + TypeScript 5.8 app with React Router v6 across 15 routes (Login, Signup, Index, EventDetail, Checkout, MyTickets, Profile, CreateEvent, Organizations, OrganizationDetail, AdminDashboard, ForgotPassword, ResetPassword, VerifyEmail, NotFound) |
| Design system | Tailwind CSS 3.4 + shadcn/ui (Radix UI primitives), responsive layout, navy + Sharks blue theme |
| Auth flow | Login / Signup / Forgot-Password / Reset-Password integrated with Supabase + auth backend; JWT persistence with refresh handling; AuthContext for role-gated routing |
| Event discovery | Search bar, category filters, date/location filters, event grid with category icons (Browse by Category) |
| Event detail | Description, schedule, organizer card, embedded Google Maps, registration progress bar, calendar/share buttons |
| Ticketing & payment | Quantity selector, mock payment form, deterministic test cards, Payment Successful confirmation page |
| Organizer dashboard | Create Event form (Basic Info + Date/Time + Location + Tickets), event list with stats, attendee management |
| Admin dashboard | 4 tabs (Overview / Users / Events / User Tickets) with stat cards, role assignment dropdowns, event approval table |
| Profile | 4 tabs (Profile / Attending / Created / History), name + email update, change password, delete account |
| Forms & validation | React Hook Form + Zod schemas; inline error messages |
| Testing | Vitest + Testing Library unit tests |

### 2) Mock Payment Service

| Area | What was delivered |
| --- | --- |
| Client-side simulator | frontend/src/lib/payment-service simulates the full Stripe-style payment lifecycle |
| Test cards | Deterministic flows: 4242 4242 4242 4242 → success, 4000 0000 0000 0002 → declined, 4000 0000 0000 9995 → insufficient funds, 4000 0000 0000 0069 → expired |
| Mock transactions | Generates mock transaction IDs (e.g. mock_txn_movoleiy_ev4xp0g92f) and integrates with the ticket purchase flow |
| Confirmation UI | Payment Successful page with transaction details, card last 4, amount, quantity, and view-tickets / back-to-home actions |

### 3) AWS Production Deployment

| Area | What was delivered |
| --- | --- |
| Runtime | Deployed the auth service backend to AWS Elastic Beanstalk (sharks-auth-prod, us-east-1, Amazon Corretto 17 platform) |
| Load balancing & autoscaling | Dedicated public Application Load Balancer with min 2 / max 4 t3.micro instances, multi-AZ, auto-scaling, target group with /actuator/health health checks |
| API Gateway | HTTPS reverse proxy in front of the ALB for HTTPS termination, CORS, and request throttling |
| IAM | Service role for Elastic Beanstalk + EC2 instance profile with least-privilege policies |
| Environment configuration | Set Supabase URLs, anon key, service-role key, JWT issuer, internal API key, and RabbitMQ host via the EB configuration panel |
| Health monitoring | Configured /actuator/health endpoint for ALB health checks; verified environment health green |
| Repository assets | Added Procfile and .ebextensions AWS deployment config to the repository |

### 4) Custom Domain & DNS

| Area | What was delivered |
| --- | --- |
| Domain | Registered and configured sharks-eventbrite.live |
| DNS & SSL | Route 53 DNS configuration, SSL certificate provisioning |
| CORS | Updated SecurityConfig to allow the production domain (sharks-eventbrite.live + www.sharks-eventbrite.live) and rebuilt + redeployed the JAR |

### 5) UI Wireframes & Architecture Diagrams

| Area | What was delivered |
| --- | --- |
| Wireframes | 15 wireframes covering every screen in the live application across 3 roles ([documentation/uiwireframes/UI Wireframes.pdf](./documentation/uiwireframes/UI%20Wireframes.pdf)) |
| Component Diagram | UML component diagram showing service boundaries and integrations |
| Deployment Diagram | UML deployment diagram showing AWS infrastructure, Supabase, and RabbitMQ |
| ER Diagram | Data model diagram for the User / Event / Ticket / Organization entities |
| README | This document |
| Project Journal | Sprint reports for all 6 sprints across all 3 team members |

### 6) Cross-cutting

| Area | What was delivered |
| --- | --- |
| API contract | Shared api.ts defining request/response shapes for every endpoint, agreed before implementation |
| Vite proxy | /api/events/* → port 8082, /api/v1/* → port 8080 — local dev hits real backends without CORS hassle |
| Integration testing | End-to-end testing of full user flows across frontend + 3 backend services |
| Demo prep | Final demo dry runs across all 3 role logins |

## Service Map

| Service | Port | Health endpoint | Database |
| --- | --- | --- | --- |
| auth | 8080 | /actuator/health | Supabase Postgres (managed) |
| user_and_organizations | 8081 | /actuator/health | user-postgres (5432, container) |
| event_and_ticketing | 8082 | /actuator/health | event-postgres (5432, container) |
| RabbitMQ AMQP | 5672 | (broker) | — |
| RabbitMQ Management UI | 15672 | [http://localhost:15672](http://localhost:15672) (guest/guest) | — |
| Frontend (Vite dev) | 5173 | [http://localhost:5173](http://localhost:5173) | — |

## Running Locally

The backend is 4 Spring Boot microservices plus 4 Postgres databases and RabbitMQ and s3 storage for images, all orchestrated via Docker Compose. The compose file lives at backend/docker-compose.yml and is the source of truth for ports, env vars, and service ordering.

### Prerequisites

- Java 17 (Amazon Corretto or Eclipse Temurin)
- Bun (or Node.js 18+) for the frontend
- Docker + Docker Compose (docker --version, docker compose version)
- A Supabase project (cloud or local)
- Free host ports: 5432 (Postgres), 5672 + 15672 (RabbitMQ), 8080–8082 (services), 5173 (frontend)

### Apply Supabase migrations

Run the SQL files in backend/auth/supabase/migrations/ against your Supabase project's database, in order. Then apply database_migrations/supabase_migration script with seed data.txt for the full event/ticket schema and seed data.

### Start backend

```bash
cd backend

export SUPABASE_URL=https://<your-project>.supabase.co
export SUPABASE_ANON_KEY=<anon key>
export SUPABASE_SERVICE_ROLE_KEY=<service role key>
export SUPABASE_JWT_ISSUER=https://<your-project>.supabase.co/auth/v1
export AUTH_INTERNAL_API_KEY=$(openssl rand -hex 24)

docker compose up --build -d
```

First build downloads Maven/Gradle dependencies for all services and takes ~5–10 minutes. Subsequent runs start in seconds. Compose handles ordering automatically — Postgres and RabbitMQ must be healthy before services start.

### Verify the stack

```bash
docker compose ps                                   # every service should show "healthy"
curl http://localhost:8080/actuator/health           # auth
curl http://localhost:8081/actuator/health           # user & orgs
curl http://localhost:8082/actuator/health           # event & ticketing
```

### Watching logs

```bash
docker compose logs -f --tail=100                    # all services, follow
docker compose logs -f auth                          # one service
```

### Frontend

```bash
cd frontend
bun install        # first time only
bun dev            # http://localhost:5173 — proxies /api/v1/* + /api/events/* to the backends
```

### Common variants

```bash
docker compose up --build                            # foreground (Ctrl-C stops)
docker compose up -d                                 # no rebuild
docker compose up --build -d auth                    # rebuild a single service
docker compose up --build --force-recreate -d        # recreate even if config unchanged
```

### Stop / kill all services

```bash
cd backend
docker compose down                                  # stop + remove containers, KEEP Postgres data
docker compose down -v                               # stop + remove containers AND wipe Postgres data
docker compose stop                                  # stop containers but keep them (faster restart)
docker compose start                                 # restart previously-stopped containers
```

### Tests

```bash
cd frontend && bun test
cd backend/auth && ./gradlew test
cd backend/user_and_organizations && ./gradlew test
cd backend/event_and_ticketing && ./gradlew test
```

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| port already allocated | `lsof -ti:8080 \| xargs kill` (replace 8080 with the conflicting port) |
| Service stuck in unhealthy | `docker compose logs <service>` — usually DB or RabbitMQ not ready; increase healthcheck start_period |
| Postgres init didn't run | `docker compose down -v` to wipe the volume; init scripts only run on first boot |
| Auth login returns 401 | Verify Supabase env vars (SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_JWT_ISSUER) match the project the user was created in |
| Booking returns "Event not found" | event_and_ticketing hadn't fully started before the booking call — `docker compose restart event_and_ticketing` recovers |
| RabbitMQ consumer logs nothing | Producer publish failed (check service logs) or topology not loaded — check `docker compose logs rabbitmq` for definitions.json parse errors |
| Docker build fails on Java source | BOM (Byte Order Mark) in source file — re-save as UTF-8 without BOM |
| CORS errors from frontend | Frontend origin not in SecurityConfig allowlist; rebuild + redeploy auth JAR after updating |
| Vite proxy 502 errors | Backend service not running; check `docker compose ps` |

## API Endpoints (Summary)

All public traffic enters through AWS API Gateway and is routed to the appropriate service. Internal endpoints (/internal/**) require the X-Internal-Api-Key header for service-to-service communication.

| Service | Path Prefix | Notes |
| --- | --- | --- |
| Auth | /api/v1/auth/* | login, register, verify-email, forgot-password, reset-password, me |
| Users | /api/v1/users/* | profile CRUD, role updates (admin), password change |
| Organizations | /api/v1/organizations/* | list, create, get by ID |
| Events | /api/events/* | list/search, create, update, delete |
| Tickets | /api/v1/tickets/* | purchase, my-tickets, refund |
| Admin | /api/v1/admin/* | dashboard stats, user / event / ticket management |
| Internal | /internal/* | JWKS, user provisioning hooks (service-to-service) |

Full endpoint list with request/response shapes in [documentation/API Endpoints Overview.txt](./documentation/API%20Endpoints%20Overview.txt).

## Security Design

- **Authentication** — JWTs issued by Supabase GoTrue (HS256, 24h expiry); all backend services validate tokens via Spring's oauth2ResourceServer.
- **Password storage** — handled by Supabase (bcrypt via pgcrypto); no plaintext or hashed passwords ever stored or transmitted by Sharks services.
- **Email verification** — required before login; verify-email and resend endpoints route through GoTrue.
- **Roles & authorization** — stored in Supabase user_metadata, surfaced as JWT custom claims. Public registration cannot self-assign ADMIN.
- **Row Level Security** — Postgres RLS policies ensure each authenticated user can only read/write their own profile and ticket history.
- **Service-to-service auth** — internal endpoints require X-Internal-Api-Key header, validated by InternalApiKeyAuthFilter. JWKS exposed via /internal/jwks for cross-service JWT validation.
- **CORS** — SecurityConfig allows the frontend origin (sharks-eventbrite.live + localhost) and rejects others.
- **Stateless sessions** — all services run with SessionCreationPolicy.STATELESS; no server-side session storage.
- **Anti-enumeration** — forgot-password always returns 200 regardless of whether the email exists, to prevent email enumeration attacks.
- **Request tracing** — RequestIdFilter attaches a correlation ID to every request for log aggregation across services.
