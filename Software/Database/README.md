# Setup PostgreSQL

### 1. Download [PostgreSQL Version 15.1](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads/)

### 2. Follow the installer instructions

- Set password := admin
- Set port := 5432
- Set locale := Croatian, Croatia

### 3. Add PostgreSQL to PATH

**[WinOS]:**
- Search environment variables
- Click button Environment Variables
- Select PATH variable in the upper box dialog and then Edit
- Click button New
- Add paths to both /bin and /lib PostgreSQL folders (should be at C:\Program Files\PostgreSQL\15)

### 4. Restart the machine

### 5. Create database and import script.sql

```bash
cd Database
psql -h localhost -p 5432 -U postgres
Enter password: admin
\i script.sql
```
