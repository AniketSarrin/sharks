# RabbitMQ Pub/Sub Architecture

This document describes the RabbitMQ messaging infrastructure used to enable asynchronous communication between microservices.

## Overview

The system uses **Topic-based Exchange and Queue bindings** with RabbitMQ to facilitate loose coupling between services. Each service can publish events and subscribe to events from other services without direct dependencies.

## Architecture Diagram

```
┌─────────────────┐
│  Auth Service   │
│  (Publisher)    │
└────────┬────────┘
         │ publishes user.created
         ↓
    ┌─────────────────┐
    │  sharks.user    │ (Topic Exchange)
    └────────┬────────┘
             │
      ┌──────┴──────┐
      ↓             ↓
  [User Queue] [Event Queue]
      │             │
      ↓             ↓
┌──────────────┐ ┌──────────────┐
│ User Service │ │ Event Service│
│(Subscriber)  │ │ (Subscriber) │
└──────────────┘ └──────────────┘


┌──────────────────────┐
│  User Service        │
│  (Publisher)         │
└────────┬─────────────┘
         │ publishes organization.created
         ↓
    ┌─────────────────────┐
    │sharks.organization  │ (Topic Exchange)
    └────────┬────────────┘
             │
             ↓
      [Event Queue]
             │
             ↓
      ┌──────────────┐
      │ Event Service│
      │ (Subscriber) │
      └──────────────┘


┌──────────────────────┐
│  Event Service       │
│  (Publisher)         │
└────────┬─────────────┘
         │
    ┌────┴─────────────────┐
    │                      │
    ↓                      ↓
[event.created]    [ticket.purchased]
    ↓                      ↓
sharks.event          sharks.ticket
(Topic Exchange)      (Topic Exchange)
```

## Naming Conventions

| Concept      | Pattern                              | Example                          |
|--------------|--------------------------------------|----------------------------------|
| Exchange     | `sharks.<entity>`                    | `sharks.user`, `sharks.event`    |
| Routing key  | `<entity>.<past-tense-verb>`         | `user.created`, `event.created`  |
| Queue        | `<consumer>.<routing-key>`           | `user.user.created`              |

* The **first segment** of a queue name is always the consuming service.
* All exchanges and queues are **durable** and **not auto-delete**.

## Exchanges and Queues

### 1. **sharks.user** (Topic Exchange)
- **Type**: Topic
- **Durable**: Yes
- **Purpose**: User-related events from auth service
- **Routing Key**: `user.created`

#### Queues:
- `user.user.created` - Consumed by User Service
- `event.user.created` - Consumed by Event Service

---

### 2. **sharks.organization** (Topic Exchange)
- **Type**: Topic
- **Durable**: Yes
- **Purpose**: Organization-related events from user service
- **Routing Key**: `organization.created`

#### Queues:
- `event.organization.created` - Consumed by Event Service

---

### 3. **sharks.event** (Topic Exchange)
- **Type**: Topic
- **Durable**: Yes
- **Purpose**: Event-related events from event service
- **Routing Key**: `event.created`

#### Queues:
- None currently, but can be extended

---

### 4. **sharks.ticket** (Topic Exchange)
- **Type**: Topic
- **Durable**: Yes
- **Purpose**: Ticket/payment-related events from event service
- **Routing Key**: `ticket.purchased`

#### Queues:
- None currently, but can be extended

---

## Full Queue / Binding Reference

### User Service Subscriptions

| Queue                     | Source Exchange | Routing Key       | Purpose                                     |
|---------------------------|-----------------|-------------------|---------------------------------------------|
| `user.user.created`       | `sharks.user`   | `user.created`    | Create user profile when new user registered|

### User Service Publications

| Exchange            | Routing Key         | Consumers                     |
|---------------------|---------------------|-------------------------------|
| `sharks.organization` | `organization.created` | Event Service               |

---

### Event Service Subscriptions

| Queue                        | Source Exchange       | Routing Key            | Purpose                                      |
|------------------------------|-----------------------|------------------------|----------------------------------------------|
| `event.user.created`         | `sharks.user`         | `user.created`         | Track new users in event service             |
| `event.organization.created` | `sharks.organization` | `organization.created` | Track new organizations for events           |

### Event Service Publications

| Exchange       | Routing Key        | Consumers                              |
|----------------|--------------------|----------------------------------------|
| `sharks.event` | `event.created`    | User Service, Payment Service (future) |
| `sharks.ticket`| `ticket.purchased` | User Service, Payment Service (future) |

---

## Message Types

