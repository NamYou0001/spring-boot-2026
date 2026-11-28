# AGENT TASK — LESSON 08
## Refresh Token Rotation & Session Management

### 1. Role

Act as a Senior Spring Security Architect and Java Backend Engineer.

Your task is to create a complete Lesson 08 about Refresh Token Rotation and Session Management.

Follow the repository's AGENTS.md, PROJECT_CONTEXT.md, existing architecture, and source code.

### 2. Mandatory Resource Review

Before writing anything:

1. Read AGENTS.md.
2. Read PROJECT_CONTEXT.md.
3. Read Lesson 07 completely.
4. Inspect the actual Lesson 07 source code.
5. Inspect RefreshToken entity and repository.
6. Inspect RefreshTokenService.
7. Inspect AuthController and GlobalExceptionHandler.
8. Inspect existing JWT implementation.
9. Review related tests.
10. Identify existing classes before creating new ones.

Do not duplicate existing implementations.

### 3. Learning Objectives

Students must understand:

- Refresh Token Rotation.
- Token Family.
- Token Reuse Detection.
- Token Revocation.
- Session Management.
- Multiple Device Sessions.
- Logout Current Session.
- Logout All Sessions.
- Concurrent Refresh Requests.
- Refresh Token Theft Detection.

### 4. Required Lesson Contents

#### Part A — Theory
- What is Refresh Token Rotation?
- Why is rotation necessary?
- Difference between Access Token and Refresh Token.
- What is Token Reuse Detection?
- What is Token Family?
- What happens when an old refresh token is reused?
- Session management concepts.

#### Part B — Architecture
Provide diagrams for:

1. Normal refresh token flow.
2. Refresh token rotation flow.
3. Token reuse detection flow.
4. Logout current session.
5. Logout all sessions.
6. Multiple-device session management.

#### Part C — Database Design

Review the existing schema first.

Determine whether the project needs:
- Token Family ID.
- Token ID.
- Parent Token ID.
- Replaced By Token ID.
- Session ID.
- Device Information.
- IP Address.
- Revoked At.
- Expiration Date.
- Reuse Detection Status.

Do not add redundant fields without justification.

Provide PostgreSQL migration scripts when required.

#### Part D — Complete Source Code

Implement or update:

- RefreshToken entity.
- RefreshTokenRepository.
- RefreshTokenService.
- TokenFamilyService, if needed.
- SessionService, if needed.
- RefreshTokenController or existing AuthController.
- Exception classes.
- GlobalExceptionHandler.
- Security configuration.
- DTOs and response models.

Follow existing project naming conventions.

#### Part E — Security Requirements

Implement:

1. Generate a new refresh token after successful refresh.
2. Revoke the previous refresh token.
3. Preserve the token family relationship.
4. Detect reuse of revoked tokens.
5. Revoke the affected token family when reuse is confirmed.
6. Protect against concurrent refresh requests.
7. Handle expired and revoked tokens.
8. Support logout from one session.
9. Support logout from all sessions.

Use database transactions and appropriate concurrency control.

Do not rely only on a check-then-update operation that permits concurrent refresh races.

#### Part F — API Design

Provide endpoints consistent with existing project conventions.

Examples:

- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- POST /api/v1/auth/logout-all
- GET /api/v1/auth/sessions
- DELETE /api/v1/auth/sessions/{sessionId}

Do not create duplicate endpoints if they already exist.

For every endpoint, include request and response examples.

#### Part G — Testing

Include tests for:

- Successful token rotation.
- Previous token invalidation.
- Reuse detection.
- Token family revocation.
- Expired refresh token.
- Revoked refresh token.
- Concurrent refresh requests.
- Logout current session.
- Logout all sessions.
- Multiple device sessions.

#### Part H — Final Deliverables

Create:

lessons/lesson-08/
- README.md
- theory.md
- architecture.md
- database.md
- api-examples.md
- exercises.md

Update source code and tests in their existing locations.

### 5. Quality Gate

Before finishing:

- Verify all referenced classes exist.
- Verify imports and dependencies.
- Verify database migration compatibility.
- Verify token rotation transaction boundaries.
- Verify concurrency handling.
- Verify tests.
- Verify consistency with Lesson 07.

Never claim tests passed unless they were actually executed.

### 6. Teaching Language

- Khmer explanations.
- English technical terminology.
- Complete source code.
- Advanced practical examples.

### 7. Final Report

Report:
1. Resources reviewed.
2. Files created.
3. Files modified.
4. Architecture changes.
5. Security improvements.
6. Tests executed and actual results.
7. Remaining issues.