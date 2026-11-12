# AnonDev Platform - API Integration Documentation for Frontend

> **Phiên bản:** v1.2.0 (Bổ sung Đấu trường Code & Pessimistic Locking)  
> **Cập nhật:** 2026-11-12  
> **Cơ sở dữ liệu:** Neon Serverless PostgreSQL  
> **Môi trường Backend:** Spring Boot 3.3.4 (Java 17)  
> **Base URL:** `http://localhost:8080/api/v1`  
> **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
> **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`  

---

## 1. Cấu trúc phản hồi chuẩn (Envelope Response)

Tất cả các API đều trả về định dạng bọc thống nhất (`ApiResponse<T>`):

### Thành công (HTTP 200 / 201)
```typescript
interface ApiResponse<T> {
  success: true;
  message: string;
  data: T;
  timestamp: string; // ISO 8601 UTC
}
```

### Thất bại / Lỗi nghiệp vụ (HTTP 400 / 401 / 403 / 404 / 500)
```typescript
interface ApiErrorResponse {
  success: false;
  message: string;
  data?: {
    errorCode?: string; // Ví dụ: "BUSINESS_ERROR"
    [field: string]: any; // Lỗi validation chi tiết theo từng field
  };
  timestamp: string;
}
```

### Tiêu chuẩn Headers
Đối với các endpoint yêu cầu xác thực:
```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

---

## 2. Danh mục API Xác thực & Đăng ký (Authentication)

### 2.1. Đăng ký Ứng viên (Candidate Registration)
- **Phương thức:** `POST`
- **Đường dẫn:** `/auth/register-candidate`
- **Xác thực:** Không (Public)
- **Quy tắc nghiệp vụ:**
  - Tự động sinh mã định danh ẩn danh dạng `CAND-xxxxx` (**BR-01**).
  - Khởi tạo đồng thời 2 bản ghi: Két sắt bảo mật (`CandidateIdentityVault`) và Hồ sơ ẩn danh (`CandidateBlindProfile`).
  - Kiểm tra tính duy nhất của Email và SĐT (**BR-16**).

#### Request Body:
```typescript
interface RegisterCandidateRequest {
  email: string;            // Bắt buộc, đúng định dạng email
  phone: string;            // Bắt buộc, Regex: ^(84|0[35789])\d{8}$
  password: string;         // Bắt buộc, tối thiểu 6 ký tự
  realFullName: string;     // Bắt buộc, họ và tên thật
  provinceCity?: string;    // Ví dụ: "Ha Noi", "TP Ho Chi Minh"
  workMode?: 'REMOTE' | 'OFFLINE' | 'HYBRID';
  originalCvUrl?: string;   // Link CV gốc
}
```

#### Ví dụ Request:
```json
{
  "email": "nguyenvana@gmail.com",
  "phone": "0987654321",
  "password": "Password123!",
  "realFullName": "Nguyễn Văn A",
  "provinceCity": "Ha Noi",
  "workMode": "HYBRID",
  "originalCvUrl": "https://storage.anondev.vn/cv/nguyenvana.pdf"
}
```

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Candidate registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userId": "d47a948e-d3e6-4ceb-87c4-d6d8f8e659fb",
    "email": "nguyenvana@gmail.com",
    "role": "CANDIDATE",
    "candidateCode": "CAND-21249",
    "companyId": null
  },
  "timestamp": "2026-11-09T05:17:04.997Z"
}
```

---

### 2.2. Đăng ký Doanh nghiệp (Employer Registration)
- **Phương thức:** `POST`
- **Đường dẫn:** `/auth/register-employer`
- **Xác thực:** Không (Public)
- **Quy tắc:** Tạo tài khoản Nhà tuyển dụng và hồ sơ doanh nghiệp với `isTaxVerified = false`.

#### Request Body:
```typescript
interface RegisterEmployerRequest {
  email: string;              // Bắt buộc
  phone: string;              // Bắt buộc, số điện thoại Việt Nam
  password: string;           // Bắt buộc, min 6 ký tự
  companyName: string;        // Bắt buộc
  taxId: string;              // Bắt buộc, Mã số thuế (MST) duy nhất
  businessLicenseUrl?: string;// URL giấy phép kinh doanh
}
```

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Employer registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userId": "989cb80b-5933-4071-a63f-67611e92773d",
    "email": "hr@techcorp.vn",
    "role": "EMPLOYER",
    "companyId": "3def6d16-77f6-4675-b51f-4d7bb3fa911c"
  }
}
```

