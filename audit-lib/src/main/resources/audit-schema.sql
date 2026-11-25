-- Bảng audit chỉ ghi thêm. Service dùng audit-lib tự chạy script này khi khởi tạo DB.
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMPTZ  NOT NULL,
    actor       VARCHAR(100) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    target      VARCHAR(200),
    detail      TEXT
);
