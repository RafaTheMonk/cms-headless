# CMS Headless Front — UCSal

Interface de administração Angular para o Headless CMS da Universidade Católica do Salvador.

## Stack

| Camada | Tecnologia |
|---|---|
| Framework | Angular 21 (standalone components) |
| Linguagem | TypeScript 5 |
| Estilos | SCSS |
| HTTP Client | `HttpClient` (Angular nativo) |
| Formulários | `ReactiveFormsModule` |
| Build | Angular CLI 21 |

---

## Arquitetura

```
src/app/
├── core/
│   ├── models/                 → interfaces TypeScript das entidades
│   │   ├── user.model.ts       → User, UserRole
│   │   ├── course.model.ts     → Course
│   │   ├── news.model.ts       → News, ContentStatus
│   │   ├── article.model.ts    → Article
│   │   ├── project.model.ts    → Project
│   │   └── event.model.ts      → Event, EventModality
│   ├── services/               → chamadas HTTP ao backend
│   │   ├── api.service.ts      → wrapper genérico do HttpClient
│   │   ├── auth.service.ts     → gerencia sessão do usuário (Angular Signal)
│   │   ├── user.service.ts
│   │   ├── course.service.ts
│   │   ├── news.service.ts
│   │   ├── article.service.ts
│   │   ├── project.service.ts
│   │   └── event.service.ts
│   ├── guards/
│   │   └── auth.guard.ts       → redireciona para /login se não autenticado
│   └── interceptors/
│       └── api.interceptor.ts  → adiciona Content-Type em todas requisições
├── shared/
│   └── components/
│       └── layout.component.ts → shell com sidebar + router-outlet
└── features/
    ├── auth/
    │   └── login.component.ts      → tela de login
    ├── dashboard/
    │   └── dashboard.component.ts  → contadores e navegação rápida
    ├── users/
    │   └── users.component.ts      → CRUD de usuários
    ├── courses/
    │   └── courses.component.ts    → CRUD de cursos
    ├── news/
    │   └── news.component.ts       → CRUD de notícias
    ├── articles/
    │   └── articles.component.ts   → CRUD de artigos
    ├── projects/
    │   └── projects.component.ts   → CRUD de projetos
    └── events/
        └── events.component.ts     → CRUD de eventos
```

### Fluxo de navegação

```
/ → redirect para /dashboard (se logado) ou /login
/login → LoginComponent
  └── sucesso → /dashboard
/dashboard → DashboardComponent (contadores de cada entidade)
/users     → UsersComponent     (tabela + modal CRUD)
/courses   → CoursesComponent   (tabela + modal CRUD)
/news      → NewsComponent       (tabela + modal CRUD)
/articles  → ArticlesComponent   (tabela + modal CRUD)
/projects  → ProjectsComponent   (tabela + modal CRUD)
/events    → EventsComponent     (tabela + modal CRUD)
```

### Autenticação

A autenticação é simulada: o frontend busca `/api/users`, encontra o usuário pelo e-mail/senha e persiste no `localStorage`. Para produção, substitua por autenticação JWT real no backend.

---

## Perfis de usuário (UserRole)

| Role | Descrição |
|---|---|
| `admin` | Acesso total |
| `editor` | Cria e edita conteúdo |
| `professor` | Cria conteúdo próprio |
| `student` | Leitura e submissão |
| `viewer` | Somente leitura |

---

## Status de conteúdo (ContentStatus)

| Status | Descrição |
|---|---|
| `draft` | Rascunho — não publicado |
| `in_review` | Em revisão |
| `published` | Publicado e visível |
| `archived` | Arquivado |

---

## Rodando localmente

### Pré-requisitos

- Node.js 18+
- npm 9+
- Backend rodando em `http://localhost:8080`

### Instalação e execução

```bash
cd cms-headless-front
npm install
npm start
```

Acesse `http://localhost:4200`.

> O backend precisa estar rodando com `CORS_ORIGIN=http://localhost:4200` ou `CORS_ORIGIN=*`.

---

## Configuração de ambiente

| Arquivo | Uso |
|---|---|
| `src/environments/environment.ts` | Desenvolvimento local (`localhost:8080`) |
| `src/environments/environment.prod.ts` | Build de produção (URL da KVM) |

### Antes do deploy, edite `environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://SEU_DOMINIO_OU_IP/api'  // URL real da KVM
};
```

---

## Deploy — Netlify (Produção)

### Arquitetura de produção

```
Usuário (navegador)
       │
       ▼
  Netlify CDN
  (arquivos estáticos gerados por ng build)
       │ HTTPS requests para /api/*
       ▼
  KVM Hostinger (Nginx + Java API)
       │
       ▼
  PostgreSQL
```

### Opção 1 — Deploy via GitHub (recomendado)

1. Edite `src/environments/environment.prod.ts` com a URL da sua KVM
2. Faça commit e push para o GitHub
3. Acesse [app.netlify.com](https://app.netlify.com) → **Add new site** → **Import from Git**
4. Selecione o repositório `cms-headless-front`
5. O `netlify.toml` já configura automaticamente:

| Campo | Valor |
|---|---|
| Build command | `npm run build` |
| Publish directory | `dist/cms-headless-front/browser` |

6. Clique em **Deploy site**

### Opção 2 — Deploy manual (drag & drop)

```bash
# Gerar build de produção:
npm run build

# Arraste esta pasta para app.netlify.com/drop:
dist/cms-headless-front/browser/
```

### Suporte a rotas do Angular

O arquivo `public/_redirects` garante que o refresh de página em qualquer rota funcione:

```
/*    /index.html    200
```

O `netlify.toml` também declara isso como `[[redirects]]`.

### Verificando o deploy

```
https://seu-app.netlify.app/          → redireciona para /login
https://seu-app.netlify.app/login     → tela de login
https://seu-app.netlify.app/dashboard → painel (requer login)
```

---

## Checklist de produção

- [ ] Editar `environment.prod.ts` com a URL real da KVM
- [ ] Build de produção: `npm run build`
- [ ] Confirmar que `public/_redirects` existe no build
- [ ] Confirmar que `netlify.toml` aponta para `dist/cms-headless-front/browser`
- [ ] No backend: `CORS_ORIGIN=https://seu-app.netlify.app`
- [ ] Testar login, CRUD de cada entidade e refresh de página nas rotas

---

## Backend relacionado

O backend Java está em `../com.cms.headless-cms-0.0.1-SNAPSHOT`.
Veja `../com.cms.headless-cms-0.0.1-SNAPSHOT/README.md` para instruções de deploy na KVM Hostinger.
