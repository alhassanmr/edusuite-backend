# EduSuite — Local Setup & End-to-End Testing Guide

Follow this top-to-bottom the first time. Expect small bugs on first run —
that's normal for a codebase this size. Fix-as-you-go notes are at the bottom.

---

## 0. Prerequisites

| Tool | Version | Check with |
|------|---------|-----------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| npm | 9+ | `npm -v` |
| Git | any | `git --version` |

Install missing tools:
- Java: https://adoptium.net (Temurin 17)
- Maven: https://maven.apache.org/download.cgi (or `sdk install maven`)
- Node: https://nodejs.org (LTS)

---

## 1. Clone Both Repos

```bash
git clone https://github.com/alhassanmr/edusuite-backend.git
git clone https://github.com/alhassanmr/edusuite-frontend.git
```

---

## 2. Start the Backend (Terminal 1)

```bash
cd edusuite-backend
mvn clean compile        # FIRST: catch any compile errors before running
mvn spring-boot:run
```

Expected:
- Console ends with `Started EdusuiteApplication in X seconds`
- Dev profile is default: in-memory H2 DB, demo school auto-seeded

Quick checks:
- API alive: open http://localhost:8080/api/auth/login (should return 405/400, NOT connection refused)
- H2 console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:edusuite` | User: `sa` | Password: (empty)
  - Verify tables exist: SCHOOLS, USERS, STUDENTS, FEE_INVOICES, etc.

Seeded account: **admin / Admin@123** (Demo School)

---

## 3. Start the Frontend (Terminal 2)

```bash
cd edusuite-frontend
npm install
npm run dev
```

Open http://localhost:5173 — you should see the login page.
(`/api` is proxied to :8080 automatically by Vite.)

---

## 4. End-to-End Test Walkthrough

Do these IN ORDER — later steps depend on earlier data.

### Test 1 — School registration (multi-tenancy)
1. Login page → "Create School Account"
2. Step 1: name "Test Academy", fill contact email; slug auto-generates
3. Step 2: admin username `testadmin`, email, password (6+ chars)
4. Submit → should land on the Dashboard for Test Academy
5. ✅ PASS if: dashboard shows "Test Academy", not "Demo School"

### Test 2 — Admin CRUD
While logged in as testadmin:
1. Classes → Add Class: "Basic 6", section "A", year "2025/2026"
2. Teachers → Add Teacher: "Ama Mensah", subject "Mathematics"
3. Students → Add Student: "Kofi Boateng", admission no. "TA-001"
   - NOTE: the student form has no class dropdown yet — assign class via
     API or check the Known Gaps list below
4. ✅ PASS if: each appears in its table after save

### Test 3 — Tenant isolation (CRITICAL for trust)
1. Logout → login as `admin` / `Admin@123` (Demo School)
2. Go to Students
3. ✅ PASS if: Kofi Boateng is NOT visible (he belongs to Test Academy)
4. ❌ FAIL = data leak between schools — stop and fix before anything else

### Test 4 — Parent record + portal invite
As testadmin (Test Academy):
1. Create a parent via API (no UI page yet for parents):
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testadmin","password":"YOUR_PASSWORD"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

   curl -X POST http://localhost:8080/api/parents \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"firstName":"Yaw","lastName":"Boateng","phone":"+233240000000","email":"yaw@example.com","relationship":"Father"}'
   ```
2. Link the student to this parent (PUT /api/students/{id} with parentGuardian: {"id": X})
3. UI: Portal Users → Invite User → role PARENT → link to "Yaw Boateng"
   → username `yawb`, temp password `Parent@123` → keep SMS+Email checked
4. ✅ PASS if: backend console shows `[DEV SMS] To: ... Welcome to Test Academy`
   (dev mode logs instead of sending — this proves the notification pipeline)

