# Deploy Log — Headless CMS UCSal
**Servidor:** KVM Hostinger `72.60.49.33` — Ubuntu 24.04 LTS  
**Data:** 2026-04-18

---

## Índice

1. [Erro: JAR muito pequeno (sem dependências)](#1-erro-jar-muito-pequeno-sem-dependências)
2. [Erro: ExecStart com -cp ao invés de -jar](#2-erro-execstart-com--cp-ao-invés-de--jar)
3. [Erro: Nginx não inicia — porta 80 ocupada pelo Docker](#3-erro-nginx-não-inicia--porta-80-ocupada-pelo-docker)
4. [Erro: Nginx default site conflitando na porta 80](#4-erro-nginx-default-site-conflitando-na-porta-80)
5. [Erro: Tabelas não existem no banco](#5-erro-tabelas-não-existem-no-banco)
6. [Erro: Heredoc com EOF corrompido](#6-erro-heredoc-com-eof-corrompido)

---

## 1. Erro: JAR muito pequeno (sem dependências)

### Sintoma
```
Active: activating (auto-restart) (Result: exit-code)
Process: ExecStart=/usr/bin/java -cp /opt/cms/headless-cms.jar com.cms.App (code=exited, status=1/FAILURE)
```
O JAR gerado tinha apenas **81KB** — o driver PostgreSQL (~1.4MB) não foi incluído.

### Causa
O `pom.xml` não tinha plugin para gerar um **fat JAR** (JAR com todas as dependências embutidas). Apenas o `exec-maven-plugin` estava configurado, que serve somente para rodar localmente via `mvn exec:java`.

### Resolução
Adicionar o `maven-shade-plugin` ao `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.2</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.cms.App</mainClass>
                    </transformer>
                </transformers>
                <createDependencyReducedPom>false</createDependencyReducedPom>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Rebuild:
```bash
mvn clean package -DskipTests
```

JAR resultante: **1.4MB** ✓

---

## 2. Erro: ExecStart com -cp ao invés de -jar

### Sintoma
```
ExecStart=/usr/bin/java -cp /opt/cms/headless-cms.jar com.cms.App
```
O serviço systemd foi criado com `-cp` (classpath) que não executa o JAR como executável standalone.

### Causa
O comando `-cp` exige que todas as dependências estejam separadas no classpath. Para um fat JAR o comando correto é `-jar`.

### Resolução
```bash
sed -i 's|ExecStart=.*|ExecStart=/usr/bin/java -jar /opt/cms/headless-cms.jar|' /etc/systemd/system/cms.service
systemctl daemon-reload
systemctl restart cms
```

---

## 3. Erro: Nginx não inicia — porta 80 ocupada pelo Docker

### Sintoma
```
nginx: [emerg] bind() to 0.0.0.0:80 failed (98: Address already in use)
Job for nginx.service failed
```

### Causa
O **Snipe-IT** (sistema de gestão de ativos) estava rodando via Docker e ocupando a porta 80:
```
CONTAINER ID   IMAGE                   PORTS
d61e08ef845f   snipe/snipe-it:latest   0.0.0.0:80->80/tcp
```

### Resolução
Mudar o Nginx do CMS para a porta **8081** em vez de conflitar com o Snipe-IT:

```bash
sed -i 's/listen 80;/listen 8081;/' /etc/nginx/sites-available/cms
```

Liberar a porta no firewall:
```bash
ufw allow 8081
```

API acessível em: `http://72.60.49.33:8081`

---

## 4. Erro: Nginx default site conflitando na porta 80

### Sintoma
Mesmo após mudar para a porta 8081, o Nginx não iniciava:
```
nginx: [emerg] bind() to 0.0.0.0:80 failed (98: Address already in use)
Job for nginx.service failed
```

### Causa
O arquivo `/etc/nginx/sites-enabled/default` (instalado por padrão no Ubuntu) ainda tentava usar a porta 80 ao lado do site do CMS.

### Resolução
```bash
rm /etc/nginx/sites-enabled/default
systemctl start nginx
```

---

## 5. Erro: Tabelas não existem no banco

### Sintoma
```
{"error":"Erro interno"}

[UserHandler] ERROR: relation "users" does not exist
  Position: 122
```

### Causa
O banco `ucsal_cms` foi criado mas o **schema (tabelas e tipos ENUM) nunca foi executado**. O projeto não tem migrations automáticas — o schema precisa ser criado manualmente.

### Resolução
Executar o schema completo no PostgreSQL:

```bash
sudo -u postgres psql -d ucsal_cms -f /caminho/para/deploy/schema.sql
```

Ou colar diretamente via heredoc (ver arquivo `deploy/schema.sql`).

**Tipos ENUM necessários:**
```sql
CREATE TYPE user_role     AS ENUM ('admin', 'editor', 'professor', 'student', 'viewer');
CREATE TYPE content_status AS ENUM ('draft', 'in_review', 'published', 'archived');
CREATE TYPE event_modality AS ENUM ('presencial', 'online', 'hibrido');
```

**Tabelas criadas:** `users`, `courses`, `news`, `articles`, `projects`, `events`

---

## 6. Erro: Heredoc com EOF corrompido

### Sintoma
```
nginx: [emerg] unexpected end of file, expecting ";" or "}" in /etc/nginx/sites-enabled/cms:16
```
E no psql, linha com conteúdo misturado:
```
);  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

### Causa
Ao copiar o bloco `cat > arquivo << 'EOF' ... EOF` no terminal, o usuário digitou `eof` (minúsculo) antes do `EOF` correto. O heredoc só fecha quando encontra `EOF` **sozinho na linha, sem espaços, exatamente igual ao delimitador de abertura**.

Resultado: o `eof` foi incluído como conteúdo do arquivo, corrompendo o texto.

### Resolução
Reescrever o arquivo corrompido com o comando correto:
```bash
cat > /etc/nginx/sites-available/cms << 'EOF'
server {
    ...conteúdo correto...
}
EOF
```

**Regras do heredoc:**
- O delimitador de fechamento (`EOF`) deve estar **sozinho na linha**
- Sem espaços antes ou depois
- Maiúsculas e minúsculas importam (`EOF` ≠ `eof`)

---

## 7. Erro: Permission denied nas tabelas

### Sintoma
```
[UserHandler] ERROR: permission denied for table users
```

### Causa
O schema foi criado pelo superusuário `postgres`, mas o serviço conecta com o usuário `cms_user`. No PostgreSQL 15+, criar um banco não concede automaticamente permissões nas tabelas a outros usuários.

### Resolução
```bash
sudo -u postgres psql -d ucsal_cms -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cms_user;"
sudo -u postgres psql -d ucsal_cms -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cms_user;"
```

> **Dica:** Para evitar isso em novos deploys, adicione esses dois GRANTs ao final do `deploy/schema.sql`.

---

## Estado final

| Componente | Status | Endereço |
|---|---|---|
| Java API | ✅ rodando | `localhost:8080` (interno) |
| Nginx proxy | ✅ rodando | `http://72.60.49.33:8081` |
| PostgreSQL | ✅ rodando | `localhost:5432` |
| Schema | ✅ criado | banco `ucsal_cms` |

### Endpoints verificados
```bash
curl http://72.60.49.33:8081/health   # {"status":"ok"}
curl http://72.60.49.33:8081/api/users
```

### Comandos úteis na KVM
```bash
# Ver logs da API em tempo real
journalctl -u cms -f

# Reiniciar após atualizar o JAR
systemctl restart cms

# Verificar processos nas portas
ss -tlnp | grep -E ':80|:8081|:8080'

# Status dos serviços
systemctl status cms nginx postgresql
```

---

## Arquivos de deploy criados

```
deploy/
├── schema.sql          → cria tipos ENUM e todas as tabelas
├── setup.sh            → instala dependências no Ubuntu 24.04
├── postgres-setup.sql  → cria banco e usuário PostgreSQL
├── nginx.conf          → configuração do reverse proxy
├── cms.service         → serviço systemd
└── DEPLOY_LOG.md       → este arquivo
```
