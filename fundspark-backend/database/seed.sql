-- ==========================================================
-- FundSpark — Seed Data (mirrors assets/js/data.js)
-- Run AFTER schema.sql
-- ==========================================================
USE fundspark;

-- ---------- Categories ----------
INSERT INTO categories (name, icon) VALUES
('Artificial Intelligence','brain-circuit'),
('Healthcare','heart-pulse'),
('Agriculture','sprout'),
('Education','graduation-cap'),
('FinTech','landmark'),
('Robotics','bot'),
('Food','utensils'),
('Travel','plane'),
('Gaming','gamepad-2'),
('Cybersecurity','shield-check'),
('E-commerce','shopping-bag'),
('Renewable Energy','sun'),
('IoT','cpu'),
('SaaS','cloud'),
('Blockchain','link-2'),
('Social Impact','hand-heart');

-- ---------- Users ----------
-- password_hash values below are placeholders only ('demo1234' hashed) — replace with real bcrypt hashes in the backend
INSERT INTO users (full_name, email, password_hash, phone, role, location, avatar_initials, status) VALUES
('Riya Kulkarni',    'riya.kulkarni@fundspark.io', '$2b$demo', '+91 98765 43210', 'founder',  'Pune, MH',       'RK', 'active'),
('Aman Deshpande',   'aman.d@fundspark.io',        '$2b$demo', '+91 98000 11111', 'founder',  'Bengaluru, KA',  'AD', 'active'),
('Simran Kaur',      'simran.kaur@fundspark.io',   '$2b$demo', '+91 98000 22222', 'founder',  'Mumbai, MH',     'SK', 'active'),
('Arjun Malhotra',   'arjun.m@fundspark.io',       '$2b$demo', '+91 91234 56780', 'investor', 'Mumbai, MH',     'AM', 'active'),
('Sneha Kapoor',     'sneha.k@fundspark.io',       '$2b$demo', '+91 98000 33333', 'investor', 'Delhi',          'SK', 'suspended'),
('Wasim Sheikh',     'wasim.s@fundspark.io',       '$2b$demo', '+91 98000 44444', 'investor', 'Hyderabad, TS',  'WS', 'pending'),
('Divya Krishnan',   'divya.k@fundspark.io',       '$2b$demo', '+91 98000 55555', 'investor', 'Chennai, TN',    'DK', 'active'),
('Karan Bhatt',      'karan.bhatt@fundspark.io',   '$2b$demo', '+91 98000 66666', 'admin',    'Pune, MH',       'KB', 'active');

-- ---------- Startups ----------
INSERT INTO startups (founder_id, category_id, name, tagline, description, funding_goal, amount_raised, deadline, location, startup_score, is_verified, is_trending, status) VALUES
(1, 3, 'AgroTech',   'IoT soil sensors that help small farmers cut water use by 40%.',       'Full description of AgroSense goes here.', 2500000, 1875000, '2026-09-15', 'Pune, MH',      92, TRUE,  TRUE,  'live'),
(2, 2, 'MediTrack AI','AI-assisted diagnostics for rural clinics without a radiologist.',      'Full description of MediTrack AI goes here.', 5000000, 3120000, '2026-09-25', 'Bengaluru, KA', 88, TRUE,  TRUE,  'live'),
(3, 5, 'PayNest',     'Micro-lending rails for gig workers, underwritten with UPI history.',   'Full description of PayNest goes here.', 8000000, 7120000, '2026-08-11', 'Mumbai, MH',    95, TRUE,  TRUE,  'live'),
(1, 4, 'ClassBridge', 'Offline-first learning app for tier-3 schools with patchy internet.',   'Full description of ClassBridge goes here.', 1800000, 940000,  '2026-10-01', 'Nagpur, MH',    81, FALSE, FALSE, 'pending');

-- ---------- Team Members ----------
INSERT INTO team_members (startup_id, name, role) VALUES
(1, 'Riya Kulkarni', 'Founder & CEO'),
(1, 'Devansh Rao',   'CTO'),
(1, 'Sana Qureshi',  'Head of Ops'),
(1, 'Farhan Ali',    'Lead Engineer');

-- ---------- Funding Usage ----------
INSERT INTO funding_usage (startup_id, label, percentage) VALUES
(1, 'Product Development', 40),
(1, 'Marketing', 25),
(1, 'Hiring', 25),
(1, 'Operations', 10);

-- ---------- Investments ----------
INSERT INTO investments (investor_id, startup_id, amount, payment_method, status) VALUES
(4, 1, 250000, 'upi', 'confirmed'),
(5, 1, 180000, 'upi', 'confirmed'),
(6, 1, 90000,  'card','pending'),
(7, 1, 400000, 'netbanking', 'confirmed'),
(4, 3, 400000, 'upi', 'confirmed');

-- ---------- Wishlist ----------
INSERT INTO wishlist (investor_id, startup_id) VALUES
(4, 2), (4, 3), (6, 1);

-- ---------- Comments ----------
INSERT INTO comments (startup_id, user_id, comment_text, likes) VALUES
(1, 4, 'Really impressed with the sensor accuracy in the demo video. What about after-sales support in remote districts?', 14),
(1, 5, 'Invested already — the traction numbers from the Nashik pilot are convincing.', 9),
(1, 6, 'Curious how this compares to imported soil-sensor kits on cost per acre.', 5);

-- ---------- Ratings ----------
INSERT INTO ratings (startup_id, investor_id, stars) VALUES
(1, 4, 5), (1, 5, 5), (1, 6, 4);

-- ---------- Notifications ----------
INSERT INTO notifications (user_id, icon, title, message, is_read) VALUES
(1, 'trending-up', 'New investment received', 'Divya Krishnan invested ₹4,00,000 in AgroSense.', FALSE),
(1, 'message-circle', 'New comment', 'Wasim Sheikh commented on your campaign.', FALSE),
(1, 'badge-check', 'Startup verified', 'AgroSense has been verified by the admin team.', FALSE);

-- ---------- Campaign Timeline ----------
INSERT INTO campaign_timeline (startup_id, title, description, event_date) VALUES
(1, 'Campaign launched', 'Initial pitch published and approved by admin.', '2026-06-01'),
(1, '50% funding milestone', 'Crossed halfway with 40+ investors.', '2026-06-22'),
(1, 'Featured on homepage', 'Selected as a trending campaign.', '2026-07-06');