---

### 2.3. Đăng nhập hệ thống (Login)
- **Phương thức:** `POST`
- **Đường dẫn:** `/auth/login`
- **Xác thực:** Không (Public)

#### Request Body:
```json
{
  "email": "nguyenvana@gmail.com",
  "password": "Password123!"
}
```

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userId": "d47a948e-d3e6-4ceb-87c4-d6d8f8e659fb",
    "email": "nguyenvana@gmail.com",
    "role": "CANDIDATE", // "CANDIDATE" | "EMPLOYER" | "ADMIN"
    "candidateCode": "CAND-21249",
    "companyId": null
  }
}
```

---

## 3. Quản lý Két Sắt & Hồ Sơ Ẩn Danh (Candidate Vault)

### 3.1. Xem Két sắt & Hồ sơ cá nhân (2 Cột Dữ Liệu)
- **Phương thức:** `GET`
- **Đường dẫn:** `/candidates/me/vault`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Mục đích:** Ứng viên xem song song cột Két sắt bảo mật (chỉ ứng viên thấy) và cột Hồ sơ ẩn danh (Nhà tuyển dụng thấy).

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "vault": {
      "realFullName": "Nguyễn Văn A",
      "contactEmail": "nguyenvana@gmail.com",
      "contactPhone": "0987654321",
      "universityName": "Đại học Bách Khoa Hà Nội",
      "gender": "MALE",
      "avatarUrl": "https://cdn.anondev.vn/avatar/u1.png",
      "originalCvUrl": "https://storage.anondev.vn/cv/nguyenvana.pdf",
      "sanitizedCvUrl": null,
      "portfolioLinks": ["https://github.com/nguyenvana"]
    },
    "blindProfile": {
      "candidateCode": "CAND-21249",
      "provinceCity": "Ha Noi",
      "workMode": "HYBRID",
      "experienceLevel": "SENIOR",
      "expectedSalaryMin": 30000000,
      "expectedSalaryMax": 45000000,
      "primaryTechStack": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
      "skillRadarScores": { "Algorithms": 90, "SystemDesign": 85, "Database": 88 },
      "sanitizedBio": "Lập trình viên Senior Java với 4 năm kinh nghiệm phát triển microservices...",
      "overallRankingScore": 89.60
    }
  }
}
```

---

### 3.2. Cập nhật Két sắt & Tự động quét che PII (AI Sanitizer)
- **Phương thức:** `PUT`
- **Đường dẫn:** `/candidates/me/vault`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc:** Tất cả thông tin SĐT, Email, Link liên hệ cá nhân (LinkedIn, Facebook, Telegram) nếu nhập vào phần `bio` sẽ tự động bị thay thế bằng nhãn `[REDACTED_BY_ANONDEV]` trước khi lưu vào CSDL (**BR-03**).

#### Request Body:
```json
{
  "realFullName": "Nguyễn Văn A",
  "contactPhone": "0987654321",
  "universityName": "ĐH Bách Khoa Hà Nội",
  "experienceLevel": "SENIOR",
  "expectedSalaryMin": 35000000,
  "expectedSalaryMax": 50000000,
  "bio": "Liên hệ với tôi qua SĐT 0987654321 hoặc email test@gmail.com. Chuyên sâu Backend."
}
```

---

