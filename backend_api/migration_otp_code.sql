-- Jalankan sekali di phpMyAdmin / MySQL untuk memastikan kolom otp_code ada
-- Kalau kolomnya sudah ada, skip / abaikan error "duplicate column"
ALTER TABLE users ADD COLUMN otp_code VARCHAR(6) NULL AFTER is_active;
