# Organization & User Events API Endpoints

This document describes the REST API endpoints for managing Organizations and User-created Events in the User & Organizations Service.

## Base URL
- **Local Development**: `http://localhost:8081`
- **Production**: TBD

## Authentication
All endpoints marked with 🔐 require JWT Bearer token in the `Authorization` header.

```
Authorization: Bearer <jwt_token>
```

---

## Organization Endpoints

### 1. Get All Organizations
**Endpoint**: `GET /api/organizations`  
**Authentication**: Not required  
**Description**: Retrieves a list of all active organizations in the system.

**Response**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Tech Conference 2026",
    "description": "Annual technology conference",
    "organizerId": "123e4567-e89b-12d3-a456-426614174000",
    "organizerEmail": "organizer@techconf.com",
    "logoUrl": "https://example.com/logo.png",
    "websiteUrl": "https://techconf2026.com",
    "active": true,
    "createdAt": "2026-01-15T10:00:00Z",
    "updatedAt": "2026-01-15T10:00:00Z"
  }
]
```

---

### 2. Get Single Organization
**Endpoint**: `GET /api/organizations/{id}`  
**Authentication**: Not required  
**Description**: Retrieves details of a specific organization by ID.

**Path Parameters**:
- `id` (string, required): UUID of the organization

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tech Conference 2026",
  "description": "Annual technology conference",
  "organizerId": "123e4567-e89b-12d3-a456-426614174000",
  "organizerEmail": "organizer@techconf.com",
  "logoUrl": "https://example.com/logo.png",
  "websiteUrl": "https://techconf2026.com",
  "active": true,
  "createdAt": "2026-01-15T10:00:00Z",
  "updatedAt": "2026-01-15T10:00:00Z"
}
```

**Error** (404 Not Found):
- Organization does not exist

---

### 3. Create Organization 🔐
**Endpoint**: `POST /api/organizations`  
**Authentication**: Required (JWT Bearer Token)  
**Status Code**: 201 Created  
**Description**: Creates a new organization. The authenticated user becomes the organizer.

**Request Body**:
```json
{
  "name": "Tech Conference 2026",
  "description": "Annual technology conference",
  "logoUrl": "https://example.com/logo.png",
  "websiteUrl": "https://techconf2026.com"
}
```

**Request Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tech Conference 2026",
  "description": "Annual technology conference",
  "organizerId": "123e4567-e89b-12d3-a456-426614174000",
  "organizerEmail": "organizer@techconf.com",
  "logoUrl": "https://example.com/logo.png",
  "websiteUrl": "https://techconf2026.com",
  "active": true,
  "createdAt": "2026-01-15T10:00:00Z",
  "updatedAt": "2026-01-15T10:00:00Z"
}
```

**Errors**:
- `400 Bad Request`: Missing required fields (name)
- `401 Unauthorized`: Invalid or missing JWT token
- `409 Conflict`: Organization name already exists

**Business Logic**:
- The authenticated user's ID and email are automatically captured as the organizer
- Organization is created in active state by default
- An `organization.created` event is published to RabbitMQ for other services to consume

---

## User Events Endpoints

### 4. Get User's Created Events/Organizations 🔐
**Endpoint**: `GET /api/v1/users/me/events-created`  
**Authentication**: Required (JWT Bearer Token)  
**Description**: Retrieves a list of all organizations (events) created by the authenticated user.

**Request Headers**:
```
Authorization: Bearer <jwt_token>
```

**Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Tech Conference 2026",
    "description": "Annual technology conference",
    "organizerId": "123e4567-e89b-12d3-a456-426614174000",
    "organizerEmail": "organizer@techconf.com",
    "logoUrl": "https://example.com/logo.png",
    "websiteUrl": "https://techconf2026.com",
    "active": true,
    "createdAt": "2026-01-15T10:00:00Z",
    "updatedAt": "2026-01-15T10:00:00Z"
  }
]
```

**Errors**:
- `401 Unauthorized`: Invalid or missing JWT token

---

## Request/Response Format

### Content-Type
All requests and responses use `application/json`

### Timestamps
Timestamps are in ISO 8601 format with UTC timezone (e.g., `2026-01-15T10:00:00Z`)

### UUIDs
All IDs are UUID v4 format

---

## Error Handling

### Common Error Responses

**400 Bad Request**:
```json
{
  "error": "Organization name is required"
}
```

**401 Unauthorized**:
```json
{
  "error": "User not authenticated"
}
```

**404 Not Found**:
```json
{
  "error": "Organization not found"
}
```

**409 Conflict**:
```json
{
  "error": "Organization with name 'Tech Conference 2026' already exists"
}
```

---

## Examples

### Create Organization Example

**Request**:
```bash
curl -X POST http://localhost:8081/api/organizations \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Conference 2026",
    "description": "Annual technology conference",
    "logoUrl": "https://techconf2026.com/logo.png",
    "websiteUrl": "https://techconf2026.com"
  }'
```

**Response**:
```bash
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tech Conference 2026",
  "description": "Annual technology conference",
  "organizerId": "123e4567-e89b-12d3-a456-426614174000",
  "organizerEmail": "user@example.com",
  "logoUrl": "https://techconf2026.com/logo.png",
  "websiteUrl": "https://techconf2026.com",
  "active": true,
  "createdAt": "2026-01-15T10:00:00Z",
  "updatedAt": "2026-01-15T10:00:00Z"
}
```

---

### Get User's Created Organizations

**Request**:
```bash
curl -X GET http://localhost:8081/api/v1/users/me/events-created \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response**:
```bash
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Tech Conference 2026",
    "description": "Annual technology conference",
    "organizerId": "123e4567-e89b-12d3-a456-426614174000",
    "organizerEmail": "user@example.com",
    "logoUrl": "https://techconf2026.com/logo.png",
    "websiteUrl": "https://techconf2026.com",
    "active": true,
    "createdAt": "2026-01-15T10:00:00Z",
    "updatedAt": "2026-01-15T10:00:00Z"
  }
]
```

---

## Database Schema

The following table is used to store organizations:

```sql
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    organizer_id UUID NOT NULL,
    organizer_email VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255),
    website_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## RabbitMQ Integration

When an organization is created, the following event is published to RabbitMQ:

**Exchange**: `sharks.organization`  
**Routing Key**: `organization.created`  
**Message Format**:
```json
{
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Tech Conference 2026",
  "organizerEmail": "organizer@techconf.com",
  "createdAt": 1704067200000
}
```

**Subscribers**:
- Event & Ticketing Service (consumes via `event.organization.created` queue)

---

## Notes

- Organizations and Users are separate entities
- Each organization must have a unique name
- The creator of an organization automatically becomes the organizer
- Only active organizations are returned by list endpoints
- All authentication uses JWT bearer tokens from the Auth Service

---
