CREATE TABLE IF NOT EXISTS attendance_record (
    transaction_id VARCHAR(64)  PRIMARY KEY,
    employee_id    VARCHAR(64)  NOT NULL,
    device_id      VARCHAR(128),
    kiosk_id       VARCHAR(64)  NOT NULL,
    site_id        VARCHAR(64)  NOT NULL,
    direction      VARCHAR(8)   NOT NULL,
    scanned_at     TIMESTAMPTZ  NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    nonce          VARCHAR(128),
    received_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_attendance_record_employee ON attendance_record (employee_id, scanned_at);