### UserCreatedMessage
**Published by**: Auth Service  
**Exchange**: `sharks.user`  
**Routing Key**: `user.created`  
**Consumed by**: User Service, Event Service

```json
{
  "email": "user@example.com",
  "role": "USER"
}
```

---

### OrganizationCreatedMessage
**Published by**: User Service  
**Exchange**: `sharks.organization`  
**Routing Key**: `organization.created`  
**Consumed by**: Event Service

```json
{
  "organizationId": "org-123",
  "organizationName": "ACME Corp",
  "organizerEmail": "organizer@acme.com",
  "createdAt": 1704067200000
}
```

---

### EventCreatedMessage
**Published by**: Event Service  
**Exchange**: `sharks.event`  
**Routing Key**: `event.created`  
**Consumed by**: Other services as needed

```json
{
  "eventId": "event-456",
  "eventName": "Tech Conference 2026",
  "organizationId": "org-123",
  "category": "TECH",
  "eventDate": 1706745600000,
  "createdAt": 1704067200000
}
```

---

### TicketPurchasedMessage
**Published by**: Event Service  
**Exchange**: `sharks.ticket`  
**Routing Key**: `ticket.purchased`  
**Consumed by**: User Service, Payment Service, etc.

```json
{
  "ticketId": "ticket-789",
  "eventId": "event-456",
  "userEmail": "attendee@example.com",
  "eventName": "Tech Conference 2026",
  "price": 99.99,
  "purchasedAt": 1704067200000
}
```

---

## Implementation Guide

### 1. Publishing an Event

In your service, inject the Publisher bean:

```java
@Autowired
private OrganizationEventPublisher organizationEventPublisher;

public void createOrganization(Organization org) {
    // ... create organization in database ...
    
    // Publish event
    OrganizationCreatedMessage message = new OrganizationCreatedMessage(
        org.getId(),
        org.getName(),
        org.getOrganizerEmail(),
        System.currentTimeMillis()
    );
    organizationEventPublisher.publishOrganizationCreated(message);
}
```

### 2. Subscribing to an Event

Use the `@RabbitListener` annotation:

```java
@Service
public class UserCreatedListener {
    
    @RabbitListener(queues = "${user.rabbitmq.user-queue}")
    public void onUserCreated(UserCreatedMessage message) {
        log.info("User created: {}", message.email());
        // Handle the event
        userService.createUserProfile(message);
    }
}
```

### 3. Configuration

Each service has a `RabbitMqConfig` class that defines:
- Topic Exchanges
- Queues
- Bindings between exchanges and queues
- Message converter (Jackson2JsonMessageConverter)

The configuration also enables `@RabbitListener` through `SimpleRabbitListenerContainerFactory`.

---

## Service-Specific Configuration

### Auth Service (`auth.rabbitmq.*`)
- **Publishes to**: `sharks.auth` (user.provisioned, role.changed)
- **Subscribes to**: None currently
- **Configuration location**: `com.sharks.auth.config.RabbitMqConfig`
- **Publisher**: `com.sharks.auth.messaging.UserEventPublisher`

### User Service (`user.rabbitmq.*`)
- **Subscribes to**: `sharks.user` (user.created)
- **Publishes to**: `sharks.organization` (organization.created)
- **Configuration location**: `com.sharks.user.config.RabbitMqConfig`
- **Publisher**: `com.sharks.user.messaging.OrganizationEventPublisher`
- **Listener**: `com.sharks.user.messaging.UserCreatedListener`

### Event Service (`event.rabbitmq.*`)
- **Subscribes to**: `sharks.user` (user.created), `sharks.organization` (organization.created)
- **Publishes to**: `sharks.event`, `sharks.ticket`
- **Configuration location**: `com.sharks.event.config.RabbitMqConfig`
- **Publisher**: `com.sharks.event.messaging.EventPublisher`
- **Listeners**: 
  - `com.sharks.event.messaging.UserCreatedListener`
  - `com.sharks.event.messaging.OrganizationCreatedListener`

---

## Environment Variables

Add these to your `.env` file or Docker environment:

```bash
# RabbitMQ Connection
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/

# Service-specific queue names (optional overrides)
USER_RABBITMQ_USER_QUEUE=user.user.created
USER_RABBITMQ_ORG_EXCHANGE=sharks.organization

EVENT_RABBITMQ_USER_QUEUE=event.user.created
EVENT_RABBITMQ_ORG_QUEUE=event.organization.created
EVENT_RABBITMQ_EVENT_EXCHANGE=sharks.event
EVENT_RABBITMQ_TICKET_EXCHANGE=sharks.ticket
```

