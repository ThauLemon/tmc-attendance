# TMC Attendance: hệ thống chấm công QR (BTL M2)

Code cho luồng ghi nhận ra vào end-to-end: điện thoại → kiosk → RabbitMQ → attendance-service → PostgreSQL.
Kiến trúc giữ nguyên M1 (Service-based, xử lý tại kiosk, hàng đợi), bổ sung ADR-08 đến ADR-10.

## Chạy toàn hệ thống bằng một lệnh

Cần Docker Desktop. Không cần cài Java hay Maven để chạy.

```bash
docker compose -f infra/docker-compose.yml up --build
```

| Thành phần | Địa chỉ |
|---|---|
| kiosk-1 (màn hình kiosk) | http://localhost:8082 |
| kiosk-2 | http://localhost:8083 |
| identity-service | http://localhost:8081 |
| attendance-service | http://localhost:8084 |
| RabbitMQ (quản lý) | http://localhost:15672 (tmc / tmc) |
| PostgreSQL | localhost:5432, DB `attendance` (tmc / tmc) |

### Thử walking skeleton

```bash
# Quét một lượt ở kiosk-1 (skeleton nhận token dạng EMP:<mã nhân viên>)
curl -X POST http://localhost:8082/api/scan -H "Content-Type: application/json" -d '{"token":"EMP:E0001","direction":"IN"}'

# Sau khoảng 2 giây, bản ghi đã lên trung tâm
curl "http://localhost:8084/api/attendance?employeeId=E0001"
curl http://localhost:8084/api/attendance/count

# Giả lập turnstile timeout → PENDING_VERIFICATION
curl -X POST "http://localhost:8082/api/simulator/mode?value=TIMEOUT"
```

## Build và test khi phát triển

Cần JDK 21 và Maven 3.9.

```bash
mvn verify                      # build + unit test toàn bộ
mvn -pl kiosk-agent -am verify  # chỉ một module và các module nó phụ thuộc
```

Chạy một service từ IDE: bật RabbitMQ và PostgreSQL trước bằng
`docker compose -f infra/docker-compose.yml up rabbitmq postgres`, rồi chạy lớp `*Application`.

## Cấu trúc repo và phân công

| Thư mục | Nội dung | Phụ trách |
|---|---|---|
| `contracts/` | Interface, DTO, định dạng QR token, `QrTokenSigner`, tên exchange/queue | Châu + Hưng (chốt trong 2–3 ngày đầu) |
| `identity-service/` | Nhân viên, đăng ký thiết bị, đẩy danh sách khóa thiết bị xuống kiosk; trang `employee-qr-web` | Phú (đăng nhập), Hưng (sinh/ký QR bằng WebCrypto) |
| `kiosk-agent/` | `AccessService`, `SqliteAttendanceStore`, `TurnstileSimulator`, giao diện kiosk | Nam |
| `kiosk-agent/` (validator) | Thay `StubQrValidator` bằng validator thật: chữ ký, TTL, nonce, anti-passback | Hưng |
| `kiosk-agent/` (đồng bộ) | `AttendanceSyncWorker`, `RabbitAttendancePublisher` | Dũng |
| `attendance-service/` | Idempotent consumer, PostgreSQL, API tra cứu, gắn cờ QR dùng lặp | Dũng |
| `audit-lib/` | `JdbcAuditWriter` (chỉ INSERT), RBAC tối thiểu | Anh |
| `load-test/` | Load test 800 lượt, đo p95 | Vũ |
| `infra/` | docker-compose, script chaos (cắt mạng, kill process) | Châu, Vũ |
| `docs/adr/` | ADR-08 đến ADR-10 | Anh |

Mỗi module cần một README riêng (chạy thế nào, interface nào, phụ thuộc gì) do người phụ trách viết.

## Trạng thái skeleton

Đã nối được luồng tối thiểu kiosk → SQLite → RabbitMQ → PostgreSQL. Những phần còn là **stub**, tìm `TODO` trong code:

- `StubQrValidator`: chưa kiểm chữ ký, chấp nhận mọi token `EMP:<mã>`.
- `DeviceKeyController`: trả danh sách khóa rỗng.
- `employee-qr-web`: mới là trang giữ chỗ.
- `PendingTransactionRecovery`: chưa hỏi turnstile theo transaction_id.
- Site Edge Node: chỉ thiết kế (ADR-09), không code ở M2.

## Quy tắc làm việc

- Không push thẳng lên `main`. Mỗi việc một feature branch (`feature/<tên>-<việc>`), mở PR.
- Mỗi PR: một người review logic, Anh review SOLID. CI phải xanh.
- Merge vào `main` ít nhất hai ngày một lần.
- Đổi `contracts/` sau khi đã chốt thì phải báo cả nhóm và cập nhật ADR liên quan.
- Tên component, interface, mã ADR trong code phải trùng với report.
