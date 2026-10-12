# Security Specification — MARKAS RAKYAT DESA KARANGMULYA

## 1. Data Invariants
1. **Authentication Invariant**: Every read and write operation across all collections (`/admins`, `/citizen_records`, `/letter_requests`, `/complaints`, `/aspirations`, `/village_posts`, `/notifications`, `/audit_logs`) strictly requires `request.auth != null` (`isSignedIn()`).
2. **Multi-User Isolation & PII Protection Invariant**: Citizen PII (`nik`, `kkNumber`, `phone`, `address`) in `/citizen_records/{recordId}` and personal submissions (`letter_requests`, `complaints`, `aspirations`, `notifications`, `audit_logs`, `village_posts`) are strictly isolated by `resource.data.userId == request.auth.uid || isAdmin()`. Blanket `allow read: if isSignedIn()` is strictly forbidden.
3. **Immutable Ownership & Creation Timestamp Invariant**: On every `update`, `incoming().userId == existing().userId` and `incoming().createdAt == existing().createdAt` must hold. Audit logs (`/audit_logs`) are strictly append-only (`allow update, delete: if false`).
4. **Terminal State Locking Invariant**: Once a `letter_requests` document reaches `'DITOLAK'` or a `complaints` document reaches `'DITOLAK'`, non-admin users cannot mutate it further.
5. **Temporal Integrity Invariant**: All timestamp writes (`createdAt`, `updatedAt`) must use server timestamps (`FieldValue.serverTimestamp()`) and satisfy `is timestamp && <= request.time`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Scrape**: Unauthenticated `list` or `get` on `/citizen_records` or `/letter_requests` -> Rejected by `isSignedIn()`.
2. **Cross-User PII Leak**: User Alice (`alice_123`) attempts `get` on Bob's (`bob_456`) `/citizen_records/bob_doc` -> Rejected by `existing().userId == request.auth.uid || isAdmin()`.
3. **Unfiltered Query Scrape**: Authenticated user queries `db.collection("letter_requests").get()` without `.whereEqualTo("userId", uid)` -> Rejected by `allow list` checking `resource.data.userId == request.auth.uid`.
4. **Identity Spoofing on Create**: User Alice creates a `/letter_requests` document with `userId: "bob_456"` -> Rejected by `incoming().userId == request.auth.uid`.
5. **Shadow Update / Ghost Field Injection**: User sends `{"isVerified": true, "hacked": "yes"}` on `/citizen_records` update -> Rejected by `incoming().diff(existing()).affectedKeys().hasOnly(...)`.
6. **Value Poisoning on Update**: User updates an allowlisted field (`status` or `purpose`) with a 1MB string or integer -> Rejected by `isValidLetterRequest(incoming())` wrapping the entire `update` rule.
7. **Ownership Hijack on Update**: User attempts to change `userId` or `createdAt` during an update -> Rejected by `incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`.
8. **Invalid NIK / KK Length**: User creates `/citizen_records` with `nik` of length 5 instead of 16 -> Rejected by `data.nik.size() == 16 && data.kkNumber.size() == 16`.
9. **Terminal State Bypass**: Non-admin user attempts to update a `'DITOLAK'` letter request -> Rejected by `existing().status != 'DITOLAK' || isAdmin()`.
10. **Audit Log Tampering**: User or attacker attempts to `update` or `delete` an `/audit_logs/{logId}` entry -> Rejected by `allow update, delete: if false;`.
11. **Admin Spoofing via Unverified Email**: Attacker with `email == 'antonhaerulhermawan@gmail.com'` but `email_verified == false` attempts admin access -> Rejected by `request.auth.token.email_verified == true`.
12. **ID Poisoning**: Attacker attempts to create a document with a 500-character or special-character ID -> Rejected by `isValidId(docId)`.
