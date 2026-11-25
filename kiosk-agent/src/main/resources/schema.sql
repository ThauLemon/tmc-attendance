PRAGMA journal_mode=WAL;

CREATE TABLE IF NOT EXISTS attendance_event (
    transaction_id TEXT PRIMARY KEY,
    employee_id    TEXT    NOT NULL,
    device_id      TEXT,
    kiosk_id       TEXT    NOT NULL,
    site_id        TEXT    NOT NULL,
    direction      TEXT    NOT NULL,
    scanned_at     TEXT    NOT NULL,
    status         TEXT    NOT NULL,
    nonce          TEXT,
    synced         INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_attendance_event_sync ON attendance_event (synced, status);