### Test 5 — Parent portal
1. Logout → login as `yawb` / `Parent@123`
2. ✅ PASS if: lands on /portal/parent showing Kofi Boateng
3. ✅ PASS if: navigating to /dashboard redirects back to the parent portal
   (role-based access control working)

### Test 6 — Fees + notifications
1. Login as testadmin. Create an invoice via API:
   ```bash
   curl -X POST http://localhost:8080/api/fees/invoices \
     -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"student":{"id":KOFI_ID},"term":"Term 1","academicYear":"2025/2026","amountDue":1500,"dueDate":"2026-09-30"}'
   ```
2. ✅ PASS if: console logs [DEV SMS] + [DEV EMAIL] to Yaw about the new invoice
3. Login as yawb → Fees tab → invoice shows with "Pay ₵1,500" button

### Test 7 — Paystack (test mode)
1. Create free account at dashboard.paystack.com → get `sk_test_...` key
2. In `src/main/resources/application.yml` (dev section) set:
   `secret-key: "sk_test_YOUR_KEY"` → restart backend
3. Settings (as testadmin) → Settlement section → pick a bank from the
   dropdown (list loads = Paystack connectivity works) → use Paystack test
   account number `0000000000` → Activate
4. As yawb → Fees → Pay → should redirect to Paystack checkout
5. Pay with test card: `4084 0840 8408 4081`, any future expiry, CVV 408
6. Redirects to /payment/callback → verify → invoice becomes PAID
7. ✅ PASS if: payment receipt SMS/EMAIL logged + invoice status updated
8. Check Paystack dashboard → transaction shows with split to subaccount

### Test 8 — Teacher portal
1. Portal Users → invite TEACHER linked to Ama Mensah
2. Add a timetable entry for her class via API (POST /api/timetable)
3. Login as the teacher → Mark Attendance → select class → load students
   → mark → save
4. ✅ PASS if: attendance saves; parent portal shows it for Kofi

### Test 9 — Exams + result publication
1. As testadmin: create exam via API (POST /api/exams with schoolClass id)
2. Record Kofi's result (POST /api/exams/{id}/results/{kofiId} {"score": 85})
3. Publish: POST /api/exams/{id}/publish
4. ✅ PASS if: console logs result SMS to parent; result visible in parent
   and student portals (grade "A" for 85)

---

## 5. Known Gaps (expected — build as needed)

- No UI page for Parents CRUD (API only) — needed before real demo
- Student form: no class/parent dropdowns — assign via API for now
- Exams/Fees pages: "Create" buttons are placeholders (use API)
- Timetable admin page is a stub
- Tenant filtering: only StudentService fully enforces school scoping on
  findAll/findById — Teachers/Classes/Fees/Exams list endpoints may leak
  across schools. VERIFY in Test 3 with each entity and patch any that fail
  (copy the StudentService pattern: TenantContext + findBySchoolId)
- No password change screen (temp passwords stay until built)

## 6. Common First-Run Errors

| Symptom | Fix |
|---------|-----|
| `mvn: command not found` | Install Maven, add to PATH |
| Lombok errors in IDE | Install Lombok plugin + enable annotation processing (CLI build unaffected) |
| Port 8080 in use | `lsof -i :8080` then kill, or change server.port |
| CORS error in browser | Frontend must run on :5173 (matches allowed-origins) |
| 403 on portal endpoints | Role mismatch — check JWT role vs route; re-login after changes |
| Paystack "not configured" | Set sk_test key + restart; settlement must be activated before Pay Now |
| H2 data gone after restart | Expected — in-memory DB resets. Use prod profile + Postgres for persistence |

## 7. When All Tests Pass

You have a demo-ready build. Suggested demo script for a headmaster (15 min):
register their school live → add a class + 2 students → invite yourself as a
"parent" on your phone → show the SMS arriving (switch provider to arkesel
with a small credit balance) → pay a test invoice with MoMo test flow →
show the money split in the Paystack dashboard.

That last part — "the money goes straight to YOUR account, we never touch
it" — is the moment schools say yes.
