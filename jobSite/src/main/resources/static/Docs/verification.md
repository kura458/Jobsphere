# Company Verification Flow

## 1. Role Selection
- User registers and selects EMPLOYER role
- Email verified via OTP

## 2. Company Verification Submission
Employer submits:
- Company Name (required)
- Trade License PDF/DOC (required - uploaded to Cloudinary)
- TIN Number (optional)
- Website (optional)

Status: PENDING

## 3. Admin Review
Admin reviews submitted documents.

Two possible actions:

### Option A: APPROVE
- Admin changes status: PENDING → APPROVED
- System generates 6-digit verification code
- Code sent to employer's registered email
- Email contains verification code and instructions

### Option B: BAN / REJECT
- Admin changes status: PENDING → BANNED
- No verification code generated
- Rejection message sent to employer's email
- Trade license deleted from Cloudinary

## 4. Code Verification
- Employer receives 6-digit code via email
- Employer enters code in system
- Code can be used only once
- After verification: Employer unlocks full company profile features

## 5. Email Notifications
All emails sent to the email used during registration.

### Approval Email Contains
- Company name
- 6-digit verification code
- Instructions to complete verification

### Rejection Email Contains
- Company name
- Rejection reason
- Contact information for support
