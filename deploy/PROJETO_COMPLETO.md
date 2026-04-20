# Projeto Headless CMS UCSal — Documentação Completa

**Stack:** Java 21 + PostgreSQL + Angular 21 + Nginx + KVM Hostinger + Netlify  
**Repositório:** https://github.com/RafaTheMonk/cms-headless  
**Frontend (Netlify):** https://blogdaucsal.netlify.app  
**Backend (KVM):** http://72.60.49.33:8081  

---

## Índice

1. [Visão geral do projeto](#1-visão-geral-do-projeto)
2. [Criação do frontend Angular](#2-criação-do-frontend-angular)
3. [Configuração de deploy](#3-configuração-de-deploy)
4. [Deploy do backend na KVM](#4-deploy-do-backend-na-kvm)
5. [Erros encontrados e resoluções](#5-erros-encontrados-e-resoluções)
6. [Deploy do frontend no Netlify](#6-deploy-do-frontend-no-netlify)
7. [Certificado autoassinado — como aceitar](#7-certificado-autoassinado--como-aceitar)
8. [Credenciais de acesso](#8-credenciais-de-acesso)
9. [Comandos úteis](#9-comandos-úteis)

---

## 1. Visão geral do projeto

### Arquitetura

```
Usuário (navegador)
       │
       ▼
Netlify CDN — https://blogdaucsal.netlify.app
(Angular 21 — arquivos estáticos)
       │ HTTPS requests
       ▼
Nginx — 72.60.49.33:8081 (HTTPS autoassinado)
       │ proxy_pass
       ▼
Java HttpServer — localhost:8080
       │
       ▼
PostgreSQL — localhost:5432 — banco: ucsal_cms
```

### Estrutura de repositório

```
github.com/RafaTheMonk/cms-headless
├── main      → backend Java
└── frontend  → frontend Angular
```

### Entidades gerenciadas

| Entidade | Endpoint |
|---|---|
| Usuários | `/api/users` |
| Cursos | `/api/courses` |
| Notícias | `/api/news` |
| Artigos | `/api/articles` |
| Projetos | `/api/projects` |
| Eventos | `/api/events` |

---

## 2. Criação do frontend Angular

### Prompt utilizado

> *"Gostaria de criar uma interface gráfica para interagir com o headless cms criado seria interessante na raiz daqui ou criar um diretório antes da raiz do projeto do headless e conectar com esse front end que seria onde os atores iriam cadastrar as informações que o headless permite enviar os inputs e aparecer no front end será que em angular seria o melhor?"*

### Decisões tomadas

- Projeto criado como **diretório irmão** do backend:
```
eclipse-workspace/
├── com.cms.headless-cms-0.0.1-SNAPSHOT/   ← backend
└── cms-headless-front/                     ← frontend
```

- Angular 21 com **standalone components** (sem NgModules)
- Lazy loading por rota
- Autenticação simulada via `localStorage` + Angular Signals

### Estrutura criada

```
src/app/
├── core/
│   ├── models/         → User, Course, News, Article, Project, Event
│   ├── services/       → ApiService + um service por entidade
│   ├── guards/         → authGuard (redireciona para /login)
│   └── interceptors/   → apiInterceptor (Content-Type header)
├── shared/
│   └── components/
│       └── layout.component.ts  → sidebar + router-outlet
└── features/
    ├── auth/           → LoginComponent
    ├── dashboard/      → contadores de cada entidade
    ├── users/          → CRUD completo
    ├── courses/        → CRUD completo
    ├── news/           → CRUD completo
    ├── articles/       → CRUD completo
    ├── projects/       → CRUD completo
    └── events/         → CRUD completo
```

### Comando de criação do projeto

```bash
npx @angular/cli new cms-headless-front --routing=true --style=scss --skip-git=true --no-interactive
```

---

## 3. Configuração de deploy

### Arquivos criados

#### `netlify.toml`
```toml
[build]
  command   = "npm run build"
  publish   = "dist/cms-headless-front/browser"

[[redirects]]
  from   = "/*"
  to     = "/index.html"
  status = 200
```

#### `public/_redirects`
```
/*    /index.html    200
```
Necessário para o Angular Router funcionar após refresh de página.

#### `angular.json` — fileReplacements (crítico)
```json
"production": {
  "fileReplacements": [
    {
      "replace": "src/environments/environment.ts",
      "with": "src/environments/environment.prod.ts"
    }
  ]
}
```
**Sem isso o build de produção usa a URL de desenvolvimento (localhost).**

#### `src/environments/environment.prod.ts`
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://72.60.49.33:8081/api'
};
```

---

## 4. Deploy do backend na KVM

### Servidor
- **IP:** 72.60.49.33
- **OS:** Ubuntu 24.04 LTS
- **Usuário:** root

### Passo a passo completo

#### 1. Instalar dependências
```bash
apt-get update && apt-get upgrade -y
apt-get install -y openjdk-21-jdk postgresql postgresql-contrib nginx ufw
```

#### 2. Criar banco de dados
```bash
sudo -u postgres psql -c "CREATE DATABASE ucsal_cms;"
sudo -u postgres psql -c "CREATE USER cms_user WITH ENCRYPTED PASSWORD 'sua_senha';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ucsal_cms TO cms_user;"
sudo -u postgres psql -d ucsal_cms -c "GRANT ALL ON SCHEMA public TO cms_user;"
```

#### 3. Criar schema do banco
```bash
sudo -u postgres psql -d ucsal_cms << 'EOF'
CREATE TYPE user_role AS ENUM ('admin', 'editor', 'professor', 'student', 'viewer');
CREATE TYPE content_status AS ENUM ('draft', 'in_review', 'published', 'archived');
CREATE TYPE event_modality AS ENUM ('presencial', 'online', 'hibrido');

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'viewer',
    avatar_url TEXT, bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(50), description TEXT,
    duration_semesters INTEGER,
    coordinator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS news (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL, slug VARCHAR(255) NOT NULL UNIQUE,
    subtitle VARCHAR(500), body TEXT NOT NULL, cover_image_url TEXT,
    status content_status NOT NULL DEFAULT 'draft',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL, slug VARCHAR(255) NOT NULL UNIQUE,
    abstract TEXT, body TEXT NOT NULL, cover_image_url TEXT,
    status content_status NOT NULL DEFAULT 'draft',
    author_id UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL, slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT, cover_image_url TEXT,
    status content_status NOT NULL DEFAULT 'draft',
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    coordinator_id UUID REFERENCES users(id) ON DELETE SET NULL,
    start_date DATE, end_date DATE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL, slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT, cover_image_url TEXT,
    status content_status NOT NULL DEFAULT 'draft',
    modality event_modality NOT NULL DEFAULT 'presencial',
    location VARCHAR(500), online_url TEXT,
    starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ,
    registration_url TEXT, max_participants INTEGER,
    organizer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cms_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cms_user;
EOF
```

#### 4. Criar usuário admin
```bash
sudo -u postgres psql -d ucsal_cms -c "
INSERT INTO users (full_name, email, password_hash, role)
VALUES ('Administrador', 'admin@ucsal.br', 'admin123', 'admin');
"
```

#### 5. Copiar o JAR (na máquina local)
```bash
# Build
mvn clean package -DskipTests

# Copiar para a KVM
scp target/headless-cms-0.0.1-SNAPSHOT.jar root@72.60.49.33:/opt/cms/headless-cms.jar
```

#### 6. Criar serviço systemd
```bash
cat > /etc/systemd/system/cms.service << 'EOF'
[Unit]
Description=Headless CMS UCSal
After=network.target postgresql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/cms
ExecStart=/usr/bin/java -jar /opt/cms/headless-cms.jar
Restart=on-failure
RestartSec=10
Environment="DB_URL=jdbc:postgresql://localhost:5432/ucsal_cms"
Environment="DB_USER=cms_user"
Environment="DB_PASSWORD=sua_senha"
Environment="CORS_ORIGIN=*"

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable cms
systemctl start cms
```

#### 7. Certificado HTTPS autoassinado
```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/ssl/private/cms.key -out /etc/ssl/certs/cms.crt -subj "/CN=72.60.49.33"
```

#### 8. Configurar Nginx
```bash
cat > /etc/nginx/sites-available/cms << 'EOF'
server {
    listen 8081 ssl;
    server_name 72.60.49.33;

    ssl_certificate     /etc/ssl/certs/cms.crt;
    ssl_certificate_key /etc/ssl/private/cms.key;

    location /api/ {
        proxy_pass         http://127.0.0.1:8080/api/;
        proxy_set_header   Host $host;
        proxy_set_header   X-Real-IP $remote_addr;
    }

    location /health {
        proxy_pass http://127.0.0.1:8080/health;
    }
}
EOF

rm -f /etc/nginx/sites-enabled/default
ln -s /etc/nginx/sites-available/cms /etc/nginx/sites-enabled/cms
nginx -t && systemctl start nginx && systemctl enable nginx
```

#### 9. Firewall
```bash
ufw allow 22
ufw allow 80
ufw allow 443
ufw allow 8081
ufw --force enable
```

---

## 5. Erros encontrados e resoluções

| # | Erro | Causa | Resolução |
|---|---|---|---|
| 1 | JAR com 81KB | Sem `maven-shade-plugin` | Adicionado ao `pom.xml` |
| 2 | `ExecStart` com `-cp` | Comando errado para fat JAR | Trocado por `-jar` |
| 3 | Nginx não inicia na porta 80 | Snipe-IT (Docker) usando a porta | Nginx movido para porta 8081 |
| 4 | Nginx não inicia mesmo na 8081 | Site `default` conflitando | `rm /etc/nginx/sites-enabled/default` |
| 5 | `relation "users" does not exist` | Schema nunca foi criado | Executado `schema.sql` |
| 6 | Heredoc corrompido | `eof` minúsculo incluído no conteúdo | `EOF` deve estar sozinho na linha |
| 7 | `permission denied for table users` | GRANTs não concedidos ao `cms_user` | `GRANT ALL PRIVILEGES ON ALL TABLES` |
| 8 | API single-threaded travando | `server.setExecutor(null)` | Trocado por `Executors.newFixedThreadPool(10)` |
| 9 | Mixed Content no Netlify | API em HTTP, Netlify em HTTPS | Certificado autoassinado + HTTPS no Nginx |
| 10 | Build usando URL localhost | `fileReplacements` ausente no `angular.json` | Adicionado `fileReplacements` na config `production` |
| 11 | `ERR_CERT_AUTHORITY_INVALID` | Certificado autoassinado não reconhecido | Aceitar manualmente no navegador (ver seção 7) |

---

## 6. Deploy do frontend no Netlify

1. Acesse [app.netlify.com](https://app.netlify.com)
2. **Add new site** → **Import from Git** → GitHub
3. Selecione o repositório `cms-headless`
4. Configure:

| Campo | Valor |
|---|---|
| Branch | `frontend` |
| Build command | `npm run build` |
| Publish directory | `dist/cms-headless-front/browser` |

5. **Deploy site**

O `netlify.toml` já configura isso automaticamente.

---

## 7. Certificado autoassinado — como aceitar

**Por que é necessário?**

O Netlify serve o site em HTTPS. Quando o frontend tenta chamar a API, o navegador exige que a API também seja HTTPS. A API usa um certificado autoassinado (gerado manualmente, não por uma autoridade reconhecida), então o navegador bloqueia por padrão.

**Procedimento (uma vez por navegador):**

1. Abra no navegador: `https://72.60.49.33:8081/api/users`
2. Clique em **"Avançado"** → **"Continuar mesmo assim"**
3. Confirme que aparece o JSON com os usuários
4. Volte para `https://blogdaucsal.netlify.app/login`

> Cada usuário precisa fazer isso uma vez no próprio navegador.  
> **Solução definitiva:** configurar um domínio com Let's Encrypt (DuckDNS gratuito).

---

## 8. Credenciais de acesso

| Campo | Valor |
|---|---|
| URL | https://blogdaucsal.netlify.app/login |
| E-mail | admin@ucsal.br |
| Senha | admin123 |

> A senha está em texto puro por enquanto. Para produção real, implementar hash (bcrypt) no backend.

---

## 9. Comandos úteis

### Na KVM

```bash
# Status dos serviços
systemctl status cms nginx postgresql

# Logs da API em tempo real
journalctl -u cms -f

# Reiniciar após novo JAR
systemctl restart cms

# Verificar portas em uso
ss -tlnp | grep -E ':80|:8081|:8080'

# Testar API localmente
curl -k https://localhost:8081/health
curl -k https://localhost:8081/api/users
```

### Na máquina local

```bash
# Build do backend
cd com.cms.headless-cms-0.0.1-SNAPSHOT
mvn clean package -DskipTests

# Enviar JAR para a KVM
scp target/headless-cms-0.0.1-SNAPSHOT.jar root@72.60.49.33:/opt/cms/headless-cms.jar

# Build do frontend
cd cms-headless-front
npm run build

# Subir alterações do frontend
git add . && git commit -m "mensagem" && git push origin frontend

# Subir alterações do backend
git add . && git commit -m "mensagem" && git push origin main
```

---

## Próximos passos recomendados

- [ ] Configurar domínio com DuckDNS + Let's Encrypt (elimina aviso de certificado)
- [ ] Implementar hash de senha no backend (bcrypt)
- [ ] Criar endpoint `/api/auth/login` com JWT real
- [ ] Implementar controle de acesso por role no frontend
- [ ] Configurar backup automático do PostgreSQL
