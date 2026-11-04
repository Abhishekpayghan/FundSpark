-- ==========================================================
-- FundSpark — MySQL Database Schema
-- DBMS Mini Project | Normalized to 3NF
-- ==========================================================

CREATE DATABASE IF NOT EXISTS fundspark;
USE fundspark;

-- ----------------------------------------------------------
-- 1. USERS  (base table for Founder / Investor / Admin)
-- ----------------------------------------------------------
CREATE TABLE users (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(120)  NOT NULL,
    email           VARCHAR(150)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    phone           VARCHAR(20),
    role            ENUM('founder','investor','admin') NOT NULL,
    location         VARCHAR(120),
    bio             TEXT,
    avatar_initials VARCHAR(4),
    status          ENUM('active','pending','suspended') DEFAULT 'active',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------
-- 2. CATEGORIES
-- ----------------------------------------------------------
CREATE TABLE categories (
    category_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(80) NOT NULL UNIQUE,
    icon            VARCHAR(60)              -- lucide icon keyword, e.g. 'brain-circuit'
);

-- ----------------------------------------------------------
-- 3. STARTUPS  (campaigns)
-- ----------------------------------------------------------
CREATE TABLE startups (
    startup_id      INT AUTO_INCREMENT PRIMARY KEY,
    founder_id      INT NOT NULL,
    category_id     INT NOT NULL,
    name            VARCHAR(150) NOT NULL,
    tagline         VARCHAR(255),
    description     TEXT,
    logo_url        VARCHAR(255),
    cover_image_url VARCHAR(255),
    pitch_video_url VARCHAR(255),
    business_plan_url VARCHAR(255),
    funding_goal    DECIMAL(14,2) NOT NULL,
    amount_raised   DECIMAL(14,2) DEFAULT 0,
    deadline        DATE NOT NULL,
    location        VARCHAR(120),
    startup_score   TINYINT DEFAULT 0,          -- 0-100 computed score
    is_verified     BOOLEAN DEFAULT FALSE,
    is_trending     BOOLEAN DEFAULT FALSE,
    status          ENUM('draft','pending','live','closed','rejected') DEFAULT 'pending',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (founder_id)  REFERENCES users(user_id)       ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- ----------------------------------------------------------
-- 4. STARTUP TEAM MEMBERS
-- ----------------------------------------------------------
CREATE TABLE team_members (
    team_member_id  INT AUTO_INCREMENT PRIMARY KEY,
    startup_id      INT NOT NULL,
    name            VARCHAR(120) NOT NULL,
    role            VARCHAR(120),
    FOREIGN KEY (startup_id) REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 5. FUNDING USAGE BREAKDOWN  (pie chart on Startup Details)
-- ----------------------------------------------------------
CREATE TABLE funding_usage (
    usage_id        INT AUTO_INCREMENT PRIMARY KEY,
    startup_id      INT NOT NULL,
    label           VARCHAR(80) NOT NULL,   -- e.g. 'Product Development'
    percentage      DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (startup_id) REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 6. INVESTMENTS  (Investor -> Startup)
-- ----------------------------------------------------------
CREATE TABLE investments (
    investment_id   INT AUTO_INCREMENT PRIMARY KEY,
    investor_id     INT NOT NULL,
    startup_id      INT NOT NULL,
    amount          DECIMAL(14,2) NOT NULL,
    payment_method  ENUM('upi','netbanking','card') DEFAULT 'upi',
    status          ENUM('pending','confirmed','failed') DEFAULT 'pending',
    invested_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investor_id) REFERENCES users(user_id)     ON DELETE CASCADE,
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 7. WISHLIST / BOOKMARKS
-- ----------------------------------------------------------
CREATE TABLE wishlist (
    wishlist_id     INT AUTO_INCREMENT PRIMARY KEY,
    investor_id     INT NOT NULL,
    startup_id      INT NOT NULL,
    added_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_wishlist (investor_id, startup_id),
    FOREIGN KEY (investor_id) REFERENCES users(user_id)     ON DELETE CASCADE,
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 8. INTEREST EXPRESSIONS  ("I'm Interested" button)
-- ----------------------------------------------------------
CREATE TABLE interests (
    interest_id     INT AUTO_INCREMENT PRIMARY KEY,
    investor_id     INT NOT NULL,
    startup_id      INT NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investor_id) REFERENCES users(user_id)     ON DELETE CASCADE,
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 9. COMMENTS
-- ----------------------------------------------------------
CREATE TABLE comments (
    comment_id      INT AUTO_INCREMENT PRIMARY KEY,
    startup_id      INT NOT NULL,
    user_id         INT NOT NULL,
    parent_comment_id INT DEFAULT NULL,     -- for replies
    comment_text    TEXT NOT NULL,
    likes           INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (startup_id) REFERENCES startups(startup_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(user_id)       ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 10. RATINGS
-- ----------------------------------------------------------
CREATE TABLE ratings (
    rating_id       INT AUTO_INCREMENT PRIMARY KEY,
    startup_id      INT NOT NULL,
    investor_id     INT NOT NULL,
    stars           TINYINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_rating (startup_id, investor_id),
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE CASCADE,
    FOREIGN KEY (investor_id) REFERENCES users(user_id)       ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 11. NOTIFICATIONS
-- ----------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    icon            VARCHAR(60),
    title           VARCHAR(150),
    message         VARCHAR(255),
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 12. MESSAGES  (Investor <-> Founder direct messages)
-- ----------------------------------------------------------
CREATE TABLE messages (
    message_id      INT AUTO_INCREMENT PRIMARY KEY,
    sender_id       INT NOT NULL,
    receiver_id     INT NOT NULL,
    startup_id      INT DEFAULT NULL,        -- optional context
    body            TEXT NOT NULL,
    sent_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read         BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
-- 13. CAMPAIGN TIMELINE / MILESTONES
-- ----------------------------------------------------------
CREATE TABLE campaign_timeline (
    timeline_id     INT AUTO_INCREMENT PRIMARY KEY,
    startup_id      INT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    description     VARCHAR(255),
    event_date      DATE,
    FOREIGN KEY (startup_id) REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 14. REPORTS / MODERATION  (Admin > Reports page)
-- ----------------------------------------------------------
CREATE TABLE reports (
    report_id       INT AUTO_INCREMENT PRIMARY KEY,
    reported_by     INT NOT NULL,
    target_type     ENUM('startup','comment','user') NOT NULL,
    target_id       INT NOT NULL,
    reason          VARCHAR(255),
    status          ENUM('open','dismissed','actioned') DEFAULT 'open',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reported_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================================
-- Helpful indexes for common lookups (Explore / filter / dashboards)
-- ==========================================================
CREATE INDEX idx_startups_category ON startups(category_id);
CREATE INDEX idx_startups_founder  ON startups(founder_id);
CREATE INDEX idx_startups_status   ON startups(status);
CREATE INDEX idx_investments_startup ON investments(startup_id);
CREATE INDEX idx_investments_investor ON investments(investor_id);
CREATE INDEX idx_comments_startup ON comments(startup_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);

-- ==========================================================
-- A couple of useful VIEWS (nice to demo in your viva —
-- shows you understand SQL beyond basic CRUD)
-- ==========================================================

-- Funding progress % per startup, computed on the fly
CREATE VIEW v_startup_progress AS
SELECT
    s.startup_id,
    s.name,
    s.funding_goal,
    s.amount_raised,
    ROUND((s.amount_raised / s.funding_goal) * 100, 1) AS percent_funded,
    DATEDIFF(s.deadline, CURDATE()) AS days_left
FROM startups s;

-- Investor leaderboard (top investors)
CREATE VIEW v_top_investors AS
SELECT
    u.user_id,
    u.full_name,
    COUNT(DISTINCT i.startup_id) AS startups_backed,
    SUM(i.amount) AS total_invested
FROM investments i
JOIN users u ON u.user_id = i.investor_id
WHERE i.status = 'confirmed'
GROUP BY u.user_id, u.full_name
ORDER BY total_invested DESC;
