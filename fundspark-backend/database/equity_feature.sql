-- ==========================================================
-- FundSpark — Investor Equity Benefit Feature (Migration 02)
-- Run this AFTER schema.sql + seed.sql are already applied.
-- Adds: Equity %, Digital Certificates, Cap Table, Dilution
-- History, Investor Badges, Valuation History.
-- ==========================================================

USE fundspark;

-- ----------------------------------------------------------
-- 1. STARTUPS — add valuation + founder's remaining equity
-- ----------------------------------------------------------
-- valuation      : the post-money valuation the founder sets (₹).
--                  Equity % for any investment = (amount / valuation) * 100
-- founder_equity : starts at 100.00 and reduces every time a new
--                  investor is confirmed (this is the "dilution").
ALTER TABLE startups
    ADD COLUMN valuation      DECIMAL(16,2) DEFAULT NULL      AFTER funding_goal,
    ADD COLUMN founder_equity DECIMAL(5,2)  DEFAULT 100.00    AFTER founder_id;

-- ----------------------------------------------------------
-- 2. INVESTMENTS — store the equity % granted for THIS investment
-- ----------------------------------------------------------
ALTER TABLE investments
    ADD COLUMN equity_percentage DECIMAL(6,3) DEFAULT 0.000 AFTER amount;

-- ----------------------------------------------------------
-- 3. BADGE_TIERS — reference table (static, seeded once)
-- ----------------------------------------------------------
CREATE TABLE badge_tiers (
    tier_id      INT AUTO_INCREMENT PRIMARY KEY,
    tier_name    VARCHAR(30) NOT NULL UNIQUE,   -- Bronze / Silver / Gold / Platinum
    min_invested DECIMAL(14,2) NOT NULL,        -- cumulative amount required to reach this tier
    icon         VARCHAR(40),                   -- lucide icon keyword
    color        VARCHAR(20)                    -- hex color for the badge chip
);

INSERT INTO badge_tiers (tier_name, min_invested, icon, color) VALUES
    ('Bronze',        0.00, 'award',  '#B87333'),
    ('Silver',   200000.00, 'medal',  '#9CA3AF'),
    ('Gold',     500000.00, 'trophy', '#F59E0B'),
    ('Platinum',1500000.00, 'gem',    '#7C3AED');

-- ----------------------------------------------------------
-- 4. INVESTOR_BADGES — one row per investor, kept in sync by the
--    backend every time a new investment is confirmed
-- ----------------------------------------------------------
CREATE TABLE investor_badges (
    investor_id     INT PRIMARY KEY,
    total_invested  DECIMAL(14,2) DEFAULT 0.00,
    tier_id         INT NOT NULL DEFAULT 1,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (investor_id) REFERENCES users(user_id)        ON DELETE CASCADE,
    FOREIGN KEY (tier_id)     REFERENCES badge_tiers(tier_id)
);

-- ----------------------------------------------------------
-- 5. EQUITY_CERTIFICATES — one certificate per confirmed investment
-- ----------------------------------------------------------
CREATE TABLE equity_certificates (
    certificate_id      INT AUTO_INCREMENT PRIMARY KEY,
    certificate_code    VARCHAR(30) UNIQUE,              -- e.g. FS-CERT-000001, auto-filled by trigger below
    investment_id       INT NOT NULL UNIQUE,
    investor_id         INT NOT NULL,
    startup_id          INT NOT NULL,
    equity_percentage   DECIMAL(6,3) NOT NULL,
    amount_invested      DECIMAL(14,2) NOT NULL,
    issue_date          DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investment_id) REFERENCES investments(investment_id) ON DELETE CASCADE,
    FOREIGN KEY (investor_id)   REFERENCES users(user_id)             ON DELETE CASCADE,
    FOREIGN KEY (startup_id)    REFERENCES startups(startup_id)       ON DELETE CASCADE
);

-- Auto-generates a human-friendly certificate code like FS-CERT-000001
-- if the backend doesn't supply one — demonstrates a BEFORE INSERT trigger.
DELIMITER //
CREATE TRIGGER trg_certificate_code
BEFORE INSERT ON equity_certificates
FOR EACH ROW
BEGIN
    IF NEW.certificate_code IS NULL THEN
        SET NEW.certificate_code = CONCAT('FS-CERT-', LPAD((SELECT IFNULL(MAX(certificate_id),0)+1 FROM equity_certificates), 6, '0'));
    END IF;
