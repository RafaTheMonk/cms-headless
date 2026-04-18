# Headless CMS — UCSal

API REST para gerenciamento de conteúdo acadêmico da Universidade Católica do Salvador.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| HTTP Server | `com.sun.net.httpserver` (nativo do JDK) |
| Banco de dados | PostgreSQL 15+ |
| Driver JDBC | `org.postgresql:postgresql:42.7.3` |
| Build | Maven 3 |

---

## Arquitetura

```
src/main/java/com/cms/
├── App.java                    → entry point, sobe o HttpServer
├── db/
│   └── Database.java           → singleton de conexão PostgreSQL
├── server/
│   ├── Router.java             → registro centralizado de rotas
│   └── JsonUtil.java           → serialização JSON + CORS
├── handler/                    → camada HTTP (recebe request, devolve response)
│   ├── UserHandler.java
│   ├── CourseHandler.java
│   ├── NewsHandler.java
│   ├── ArticleHandler.java
│   ├── ProjectHandler.java
│   ├── EventHandler.java
│   └── ContentHandler.java
├── repository/                 → camada de dados (SQL via JDBC)
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   ├── NewsRepository.java
│   ├── ArticleRepository.java
│   ├── ProjectRepository.java
│   └── EventRepository.java
└── model/                      → entidades e enums de domínio
    ├── User.java
    ├── Course.java
    ├── News.java
    ├── Article.java
    ├── Project.java
    ├── Event.java
    └── enums/
        ├── UserRole.java       → admin | editor | professor | student | viewer
        ├── ContentStatus.java  → draft | in_review | published | archived
        ├── EventModality.java  → presencial | online | hibrido
        └── SemesterPeriod.java
```

### Fluxo de uma requisição

```
Cliente HTTP
    │
    ▼
HttpServer (porta 8080)
    │
    ▼
Router → registra cada path em seu Handler
    │
    ▼
Handler → parse JSON → chama Repository
    │
    ▼
Repository → PreparedStatement → PostgreSQL
    │
    ▼
Handler → JsonUtil.sendJson() → resposta HTTP
```

---

## Endpoints

| Método | Path | Descrição |
|---|---|---|
| GET | `/health` | Verifica se o servidor está no ar |
| GET | `/api/users` | Lista todos os usuários |
| POST | `/api/users` | Cria um usuário |
| PUT | `/api/users/{id}` | Atualiza um usuário |
| DELETE | `/api/users/{id}` | Remove um usuário |
| GET | `/api/courses` | Lista todos os cursos |
| POST | `/api/courses` | Cria um curso |
| PUT | `/api/courses/{id}` | Atualiza um curso |
| DELETE | `/api/courses/{id}` | Remove um curso |
| GET | `/api/news` | Lista todas as notícias |
| POST | `/api/news` | Cria uma notícia |
| PUT | `/api/news/{id}` | Atualiza uma notícia |
| DELETE | `/api/news/{id}` | Remove uma notícia |
| GET | `/api/articles` | Lista todos os artigos |
| POST | `/api/articles` | Cria um artigo |
| PUT | `/api/articles/{id}` | Atualiza um artigo |
| DELETE | `/api/articles/{id}` | Remove um artigo |
| GET | `/api/projects` | Lista todos os projetos |
| POST | `/api/projects` | Cria um projeto |
| PUT | `/api/projects/{id}` | Atualiza um projeto |
| DELETE | `/api/projects/{id}` | Remove um projeto |
| GET | `/api/events` | Lista todos os eventos |
| POST | `/api/events` | Cria um evento |
| PUT | `/api/events/{id}` | Atualiza um evento |
| DELETE | `/api/events/{id}` | Remove um evento |

---

## Variáveis de ambiente

Copie `.env.example` para `.env` e preencha os valores:

```bash
cp .env.example .env
```

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ucsal_cms` | URL de conexão JDBC |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `CORS_ORIGIN` | `*` | Origem permitida pelo CORS (ex: `https://app.netlify.app`) |

---

## Rodando localmente

### Pré-requisitos

- Java 21+
- Maven 3.6+
- PostgreSQL rodando localmente

### Banco de dados