---

## Running the System

### Using Docker Compose

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up

# Access RabbitMQ Management UI
# http://localhost:15672 (default user: guest, password: guest)
```

### Local Development

```bash
# Start RabbitMQ locally
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management

# Update application.properties to point to localhost:5672
spring.rabbitmq.host=localhost
```

---

## Monitoring

### RabbitMQ Management UI

Access at `http://localhost:15672`

**Useful views:**
- **Queues tab**: See queue lengths, message rates
- **Exchanges tab**: View all exchanges and bindings
- **Connections tab**: See active connections from services
- **Channels tab**: Monitor message publishing/consumption

### Application Logs

Each service logs:
- Published events: `"Publishing [event type]: ..."`
- Consumed events: `"[Event type] received in [service]: ..."`

---

## Error Handling & Dead Letter Exchanges

Currently, messages are not redirected to Dead Letter Exchanges on failure. Consider implementing this for production:

```java
@Bean
public Queue dlq(EventRabbitProperties properties) {
    return new Queue(properties.getUserQueue() + ".dlq", true);
}

@Bean
public Binding dlqBinding(Queue dlq, TopicExchange exchange) {
    return BindingBuilder.bind(dlq).to(exchange).with("*.error");
}
```

---

## Best Practices

1. **Always use try-catch** in listeners to prevent message loss
2. **Log message receipt** for debugging
3. **Validate message content** before processing
4. **Use timestamps** in messages for tracking
5. **Keep messages small** to reduce network overhead
6. **Use meaningful routing keys** that describe the event
7. **Make listeners idempotent** - handle duplicate messages gracefully
8. **Implement circuit breaker** pattern for service resilience

---

## Future Enhancements

1. Dead Letter Exchange (DLX) for failed messages
2. Message retry logic with exponential backoff
3. Circuit breaker pattern for service resilience
4. Message deduplication for idempotency
5. Saga pattern for distributed transactions
6. Event sourcing for audit trails
7. Integration with payment service

---

## Troubleshooting

**Issue**: Messages not being consumed
- Check queue bindings in RabbitMQ UI
- Verify `@RabbitListener` queue name matches configuration
- Check application logs for listener errors

**Issue**: Connection refused
- Verify RabbitMQ is running
- Check `spring.rabbitmq.host` matches your setup
- Ensure firewall allows port 5672

**Issue**: Messages piling up in queue
- Check if listener service is running
- Check application logs for exceptions
- Verify message format matches expected DTO

**Issue**: ClassNotFoundException for message classes
- Ensure all message DTOs are in the classpath
- Check that record classes are properly compiled
- Verify Jackson dependencies are included

---


---

### User & Org Service  (subscribes)

| Queue                     | Source Exchange  | Routing Key       | Trigger / Purpose                                          |
|---------------------------|------------------|-------------------|------------------------------------------------------------|
| `user.user.provisioned`   | `sharks.auth`    | `user.provisioned` | Auth provisioned Supabase user; create local UserProfile   |
| `user.role.changed`       | `sharks.auth`    | `role.changed`     | Sync role change into local profile                        |
| `user.ticket.confirmed`   | `sharks.event`   | `ticket.confirmed` | Ticket purchase succeeded; add to user's ticket history    |
| `user.ticket.cancelled`   | `sharks.event`   | `ticket.cancelled` | Ticket cancelled or payment failed; update history         |
| `user.event.cancelled`    | `sharks.event`   | `event.cancelled`  | Event cancelled; notify affected attendees                 |

**User & Org publishes to `sharks.user`:**

| Routing Key   | Payload (suggested)                                | When                                       |
|---------------|----------------------------------------------------|--------------------------------------------|
| `user.created`| `{ email, password, role }`                        | POST `/api/v1/auth/register`               |
| `user.updated`| `{ userId, changedFields }`                        | PUT/PATCH `/api/v1/users/me`               |
| `user.deleted`| `{ userId, email }`                                | DELETE `/api/v1/users/me`                  |
| `org.created` | `{ orgId, orgName, ownerId }`                      | POST `/api/organizations`                  |
| `org.updated` | `{ orgId, changedFields }`                         | PUT `/api/organizations/:id`               |

---

### Event & Ticketing Service  (subscribes)