END //
DELIMITER ;

-- ----------------------------------------------------------
-- 6. EQUITY_HISTORY — dilution log (one row per investor per
--    startup every time their % changes because someone new invested)
-- ----------------------------------------------------------
CREATE TABLE equity_history (
    history_id          INT AUTO_INCREMENT PRIMARY KEY,
    investor_id         INT NOT NULL,
    startup_id          INT NOT NULL,
    old_equity_percentage DECIMAL(6,3) NOT NULL,
    new_equity_percentage DECIMAL(6,3) NOT NULL,
    change_reason        VARCHAR(150),
    changed_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (investor_id) REFERENCES users(user_id)     ON DELETE CASCADE,
    FOREIGN KEY (startup_id)  REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- 7. VALUATION_HISTORY — every time a founder changes the valuation
-- ----------------------------------------------------------
CREATE TABLE valuation_history (
    valuation_id     INT AUTO_INCREMENT PRIMARY KEY,
    startup_id       INT NOT NULL,
    round_name       VARCHAR(60) NOT NULL,      -- e.g. 'Seed', 'Series A'
    valuation_amount DECIMAL(16,2) NOT NULL,
    changed_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (startup_id) REFERENCES startups(startup_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
-- Helpful indexes
-- ----------------------------------------------------------
CREATE INDEX idx_equity_cert_investor ON equity_certificates(investor_id);
CREATE INDEX idx_equity_cert_startup  ON equity_certificates(startup_id);
CREATE INDEX idx_equity_hist_startup  ON equity_history(startup_id);
CREATE INDEX idx_equity_hist_investor ON equity_history(investor_id);
CREATE INDEX idx_valuation_startup    ON valuation_history(startup_id);

-- ==========================================================
-- VIEWS — for the Cap Table & Portfolio features
-- ==========================================================

-- Live Cap Table: Founder row + every investor's row, per startup
CREATE VIEW v_cap_table AS
SELECT s.startup_id, s.name AS startup_name,
       s.founder_id AS holder_id, u_f.full_name AS holder_name,
       'Founder' AS holder_type, s.founder_equity AS equity_percentage
FROM startups s
JOIN users u_f ON u_f.user_id = s.founder_id
UNION ALL
SELECT i.startup_id, s.name AS startup_name,
       i.investor_id AS holder_id, u_i.full_name AS holder_name,
       'Investor' AS holder_type, SUM(i.equity_percentage) AS equity_percentage
FROM investments i
JOIN startups s ON s.startup_id = i.startup_id
JOIN users u_i ON u_i.user_id = i.investor_id
WHERE i.status = 'confirmed'
GROUP BY i.startup_id, i.investor_id, s.name, u_i.full_name;

-- Investor's total equity portfolio across all startups they've backed
CREATE VIEW v_investor_portfolio AS
SELECT i.investor_id, u.full_name, i.startup_id, s.name AS startup_name,
       SUM(i.equity_percentage) AS total_equity_percent,
       SUM(i.amount) AS total_amount_invested
FROM investments i
JOIN startups s ON s.startup_id = i.startup_id
JOIN users u ON u.user_id = i.investor_id
WHERE i.status = 'confirmed'
GROUP BY i.investor_id, u.full_name, i.startup_id, s.name;

-- ==========================================================
-- Backfill: give every EXISTING startup a default valuation
-- (10x its funding goal, a common placeholder) so equity math
-- works immediately without founders having to edit every campaign.
-- ==========================================================
UPDATE startups SET valuation = funding_goal * 10 WHERE valuation IS NULL;

-- Seed one badge row for every existing user with role='investor'
-- so the Badges feature has data to show immediately.
INSERT INTO investor_badges (investor_id, total_invested, tier_id)
SELECT u.user_id, IFNULL(SUM(i.amount),0),
       (SELECT tier_id FROM badge_tiers WHERE min_invested <= IFNULL(SUM(i.amount),0) ORDER BY min_invested DESC LIMIT 1)
FROM users u
LEFT JOIN investments i ON i.investor_id = u.user_id AND i.status = 'confirmed'
WHERE u.role = 'investor'
GROUP BY u.user_id;
