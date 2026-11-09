CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Bảng users (BR-16, BR-17)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CANDIDATE', 'EMPLOYER', 'ADMIN')),
    is_email_verified BOOLEAN DEFAULT TRUE,
    is_phone_verified BOOLEAN DEFAULT TRUE,
    account_status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'WARNED', 'RESTRICTED', 'BANNED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Bảng companies (BR-17, BR-24, BR-09)
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    business_license_url TEXT,
    is_tax_verified BOOLEAN DEFAULT FALSE,
    has_used_free_job BOOLEAN DEFAULT FALSE,
    credit_balance NUMERIC(12, 2) DEFAULT 0 CHECK (credit_balance >= 0),
    held_credit_balance NUMERIC(12, 2) DEFAULT 0 CHECK (held_credit_balance >= 0),
    status_update_counter INT DEFAULT 0,
    is_blocked_by_overdue_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Bảng candidate_blind_profiles (Công khai trước khi Unlock - BR-01)
CREATE TABLE IF NOT EXISTS candidate_blind_profiles (
    candidate_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    candidate_code VARCHAR(20) NOT NULL UNIQUE,
    province_city VARCHAR(100) NOT NULL,
    work_mode VARCHAR(20) NOT NULL CHECK (work_mode IN ('REMOTE', 'OFFLINE', 'HYBRID')),
    experience_level VARCHAR(30) CHECK (experience_level IN ('INTERN', 'FRESHER', 'JUNIOR', 'MID', 'SENIOR')),
    expected_salary_min NUMERIC(12, 2),
    expected_salary_max NUMERIC(12, 2),
    primary_tech_stack JSONB NOT NULL DEFAULT '[]',
    skill_radar_scores JSONB NOT NULL DEFAULT '{}',
    sanitized_bio TEXT,
    overall_ranking_score NUMERIC(5, 2) DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Bảng candidate_identity_vaults (Cô lập 100%, chỉ mở khi Mutual Unlock - BR-01, BR-02)
CREATE TABLE IF NOT EXISTS candidate_identity_vaults (
    candidate_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    real_full_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    university_name VARCHAR(255),
    gender VARCHAR(20),
    avatar_url TEXT,
    original_cv_url TEXT NOT NULL,
    sanitized_cv_url TEXT,
    portfolio_links JSONB DEFAULT '[]',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Bảng subscription_plans (BR-22, BR-23)
CREATE TABLE IF NOT EXISTS subscription_plans (
    id SERIAL PRIMARY KEY,
    plan_code VARCHAR(30) NOT NULL UNIQUE,
    plan_name VARCHAR(100) NOT NULL,
    monthly_price NUMERIC(12, 2) NOT NULL,
    annual_price NUMERIC(12, 2),
    max_concurrent_jobs INT NOT NULL,
    monthly_unlock_quota INT NOT NULL,
    extra_unlock_price NUMERIC(12, 2) NOT NULL,
    has_priority_display BOOLEAN DEFAULT FALSE,
    report_tier VARCHAR(20) DEFAULT 'NONE',
    support_tier VARCHAR(20) DEFAULT 'BASIC'
);

-- 6. Bảng company_subscriptions (BR-26, BR-27, BR-28)
CREATE TABLE IF NOT EXISTS company_subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    plan_id INT NOT NULL REFERENCES subscription_plans(id),
    billing_cycle VARCHAR(20) NOT NULL CHECK (billing_cycle IN ('MONTHLY', 'ANNUAL')),
    locked_price NUMERIC(12, 2) NOT NULL,
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED_NO_REFUND')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Bảng monthly_quota_ledgers (BR-26, BR-09, BR-19)
CREATE TABLE IF NOT EXISTS monthly_quota_ledgers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES company_subscriptions(id) ON DELETE SET NULL,
    cycle_year INT NOT NULL,
    cycle_month INT NOT NULL CHECK (cycle_month BETWEEN 1 AND 12),
    allocated_quota INT NOT NULL DEFAULT 0,
    rewarded_quota INT NOT NULL DEFAULT 0,
    refunded_quota INT NOT NULL DEFAULT 0,
    held_quota INT NOT NULL DEFAULT 0,
    used_quota INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ NOT NULL,
    UNIQUE (company_id, cycle_year, cycle_month)
);

-- 8. Bảng wallet_transactions (BR-22, BR-25, BR-19)
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    payment_gateway VARCHAR(30) DEFAULT 'INTERNAL_WALLET',
    reference_id UUID,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. Bảng job_postings (BR-05, BR-24, BR-25, BR-26)
CREATE TABLE IF NOT EXISTS job_postings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description_sanitized TEXT NOT NULL,
    province_city VARCHAR(100) NOT NULL,
    work_mode VARCHAR(20) NOT NULL CHECK (work_mode IN ('REMOTE', 'OFFLINE', 'HYBRID')),
    salary_min NUMERIC(12, 2),
    salary_max NUMERIC(12, 2),
    min_pass_score NUMERIC(5, 2) NOT NULL DEFAULT 50.00,
    funding_source VARCHAR(20) NOT NULL CHECK (funding_source IN ('FREE_TRIAL', 'SINGLE_PURCHASE', 'SUBSCRIPTION_SLOT')),
    is_priority_display BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '30 days'),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'EXPIRED'))
);

