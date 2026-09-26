# java_boilerplate

Bản Spring Boot của `nest-boilerplate`, tổ chức nhiều service trong một repo (giống katech-billing-platform).
Contract auth khớp Nest nên `flutter_boilerplate` gọi được nguyên trạng.

```
Client ──► gateway :8088 ──┬─► kruzetech-auth :3000   /api/auth/**, /api/users/**
                           └─► kruzetech-task :3010   /api/tasks/**
```

Client chỉ cần đổi host sang gateway (`http://localhost:8088/api`); đường dẫn `/api/...` giữ nguyên.

## Cấu trúc

```
docker-compose.yml, Makefile
infra/postgres-init.sql        # mỗi service một DB + user riêng
services/                      # mỗi service có .env và .env.example riêng
├── kruzetech-auth/            # đăng ký/đăng nhập/Firebase/refresh + quản lý user
├── kruzetech-task/            # CRUD task theo user
└── kruzetech-gateway/         # Spring Cloud Gateway: route, CORS, X-Trace-Id, lỗi 502/504 dạng envelope
```

Mỗi service là một project Gradle độc lập (có wrapper, Dockerfile). Không có module chung: phần envelope / exception / traceId
được copy giữa các service.

## Chạy nhanh

```bash
make setup                    # kiểm tra Docker, tạo services/*/.env (tự sinh JWT secret), bật Postgres + Redis
make up                       # build và chạy Postgres + Redis + auth + task + gateway trong Docker
```

Mỗi service tự đọc `.env` ở thư mục của nó (Spring `spring.config.import`), nên tách service ra repo riêng chỉ cần mang theo thư mục đó.
`JWT_SECRET` của auth và task phải giống nhau. Biến môi trường thật luôn ưu tiên hơn `.env`.

Hoặc chạy từng service ngoài Docker: `make infra-up` (Postgres map ra cổng máy **5433** để không đụng Postgres cài sẵn trên 5432), rồi ba terminal `make run-auth`, `make run-task`, `make run-gateway`.
Test không cần Docker (dùng H2): `make test`. Xem trạng thái: `make status`. Xem log: `make logs` (hoặc `make logs-auth`, `logs-task`, `logs-gateway`). Dừng: `make stop` (giữ container) hoặc `make down`. Xem đủ lệnh: `make help`.

Swagger: `http://localhost:3000/api/docs` (auth), `http://localhost:3010/api/docs` (task). Gateway chưa gộp Swagger.

## Auth và task

- `kruzetech-auth` cấp JWT (HS256). `kruzetech-task` tự verify access token bằng cùng `JWT_SECRET`, không gọi sang auth.
  Danh tính lấy từ claim `sub`, `email`, `role`.
- Task: user chỉ thấy/sửa/xoá task của mình, ADMIN thấy tất cả. Task của người khác trả 404.

## Endpoint

| Service | Method | Path | Quyền |
|---|---|---|---|
| auth | POST | `/auth/register` (201), `/auth/login`, `/auth/firebase/login`, `/auth/refresh` | public |
| auth | POST | `/auth/logout` | đăng nhập |
| auth | GET/PATCH | `/users/profile` | đăng nhập |
| auth | POST, GET(list), GET/PATCH/DELETE `/users/{id}` | | ADMIN (list và get: + MODERATOR) |
| auth | GET | `/health` | public |
| task | POST (201), GET (page, limit, status, search), GET/PATCH/DELETE `/tasks/{id}` | | đăng nhập |
| task | GET | `/health` | public |

Response luôn là `{error, code, message, data, traceId}`; thành công `code = 0`, lỗi `code = HTTP status`.
Danh sách trả `data = {items, meta{itemCount,totalItems,itemsPerPage,totalPages,currentPage}}`.

## Ghi chú

- Endpoint public của mỗi service khai trong `SecurityConfig` (thay cho `@Public()` bên Nest).
- Firebase login cần cả `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY`.
- Chưa có (so với Nest): search, websocket, storage, mail, queue, throttle, notifications.
