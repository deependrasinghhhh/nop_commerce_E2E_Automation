#!/usr/bin/env bash
set -euo pipefail

# Git Bash otherwise rewrites Linux paths passed through `docker exec` into Windows paths.
export MSYS_NO_PATHCONV=1

if [[ -z "${DB_PASSWORD:-}" && -f .env ]]; then
    set -a
    # Local .env files use simple KEY=value entries, compatible with shell sourcing.
    # CI supplies these values directly through its secret environment variables.
    # shellcheck disable=SC1091
    . ./.env
    set +a
fi

export DB_USER="${DB_USER:-sa}"
: "${DB_PASSWORD:?Set DB_PASSWORD in the environment or root .env file before starting Docker}"
export BASE_URL="${BASE_URL:-http://127.0.0.1:5000}"

echo "=== [1/5] Starting isolated nopCommerce Docker stack ==="
if ! docker network inspect docker_qa-network >/dev/null 2>&1; then
    docker network create docker_qa-network >/dev/null
fi
docker compose -f docker/docker-compose.yml up -d

echo "=== [2/5] Waiting for nopcommerce_mssql to report healthy ==="
timeout 120s bash -c 'until [ "$(docker inspect -f {{.State.Health.Status}} nopcommerce_mssql 2>/dev/null)" = "healthy" ]; do sleep 2; done'

echo "=== [3/5] Checking and restoring baseline nopCommerce database ==="
if ! TABLE_COUNT=$(docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U "${DB_USER}" -P "${DB_PASSWORD}" -C -d master -Q "SET NOCOUNT ON; IF DB_ID(N'nopcommerce_db') IS NULL SELECT 0 ELSE EXEC(N'SELECT COUNT(*) FROM [nopcommerce_db].sys.tables')" -h -1 2>/dev/null | tr -d ' \r\n'); then
    echo "ERROR: Could not query SQL Server to verify the nopCommerce database." >&2
    exit 1
fi
if [[ ! "${TABLE_COUNT}" =~ ^[0-9]+$ ]]; then
    echo "ERROR: SQL Server returned an invalid table count; refusing to continue." >&2
    exit 1
fi

if [ "${TABLE_COUNT}" -lt "100" ]; then
    echo "Restoring database from /docker-seed/nopcommerce_baseline.bak..."
    docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U "${DB_USER}" -P "${DB_PASSWORD}" -C -Q \
        "IF DB_ID('nopcommerce_db') IS NOT NULL ALTER DATABASE [nopcommerce_db] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; RESTORE DATABASE [nopcommerce_db] FROM DISK = '/docker-seed/nopcommerce_baseline.bak' WITH MOVE 'nopcommerce_db' TO '/var/opt/mssql/data/nopcommerce_db.mdf', MOVE 'nopcommerce_db_log' TO '/var/opt/mssql/data/nopcommerce_db_log.ldf', REPLACE; ALTER DATABASE [nopcommerce_db] SET MULTI_USER;"
    echo "Database restoration completed successfully."
else
    echo "Database already initialized (${TABLE_COUNT} tables present)."
fi

echo "=== [4/5] Ensuring nopCommerce App_Data configuration ==="
if ! APPSETTINGS_EXISTS=$(docker exec nopcommerce_web sh -c '[ -f /app/App_Data/appsettings.json ] && echo 1 || echo 0' 2>/dev/null); then
    echo "ERROR: Could not check nopCommerce App_Data configuration." >&2
    exit 1
fi
if [ "${APPSETTINGS_EXISTS}" != "1" ] || [ "${TABLE_COUNT}" -lt "100" ]; then
    echo "Deploying /app/App_Data/appsettings.json..."
    docker exec nopcommerce_web cp /docker-seed/appsettings.json /app/App_Data/appsettings.json
    echo "Restarting nopcommerce_web to apply configuration..."
    docker restart nopcommerce_web
fi

echo "=== [5/5] Verifying storefront HTTP 200 readiness on ${BASE_URL} ==="
timeout 120s bash -c 'until curl -fsSL "${BASE_URL}/" 2>/dev/null | grep -qi "nopCommerce"; do sleep 3; done'

echo "=== Isolated nopCommerce AUT is fully initialized, healthy, and ready! ==="

