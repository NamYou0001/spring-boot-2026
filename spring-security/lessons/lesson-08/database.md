# Lesson 08: Database Design & PostgreSQL Schema
## ការរចនា Database Schema សម្រាប់ Refresh Token Rotation & Session Management

---

### 1. ការវិភាគលើ Schema បច្ចុប្បន្ន និងការបន្ថែម Columns ថ្មី

នៅក្នុង Lesson 07 តារាង `refresh_tokens` មានតែ Field មូលដ្ឋានប៉ុណ្ណោះ (`id`, `user_id`, `token_hash`, `expires_at`, `revoked_at`, `created_at`)។

ដើម្បីគាំទ្រ **Refresh Token Rotation**, **Token Reuse Detection**, និង **Multi-device Session Management** ក្នុង Lesson 08 យើងបានពង្រីកតារាងដូចខាងក្រោម៖

| Column Name | Data Type | Constraints | ការពន្យល់ និងមូលហេតុ (Justification) |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique identifier សម្រាប់ Token record នីមួយៗ |
| `user_id` | `BIGINT` | `NOT NULL, FK -> tbl_users(id)` | សម្គាល់ថា Token នេះជារបស់ User ណា |
| `token_hash` | `VARCHAR(64)` | `NOT NULL, UNIQUE` | តម្លៃ SHA-256 Hex Hash (មិនរក្សាទុក Raw Token ជា Plaintext ដាច់ខាត) |
| `family_id` | `VARCHAR(64)` | `NOT NULL` | សម្គាល់ Token Family សម្រាប់ Token Lineage / Chain ប្រើចាប់ Reuse Detection |
| `session_id` | `VARCHAR(64)` | `NOT NULL` | Unique ID សម្រាប់ឧបករណ៍/Session មួយ (មិនប្រែប្រួលពេល Token rotate) |
| `device_info` | `VARCHAR(255)` | `NULLABLE` | User-Agent string នៃឧបករណ៍ (e.g., Chrome, Postman, Safari iOS) |
| `ip_address` | `VARCHAR(45)` | `NULLABLE` | Client IP Address (គាំទ្រទាំង IPv4 និង IPv6) |
| `expires_at` | `TIMESTAMPTZ` | `NOT NULL` | កាលបរិច្ឆេទផុតកំណត់នៃ Token |
| `revoked_at` | `TIMESTAMPTZ` | `NULLABLE` | ពេលវេលាដែល Token ត្រូវបាន Revoke (null = active) |
| `revoked_reason` | `VARCHAR(50)` | `NULLABLE` | មូលហេតុ: `ROTATED`, `USER_LOGOUT`, `REUSE_DETECTED`, `LOGOUT_ALL`, `SESSION_TERMINATED` |
| `replaced_by_token_hash` | `VARCHAR(64)` | `NULLABLE` | Hash នៃ Token ថ្មីដែលបានមកជំនួស Token ចាស់នេះ (សម្រាប់តាមដាន Audit Trail) |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL, DEFAULT now()` | ពេលវេលាដែលបានបង្កើត Token |
| `version` | `BIGINT` | `DEFAULT 0` | Optimistic locking version ដើម្បីការពារ Concurrent Refresh Race Condition |

---

### 2. ការបង្កើត Indexes សម្រាប់ Performance Optimization

ក្នុងប្រព័ន្ធដែលមាន User រាប់ម៉ឺននាក់ Table នេះនឹងផ្ទុកទិន្នន័យច្រើន ដូច្នេះ Index មានសារៈសំខាន់ខ្លាំងណាស់៖

1. `uk_refresh_token_hash` (`UNIQUE INDEX` លើ `token_hash`):
   - ប្រើសម្រាប់ Lookup យ៉ាងលឿនបំផុត ($O(1)$) នៅពេល Client ផ្ញើ Refresh Token មកផ្ទៀងផ្ទាត់។
2. `idx_refresh_token_family` (`INDEX` លើ `family_id`):
   - ប្រើសម្រាប់ `revokeFamily()` នៅពេល Reuse Detection ចាប់បានការវាយប្រហារ ដើម្បី Revoke គ្រប់ Token ក្នុង Family បានលឿន។
3. `idx_refresh_token_session` (`INDEX` លើ `session_id`):
   - ប្រើសម្រាប់ទម្លាក់ ឬទាញយកទិន្នន័យតាម Session ID។
4. `idx_refresh_token_user` (`INDEX` លើ `user_id`):
   - ប្រើសម្រាប់ `revokeAllByUserId()` និង `findActiveSessionsByUserId()`។
5. `idx_refresh_token_expiry` (`INDEX` លើ `expires_at`):
   - ប្រើសម្រាប់ Background Cleanup Job ដើម្បីលុប Expired Tokens ចោល។

---

### 3. PostgreSQL Database Migration Script (DDL)

ប្រសិនបើអ្នកចង់បង្កើត ឬ Migrate លើ PostgreSQL ដោយផ្ទាល់៖

```sql
-- 1. បង្កើតតារាង refresh_tokens ប្រសិនបើមិនទាន់មាន
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    family_id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoked_reason VARCHAR(50),
    replaced_by_token_hash VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

-- 2. បង្កើត Indexes
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_family ON refresh_tokens (family_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_session ON refresh_tokens (session_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens (expires_at);

-- 3. Migration Alter Script (សម្រាប់ Update ពី Lesson 07 Schema)
ALTER TABLE refresh_tokens 
    ADD COLUMN IF NOT EXISTS family_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS session_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS device_info VARCHAR(255),
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45),
    ADD COLUMN IF NOT EXISTS revoked_reason VARCHAR(50),
    ADD COLUMN IF NOT EXISTS replaced_by_token_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
```

---

### 4. JPA Entity Mapping Reference (`RefreshToken.java`)

```java
@Entity
@Table(
    name = "refresh_tokens",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash")
    },
    indexes = {
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_family", columnList = "family_id"),
        @Index(name = "idx_refresh_token_session", columnList = "session_id"),
        @Index(name = "idx_refresh_token_expiry", columnList = "expires_at")
    }
)
public class RefreshToken { ... }
```
