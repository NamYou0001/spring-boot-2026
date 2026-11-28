# Lesson 08: Architecture & Sequence Flows
## គំនូសតាងស្ថាបត្យកម្ម និងលំហូរការងារ (Architecture & Diagrams)

---

### 1. លំហូរ Login ដំបូង និងការបង្កើត Session (Initial Login Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App (Browser/Mobile)
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant AuthMgr as AuthenticationManager
    participant TokenSvc as TokenService
    participant RefreshSvc as RefreshTokenService
    participant DB as PostgreSQL DB

    Client->>AuthCtrl: POST /api/v1/auth/login (username, password)
    AuthCtrl->>AuthSvc: login(request, userAgent, ip)
    AuthSvc->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr-->>AuthSvc: Authentication (CustomUserDetail)

    AuthSvc->>TokenSvc: generateAccessToken(userDetails)
    TokenSvc-->>AuthSvc: IssuedAccessToken (JWT)

    AuthSvc->>RefreshSvc: createForUser(userId, userAgent, ip)
    Note over RefreshSvc: បង្កើត Secure Random (64 bytes)<br/>Hash SHA-256<br/>familyId = UUID, sessionId = UUID
    RefreshSvc->>DB: INSERT into refresh_tokens
    RefreshSvc-->>AuthSvc: IssuedRefreshToken (rawToken, familyId, sessionId)

    AuthSvc-->>AuthCtrl: LoginResponse (accessToken, refreshToken, sessionId)
    AuthCtrl-->>Client: 200 OK + JSON Response
```

---

### 2. លំហូរ Refresh Token Rotation (RTR Flow)

រាល់ពេល Access Token ផុតកំណត់ Client ផ្ញើ Refresh Token ដើម្បីប្តូរយក Token ថ្មីទាំងពីរ៖

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant RefreshSvc as RefreshTokenService
    participant TokenSvc as TokenService
    participant DB as PostgreSQL DB

    Client->>AuthCtrl: POST /api/v1/auth/refresh (refreshToken)
    AuthCtrl->>AuthSvc: refresh(request, userAgent, ip)
    AuthSvc->>RefreshSvc: rotateRefreshToken(rawToken, userAgent, ip)

    Note over RefreshSvc: 1. Hash rawToken (SHA-256)
    RefreshSvc->>DB: SELECT * FROM refresh_tokens WHERE token_hash = ?
    DB-->>RefreshSvc: storedToken

    alt Token is revoked (revokedAt != null)
        Note over RefreshSvc: ⚠️ REUSE DETECTED!
        RefreshSvc->>DB: UPDATE refresh_tokens SET revoked_at = now() WHERE family_id = ?
        RefreshSvc-->>AuthSvc: throw TokenReuseException
        AuthSvc-->>Client: 401 Unauthorized (Security Alert)
    else Token is expired (expiresAt < now)
        RefreshSvc-->>AuthSvc: throw InvalidRefreshTokenException
        AuthSvc-->>Client: 401 Unauthorized (Expired)
    else Token is valid
        Note over RefreshSvc: 2. បង្កើត newRawToken (SHA-256)<br/>3. storedToken.revoke("ROTATED")<br/>4. newRefreshToken (SAME familyId, SAME sessionId)
        RefreshSvc->>DB: UPDATE storedToken (revoked_at, replaced_by)
        RefreshSvc->>DB: INSERT newRefreshToken
        RefreshSvc-->>AuthSvc: RotatedTokenResult(newIssuedRefreshToken, user)

        AuthSvc->>TokenSvc: generateAccessToken(new CustomUserDetail(user))
        TokenSvc-->>AuthSvc: newAccessToken (JWT)

        AuthSvc-->>AuthCtrl: RefreshResponse (newAccessToken, newRefreshToken)
        AuthCtrl-->>Client: 200 OK
    end
```

---

### 3. លំហូរ Token Reuse Detection (ការចាប់ការលួចប្រើ Token ចាស់)

សេណារីយ៉ូ៖ Hacker លួចបាន Refresh Token $RT_1$ ដែល Client ធ្លាប់បានប្រើដើម្បី Rotate ទៅ $RT_2$ រួចហើយ៖