| Queue                              | Source Exchange    | Routing Key                | Trigger / Purpose                                         |
|------------------------------------|--------------------|----------------------------|-----------------------------------------------------------|
| `event.payment.completed`          | `sharks.payment`   | `payment.completed`         | Payment succeeded; confirm the reserved ticket            |
| `event.payment.failed`             | `sharks.payment`   | `payment.failed`            | Payment failed; release the reserved ticket               |
| `event.payment.refund.completed`   | `sharks.payment`   | `payment.refund.completed`  | Refund processed; finalize ticket cancellation            |
| `event.payment.refund.failed`      | `sharks.payment`   | `payment.refund.failed`     | Refund failed; keep ticket active, notify user            |
| `event.user.deleted`               | `sharks.user`      | `user.deleted`              | User removed; handle orphaned tickets/events              |
| `event.org.created`                | `sharks.user`      | `org.created`               | New org available; allow org-based event creation          |

**Event & Ticketing publishes to `sharks.event`:**

| Routing Key                | Payload (suggested)                                       | When                                          |
|----------------------------|-----------------------------------------------------------|-----------------------------------------------|
| `event.created`            | `{ eventId, orgId, title, date, location }`               | POST `/api/events`                            |
| `event.updated`            | `{ eventId, changedFields }`                              | PUT `/api/events/:id`                         |
| `event.cancelled`          | `{ eventId, reason }`                                     | DELETE `/api/events/:id`                      |
| `ticket.purchase.initiated`| `{ ticketId, eventId, userId, amount, currency }`         | POST `/api/tickets/purchase` (after reserving)|
| `ticket.refund.initiated`  | `{ ticketId, eventId, userId, amount, currency }`         | PUT `/api/tickets/:id/refund`                 |
| `ticket.confirmed`         | `{ ticketId, eventId, userId }`                           | After receiving `payment.completed`           |
| `ticket.cancelled`         | `{ ticketId, eventId, userId, reason }`                   | After receiving `payment.failed` or refund    |

---

### Payment Service — stub  (subscribes)

| Queue                              | Source Exchange   | Routing Key                  | Trigger / Purpose                                    |
|------------------------------------|-------------------|------------------------------|------------------------------------------------------|
| `payment.ticket.purchase.initiated`| `sharks.event`    | `ticket.purchase.initiated`  | Charge the user for a ticket                         |
| `payment.ticket.refund.initiated`  | `sharks.event`    | `ticket.refund.initiated`    | Refund the user for a cancelled ticket               |

**Payment publishes to `sharks.payment`:**

| Routing Key               | Payload (suggested)                                        | When                             |
|----------------------------|------------------------------------------------------------|----------------------------------|
| `payment.completed`        | `{ paymentId, ticketId, userId, amount, status:"success" }`| Charge succeeded                 |
| `payment.failed`           | `{ paymentId, ticketId, userId, amount, reason }`          | Charge failed                    |
| `payment.refund.completed` | `{ paymentId, ticketId, userId, amount }`                  | Refund succeeded                 |
| `payment.refund.failed`    | `{ paymentId, ticketId, userId, reason }`                  | Refund failed                    |

---

## Message Flows (End-to-End)

### 1. User Registration

```
User ──POST /register──▶ [User & Org]
                              │
                    publish  user.created  ──▶  sharks.user exchange
                                                      │
                                          ┌───────────┘
                                          ▼
                                   [Auth Service]
                              provisions Supabase user
                                          │
                    publish  user.provisioned  ──▶  sharks.auth exchange
                                                          │
                                              ┌───────────┘
                                              ▼
                                       [User & Org]
                                  creates local UserProfile
```

### 2. Ticket Purchase

```
User ──POST /tickets/purchase──▶ [Event & Ticketing]
                                        │
                              reserves ticket (PENDING)
                                        │
              publish  ticket.purchase.initiated  ──▶  sharks.event exchange
                                                              │
                                                  ┌───────────┘
                                                  ▼
                                          [Payment Service]
                                        processes charge
                                                  │
                    ┌─────── success ──────────────┼─────── failure ───────┐
                    ▼                                                      ▼
     publish  payment.completed           publish  payment.failed
         ──▶  sharks.payment                  ──▶  sharks.payment
                    │                                      │
        ┌───────────┘                          ┌───────────┘
        ▼                                      ▼
 [Event & Ticketing]                    [Event & Ticketing]
  confirms ticket                        cancels reservation
        │                                      │
 publish  ticket.confirmed              publish  ticket.cancelled
   ──▶  sharks.event                      ──▶  sharks.event
        │                                      │
    ┌───┘                                  ┌───┘
    ▼                                      ▼
 [User & Org]                           [User & Org]
  adds to history                        updates history
```

### 3. Ticket Refund

