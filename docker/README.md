# Docker Environment Setup — nopCommerce 4.90.8 + MSSQL 2022

> **Component:** Local Containerized AUT Infrastructure  
> **Image Tags:** `nopcommerceteam/nopcommerce:4.90.8` · `mcr.microsoft.com/mssql/server:2022-latest`  
> **Exposed Ports:** `5000` (Web UI/HTTP), `1433` (MSSQL Database)  

---

## 1. Architecture Overview

This Docker environment provisions an isolated, reproducible instance of nopCommerce 4.90.8 backed by Microsoft SQL Server 2022 Developer edition. It decouples the automation test suite from the volatile, multi-tenant public demo site.

```
[ Playwright Java Test Runner / TestNG ]
               |
      +--------+------------------------+
      | (HTTP :5000)                    | (JDBC :1433)
      v                                 v
[ nopcommerce_web (Port 5000->80) ]  [ nopcommerce_mssql (Port 1433) ]
      |                                 ^
      +-- (Internal Container Network) -+
```

---

## 2. Quick Start Commands

### 2.1 Starting the Environment
From the repository root or `docker/` directory:
```bash
docker compose -f docker/docker-compose.yml up -d
```

### 2.2 Verifying Container Health
```bash
docker compose -f docker/docker-compose.yml ps
```

### 2.3 Inspecting Logs
```bash
# Application web logs
docker compose -f docker/docker-compose.yml logs -f nopcommerce_web

# Database server logs
docker compose -f docker/docker-compose.yml logs -f nopcommerce_database
```

### 2.4 Stopping the Environment
```bash
docker compose -f docker/docker-compose.yml down
```

---

## 3. Initial nopCommerce Database Setup Wizard

Upon first launch, navigate to `http://localhost:5000/` in a web browser to complete the standard one-time nopCommerce installation wizard:

1. **Store Information:**
   * **Admin Email:** `admin@yourstore.com` (configured in `.env`)
   * **Admin Password:** `Admin@12345` (configured in `.env`)
2. **Database Information:**
   * **Database Engine:** `Microsoft SQL Server`
   * **Server Name:** `nopcommerce_database` (or `localhost,1433` if accessing from host)
   * **Database Name:** `nopcommerce_db`
   * **Authentication:** `SQL Server account`
   * **Username:** `sa`
   * **Password:** `NopCommerce@2026!`
   * **Create Database If Not Exists:** Checked
   * **Specify Custom Connection String (Optional):**
     `Server=localhost,1433;Database=nopcommerce_db;User Id=sa;Password=NopCommerce@2026!;TrustServerCertificate=True;Encrypt=False;`
3. **Sample Data:**
   * Check **"Create sample data"** to populate standard test catalog items (Build your own computer, Apple MacBook, Electronics, etc.).
4. Click **Install**. The process takes approximately 60–90 seconds to run Linq2DB migrations and seed catalog items.

---

## 4. Reset & State Restoration Strategy

To ensure zero cross-suite test state contamination and restore the environment to a pristine state:

### Strategy A: Volume Reset (Pristine Rebuild)
```bash
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d
```
*Destroys named volumes `nopcommerce_db_data` and `nopcommerce_app_data`, triggering a fresh installation state.*

### Strategy B: Database Snapshot / Backup & Restore
For fast test reset without reinstalling the application:
```sql
-- Take backup post-initialization
BACKUP DATABASE [nopcommerce_db] TO DISK = '/var/opt/mssql/nopcommerce_baseline.bak';

-- Restore prior to full regression run
ALTER DATABASE [nopcommerce_db] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
RESTORE DATABASE [nopcommerce_db] FROM DISK = '/var/opt/mssql/nopcommerce_baseline.bak' WITH REPLACE;
ALTER DATABASE [nopcommerce_db] SET MULTI_USER;
```

---

## 5. Troubleshooting & Healthcheck

* **Port 5000 Conflict:** If port 5000 is occupied on the host system, adjust the port mapping in `docker/docker-compose.yml` (e.g., `"5001:80"`).
* **MSSQL TLS 1.3 / Linux Handshake:** If Linq2DB fails to connect with an SSL encryption error, ensure the connection string specifies `TrustServerCertificate=True;Encrypt=False;`.