```mermaid
sequenceDiagram
    autonumber
    actor Attacker as Attacker (Malicious Actor)
    actor Victim as Legitimate User
    participant Server as Spring Boot API
    participant DB as PostgreSQL DB

    Note over Victim,Server: 1. User rotate RT-1 ទៅជា RT-2<br/>RT-1 ស្ថិតក្នុងស្ថានភាព 'ROTATED'<br/>RT-2 សកម្ម (Active)
    
    Attacker->>Server: POST /api/v1/auth/refresh with RT-1
    Note over Server: Server រកឃើញ RT-1 មាន revoked_at != null<br/>(មូលហេតុ = 'ROTATED')
    
    rect rgb(255, 230, 230)
        Note over Server,DB: 🚨 ALARM: Token Reuse Breach!
        Server->>DB: UPDATE refresh_tokens<br/>SET revoked_at = now(), revoked_reason = 'REUSE_DETECTED'<br/>WHERE family_id = 'compromised-family-id'
        Server-->>Attacker: 401 Unauthorized ("Token Reuse Detected")
    end

    Note over Victim,Server: 2. ពេល User ផ្ញើ RT-2 មក refresh
    Victim->>Server: POST /api/v1/auth/refresh with RT-2
    Server->>DB: SELECT RT-2
    Note over Server: RT-2 ត្រូវបាន Revoke រួចរាល់ដោយស្វ័យប្រវត្តិ
    Server-->>Victim: 401 Unauthorized ("Token revoked. Please login again.")
    Note over Victim: User ត្រូវបានការពារ និងត្រូវ Login ឡើងវិញ
```

---

### 4. លំហូរ Logout Current Session

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant RefreshSvc as RefreshTokenService
    participant DB as PostgreSQL DB

    Client->>AuthCtrl: POST /api/v1/auth/logout (refreshToken)
    AuthCtrl->>AuthSvc: logout(request)
    AuthSvc->>RefreshSvc: revokeToken(rawToken)
    Note over RefreshSvc: Hash rawToken
    RefreshSvc->>DB: UPDATE refresh_tokens SET revoked_at = now(), revoked_reason = 'USER_LOGOUT' WHERE token_hash = ?
    RefreshSvc-->>AuthSvc: done
    AuthSvc-->>AuthCtrl: done
    AuthCtrl-->>Client: 200 OK ("Successfully logged out from current session")
```

---

### 5. លំហូរ Logout All Sessions (Global Logout)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant RefreshSvc as RefreshTokenService
    participant DB as PostgreSQL DB

    Client->>AuthCtrl: POST /api/v1/auth/logout-all<br/>(Header: Authorization: Bearer <JWT>)
    Note over AuthCtrl: Extract userId ពី JWT Subject
    AuthCtrl->>AuthSvc: logoutAll(userId)
    AuthSvc->>RefreshSvc: revokeAllForUser(userId)
    RefreshSvc->>DB: UPDATE refresh_tokens<br/>SET revoked_at = now(), revoked_reason = 'LOGOUT_ALL'<br/>WHERE user_id = ? AND revoked_at IS NULL
    RefreshSvc-->>AuthSvc: done
    AuthSvc-->>AuthCtrl: done
    AuthCtrl-->>Client: 200 OK ("Logged out from all active sessions")
```

---

### 6. Multiple Device Session Management

```mermaid
graph TD
    User["User: admin (ID: 1)"]
    
    subgraph Sessions ["Active Sessions in Database"]
        S1["Session 1: iPhone 15<br/>SessionId: sess-111<br/>FamilyId: fam-aaa<br/>Status: ACTIVE"]
        S2["Session 2: Chrome on Windows<br/>SessionId: sess-222<br/>FamilyId: fam-bbb<br/>Status: ACTIVE"]
        S3["Session 3: MacBook Pro Safari<br/>SessionId: sess-333<br/>FamilyId: fam-ccc<br/>Status: ACTIVE"]
    end

    User --> S1
    User --> S2
    User --> S3

    AdminAction["User calls DELETE /api/v1/auth/sessions/sess-111"]
    AdminAction -.->|Revoke only sess-111| S1
```
