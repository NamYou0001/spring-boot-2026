# Lesson 08: Refresh Token Rotation & Session Management
## ការបង្វិល Refresh Token (Rotation) និងការគ្រប់គ្រង Session

---

### 1. គោលបំណងនៃមេរៀន (Learning Objectives)

នៅក្នុងមេរៀន Lesson 08 នេះ សិក្ខាកាមនឹងសិក្សាយល់ដឹងស៊ីជម្រៅអំពី៖

1. **Refresh Token Rotation (RTR)**: ស្វែងយល់ពីរបៀបដែលប្រព័ន្ធផ្លាស់ប្តូរ Refresh Token ថ្មីរាល់ពេលដែល Client ធ្វើការ Refresh Access Token និងមូលហេតុដែលត្រូវដកហូត (Revoke) Token ចាស់ចោល។
2. **Token Reuse Detection (ការចាប់ការប្រើឡើងវិញ)**: ការរកឃើញការវាយប្រហារ (Attack/Theft) នៅពេលដែលមានជនអនាមិក ឬ Attacker ព្យាយាមយក Refresh Token ដែលត្រូវបាន Revoke រួចមកប្រើឡើងវិញ។
3. **Token Family & Chain**: ការចងក្រងខ្សែស្រឡាយនៃ Token នៅក្នុង Session តែមួយ ដើម្បីងាយស្រួលក្នុងការ Revoke ចោលទាំងអស់ (Cascade Revocation) នៅពេលមានការសង្ស័យអំពី Security Breach។
4. **Session Management (ការគ្រប់គ្រង Session)**:
   - ការតាមដាន Multi-device Sessions (Device Info, Client IP, Created At, Expires At)
   - ការ Logout Current Session (ចាកចេញពីឧបករណ៍បច្ចុប្បន្ន)
   - ការ Logout All Sessions (ចាកចេញពីគ្រប់ឧបករណ៍ទាំងអស់)
   - ការ Revoke ទៅលើឧបករណ៍ជាក់លាក់ណាមួយ (Specific Device Termination)
5. **Concurrency Protection**: ការការពារបញ្ហា Race Condition នៅពេលមាន Concurrent Refresh Requests ដំណាលគ្នា។

---

### 2. មាតិកានៃមេរៀន (Lesson Structure)

ឯកសារ និងកូដត្រូវបានបែងចែកជាផ្នែកៗដូចខាងក្រោម៖

- **[theory.md](./theory.md)**: ទ្រឹស្តីលម្អិតអំពី Refresh Token Rotation, Token Reuse Detection, Token Family និងយន្តការសុវត្ថិភាព។
- **[architecture.md](./architecture.md)**: គំនូសតាង Architecture & Sequence Diagrams សម្រាប់ Flow នីមួយៗ (Rotation, Reuse Detection, Single Logout, Logout All)។
- **[database.md](./database.md)**: ការរចនា Database Schema លើ PostgreSQL, Migration Scripts និង Indexing Optimization។
- **[api-examples.md](./api-examples.md)**: cURL, HTTP Requests និង Response Examples សម្រាប់គ្រប់ API Endpoints។
- **[exercises.md](./exercises.md)**: លំហាត់អនុវត្តន៍ជាក់ស្តែង និងសំនួរសួរដេញដោល (Interview Questions & Lab Exercises)។

---

### 3. រចនាសម្ព័ន្ធ Package និង Class ដែលពាក់ព័ន្ធ

```text
com.you.springsecurity
├── auth
│   ├── AuthController.java          # Login, Refresh, Logout, Logout-All, Sessions APIs
│   └── AuthService.java             # Business logic សម្រាប់ Authentication និង Session
├── config
│   ├── SecurityConfig.java          # SecurityFilterChain, Public & Protected endpoints
│   ├── JwtConfig.java               # NimbusJwtEncoder & NimbusJwtDecoder Beans
│   └── RefreshTokenProperties.java  # Configuration properties សម្រាប់ TTL
├── dto
│   ├── LoginRequest.java
│   ├── LoginResponse.java           # រួមបញ្ចូល sessionId
│   ├── RefreshRequest.java
│   ├── RefreshResponse.java         # បង្វិល accessToken និង refreshToken ថ្មី
│   ├── LogoutRequest.java           # Request សម្រាប់ Logout current session
│   └── SessionResponse.java         # ព័ត៌មាន Active session (Device, IP, SessionId)
├── exception
│   ├── AuthExceptionHandler.java    # ដោះស្រាយ TokenReuseException, InvalidRefreshTokenException
│   ├── GlobalExceptionHandler.java
│   ├── InvalidRefreshTokenException.java
│   └── TokenReuseException.java     # Exception ពេលរកឃើញការលួចប្រើ Token ចាស់
├── jwt
│   ├── IssuedAccessToken.java
│   ├── IssuedRefreshToken.java      # រួមបញ្ចូល familyId និង sessionId
│   ├── JwtProperties.java
│   ├── RefreshTokenService.java     # Logic ចម្បងនៃ Rotation, Reuse Detection, Sessions
│   └── TokenService.java            # បង្កើត Stateless Access Token (JWT)
├── model
│   ├── RefreshToken.java            # Entity ផ្ទុក Token Hash, FamilyId, SessionId, Audit fields
│   ├── Roles.java
│   └── Users.java                   # User entity
└── repository
    ├── RefreshTokenRepository.java  # Database queries (findByTokenHash, revokeFamily, findActiveSessions)
    └── UserRepository.java
```