```sql
CREATE DATABASE ucsal_cms;
CREATE USER cms_user WITH ENCRYPTED PASSWORD 'senha';
GRANT ALL PRIVILEGES ON DATABASE ucsal_cms TO cms_user;
```

### Subindo a API

```bash
# Compilar e empacotar
mvn clean package -DskipTests

# Definir variáveis de ambiente e subir
export DB_URL=jdbc:postgresql://localhost:5432/ucsal_cms
export DB_USER=cms_user
export DB_PASSWORD=senha
export CORS_ORIGIN=http://localhost:4200

java -jar target/headless-cms-0.0.1-SNAPSHOT.jar
```

A API ficará disponível em `http://localhost:8080`.

---

## Deploy — KVM Hostinger (Produção)

### Arquitetura de produção

```
Usuário (navegador)
       │
       ▼
  Netlify CDN
  (Angular — estático)
       │ HTTPS requests para /api/*
       ▼
  Nginx (KVM Hostinger)
  porta 443 → proxy_pass → 127.0.0.1:8080
       │
       ▼
  Java HttpServer (JAR)
       │
       ▼
  PostgreSQL (local na KVM)
```

### Arquivos de deploy

```
deploy/
├── setup.sh            → instala Java, PostgreSQL, Nginx, Certbot
├── postgres-setup.sql  → cria banco e usuário
├── nginx.conf          → configuração do reverse proxy + HTTPS
└── cms.service         → serviço systemd para o JAR
```

### Passo a passo na KVM

#### 1. Setup inicial (execute uma vez)

```bash
# Na KVM, como root:
bash deploy/setup.sh
```

#### 2. Banco de dados

```bash
# Edite a senha antes de executar:
nano deploy/postgres-setup.sql

sudo -u postgres psql -f deploy/postgres-setup.sql
```

#### 3. Build e cópia do JAR

```bash
# Na sua máquina local:
mvn clean package -DskipTests

# Copiar para a KVM:
scp target/headless-cms-0.0.1-SNAPSHOT.jar ubuntu@SEU_IP:/opt/cms/headless-cms.jar
```

#### 4. Serviço systemd

```bash
# Edite com seus valores reais (DB_PASSWORD, CORS_ORIGIN, etc.):
nano deploy/cms.service

# Copiar e ativar:
sudo cp deploy/cms.service /etc/systemd/system/cms.service
sudo systemctl daemon-reload
sudo systemctl enable cms
sudo systemctl start cms

# Verificar status:
sudo systemctl status cms
sudo journalctl -u cms -f
```

#### 5. Nginx como reverse proxy

```bash
# Substitua SEU_DOMINIO_OU_IP no arquivo:
nano deploy/nginx.conf

# Ativar:
sudo cp deploy/nginx.conf /etc/nginx/sites-available/cms
sudo ln -s /etc/nginx/sites-available/cms /etc/nginx/sites-enabled/cms
sudo nginx -t && sudo systemctl reload nginx
```

#### 6. HTTPS com Let's Encrypt

```bash
sudo certbot --nginx -d SEU_DOMINIO
```

#### 7. Firewall

```bash
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
# Porta 8080 NÃO deve ser pública — apenas Nginx acessa localmente
```

### Verificando o deploy

```bash
curl https://SEU_DOMINIO/health
# {"status":"ok"}

curl https://SEU_DOMINIO/api/users
# [] ou lista de usuários
```

### Comandos úteis na KVM

```bash
# Reiniciar o serviço após atualizar o JAR:
sudo systemctl restart cms

# Ver logs em tempo real:
sudo journalctl -u cms -f

# Verificar se está rodando na porta 8080:
ss -tlnp | grep 8080
```

---

## Segurança — checklist de produção

- [ ] Alterar `DB_PASSWORD` para uma senha forte
- [ ] Definir `CORS_ORIGIN` com o domínio exato do Netlify (não `*`)
- [ ] Habilitar firewall (UFW) — porta 8080 não deve ser pública
- [ ] Habilitar HTTPS via Let's Encrypt
- [ ] Configurar backup automático do PostgreSQL (`pg_dump`)
- [ ] Adicionar autenticação JWT (não implementado nesta versão)

---

## Frontend relacionado

O frontend Angular está em `../cms-headless-front`.
Veja `../cms-headless-front/README.md` para instruções de deploy no Netlify.
