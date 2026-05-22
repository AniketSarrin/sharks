# RabbitMQ Pub-Sub Implementation Summary

## ✅ Completed Setup

Your microservices architecture now has a complete RabbitMQ pub-sub messaging system connecting all services:

```
┌───────────────┐
│  Auth Service │ ── publishes ──> [sharks.user exchange] ──> User & Event Services
└───────────────┘                    (user.created)

┌──────────────────────────┐
│ User & Organizations     │ ── publishes ──> [sharks.organization exchange] ──> Event Service
│ Service                  │                   (organization.created)
└──────────────────────────┘

┌────────────────────────┐
│ Event & Ticketing      │ ── publishes ──> [sharks.event] + [sharks.ticket]
│ Service                │                   (event.created, ticket.purchased)
└────────────────────────┘
```

## 📦 What Was Created/Modified

### 1. Dependencies Added
```gradle
// Added to both user_and_organizations and event_and_ticketing
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

### 2. Message Classes (DTOs)
| Service | Class | Purpose |
|---------|-------|---------|
| User | `UserCreatedMessage` | Received from Auth |
| User | `OrganizationCreatedMessage` | Published to Event service |
| Event | `UserCreatedMessage` | Received from Auth |
| Event | `OrganizationCreatedMessage` | Received from User |
| Event | `EventCreatedMessage` | Published by Event service |
| Event | `TicketPurchasedMessage` | Published by Event service |

### 3. Configuration Classes

Each service now has:
- **`RabbitMqConfig.java`** - Defines exchanges, queues, and bindings
- **`[Service]RabbitProperties.java`** - Configuration properties for exchanges/queues
- **Message Listeners** - `@RabbitListener` annotated methods
- **Event Publishers** - Classes to publish events to RabbitMQ

### 4. Application Properties Updated
```properties
# RabbitMQ Connection
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}

# Service-specific queue configurations
user.rabbitmq.user-queue=user.user.created
event.rabbitmq.user-queue=event.user.created
# ... and more
```

### 5. Docker Compose Enhanced
```yaml
# Added event-postgres database
event-postgres:
  image: postgres:16-alpine
  
# Added event-and-ticketing service
event-and-ticketing:
  build: ./event_and_ticketing
  depends_on:
    - event-postgres
    - rabbitmq
  ports:
    - "8082:8080"
```

## 🔄 Event Flow Diagrams

### User Creation Flow
```
Auth Service creates user
         ↓
UserEventPublisher.publishUserCreated()
         ↓
Message sent to "sharks.user" exchange with key "user.created"
         ↓
    ┌────────────────────────────────┐
    │ Two queues bind to this event: │
    └────────┬───────────────────────┘
             │
      ┌──────┴─────────┐
      ↓                ↓
user.user.created  event.user.created
      ↓                ↓
User Service     Event Service
creates profile   records user
```

### Organization Creation Flow
```
User Service creates organization
         ↓
OrganizationEventPublisher.publishOrganizationCreated()
         ↓
Message sent to "sharks.organization" exchange with key "organization.created"
         ↓
event.organization.created queue
         ↓
Event Service processes
```

## 🚀 Quick Start

### Start Everything
```bash
cd backend
docker-compose up --build
```

### Access RabbitMQ Management
- URL: http://localhost:15672
- Username: guest
- Password: guest

### Service Ports
- Auth Service: http://localhost:8080
- User Service: http://localhost:8081
- Event Service: http://localhost:8082

## 📝 Using in Your Code

### Publishing an Event
```java
@Autowired
private OrganizationEventPublisher publisher;

// In your service method:
OrganizationCreatedMessage message = new OrganizationCreatedMessage(
    orgId, orgName, email, System.currentTimeMillis()
);
publisher.publishOrganizationCreated(message);
```

### Subscribing to an Event
Event listeners are already set up with `@RabbitListener`:
```java
@RabbitListener(queues = "${user.rabbitmq.user-queue}")
public void onUserCreated(UserCreatedMessage message) {
    log.info("User created: {}", message.email());
    // TODO: Implement your business logic
}
```

## 📚 Documentation Files

1. **`backend/infrastructure/MESSAGING.md`** - Comprehensive architecture documentation
2. **`backend/RABBITMQ_QUICKSTART.md`** - Quick start guide with examples
3. **Code comments** - Implementation guides in listener classes marked with `// TODO:`

## 🔌 Connection Details

| Component | Host | Port | Purpose |
|-----------|------|------|---------|
| RabbitMQ AMQP | rabbitmq | 5672 | Message broker |
| RabbitMQ Management | http://localhost | 15672 | UI and API |
| Auth Service | localhost | 8080 | REST API |
| User Service | localhost | 8081 | REST API |
| Event Service | localhost | 8082 | REST API |

## ⚙️ Environment Variables

```bash
# RabbitMQ connection (Docker Compose will use these)
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VHOST=/

# Optional: Override queue names
USER_RABBITMQ_USER_QUEUE=user.user.created
EVENT_RABBITMQ_USER_QUEUE=event.user.created
```

## 🎯 Next Steps

1. **Implement message handling** - Add logic to TODO comments in listeners
2. **Add event publishing** - Call publisher methods in your service business logic
3. **Add error handling** - Wrap listeners in try-catch blocks
4. **Add Dead Letter Queue** - For handling failed messages (optional)
5. **Write tests** - Integration tests for pub-sub functionality

## 📊 Message Format Reference

All messages are JSON. Examples:

```json
// UserCreatedMessage
{"email": "user@example.com", "role": "USER"}

// OrganizationCreatedMessage
{
  "organizationId": "org-123",
  "organizationName": "ACME Corp",
  "organizerEmail": "organizer@acme.com",
  "createdAt": 1704067200000
}

// EventCreatedMessage
{
  "eventId": "event-456",
  "eventName": "Tech Conference 2026",
  "organizationId": "org-123",
  "category": "TECH",
  "eventDate": 1706745600000,
  "createdAt": 1704067200000
}

// TicketPurchasedMessage
{
  "ticketId": "ticket-789",
  "eventId": "event-456",
  "userEmail": "attendee@example.com",
  "eventName": "Tech Conference 2026",
  "price": 99.99,
  "purchasedAt": 1704067200000
}
```

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| Services won't start | Check RabbitMQ logs: `docker-compose logs rabbitmq` |
| Messages not processing | Check listener logs, verify queue names in RabbitMQ UI |
| Connection refused | Verify RabbitMQ is running, check host/port config |
| Classes not found | Ensure gradle build completed successfully |

## 📖 Additional Resources

- **Spring AMQP**: https://spring.io/projects/spring-amqp
- **RabbitMQ Docs**: https://www.rabbitmq.com/documentation.html
- **This Project**: See `backend/infrastructure/MESSAGING.md` and `backend/RABBITMQ_QUICKSTART.md`

---

**Your microservices are now fully connected with asynchronous pub-sub messaging!** 🎉
