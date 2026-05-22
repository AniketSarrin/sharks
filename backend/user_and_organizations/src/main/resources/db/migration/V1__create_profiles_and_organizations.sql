-- User profiles (com.sharks.user.entity.UserProfile) and organizations (Organization).
-- Matches spring.jpa.hibernate.ddl-auto=none; applied on startup via Flyway.

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    bio TEXT,
    phone VARCHAR(255),
    location VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_profiles_role CHECK (role IN ('attendee', 'organizer', 'admin'))
);

CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    organizer_id UUID NOT NULL REFERENCES profiles (id),
    organizer_email VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255),
    website_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_organizations_organizer_id ON organizations (organizer_id);
