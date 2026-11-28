# Lesson 08: API Documentation & Examples
## ឯកសារណែនាំ និងគំរូហៅប្រើប្រាស់ API Endpoints

---

### 1. Login API (បង្កើត Session & Token Family)

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/login`
- **Headers**:
  - `Content-Type: application/json`
  - `User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0`

#### Request Body:
```json
{
  "username": "Admin",
  "password": "admin@123"
}
```

#### Response (`200 OK`):
```json
{
  "userId": 1,
  "username": "Admin",
  "authorities": [
    "ROLE_ADMIN"
  ],
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 600,
  "refreshToken": "V3b8-67aK2L-9...O_Xq8",
  "refreshTokenExpiresIn": 604800,
  "sessionId": "a7e6b01f-0e12-4217-91f8-091f24d9c021",
  "authenticated": true,
  "message": "Login successful"
}
```

---

### 2. Refresh Token Rotation API (បង្វិល Token ថ្មី)

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/refresh`
- **Headers**:
  - `Content-Type: application/json`
  - `User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)`

#### Request Body:
```json
{
  "refreshToken": "V3b8-67aK2L-9...O_Xq8"
}
```

#### Response (`200 OK` - Rotation Success):
```json
{
  "userId": 1,
  "username": "Admin",
  "authorities": [
    "ROLE_ADMIN"
  ],
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_ACCESS_TOKEN...",
  "expiresIn": 600,
  "refreshToken": "X9pL-44bM1P-2...Z_Ab1_NEW_REFRESH_TOKEN",
  "refreshTokenExpiresIn": 604800,
  "sessionId": "a7e6b01f-0e12-4217-91f8-091f24d9c021",
  "message": "Access and refresh tokens rotated successfully"
}
```

#### Response (`401 Unauthorized` - Token Reuse Detection Alert!):
ប្រសិនបើ Attacker យក Token ចាស់ដែលទើបតែ Revoked មក Refresh ម្តងទៀត៖
```json
{
  "type": "https://errors.security-lab.io/token-reuse-detected",
  "title": "Token Reuse Detected",
  "status": 401,
  "detail": "Security Breach Detected: An attempt was made to reuse a revoked refresh token. All active sessions in this token family have been immediately revoked.",
  "timestamp": "2026-11-26T11:00:00Z",
  "compromisedFamilyId": "4a73ec99-f22e-4b68-b8ce-3fa9d8bf0a01"
}
```

---

### 3. Logout Current Session API

ចាកចេញពី Session បច្ចុប្បន្ន (Revoke តែ Refresh Token នេះ)៖

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/logout`
- **Headers**:
  - `Content-Type: application/json`

#### Request Body:
```json
{
  "refreshToken": "X9pL-44bM1P-2...Z_Ab1_NEW_REFRESH_TOKEN"
}
```

#### Response (`200 OK`):
```json
{
  "success": true,
  "message": "Successfully logged out from current session"
}
```

---

### 4. Logout All Sessions API (Global Logout)

ចាកចេញពីគ្រប់ឧបករណ៍ទាំងអស់របស់ User៖

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/logout-all`
- **Headers**:
  - `Authorization: Bearer <ACCESS_TOKEN_JWT>`

#### Response (`200 OK`):
```json
{
  "success": true,
  "message": "Successfully logged out from all active sessions"
}
```

---

### 5. Get Active Sessions API

ទាញយកបញ្ជីឧបករណ៍ដែលកំពុង Active ទាំងអស់របស់ User៖

- **Method**: `GET`
- **URL**: `http://localhost:8081/api/v1/auth/sessions`
- **Headers**:
  - `Authorization: Bearer <ACCESS_TOKEN_JWT>`

#### Response (`200 OK`):
```json
[
  {
    "sessionId": "a7e6b01f-0e12-4217-91f8-091f24d9c021",
    "deviceInfo": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0",
    "ipAddress": "192.168.1.10",
    "createdAt": "2026-11-26T10:45:00Z",
    "expiresAt": "2026-12-03T10:45:00Z"
  },
  {
    "sessionId": "b8f7c12e-1f23-5328-a2g9-102g35e0d132",
    "deviceInfo": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15",
    "ipAddress": "10.0.0.2",
    "createdAt": "2026-11-26T08:30:00Z",
    "expiresAt": "2026-12-03T08:30:00Z"
  }
]
```

---

### 6. Terminate Specific Session API

ទម្លាក់ឧបករណ៍ជាក់លាក់ណាមួយចេញ៖

- **Method**: `DELETE`
- **URL**: `http://localhost:8081/api/v1/auth/sessions/b8f7c12e-1f23-5328-a2g9-102g35e0d132`
- **Headers**:
  - `Authorization: Bearer <ACCESS_TOKEN_JWT>`

#### Response (`200 OK`):
```json
{
  "success": true,
  "message": "Session b8f7c12e-1f23-5328-a2g9-102g35e0d132 terminated successfully"
}
```

---

### 7. cURL Commands Cheat Sheet

#### cURL Login:
```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Admin","password":"admin@123"}'
```

#### cURL Refresh:
```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<PASTE_YOUR_REFRESH_TOKEN>"}'
```

#### cURL Get Sessions:
```bash
curl -X GET http://localhost:8081/api/v1/auth/sessions \
  -H "Authorization: Bearer <PASTE_YOUR_ACCESS_TOKEN>"
```