## 4. Quản lý Doanh nghiệp & Xác minh KYB (Company)

### 4.1. Lấy thông tin Doanh nghiệp hiện tại
- **Phương thức:** `GET`
- **Đường dẫn:** `/companies/me`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Dữ liệu trả về:** Số dư ví tiền, hạn mức tạm giữ, trạng thái xác minh thuế.

#### Phản hồi (Data):
```json
{
  "success": true,
  "data": {
    "id": "7d0374e2-b46e-4c27-94e7-78cd5120dfc7",
    "companyName": "TechCorp Vietnam LLC",
    "taxId": "0314567890",
    "isTaxVerified": true,
    "hasUsedFreeJob": true,
    "creditBalance": 400000.00,
    "heldCreditBalance": 0.00,
    "statusUpdateCounter": 1,
    "isBlockedByOverdueStatus": false
  }
}
```

---

### 4.2. Cập nhật hồ sơ KYB
- **Phương thức:** `PUT`
- **Đường dẫn:** `/companies/me/kyb`
- **Quyền hạn:** `ROLE_EMPLOYER`

#### Request Body:
```json
{
  "taxId": "0314567890",
  "businessLicenseUrl": "https://storage.anondev.vn/licenses/techcorp.pdf"
}
```

---

### 4.3. Admin duyệt xác minh thuế (KYB Verification)
- **Phương thức:** `PUT`
- **Đường dẫn:** `/companies/{companyId}/verify-tax?isVerified=true`
- **Quyền hạn:** `ROLE_ADMIN` (hoặc Employer trong môi trường phát triển)

---

## 5. Quản lý Tin Tuyển Dụng & Bài Test (Job & Assessment)

### 5.1. Đăng tin tuyển dụng kèm bài Test kỹ thuật
- **Phương thức:** `POST`
- **Đường dẫn:** `/jobs`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Quy tắc nghiệp vụ:**
  - Bắt buộc MST đã xác minh (`isTaxVerified = true`) (**BR-17**).
  - Không bị khóa do quá hạn cập nhật 14 ngày (**BR-09**).
  - Kiểm soát slot (**BR-24, BR-25**): 
    - `fundingSource: "FREE_TRIAL"`: Chỉ được dùng 1 lần duy nhất cho mỗi MST.
    - `fundingSource: "SINGLE_PURCHASE"`: Yêu cầu số dư ví >= 100.000 VNĐ.
    - `fundingSource: "SUBSCRIPTION_SLOT"`: Khấu trừ slot gói hội viên.
  - Tự động sinh bài test kỹ thuật song hành (**BR-11**).

#### Request Body:
```typescript
interface CreateJobRequest {
  title: string;                // Tiêu đề việc làm
  description: string;          // Mô tả công việc (tự động lọc PII)
  provinceCity: string;         // "Ha Noi", "TP Ho Chi Minh", "Da Nang"
  workMode: 'REMOTE' | 'OFFLINE' | 'HYBRID';
  salaryMin?: number;           // Mức lương tối thiểu (VNĐ)
  salaryMax?: number;           // Mức lương tối đa (VNĐ)
  minPassScore: number;         // Điểm sàn để vào Talent Pool (ví dụ: 70.0)
  fundingSource: 'FREE_TRIAL' | 'SINGLE_PURCHASE' | 'SUBSCRIPTION_SLOT';
  
  // Cấu hình bài test kèm theo (BR-11)
  testTitle?: string;
  problemStatement?: string;
  timeLimitMinutes?: number;    // Thời lượng làm bài (phút)
  starterCodeJson?: string;
  testCasesJson?: string;
}
```

---

### 5.2. Danh sách tin tuyển dụng đang hoạt động
- **Phương thức:** `GET`
- **Đường dẫn:** `/jobs`
- **Xác thực:** Public hoặc Authenticated

---

