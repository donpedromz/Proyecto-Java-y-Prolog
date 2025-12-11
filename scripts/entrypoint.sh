#!/bin/sh
set -euo pipefail

# Espera a que MySQL esté listo
until mysqladmin ping -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" --silent; do
  echo "MySQL no está listo, reintentando..."
  sleep 2
done

echo "Aplicando schema y datos base..."
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < /app/db/init.sql

echo "Iniciando aplicación Java..."
exec java -jar /app/app.jar
