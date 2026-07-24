# EduSuite Backend

Spring Boot REST API for the EduSuite School Management System.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Development (H2 in-memory)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```
API runs on `http://localhost:8080`

**Default admin account:**
- Username: `admin`
- Password: `Admin@123`

### Production (PostgreSQL)
```bash
export DB_URL=jdbc:postgresql://localhost:5432/edusuite
export DB_USERNAME=edusuite
export DB_PASSWORD=your_password
export JWT_SECRET=your_base64_secret_key
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## API Endpoints

### Auth
- `POST /api/auth/register` — Create new user
- `POST /api/auth/login` — Get JWT token

### Students
- `GET /api/students` — List all / search / filter by class
- `POST /api/students` — Create student
- `PUT /api/students/{id}` — Update
- `DELETE /api/students/{id}` — Delete

### Teachers
- `GET /api/teachers`
- `POST /api/teachers`
- `PUT /api/teachers/{id}`
- `DELETE /api/teachers/{id}`

### Classes
- `GET /api/classes`
- `POST /api/classes`
- `PUT /api/classes/{id}`

### Attendance
- `POST /api/attendance/mark` — Bulk mark attendance
- `GET /api/attendance/class/{classId}` — By class & date
- `GET /api/attendance/student/{studentId}` — Student history

### Fees
- `GET /api/fees/invoices` — All invoices
- `POST /api/fees/invoices` — Create invoice
- `POST /api/fees/invoices/{id}/payments` — Record payment

### Exams & Results
- `GET /api/exams` — List exams
- `POST /api/exams` — Create exam
- `POST /api/exams/{id}/publish` — Publish results
- `POST /api/exams/{examId}/results/{studentId}` — Record result

### Notices
- `GET /api/notices` — All notices
- `POST /api/notices` — Post notice

### Timetable
- `GET /api/timetable/class/{classId}`
- `GET /api/timetable/teacher/{teacherId}`

### Dashboard
- `GET /api/dashboard/stats` — Key metrics

## Database Schema
14 entities: User, Student, Teacher, ParentGuardian, SchoolClass, Subject, Attendance, FeeInvoice, FeePayment, Exam, Result, Notice, TimetableEntry.

All JPA-managed with auto-DDL in dev mode.

## Security
- JWT-based stateless auth
- Role-based access: ADMIN, TEACHER, PARENT, STUDENT
- CORS enabled for frontend origin
- Password hashing with BCrypt

## Built with
- Spring Boot 3.3.2
- Spring Data JPA
- Spring Security
- JJWT (JWT)
- PostgreSQL / H2