### 5.3. Xem đề bài Test kỹ thuật
- **Phương thức:** `GET`
- **Đường dẫn:** `/jobs/{jobId}/test`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc:** Chặn truy cập nếu ứng viên đã từng làm bài test cho tin tuyển dụng này (**BR-15**).

---

### 5.4. Nộp bài Test kỹ thuật & Ghi nhận Giám sát (Proctoring)
- **Phương thức:** `POST`
- **Đường dẫn:** `/jobs/{jobId}/test/submit`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc nghiệp vụ:**
  - **Bắt buộc** chấp thuận Nghị định 13/2023/NĐ-CP (`decree13ConsentAccepted: true`) (**BR-13**).
  - Tự động lọc sạch thông tin liên hệ ẩn trong code comments (**BR-03**).
  - Tự động chấm điểm (60% TestCase + 40% Clean Code) (**BR-05**).
  - Ghi nhận telemetry gian lận (rời màn hình, chuyển tab) và đặt thời hạn lưu trữ đúng **90 ngày** (**BR-12**).
  - Chỉ cho phép nộp duy nhất 1 lần (**BR-15**).

#### Request Body:
```typescript
interface SubmitTestRequest {
  decree13ConsentAccepted: boolean; // Bắt buộc = true
  submittedCode: string;            // Mã nguồn bài làm
  fullscreenExitCount?: number;     // Số lần thoát toàn màn hình
  tabSwitchCount?: number;          // Số lần chuyển tab trình duyệt
  keystrokeHistoryJson?: string;    // Chuỗi JSON nhịp gõ phím
  commitHistoryJson?: string;
}
```

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Test attempt evaluated successfully",
  "data": {
    "attemptId": "26f3dadd-b2ea-413d-96fe-3e06721d6ec9",
    "jobPostingId": "a7f271b1-3fa1-4835-82f1-55d8ec934001",
    "testcasePassRate": 88.00,
    "cleanCodeScore": 92.00,
    "totalScore": 89.60,
    "isPassedThreshold": true,
    "aiCodeFlagged": false,
    "submittedAt": "2026-11-09T05:30:10.512Z"
  }
}
```

---

## 6. Ghép Đôi, Mời Phỏng Vấn & Bung Két Sắt (Matching & Unlock)

### 6.1. Danh sách Ứng viên đạt chuẩn (Talent Pool Ẩn Danh)
- **Phương thức:** `GET`
- **Đường dẫn:** `/matching/jobs/{jobId}/qualified-candidates`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Bảo mật:** Cam đoan **0% rò rỉ PII**. Không trả về họ tên, SĐT, Email, tên trường đại học (**BR-01**).

#### Phản hồi (Data):
```typescript
interface BlindCandidateCardDto {
  candidateId: string;
  candidateCode: string;          // Ví dụ: "CAND-20351"
  provinceCity: string;
  workMode: string;
  experienceLevel: string;
  expectedSalaryMin: number;
  expectedSalaryMax: number;
  primaryTechStack: string[];
  testScore: number;              // Điểm bài test đã đạt
  aiCodeFlagged: boolean;
  invitationStatus: 'NONE' | 'PENDING' | 'ACCEPTED_UNLOCKED' | 'REJECTED';
  isUnlocked: boolean;
}
```

---

### 6.2. Gửi lời mời phỏng vấn (Tạm giữ 100.000đ hoặc 1 Quota)
- **Phương thức:** `POST`
- **Đường dẫn:** `/matching/invitations`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Quy tắc nghiệp vụ:**
  - Nhà tuyển dụng phải có MST hợp lệ và không bị chặn.
  - Chặn gửi trùng lặp nếu ứng viên đang có lời mời chờ duyệt (**BR-06**).
  - Tạm giữ ví tiền: `heldCreditBalance += 100.000 VNĐ` (**BR-06, BR-25**) hoặc tạm giữ 1 Quota tháng (`heldQuota += 1`).
  - Hạn phản hồi: 24h - 72h (mặc định 48h).
  - Tự động quét lọc thông tin liên hệ trực tiếp trong lời nhắn (**BR-03**).

#### Request Body:
```typescript
interface SendInvitationRequest {
  jobPostingId: string;           // UUID tin tuyển dụng
  candidateId: string;            // UUID ứng viên
  testAttemptId: string;          // UUID bài test đã đạt
  offeredSalaryRange?: string;    // Ví dụ: "40 - 55 Triệu"
  invitationMessage: string;      // Lời mời
  expiryHours?: number;           // 24 đến 72 giờ (mặc định 48)
  billingMethod: 'WALLET_CREDIT' | 'MONTHLY_QUOTA';
}
```

---

### 6.3. Ứng viên xem danh sách lời mời nhận được
- **Phương thức:** `GET`
- **Đường dẫn:** `/matching/candidates/me/invitations`
- **Quyền hạn:** `ROLE_CANDIDATE`

#### Phản hồi (Data):
```json
{
  "success": true,
  "data": [
    {
      "id": "7bee6003-852c-4d13-8472-6982d3bcdea1",
      "jobPostingId": "a7f271b1-3fa1-4835-82f1-55d8ec934001",
      "jobTitle": "Fullstack Java / React Architect",
      "companyName": "Innovate Solutions Corp",
      "offeredSalaryRange": "45 - 55 Trieu",
      "invitationMessage": "Chào bạn, chúng tôi rất ấn tượng với kết quả test...",
      "invitationStatus": "PENDING",
      "expiresAt": "2026-11-11T05:30:12.420Z",
      "sentAt": "2026-11-09T05:30:12.420Z"
    }
  ]
}
```

---

### 6.4. Ứng viên phản hồi lời mời (Đồng thuận 2 chiều)
- **Phương thức:** `POST`
- **Đường dẫn:** `/matching/invitations/{id}/respond`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc nghiệp vụ:**
  - **Khi chọn ACCEPT**:
    - Trạng thái thành `ACCEPTED_UNLOCKED`, `isBilled = true`.
    - Trừ vĩnh viễn 100.000 VNĐ hoặc 1 Quota đã tạm giữ của doanh nghiệp.
    - Kích hoạt thời hạn cập nhật trạng thái tuyển dụng 14 ngày (`statusUpdateDeadline = +14d`) (**BR-09**).
  - **Khi chọn REJECT**:
    - Trạng thái thành `REJECTED`.
    - Hủy khoản tạm giữ và hoàn trả nguyên vẹn về số dư khả dụng của doanh nghiệp (**0 VNĐ chi phí**) (**BR-06**).

#### Request Body:
```typescript
interface RespondInvitationRequest {
  action: 'ACCEPT' | 'REJECT';
}
```

---

### 6.5. Nhà tuyển dụng Bung Két Sắt (Giải mã thông tin liên hệ thật)
- **Phương thức:** `GET`
- **Đường dẫn:** `/matching/invitations/{id}/unlocked-profile`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Quy tắc nghiệp vụ:** Chỉ mở khóa thành công khi trạng thái là `ACCEPTED_UNLOCKED` và `isBilled == true` (**BR-02**).

#### Phản hồi (Data):
```json
{
  "success": true,
  "data": {
    "invitationId": "7bee6003-852c-4d13-8472-6982d3bcdea1",
    "candidateCode": "CAND-20351",
    "invitationStatus": "ACCEPTED_UNLOCKED",
    "isBilled": true,
    "billingMethodUsed": "WALLET_CREDIT",
    "submittedCodeSanitized": "public class Worker { ... }",
    "identityVault": {
      "realFullName": "Trần Thị Thu Thảo",
      "contactEmail": "thao.tran@example.com",
      "contactPhone": "0993261645",
      "universityName": "Đại học Bách Khoa",
      "gender": "FEMALE",
      "avatarUrl": "https://cdn.anondev.vn/avatar/thao.jpg",
      "originalCvUrl": "https://storage.anondev.vn/cv/thao-cv.pdf",
      "portfolioLinks": ["https://github.com/thaotran"]
    },
    "postUnlockHiringStatus": "AWAITING_UPDATE",
    "statusUpdateDeadline": "2026-11-23T05:30:12.891Z"
  }
}
```

---

### 6.6. Cập nhật trạng thái tuyển dụng hậu mở khóa (Quy tắc 14 ngày)
- **Phương thức:** `PUT`
- **Đường dẫn:** `/matching/invitations/{id}/hiring-status?status={STATUS}`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Danh sách trạng thái hợp lệ:**
  - `AWAITING_UPDATE`
  - `INTERVIEW_SCHEDULED` (Đã lên lịch phỏng vấn)
  - `HIRED` (Đã tuyển dụng)
  - `REJECTED` (Đã từ chối sau phỏng vấn)
- **Cơ chế thưởng (BR-09):** Mỗi 5 lần cập nhật trạng thái đúng hạn, doanh nghiệp được hệ thống tặng miễn phí `+1 Quota` mở khóa.

---

## 7. Quản lý Ví & Gói Cước (Billing & Wallet)

### 7.1. Nạp tiền ví thử nghiệm (Mock Topup)
- **Phương thức:** `POST`
- **Đường dẫn:** `/billing/mock-topup`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Số tiền tối thiểu:** 10.000 VNĐ

#### Request Body:
```json
{
  "amount": 500000.00
}
```

---

### 7.2. Lấy danh sách gói cước định kỳ
- **Phương thức:** `GET`
- **Đường dẫn:** `/billing/plans`
- **Xác thực:** Public

---

### 7.3. Đăng ký mua gói cước
- **Phương thức:** `POST`
- **Đường dẫn:** `/billing/subscribe`
- **Quyền hạn:** `ROLE_EMPLOYER`

#### Request Body:
```json
{
  "planCode": "PRO",
  "billingCycle": "MONTHLY" // hoặc "ANNUAL"
}
```

---

### 7.4. Nộp khiếu nại hoàn phí mở khóa (Refund Dispute)
- **Phương thức:** `POST`
- **Đường dẫn:** `/disputes/refund`
- **Quyền hạn:** `ROLE_EMPLOYER`
- **Quy tắc:** Nộp trong vòng **7 ngày** kể từ khi mở khóa nếu phát hiện SĐT/Email sai, ứng viên không phản hồi sau 5 ngày, hoặc gian lận (**BR-18, BR-20**).

#### Request Body:
```typescript
interface RefundDisputeRequest {
  invitationId: string;       // UUID lời mời đã mở khóa
  refundReason: 'INVALID_CONTACT_INFO' | 'CANDIDATE_NO_SHOW_5_DAYS' | 'CONFIRMED_CHEATING';
  evidenceUrls?: string[];    // Danh sách URL ảnh chụp bằng chứng
}
```

---

### 7.5. Admin phân quyết khiếu nại hoàn phí
- **Phương thức:** `PUT`
- **Đường dẫn:** `/admin/disputes/{id}/resolve?approve=true`
- **Quyền hạn:** `ROLE_ADMIN`
- **Quy tắc:** Nếu `approve = true`, hệ thống tự động hoàn trả 100.000 VNĐ vào ví hoặc cộng lại 1 Quota vào hạn mức tháng của công ty (**BR-19**).

---

## 8. Danh mục API Đấu trường Code & Giải đấu Lập trình (Coding Arena & Tournament - B2C)

### 8.1. Lấy danh sách giải đấu lập trình
- **Phương thức:** `GET`
- **Đường dẫn:** `/tournaments`
- **Xác thực:** Public (Nếu có token Candidate, hệ thống trả về thêm trường `isRegistered: true/false`)
- **Mô tả:** Lấy danh sách các giải đấu với trạng thái động (`UPCOMING`, `ONGOING`, `FINISHED`), số lượng thí sinh tham gia và số lượng đề bài.

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": "8c981d18-3c93-4bf1-8347-d8431864eba9",
      "title": "AnonDev Weekly Arena #1 - Autumn Clash",
      "entryFee": 50000.00,
      "prizePoolAmount": 5000000.00,
      "startTime": "2026-11-12T15:26:00Z",
      "endTime": "2026-11-12T17:26:00Z",
      "status": "ONGOING",
      "participantCount": 24,
      "problemCount": 3,
      "isRegistered": true
    }
  ],
  "timestamp": "2026-11-12T15:30:00Z"
}
```

