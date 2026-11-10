#!/usr/bin/env bash
set -euo pipefail

export DB_USER="${DB_USER:-sa}"
export DB_PASSWORD="${DB_PASSWORD:-NopCommerce@2026!}"
export BASE_URL="${BASE_URL:-http://127.0.0.1:5000}"

echo "=== [1/5] Starting isolated nopCommerce Docker stack ==="
docker compose -f docker/docker-compose.yml up -d

echo "=== [2/5] Waiting for nopcommerce_mssql to report healthy ==="
timeout 120s bash -c 'until [ "$(docker inspect -f {{.State.Health.Status}} nopcommerce_mssql 2>/dev/null)" = "healthy" ]; do sleep 2; done'

echo "=== [3/5] Checking and restoring baseline nopCommerce database ==="
TABLE_COUNT=$(docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U "${DB_USER}" -P "${DB_PASSWORD}" -C -d nopcommerce_db -Q "SET NOCOUNT ON; SELECT count(*) FROM sys.tables" -h -1 2>/dev/null | tr -d ' \r\n' || echo "0")

if [ "${TABLE_COUNT}" -lt "100" ]; then
    echo "Restoring database from /docker-seed/nopcommerce_baseline.bak..."
    docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U "${DB_USER}" -P "${DB_PASSWORD}" -C -Q \
        "IF DB_ID('nopcommerce_db') IS NOT NULL ALTER DATABASE [nopcommerce_db] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; RESTORE DATABASE [nopcommerce_db] FROM DISK = '/docker-seed/nopcommerce_baseline.bak' WITH MOVE 'nopcommerce_db' TO '/var/opt/mssql/data/nopcommerce_db.mdf', MOVE 'nopcommerce_db_log' TO '/var/opt/mssql/data/nopcommerce_db_log.ldf', REPLACE; ALTER DATABASE [nopcommerce_db] SET MULTI_USER;"
    echo "Database restoration completed successfully."
else
    echo "Database already initialized (${TABLE_COUNT} tables present)."
fi

echo "=== [4/5] Ensuring nopCommerce App_Data configuration ==="
APPSETTINGS_EXISTS=$(docker exec nopcommerce_web sh -c '[ -f /app/App_Data/appsettings.json ] && echo 1 || echo 0' 2>/dev/null || echo "0")
if [ "${APPSETTINGS_EXISTS}" != "1" ] || [ "${TABLE_COUNT}" -lt "100" ]; then
    echo "Deploying /app/App_Data/appsettings.json..."
    docker exec nopcommerce_web cp /docker-seed/appsettings.json /app/App_Data/appsettings.json
    docker exec nopcommerce_web chmod 666 /app/App_Data/appsettings.json 2>/dev/null || true
    echo "Restarting nopcommerce_web to apply configuration..."
    docker restart nopcommerce_web
fi

echo "=== [5/5] Verifying storefront HTTP 200 readiness on ${BASE_URL} ==="
timeout 120s bash -c 'until curl -fsSL "${BASE_URL}/" 2>/dev/null | grep -qi "nopCommerce"; do sleep 3; done'

echo "=== Isolated nopCommerce AUT is fully initialized, healthy, and ready! ==="

