-- Schema do Headless CMS UCSal
-- Execute: sudo -u postgres psql -d ucsal_cms -f schema.sql

-- Tipos ENUM
CREATE TYPE user_role AS ENUM ('admin', 'editor', 'professor', 'student', 'viewer');
CREATE TYPE content_status AS ENUM ('draft', 'in_review', 'published', 'archived');
CREATE TYPE event_modality AS ENUM ('presencial', 'online', 'hibrido');

-- Usuários
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            user_role NOT NULL DEFAULT 'viewer',
    avatar_url      TEXT,
    bio             TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Cursos
CREATE TABLE IF NOT EXISTS courses (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    code                VARCHAR(50),
    description         TEXT,
    duration_semesters  INTEGER,
    coordinator_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Notícias
CREATE TABLE IF NOT EXISTS news (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    subtitle        VARCHAR(500),
    body            TEXT NOT NULL,
    cover_image_url TEXT,
    status          content_status NOT NULL DEFAULT 'draft',
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    author_id       UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id       UUID REFERENCES courses(id) ON DELETE SET NULL,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Artigos
CREATE TABLE IF NOT EXISTS articles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    abstract        TEXT,
    body            TEXT NOT NULL,
    cover_image_url TEXT,
    status          content_status NOT NULL DEFAULT 'draft',
    author_id       UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id       UUID REFERENCES courses(id) ON DELETE SET NULL,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Projetos
CREATE TABLE IF NOT EXISTS projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    cover_image_url TEXT,
    status          content_status NOT NULL DEFAULT 'draft',
    course_id       UUID REFERENCES courses(id) ON DELETE SET NULL,
    coordinator_id  UUID REFERENCES users(id) ON DELETE SET NULL,
    start_date      DATE,
    end_date        DATE,
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Eventos
CREATE TABLE IF NOT EXISTS events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    description         TEXT,
    cover_image_url     TEXT,
    status              content_status NOT NULL DEFAULT 'draft',
    modality            event_modality NOT NULL DEFAULT 'presencial',
    location            VARCHAR(500),
    online_url          TEXT,
    starts_at           TIMESTAMPTZ NOT NULL,
    ends_at             TIMESTAMPTZ,
    registration_url    TEXT,
    max_participants    INTEGER,
    organizer_id        UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id           UUID REFERENCES courses(id) ON DELETE SET NULL,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Permissões para o usuário da aplicação
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cms_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cms_user;