---

### 8.2. Lấy chi tiết giải đấu
- **Phương thức:** `GET`
- **Đường dẫn:** `/tournaments/{id}`
- **Xác thực:** Public

---

### 8.3. Thí sinh đăng ký tham gia Đấu trường (Join Tournament)
- **Phương thức:** `POST`
- **Đường dẫn:** `/tournaments/{id}/join`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc:**
  - Chỉ được đăng ký khi giải đấu đang ở trạng thái `UPCOMING` hoặc `ONGOING`.
  - Chống đăng ký trùng lặp (trả về `400 Bad Request` nếu đã tham gia).

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Successfully registered for tournament",
  "data": {
    "id": "f550972b-8a8f-4ba3-8b74-3be8bf33cbfa",
    "tournamentId": "8c981d18-3c93-4bf1-8347-d8431864eba9",
    "candidateId": "d47a948e-d3e6-4ceb-87c4-d6d8f8e659fb",
    "registeredAt": "2026-11-12T15:30:15Z",
    "totalScore": 0,
    "penaltySeconds": 0,
    "rankPosition": null,
    "isDisqualified": false
  },
  "timestamp": "2026-11-12T15:30:15Z"
}
```

---

### 8.4. Thí sinh lấy danh sách đề thi của giải đấu
- **Phương thức:** `GET`
- **Đường dẫn:** `/tournaments/{id}/problems`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc bảo mật đề thi:**
  - Bắt buộc ứng viên phải bấm đăng ký tham gia giải trước.
  - Đề bài bị khóa và chỉ mở khi giải đấu đã chính thức bắt đầu (`startTime <= now`).

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": "1ffdd5d7-3d9b-4cfa-8660-c5f5355bcbb5",
      "tournamentId": "8c981d18-3c93-4bf1-8347-d8431864eba9",
      "title": "Problem A: Two Sum Ultra",
      "difficulty": "EASY",
      "problemStatement": "Cho mang so nguyen nums va target, tim 2 vi tri sao cho tong bang target.",
      "points": 100,
      "timeLimitMs": 1000,
      "memoryLimitKb": 256000,
      "starterCodeJson": "{\"java\": \"class Solution { public int[] twoSum(int[] nums, int target) {} }\"}",
      "testCasesJson": "[{\"input\": \"[2,7,11,15], 9\", \"expected\": \"[0,1]\"}]"
    }
  ],
  "timestamp": "2026-11-12T15:30:20Z"
}
```