```
User ──PUT /tickets/:id/refund──▶ [Event & Ticketing]
                                          │
                  publish  ticket.refund.initiated  ──▶  sharks.event exchange
                                                                │
                                                    ┌───────────┘
                                                    ▼
                                            [Payment Service]
                                          processes refund
                                                    │
                     ┌─── success ──────────────────┼─── failure ─────┐
                     ▼                                                ▼
      publish  payment.refund.completed       publish  payment.refund.failed
          ──▶  sharks.payment                     ──▶  sharks.payment
                     │                                         │
         ┌───────────┘                             ┌───────────┘
         ▼                                         ▼
  [Event & Ticketing]                       [Event & Ticketing]
   finalizes refund                          keeps ticket active
         │
  publish  ticket.cancelled
    ──▶  sharks.event
         │
     ┌───┘
     ▼
  [User & Org]
   updates history
```

### 4. Role Change

```
Admin ──POST /users/{id}/role──▶ [Auth Service]
                                       │
                             updates Supabase role
                                       │
                 publish  role.changed  ──▶  sharks.auth exchange
                                                    │
                                        ┌───────────┘
                                        ▼
                                 [User & Org]
                              syncs role in profile
```

### 5. Account Deletion

```
User ──DELETE /users/me──▶ [User & Org]
                                │
                      deletes local profile
                                │
              publish  user.deleted  ──▶  sharks.user exchange
                                                │
                          ┌─────────────────────┼──────────────────┐
                          ▼                                        ▼
                   [Auth Service]                        [Event & Ticketing]
              deletes Supabase user                 cancels tickets / transfers events
```

---

## Spring AMQP Configuration Cheat-Sheet

Each service needs these properties (adjust names per service):

```yaml
# application.yml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

### Publishing example (Java)

```java
@Service
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTicketPurchaseInitiated(TicketPurchaseMessage msg) {
        rabbitTemplate.convertAndSend(
            "sharks.event",                  // exchange
            "ticket.purchase.initiated",     // routing key
            msg
        );
    }
}
```

### Subscribing example (Java)

```java
@Service
public class PaymentListener {

    @RabbitListener(queues = "payment.ticket.purchase.initiated")
    public void onTicketPurchase(TicketPurchaseMessage msg) {
        // process payment ...
    }
}
```

---

## Quick Reference — Who Publishes Where, Who Subscribes Where

```
┌─────────────────────┐          ┌─────────────────────┐
│   Auth Service       │          │  User & Org Service  │
│                      │          │                      │
│  PUBLISHES TO:       │          │  PUBLISHES TO:       │
│    sharks.auth       │          │    sharks.user       │
│    ├ user.provisioned│          │    ├ user.created    │
│    └ role.changed    │          │    ├ user.updated    │
│                      │          │    ├ user.deleted    │
│  SUBSCRIBES FROM:    │          │    ├ org.created     │
│    sharks.user       │          │    └ org.updated     │
│    ├ user.created    │          │                      │
│    └ user.deleted    │          │  SUBSCRIBES FROM:    │
│                      │          │    sharks.auth       │
└─────────────────────┘          │    ├ user.provisioned│
                                  │    └ role.changed    │
                                  │    sharks.event      │
                                  │    ├ ticket.confirmed│
                                  │    ├ ticket.cancelled│
                                  │    └ event.cancelled │
                                  └─────────────────────┘

┌─────────────────────┐          ┌─────────────────────┐
│ Event & Ticketing    │          │  Payment (stub)      │
│                      │          │                      │
│  PUBLISHES TO:       │          │  PUBLISHES TO:       │
│    sharks.event      │          │    sharks.payment    │
│    ├ event.created   │          │    ├ payment.completed│
│    ├ event.updated   │          │    ├ payment.failed   │
│    ├ event.cancelled │          │    ├ payment.refund.completed│
│    ├ ticket.purchase │          │    └ payment.refund.failed  │
│    │   .initiated    │          │                      │
│    ├ ticket.refund   │          │  SUBSCRIBES FROM:    │
│    │   .initiated    │          │    sharks.event      │
│    ├ ticket.confirmed│          │    ├ ticket.purchase │
│    └ ticket.cancelled│          │    │   .initiated    │
│                      │          │    └ ticket.refund   │
│  SUBSCRIBES FROM:    │          │        .initiated    │
│    sharks.payment    │          └─────────────────────┘
│    ├ payment.completed│
│    ├ payment.failed   │
│    ├ payment.refund   │
│    │   .completed     │
│    └ payment.refund   │
│        .failed        │
│    sharks.user        │
│    ├ user.deleted     │
│    └ org.created      │
└─────────────────────┘
```
