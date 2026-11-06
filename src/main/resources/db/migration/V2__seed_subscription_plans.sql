-- SEED DỮ LIỆU 5 GÓI DỊCH VỤ CHUẨN BR-23
INSERT INTO subscription_plans (plan_code, plan_name, monthly_price, annual_price, max_concurrent_jobs, monthly_unlock_quota, extra_unlock_price, has_priority_display, report_tier, support_tier) VALUES
('FREE', 'Gói Free (1 Job đầu/MST)', 0, NULL, 1, 0, 100000, FALSE, 'NONE', 'BASIC'),
('PAY_PER_USE', 'Gói Trả Lẻ (Pay-per-use)', 0, NULL, 1, 0, 100000, FALSE, 'NONE', 'BASIC'),
('BASIC', 'Gói Basic', 599000, 6290000, 3, 6, 85000, FALSE, 'BASIC', 'BASIC'),
('PRO', 'Gói Pro', 999000, 10490000, 5, 10, 75000, TRUE, 'STANDARD', 'PRIORITY'),
('ULTRA', 'Gói Ultra', 1999000, 20990000, 10, 25, 60000, TRUE, 'ADVANCED', 'DEDICATED')
ON CONFLICT (plan_code) DO NOTHING;