-- 10. Bảng technical_tests (BR-11)
CREATE TABLE IF NOT EXISTS technical_tests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_posting_id UUID NOT NULL UNIQUE REFERENCES job_postings(id) ON DELETE CASCADE,
    creation_method VARCHAR(20) NOT NULL CHECK (creation_method IN ('MANUAL', 'AI_FROM_JD')),
    title VARCHAR(255) NOT NULL,
    problem_statement TEXT NOT NULL,
    time_limit_minutes INT NOT NULL DEFAULT 45,
    starter_code_json JSONB DEFAULT '{}',
    test_cases_json JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. Bảng test_attempts (BR-05, BR-13, BR-14, BR-15)
CREATE TABLE IF NOT EXISTS test_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    technical_test_id UUID NOT NULL REFERENCES technical_tests(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    decree13_consent_accepted BOOLEAN NOT NULL CHECK (decree13_consent_accepted = TRUE),
    consent_accepted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    submitted_code_sanitized TEXT,
    testcase_pass_rate NUMERIC(5, 2) DEFAULT 0,
    clean_code_score NUMERIC(5, 2) DEFAULT 0,
    total_score NUMERIC(5, 2) DEFAULT 0,
    is_passed_threshold BOOLEAN DEFAULT FALSE,
    ai_code_flagged BOOLEAN DEFAULT FALSE,
    appeal_status VARCHAR(30) DEFAULT 'NONE',
    appeal_explanation TEXT,
    is_retake_attempt BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    submitted_at TIMESTAMPTZ,
    UNIQUE (job_posting_id, candidate_id, is_retake_attempt)
);

-- 12. Bảng proctoring_logs (BR-12, BR-13)
CREATE TABLE IF NOT EXISTS proctoring_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    attempt_id UUID NOT NULL UNIQUE REFERENCES test_attempts(id) ON DELETE CASCADE,
    fullscreen_exit_count INT DEFAULT 0,
    tab_switch_count INT DEFAULT 0,
    keystroke_history_json JSONB DEFAULT '[]',
    commit_history_json JSONB DEFAULT '[]',
    ai_detection_confidence NUMERIC(5, 2) DEFAULT 0,
    is_anonymized_after_expiry BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    retention_expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '90 days')
);

-- 13. Bảng interview_invitations (BR-02, BR-06, BR-07, BR-09)
CREATE TABLE IF NOT EXISTS interview_invitations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_posting_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    candidate_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    test_attempt_id UUID NOT NULL REFERENCES test_attempts(id),
    offered_salary_range VARCHAR(100),
    invitation_message_sanitized TEXT,
    expiry_hours INT NOT NULL CHECK (expiry_hours BETWEEN 24 AND 72),
    sent_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    invitation_status VARCHAR(30) DEFAULT 'PENDING' CHECK (invitation_status IN (
        'PENDING', 'ACCEPTED_UNLOCKED', 'REJECTED', 'EXPIRED', 'REVOKED_BY_COMPANY'
    )),
    billing_method VARCHAR(20) NOT NULL CHECK (billing_method IN ('MONTHLY_QUOTA', 'WALLET_CREDIT')),
    unlock_fee_charged NUMERIC(12, 2) DEFAULT 0,
    is_billed BOOLEAN DEFAULT FALSE,
    post_unlock_hiring_status VARCHAR(30) DEFAULT 'AWAITING_UPDATE' CHECK (post_unlock_hiring_status IN (
        'AWAITING_UPDATE', 'INTERVIEW_SCHEDULED', 'HIRED', 'REJECTED'
    )),
    status_update_deadline TIMESTAMPTZ,
    status_updated_at TIMESTAMPTZ,
    UNIQUE (job_posting_id, candidate_id)
);

-- 14. Bảng refund_disputes (BR-18, BR-19, BR-20)
CREATE TABLE IF NOT EXISTS refund_disputes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invitation_id UUID NOT NULL UNIQUE REFERENCES interview_invitations(id) ON DELETE CASCADE,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    refund_reason VARCHAR(40) NOT NULL CHECK (refund_reason IN (
        'INVALID_CONTACT_INFO', 'CANDIDATE_NO_SHOW_5_DAYS', 'CONFIRMED_CHEATING'
    )),
    evidence_urls_json JSONB NOT NULL DEFAULT '[]',
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    resolution_deadline TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '5 days'),
    dispute_status VARCHAR(20) DEFAULT 'PENDING' CHECK (dispute_status IN ('PENDING', 'APPROVED_REFUNDED', 'REJECTED')),
    refund_destination VARCHAR(20) CHECK (refund_destination IN ('WALLET_CREDIT', 'MONTHLY_QUOTA')),
    resolved_by_admin_id UUID REFERENCES users(id),
    resolved_at TIMESTAMPTZ
);

-- 15. Bảng compliance_violations (BR-04, BR-10, BR-21)
CREATE TABLE IF NOT EXISTS compliance_violations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    violator_user_id UUID NOT NULL REFERENCES users(id),
    reported_by_user_id UUID REFERENCES users(id),
    rule_code VARCHAR(15) NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    penalty_applied VARCHAR(40) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 16. Bảng coding_tournaments (B2C)
CREATE TABLE IF NOT EXISTS coding_tournaments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    entry_fee NUMERIC(12, 2) NOT NULL DEFAULT 50000,
    prize_pool_amount NUMERIC(12, 2) NOT NULL DEFAULT 5000000,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) DEFAULT 'UPCOMING'
);