---

### 8.5. Thí sinh nộp bài giải thuật (Submit Solution)
- **Phương thức:** `POST`
- **Đường dẫn:** `/tournaments/{id}/problems/{problemId}/submit`
- **Quyền hạn:** `ROLE_CANDIDATE`
- **Quy tắc chấm điểm & thời gian phạt:**
  - Chỉ chấp nhận nộp bài trong khung giờ `ONGOING`.
  - Tính điểm theo tỷ lệ test cases vượt qua và cộng dồn vào `totalScore` của thí sinh.
  - Tính thời gian phạt (`penaltySeconds`) chuẩn ICPC: thời gian nộp bài tính từ thời điểm mở giải.

#### Request Body:
```typescript
interface SubmitTournamentSolutionRequest {
  language: string;     // Ví dụ: "JAVA", "PYTHON", "CPP", "JAVASCRIPT"
  sourceCode: string;   // Mã nguồn giải thuật của thí sinh (tối đa 100KB)
}
```

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Solution evaluated: ACCEPTED",
  "data": {
    "id": "c1f6d338-9279-46c5-a6e5-475ec76a26df",
    "tournamentId": "8c981d18-3c93-4bf1-8347-d8431864eba9",
    "problemId": "1ffdd5d7-3d9b-4cfa-8660-c5f5355bcbb5",
    "candidateId": "d47a948e-d3e6-4ceb-87c4-d6d8f8e659fb",
    "language": "JAVA",
    "verdict": "ACCEPTED",
    "passedTestCases": 10,
    "totalTestCases": 10,
    "executionTimeMs": 120,
    "scoreAwarded": 100,
    "submittedAt": "2026-11-12T15:31:00Z"
  },
  "timestamp": "2026-11-12T15:31:00Z"
}
```

---

### 8.6. Bảng xếp hạng Live Leaderboard (Ẩn danh)
- **Phương thức:** `GET`
- **Đường dẫn:** `/tournaments/{id}/leaderboard`
- **Xác thực:** Public
- **Quy tắc:**
  - Sắp xếp ưu tiên: Điểm số cao nhất (`totalScore DESC`), thời gian phạt ít nhất (`penaltySeconds ASC`).
  - Danh tính thí sinh được bảo mật bằng mã ẩn danh `candidateCode` (ví dụ `CAND-76900`).

#### Phản hồi (Data):
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "rank": 1,
      "candidateCode": "CAND-76900",
      "candidateId": "d47a948e-d3e6-4ceb-87c4-d6d8f8e659fb",
      "totalScore": 300,
      "penaltySeconds": 602,
      "problemsSolved": 2
    },
    {
      "rank": 2,
      "candidateCode": "CAND-38969",
      "candidateId": "1e75a892-0b31-4a25-a1c2-75d165f128bc",
      "totalScore": 100,
      "penaltySeconds": 302,
      "problemsSolved": 1
    }
  ],
  "timestamp": "2026-11-12T15:31:15Z"
}
```

