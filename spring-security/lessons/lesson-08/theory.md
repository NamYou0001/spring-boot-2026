# Lesson 08: Theory — Refresh Token Rotation & Session Management
## ទ្រឹស្តីស៊ីជម្រៅអំពី Refresh Token Rotation និង Session Management

---

### 1. ភាពខុសគ្នារវាង Access Token និង Refresh Token (Access Token vs Refresh Token)

ក្នុងប្រព័ន្ធសុវត្ថិភាពបែបទំនើប (OAuth2 / OpenID Connect) យើងបែងចែក Token ជាពីរប្រភេទ៖

| លក្ខណៈ (Attribute) | Access Token (JWT) | Refresh Token |
| :--- | :--- | :--- |
| **គោលបំណង (Purpose)** | ប្រើសម្រាប់ Authenticate លើ Protected API Endpoints | ប្រើសម្រាប់ស្នើសុំ Access Token ថ្មី (និង Refresh Token ថ្មី) |
| **អាយុកាល (Lifetime)** | ខ្លី (Short-lived, e.g. 5 – 15 នាទី) | វែង (Long-lived, e.g. 7 – 30 ថ្ងៃ) |
| **ទម្រង់ (Format)** | Stateless Signed JWT (Header.Payload.Signature) | Opaque Random String (High Entropy) |
| **ការផ្ទុកទិន្នន័យ (Storage)** | មិនរក្សាទុកក្នុង Database ទេ (Resource Server ផ្ទៀងផ្ទាត់ដោយ Signature) | ផ្ទុកក្នុង Database ក្នុងទម្រង់ **SHA-256 Hashed** |
| **ហានិភ័យ (Risk)** | ប្រសិនបើត្រូវ Hacker លួចបាន វានឹងផុតកំណត់ក្នុងពេលដ៏ខ្លី | ប្រសិនបើលួចបាន Hacker អាចនៅតែបន្ត Refresh បានយូរ ប្រសិនបើគ្មាន Rotation |

---

### 2. អ្វីទៅជា Refresh Token Rotation (RTR)?

**Refresh Token Rotation (RTR)** គឺជាយន្តការសុវត្ថិភាពមួយដែលកំណត់ថា៖
> **"រាល់ពេលដែល Client ផ្ញើ Refresh Token ចាស់មកប្តូរយក Access Token ថ្មី ប្រព័ន្ធនឹង Revoke (មោឃភាព) Refresh Token ចាស់នោះចោលភ្លាម ហើយបង្កើត Refresh Token ថ្មីមួយទៀតផ្ញើត្រឡប់ទៅឱ្យ Client"**

#### មូលហេតុដែលត្រូវប្រើ Rotation:
1. **កាត់បន្ថយអាយុកាលនៃ Refresh Token (Token Ephemerality)**: Refresh Token នីមួយៗអាចប្រើប្រាស់បានតែ **ម្តងគត់ (One-time use)**។
2. **ការពារ Token Theft / Replay Attacks**: ប្រសិនបើ Attacker លួចយក Refresh Token បាន ពួកគេមិនអាចប្រើវាដដែលៗជារៀងរហូតបានឡើយ។
3. **អនុញ្ញាតឱ្យប្រព័ន្ធធ្វើការ Reuse Detection**: ដោយសារតែ Token ចាស់ត្រូវតែ Revoked បន្ទាប់ពីប្រើរួច ប្រសិនបើមាននរណាម្នាក់ព្យាយាមយកវាទៅប្រើម្តងទៀត ប្រព័ន្ធដឹងភ្លាមថាមាន Token Leak កើតឡើង!

---

### 3. អ្វីទៅជា Token Family និង Token Reuse Detection?

#### ក. Token Family (គ្រួសារ Token)
- នៅពេល User ធ្វើការ **Login** ម្តង ប្រព័ន្ធនឹងបង្កើត `familyId` មួយ (ជាទូទៅ UUID) សម្រាប់តំណាងខ្សែស្រឡាយ Token នៅក្នុង Session នោះ។
- នៅពេល User ធ្វើការ **Refresh** (Rotate)៖
  - Token ចាស់ត្រូវបានសម្គាល់ថា `revoked_reason = 'ROTATED'` និង `replaced_by_token_hash = '<new_hash>'`។
  - Token ថ្មីត្រូវបានបង្កើតឡើងដោយ **រក្សា `familyId` ដដែល**។
  - ដូចនេះ Token ទាំងអស់ដែលកើតចេញពីការ Login នោះ ស្ថិតក្នុង Token Family តែមួយ។

