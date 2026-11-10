# Local nopCommerce Test Environment

This Compose stack runs the seeded nopCommerce 4.90.8 test store with SQL Server 2022. It is intended for local and CI test use. SQL is published only on host loopback; the web app remains available at `http://127.0.0.1:5000`. Both services join the shared `docker_qa-network` bridge so a containerized Jenkins agent can reach them by service name.

## Start the stack

Run these commands from the repository root in Bash (Git Bash or WSL on Windows):

```bash
cp .env.example .env
# Edit .env and set a strong DB_PASSWORD. Set ADMIN_EMAIL and ADMIN_PASSWORD
# to credentials belonging to the seeded nopCommerce database.
bash docker/bootstrap-aut.sh
```

`docker/bootstrap-aut.sh` reads `DB_PASSWORD` and `DB_USER` from the process environment or the root `.env` file, starts Compose, restores the bundled baseline database when needed, and waits for the storefront health check. CI supplies those values through secret environment variables.

Validate the resolved Compose configuration without starting containers:

```bash
docker compose --env-file .env -f docker/docker-compose.yml config
```

The SQL password is required; Compose fails before startup if `DB_PASSWORD` is missing. Do not commit `.env`. The starter template intentionally contains blank credential fields.

## Inspect and stop

```bash
docker compose --env-file .env -f docker/docker-compose.yml ps
docker compose --env-file .env -f docker/docker-compose.yml logs -f nopcommerce_web
docker compose --env-file .env -f docker/docker-compose.yml logs -f nopcommerce_database
docker compose --env-file .env -f docker/docker-compose.yml down
```

To discard the test database and app state, use `docker compose --env-file .env -f docker/docker-compose.yml down -v` before starting the bootstrap script again. This deletes the named Docker volumes.

## Configuration and ports

- Storefront: `http://127.0.0.1:5000`
- MSSQL: `127.0.0.1:1433` (not exposed to other host interfaces)
- Database: `nopcommerce_db`; Docker service name: `nopcommerce_database`
- The shared Docker network `docker_qa-network` is created by the bootstrap script. Attach a containerized Jenkins agent to this network to reach the app and database by service name.
- The app receives its connection string through `ConnectionStrings__ConnectionString`; no database password is stored in the tracked seed appsettings file.
- `ADMIN_EMAIL` and `ADMIN_PASSWORD` are test-runner credentials and must match an account in the seeded database. They do not configure the store's admin account.

If host port `5000` or `1433` is already in use, stop the conflicting service or change the host-side mapping in `docker/docker-compose.yml` and update the corresponding test URLs.