---

### 8.7. [Admin] Tạo giải đấu mới
- **Phương thức:** `POST`
- **Đường dẫn:** `/admin/tournaments`
- **Quyền hạn:** `ROLE_ADMIN`

#### Request Body:
```json
{
  "title": "AnonDev Winter Arena 2026",
  "entryFee": 50000,
  "prizePoolAmount": 5000000,
  "startTime": "2026-11-20T19:00:00Z",
  "endTime": "2026-11-20T21:00:00Z"
}
```

---

### 8.8. [Admin] Thêm bài toán vào giải đấu
- **Phương thức:** `POST`
- **Đường dẫn:** `/admin/tournaments/{id}/problems`
- **Quyền hạn:** `ROLE_ADMIN`

#### Request Body:
```json
{
  "title": "Problem A: Two Sum Ultra",
  "difficulty": "EASY",
  "problemStatement": "Cho mảng số nguyên nums và số nguyên target, tìm 2 chỉ số sao cho tổng bằng target.",
  "points": 100,
  "timeLimitMs": 1000,
  "memoryLimitKb": 256000,
  "starterCodeJson": "{\"java\": \"class Solution { public int[] twoSum(int[] nums, int target) {} }\"}",
  "testCasesJson": "[{\"input\": \"[2,7,11,15], 9\", \"expected\": \"[0,1]\"}]"
}
```

---

## 9. Hướng dẫn Tích hợp Phía Frontend (Axios Snippets)

### Cấu hình `src/api/axiosClient.ts`
```typescript
import axios from 'axios';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const errorData = error.response?.data;
    return Promise.reject(errorData || error);
  }
);

export default axiosClient;
```
