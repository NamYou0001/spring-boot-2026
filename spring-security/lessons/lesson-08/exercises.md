# Lesson 08: Exercises & Interview Questions
## លំហាត់អនុវត្តន៍ជាក់ស្តែង និងសំណួរសម្ភាសន៍ (Exercises & Lab)

---

### 1. លំហាត់អនុវត្តន៍ទី ១ (Lab 1): សាកល្បង Token Rotation ដោយផ្ទាល់

#### ជំហានអនុវត្តន៍ (Steps):
1. **Login**: ផ្ញើ Request ទៅ `POST /api/v1/auth/login` ដោយប្រើប្រាស់ Account:
   - Username: `Admin`
   - Password: `admin@123`
   - ចម្លងទុក (Copy) `accessToken`, `refreshToken`, និង `sessionId`។
2. **Refresh ដំបូង**: ផ្ញើ `refreshToken` នោះទៅ `POST /api/v1/auth/refresh`។
   - ពិនិត្យមើល Response: តើអ្នកទទួលបាន `refreshToken` ថ្មីដែលមានតម្លៃខុសពី `refreshToken` ចាស់ដែរឬទេ?
   - តើ `sessionId` មានតម្លៃដដែលដែរឬទេ?
3. **ពិនិត្យក្នុង Database**:
   - ចូលទៅ PostgreSQL Client (`psql` ឬ DBeaver):
     ```sql
     SELECT id, user_id, family_id, session_id, revoked_reason, replaced_by_token_hash FROM refresh_tokens;
     ```
   - ពិនិត្យមើលថាតើ Token ចាស់មាន `revoked_reason = 'ROTATED'` និង `replaced_by_token_hash` ចង្អុលទៅ Hash នៃ Token ថ្មីដែរឬទេ?

---

### 2. លំហាត់អនុវត្តន៍ទី ២ (Lab 2): ក្លែងបន្លំការវាយប្រហារ Token Reuse Detection

#### សេណារីយ៉ូ (Scenario):
1. យក `refreshToken` ចាស់ពីជំហានទី ១ (ដែលត្រូវបាន Revoke រួចហើយ)។
2. ធ្វើជា Hacker ដោយផ្ញើ Token ចាស់នោះទៅកាន់ `POST /api/v1/auth/refresh` ម្តងទៀត។
3. **លទ្ធផលរំពឹងទុក (Expected Outcome)**:
   - API ត្រូវឆ្លើយតបមកវិញនូវ `401 Unauthorized` ជាមួយ title `"Token Reuse Detected"`។
   - ពិនិត្យក្នុង Database ម្តងទៀត៖ តើ Token ថ្មីដែល User កំពុងកាន់កាប់ (នៅក្នុង `family_id` តែមួយ) ត្រូវបានកែប្រែទៅជា `revoked_reason = 'REUSE_DETECTED'` ដោយស្វ័យប្រវត្តិដែរឬទេ?
   - សាកល្បងយក Token ថ្មីរបស់ User មក Refresh ម្តងទៀត -> ត្រូវតែបរាជ័យ (Failed) ព្រោះប្រព័ន្ធបានកម្ទេច Family នោះចោលទាំងស្រុងដើម្បីសុវត្ថិភាព!

---

### 3. សំណួរសម្ភាសន៍គន្លឹះ (Top Interview Questions)

#### សំណួរទី ១៖ ហេតុអ្វីបានជាយើងមិនត្រូវទុក Refresh Token ជា Plaintext ក្នុង Database?
**ចម្លើយគំរូ**: ប្រសិនបើ Database ត្រូវបាន Hacker Dump ឬ Data Breach កើតឡើង ប្រសិនបើយើងរក្សាទុក Plaintext Hacker អាចយក Token ទាំងនោះទៅ Generate Access Token ថ្មីបានភ្លាមៗ។ ប៉ុន្តែបើយើង Hash ដោយ **SHA-256 (One-way Hash)** ទោះបីជា Hacker ឃើញ Hash ក៏មិនអាចយក Hash មកផ្ញើតាម HTTP Request បានដែរ ព្រោះ Client ត្រូវតែបញ្ជូន Raw Token ដើមមកឱ្យ Server Hash ផ្ទៀងផ្ទាត់។

#### សំណួរទី ២៖ តើ Grace Period ក្នុង Refresh Token Rotation មានអត្ថប្រយោជន៍អ្វីខ្លះ?
**ចម្លើយគំរូ**: នៅក្នុងកម្មវិធីទូរស័ព្ទ (Mobile) ឬ Single Page Apps (React/Angular) បណ្តាញអ៊ីនធឺណិតអាចមិនស្ថិតស្ថេរ ឬមាន Concurrent Requests បាញ់ទៅ Refresh ក្នុងពេលដំណាលគ្នា (Race Condition)។ ប្រសិនបើគ្មាន Grace Period (ឧ. 10 ទៅ 30 វិនាទី) សំណើទី ២ អាចនឹងធ្វើឱ្យប្រព័ន្ធយល់ច្រឡំថាជា Hacker ហើយ Trigger Reuse Detection មោឃភាព Token របស់ User ចោលទាំងអស់។ Grace Period អនុញ្ញាតឱ្យសំណើដែលបញ្ជូនមកក្នុងរង្វង់វិនាទីខ្លីនៃ Token ទើប Rotate ទទួលបាន Token ថ្មីដូចគ្នាដោយមិនចាត់ទុកជាការវាយប្រហារ។

#### សំណួរទី ៣៖ តើ Token Family ខុសពី Session ID ដូចម្តេច?
**ចម្លើយគំរូ**:
- `sessionId`: តំណាងឱ្យអត្តសញ្ញាណនៃ Session ឬឧបករណ៍មួយគត់ (Physical Device/Browser)។
- `familyId`: តំណាងឱ្យខ្សែស្រឡាយសុវត្ថិភាព (Security Lineage/Chain) នៃ Token Rotation។ ប្រសិនបើមានការលួច Token នៅក្នុង Session នោះ `familyId` ត្រូវបានប្រើដើម្បីកម្ទេចខ្សែស្រឡាយនោះចោលភ្លាមៗ។

---

### 4. លំហាត់កម្រិតខ្ពស់ (Bonus Challenges)

1. **Scheduled Token Cleanup Job**:
   - បង្កើត `@Scheduled(cron = "0 0 2 * * ?")` មួយនៅក្នុង Spring Boot ដើម្បីលុប (DELETE) រាល់ Token ណាដែលមាន `expires_at < NOW()` ឬត្រូវបាន `revoked_at` លើសពី 30 ថ្ងៃ ចេញពី Database ដើម្បីកាត់បន្ថយទំហំផ្ទុក។
2. **IP Whitelist / Geolocation Alert**:
   - ប្រសិនបើ User ធ្វើការ Refresh ចេញពី IP Address ដែលមានប្រទេសខុសគ្នាទាំងស្រុងពី IP ដំបូងក្នុងរយៈពេលតិចជាង 5 នាទី ចាត់ទុកថាជា Impossible Travel ហើយទាមទារឱ្យមាន Re-authentication។