#### ខ. Token Reuse Detection (ការចាប់ការលួចប្រើឡើងវិញ)
ពិចារណាករណី Attacker លួចបាន Refresh Token ($RT_1$)៖
1. User ស្របច្បាប់ (Legitimate User) ប្រើ $RT_1$ ដើម្បី Refresh -> ប្រព័ន្ធបង្កើត $RT_2$ ឱ្យ User ហើយ Revoke $RT_1$ (ស្ថានភាព = `ROTATED`)។
2. ក្រោយមក Attacker ព្យាយាមយក $RT_1$ មក Refresh ម្តងទៀត៖
   - Backend ពិនិត្យឃើញថា $RT_1$ **ត្រូវបាន Revoked រួចហើយ!**
   - នេះគឺជាសញ្ញាព្រមានអំពី **Security Breach / Token Theft**!
3. **វិធានការដោះស្រាយភ្លាមៗ (Automatic Remediation)**:
   - Backend នឹងធ្វើការ **Revoke គ្រប់ Tokens ទាំងអស់ដែលស្ថិតក្នុង `familyId` នោះភ្លាមៗ (`revokeFamily()`)**!
   - ជាលទ្ធផល $RT_2$ របស់ User ក៏ត្រូវ Revoke ដែរ ដែលបង្ខំឱ្យ Hacker មិនអាចបន្តចូលប្រើប្រាស់បាន ហើយ User ស្របច្បាប់នឹងតម្រូវឱ្យ Login សារជាថ្មី (Re-authentication) ដើម្បីការពារគណនី។

```text
[Login] ──> RT-1 (Family-A, Active)
              │
         [User Refresh]
              ▼
            RT-1 (Revoked: ROTATED)
            RT-2 (Family-A, Active)
              │
    [Attacker sends RT-1 again!] ──> ⚠️ REUSE DETECTED!
                                          │
                                          ▼
                                   [Revoke Family-A]
                                   RT-1 (Revoked)
                                   RT-2 (Revoked: REUSE_DETECTED)
                                   🚨 Attacker & Session Blocked!
```

---

### 4. យន្តការគ្រប់គ្រង Session (Session Management Concepts)

1. **Multi-device Sessions**: User ម្នាក់អាច Login លើឧបករណ៍ជាច្រើនក្នុងពេលតែមួយ (ទូរស័ព្ទ, កុំព្យូទ័រយួរដៃ, Tablet)។ ឧបករណ៍នីមួយៗមាន `sessionId` និង `familyId` ដាច់ដោយឡែកពីគ្នា។
2. **Device & IP Auditing**: រាល់ Token ត្រូវកត់ត្រា `device_info` (User-Agent) និង `ip_address` ដើម្បីឱ្យ User អាចពិនិត្យមើលបានតាម API `GET /api/v1/auth/sessions`។
3. **Logout Granularity**:
   - **Logout Current Session (`POST /api/v1/auth/logout`)**: Revoke តែ Refresh Token នៃឧបករណ៍ដែលកំពុងប្រើប្រាស់។
   - **Logout All Sessions (`POST /api/v1/auth/logout-all`)**: Revoke គ្រប់ Refresh Token ទាំងអស់របស់ User លើគ្រប់ឧបករណ៍ (មានប្រយោជន៍ពេល User សង្ស័យថាបាត់ទូរស័ព្ទ ឬប្តូរលេខសម្ងាត់)។
   - **Terminate Specific Session (`DELETE /api/v1/auth/sessions/{sessionId}`)**: User អាចជ្រើសរើសទម្លាក់ (Kick out) ឧបករណ៍ជាក់លាក់ណាមួយចេញពីគណនី។

---

### 5. ការការពារ Concurrent Refresh Race Conditions

នៅក្នុង Single Page Application (SPA) ឬ Mobile App នៅពេល Access Token ផុតកំណត់ សំណើ HTTP ច្រើន (Concurrent API calls) អាចនឹងត្រូវបានបញ្ជូនទៅកាន់ `/refresh` ក្នុងមីលីវិនាទីដំណាលគ្នា។
ប្រសិនបើយើងមិនមានយន្តការការពារទេ៖
- Request ទី១ កំពុងដំណើរការ Rotation។
- Request ទី២ មកដល់ពេល Request ទី១ ទើបតែ Revoke Token ចាស់ -> Request ទី២ អាចនឹងបង្កឱ្យមាន False Positive Reuse Detection!
- **ដំណោះស្រាយ**:
  - ប្រើ Database Transaction `@Transactional`
  - ប្រើ Optimistic Locking (`@Version` column ក្នុង Entity) ឬ Pessimistic Lock (`PESSIMISTIC_WRITE`)
  - ផ្តល់ Grace Period រយៈពេលខ្លី (ឧ. 10 ទៅ 30 វិនាទី) សម្រាប់ Token ដែលទើបតែ ROTATED ប្រសិនបើ Architecture តម្រូវ ឬ Client-side Request Mutex/Deduplication។
