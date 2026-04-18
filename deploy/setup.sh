#!/bin/bash
# Script de setup inicial na KVM Hostinger (Ubuntu 22.04)
# Execute como root: bash setup.sh

set -e

echo "=== [1/6] Atualizando pacotes ==="
apt-get update && apt-get upgrade -y

echo "=== [2/6] Instalando Java 21 ==="
apt-get install -y openjdk-21-jdk

echo "=== [3/6] Instalando PostgreSQL ==="
apt-get install -y postgresql postgresql-contrib

echo "=== [4/6] Instalando Nginx ==="
apt-get install -y nginx

echo "=== [5/6] Instalando Certbot (HTTPS) ==="
apt-get install -y certbot python3-certbot-nginx

echo "=== [6/6] Criando diretório da aplicação ==="
mkdir -p /opt/cms
chown ubuntu:ubuntu /opt/cms

echo ""
echo "Setup concluído. Próximos passos:"
echo "  1. Copie o JAR para /opt/cms/headless-cms.jar"
echo "  2. Configure o banco: veja deploy/postgres-setup.sql"
echo "  3. Copie o nginx.conf para /etc/nginx/sites-available/cms"
echo "  4. Copie o cms.service para /etc/systemd/system/cms.service"
echo "  5. Execute: systemctl enable cms && systemctl start cms"
echo "  6. Execute: certbot --nginx -d SEU_DOMINIO"
