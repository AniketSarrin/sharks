# RabbitMQ Pub-Sub Quick Start Guide

## Overview
This is a complete RabbitMQ pub-sub system connecting all microservices (Auth, User & Organizations, Event & Ticketing).

## Services Involved

- **Auth Service** (port 8080) - Publishes user creation events
- **User & Organizations Service** (port 8081) - Subscribes to user events, publishes organization events
- **Event & Ticketing Service** (port 8082) - Subscribes to user and organization events, publishes event/ticket events

## Quick Start

### 1. Start Services with Docker Compose

```bash
cd backend
docker-compose up --build
```

This will start:
- RabbitMQ on port 5672 (AMQP) and 15672 (Management UI)
- All three services with their respective databases

### 2. Access RabbitMQ Management UI

- **URL**: http://localhost:15672
- **Username**: guest
- **Password**: guest

Go to the **Queues** tab to see messages being processed.

### 3. Test the Pub-Sub System

#### Flow 1: User Creation
```bash
# When a user is created in Auth service:
# 1. Auth publishes user.created to sharks.user exchange
# 2. User service receives it on user.user.created queue
# 3. Event service receives it on event.user.created queue
```

#### Flow 2: Organization Creation
```bash
# When an organization is created in User service:
# 1. User service publishes organization.created to sharks.organization exchange
# 2. Event service receives it on event.organization.created queue
```

## Architecture at a Glance

```
User Registration (Auth Service)
          ↓
    Publish to "sharks.user"
          ↓
    ┌─────────────────┐
    │ Route with key: │
    │ "user.created"  │
    └────────┬────────┘
             │
      ┌──────┴─────────┐
      ↓                ↓
  [User Service]  [Event Service]
```

## Key Files Created/Modified

### New Files (Messaging/Configuration)
- `backend/auth/src/main/java/com/sharks/auth/messaging/UserEventPublisher.java`
- `backend/user_and_organizations/src/main/java/com/sharks/user/messaging/UserCreatedListener.java`
- `backend/user_and_organizations/src/main/java/com/sharks/user/messaging/OrganizationCreatedMessage.java`
- `backend/user_and_organizations/src/main/java/com/sharks/user/messaging/OrganizationEventPublisher.java`
- `backend/user_and_organizations/src/main/java/com/sharks/user/config/RabbitMqConfig.java`
- `backend/user_and_organizations/src/main/java/com/sharks/user/config/UserRabbitProperties.java`
- `backend/event_and_ticketing/src/main/java/com/sharks/event/messaging/*.java` (multiple listeners and publishers)
- `backend/event_and_ticketing/src/main/java/com/sharks/event/config/RabbitMqConfig.java`
- `backend/event_and_ticketing/src/main/java/com/sharks/event/config/EventRabbitProperties.java`

### Modified Files
- `backend/user_and_organizations/build.gradle` - Added AMQP dependency
- `backend/event_and_ticketing/build.gradle` - Added AMQP and JPA dependencies
- `backend/user_and_organizations/src/main/resources/application.properties` - Added RabbitMQ config
- `backend/event_and_ticketing/src/main/resources/application.properties` - Added RabbitMQ config
- `backend/auth/src/main/resources/application.properties` - Updated RabbitMQ config
- `backend/docker-compose.yml` - Added event_and_ticketing service and event-postgres database
- `backend/infrastructure/MESSAGING.md` - Comprehensive documentation

## How to Use in Your Code

### Publishing an Event

Example: Publishing an organization created event from User Service

```java
@Service
public class OrganizationService {
    
    @Autowired
    private OrganizationEventPublisher eventPublisher;
    
    public void createOrganization(Organization org) {
        // ... save to database ...
        
        // Publish event to RabbitMQ
        OrganizationCreatedMessage message = new OrganizationCreatedMessage(
            org.getId(),
            org.getName(),
            org.getOrganizerEmail(),
            System.currentTimeMillis()
        );
        eventPublisher.publishOrganizationCreated(message);
    }
}
```

### Consuming an Event

Example: Listening for user created events in User Service

The listener is already set up in `UserCreatedListener.java`:

```java
@Service
public class UserCreatedListener {
    
    @RabbitListener(queues = "${user.rabbitmq.user-queue}")
    public void onUserCreated(UserCreatedMessage message) {
        log.info("User created: {}", message.email());
        // TODO: Implement your business logic here
        // e.g., Create user profile, send welcome email, etc.
    }
}
```

## Environment Variables

Default values are set in `application.properties` but can be overridden with environment variables:

```bash
RABBITMQ_HOST=rabbitmq          # RabbitMQ server host
RABBITMQ_PORT=5672              # RabbitMQ port
RABBITMQ_USERNAME=guest          # RabbitMQ username
RABBITMQ_PASSWORD=guest          # RabbitMQ password
RABBITMQ_VHOST=/                 # RabbitMQ virtual host
```

## Exchanges Overview

| Exchange          | Owner Service       | Routing Key           | Purpose                    |
|-------------------|---------------------|-----------------------|----------------------------|
| `sharks.user`     | Auth Service        | `user.created`        | New user registration      |
| `sharks.organization` | User Service   | `organization.created` | New organization created   |
| `sharks.event`    | Event Service       | `event.created`       | New event created          |
| `sharks.ticket`   | Event Service       | `ticket.purchased`    | Ticket purchased           |

## Troubleshooting

### Services not connecting to RabbitMQ?

1. Check RabbitMQ is running: `docker-compose ps`
2. Check logs: `docker-compose logs rabbitmq`
3. Verify connection settings in `application.properties`

### Messages not being processed?

1. Check the RabbitMQ Management UI for queue backlog
2. Look at service logs for listener errors: `docker-compose logs user-and-organizations`
3. Verify the listener queue name matches the configuration

### Testing Locally?

```bash
# Start RabbitMQ only
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management

# Update application.properties
spring.rabbitmq.host=localhost

# Run services individually
./gradlew bootRun
```

## Next Implementation Steps

1. **Implement TODO comments** in listener classes to handle events
2. **Add event publishers** to your service business logic
3. **Add error handling** in listeners to prevent message loss
4. **Add Dead Letter Queue** support for failed messages
5. **Write integration tests** for pub-sub functionality

## Useful Links

- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [RabbitMQ Management Console](http://localhost:15672)

## Architecture Documentation

For detailed architecture, implementation patterns, and best practices, see:
- `backend/infrastructure/MESSAGING.md`
