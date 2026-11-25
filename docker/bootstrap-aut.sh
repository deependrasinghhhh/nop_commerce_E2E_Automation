#!/usr/bin/env bash
set -euo pipefail

# Git Bash otherwise rewrites Linux paths passed through `docker exec` into Windows paths.
export MSYS_NO_PATHCONV=1

if [[ -z "${DB_PASSWORD:-}" && -f .env ]]; then
    # Parse only the settings this bootstrap needs; never execute .env as shell code.
    while IFS= read -r line || [[ -n "${line}" ]]; do
        [[ "${line}" =~ ^[[:space:]]*($|#) ]] && continue
        if [[ "${line}" =~ ^[[:space:]]*(DB_USER|DB_PASSWORD|BASE_URL)[[:space:]]*=[[:space:]]*(.*)$ ]]; then
            key="${BASH_REMATCH[1]}"
            value="${BASH_REMATCH[2]%$'\r'}"
            if [[ "${value}" == \"*\" ]]; then
                value="${value:1:${#value}-2}"
            elif [[ "${value}" == \'*\' ]]; then
                value="${value:1:${#value}-2}"
            fi
            if [[ -z "${!key:-}" ]]; then
                export "${key}=${value}"
            fi
        fi
    done < .env
fi

export DB_USER="${DB_USER:-sa}"
: "${DB_PASSWORD:?Set DB_PASSWORD in the environment or root .env file before starting Docker}"
export BASE_URL="${BASE_URL:-http://127.0.0.1:5000}"

wait_until() {
    local max_seconds="$1"
    local interval="$2"
    local command_str="$3"
    local elapsed=0
    until eval "${command_str}"; do
        if [ "${elapsed}" -ge "${max_seconds}" ]; then
            return 1
        fi
        sleep "${interval}"
        elapsed=$((elapsed + interval))
    done
}

echo "=== [1/5] Starting MSSQL database container ==="
if ! docker network inspect docker_qa-network >/dev/null 2>&1; then
    docker network create docker_qa-network >/dev/null
fi
docker compose -f docker/docker-compose.yml up -d nopcommerce_database

echo "=== [2/5] Waiting for nopcommerce_mssql to report healthy ==="
wait_until 120 2 '[ "$(docker inspect -f {{.State.Health.Status}} nopcommerce_mssql 2>/dev/null)" = "healthy" ]'

echo "=== [3/5] Checking and restoring baseline nopCommerce database ==="
if ! TABLE_COUNT=$(docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "${DB_PASSWORD}" -C -d master -Q "SET NOCOUNT ON; IF DB_ID(N'nopcommerce_db') IS NULL SELECT 0 ELSE EXEC(N'SELECT COUNT(*) FROM [nopcommerce_db].sys.tables')" -h -1 2>/dev/null | tr -d ' \r\n'); then
    echo "ERROR: Could not query SQL Server to verify the nopCommerce database." >&2
    exit 1
fi
if [[ ! "${TABLE_COUNT}" =~ ^[0-9]+$ ]]; then
    echo "ERROR: SQL Server returned an invalid table count; refusing to continue." >&2
    exit 1
fi

if [ "${TABLE_COUNT}" -lt "100" ]; then
    echo "Restoring database from /docker-seed/nopcommerce_baseline.bak..."
    docker exec nopcommerce_mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "${DB_PASSWORD}" -C -Q \
        "IF DB_ID('nopcommerce_db') IS NOT NULL ALTER DATABASE [nopcommerce_db] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; RESTORE DATABASE [nopcommerce_db] FROM DISK = '/docker-seed/nopcommerce_baseline.bak' WITH MOVE 'nopcommerce_db' TO '/var/opt/mssql/data/nopcommerce_db.mdf', MOVE 'nopcommerce_db_log' TO '/var/opt/mssql/data/nopcommerce_db_log.ldf', REPLACE; ALTER DATABASE [nopcommerce_db] SET MULTI_USER;"
    echo "Database restoration completed successfully."
else
    echo "Database already initialized (${TABLE_COUNT} tables present)."
fi

echo "=== [4/5] Ensuring nopCommerce App_Data configuration & starting web container ==="
WEB_RUNNING=$(docker inspect -f '{{.State.Running}}' nopcommerce_web 2>/dev/null || echo "false")
if [ "${WEB_RUNNING}" != "true" ]; then
    docker compose -f docker/docker-compose.yml run --rm --no-deps -T --entrypoint sh nopcommerce_web -c \
        'mkdir -p /app/App_Data && ([ -f /app/App_Data/appsettings.json ] || cp /docker-seed/appsettings.json /app/App_Data/appsettings.json) && ([ -f /app/App_Data/plugins.json ] || cp /docker-seed/plugins.json /app/App_Data/plugins.json)'
    docker compose -f docker/docker-compose.yml up -d nopcommerce_web
else
    APP_FILES_OK=$(docker exec nopcommerce_web sh -c '[ -f /app/App_Data/appsettings.json ] && [ -f /app/App_Data/plugins.json ] && echo 1 || echo 0' 2>/dev/null || echo "0")
    if [ "${APP_FILES_OK}" != "1" ] || [ "${TABLE_COUNT}" -lt "100" ]; then
        echo "Deploying /app/App_Data/appsettings.json and plugins.json..."
        docker exec nopcommerce_web sh -c 'cp /docker-seed/appsettings.json /app/App_Data/appsettings.json && cp /docker-seed/plugins.json /app/App_Data/plugins.json'
        echo "Restarting nopcommerce_web to apply configuration..."
        docker restart nopcommerce_web >/dev/null
    fi
fi

echo "=== [5/5] Verifying storefront HTTP 200 readiness on ${BASE_URL} ==="
wait_until 120 3 'curl -fsSL "${BASE_URL}/" 2>/dev/null | grep -qi "small-searchterms"'

echo "=== Isolated nopCommerce AUT is fully initialized, healthy, and ready! ==="
