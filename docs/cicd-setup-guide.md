# Hướng dẫn Vận hành & Cấu hình CI/CD Backend Sync Flow

Hệ thống CI/CD của Sync Flow Backend được xây dựng hoàn toàn tự động bằng **GitHub Actions**, **GitHub Container Registry (GHCR)**, bộ script **Local CI (`backend/ci/`)** và **Docker Compose**.

---

## 1. Cấu trúc Pipeline

```text
[ Git Push / PR ]
       │
       ├──► CI Pipeline (.github/workflows/ci.yml)
       │      ├─ Matrix Test 4 Services song song (Auth, Task, Vocab, Gateway)
       │      ├─ Tự động cache Gradle (tối ưu tốc độ chạy < 1 phút)
       │      ├─ Compile Jar (`./gradlew bootJar -x test`)
       │      ├─ Build Docker Image Check (kiểm tra cú pháp & tính hợp lệ của Dockerfile)
       │      └─ Tự động xuất Test Report Artifact nếu có lỗi
       │
       └──► CD Pipeline (.github/workflows/cd.yml) (Chạy khi push vào nhánh `main` hoặc tạo Tag)
              ├─ Build Docker Image cho cả 4 services song song
              ├─ Push Docker Images lên GitHub Container Registry (ghcr.io)
              └─ Tự động SSH vào VPS Linux, pull image mới và restart container (nếu đã cấu hình SSH Secrets)
```

---

## 2. Kiến trúc Mạng, Port & Cơ chế Tải người dùng (User Scaling)

Trong kiến trúc Web API / Microservices của Sync Flow:
1. **Một cổng Gateway duy nhất cho toàn bộ người dùng**:
   - Cổng Gateway (`8088` hoặc `80`/`443`) sử dụng socket multiplexing để xử lý đồng thời hàng chục ngàn kết nối của người dùng.
   - Hệ thống **không bao giờ tăng thêm port ngoài host** cho từng người dùng kết nối vào.
2. **Bảo mật và Scale container ngầm**:
   - Trong `docker-compose.prod.yml`, các service nội bộ (`auth`, `task`, `vocab`) được cấu hình bằng `expose` (chỉ mở cổng trong mạng Docker `backend-net`), không bind cổng cố định ra ngoài máy chủ host.
   - Nhờ đó, khi tải tăng cao, ta có thể scale thêm số lượng instance (ví dụ: `docker compose -f docker-compose.prod.yml up -d --scale task=3`) mà **không bao giờ bị lỗi xung đột cổng** (`port is already allocated`).

---

## 3. Chạy CI Cục bộ (Local CI Suite `backend/ci/`)

Dự án cung cấp sẵn bộ script CI tại thư mục `backend/ci/` để lập trình viên có thể kiểm tra toàn diện trước khi push code:

```bash
# 1. Chạy CI cho 1 service cụ thể:
bash ci/start-ci.sh auth
bash ci/start-ci.sh task
bash ci/start-ci.sh vocab
bash ci/start-ci.sh gateway

# 2. Chạy CI cho toàn bộ 4 services:
bash ci/start-ci.sh all

# 3. Chạy kèm build & publish Docker Image lên Registry:
export IMAGE_REGISTRY=ghcr.io/thang2k6adu/sync-flow-backend
export REGISTRY_USER=thang2k6adu
export REGISTRY_TOKEN=ghp_xxxxxxxxxxxx
bash ci/start-ci.sh auth
```

---

## 4. Cấp quyền GitHub Actions để ghi Packages lên GHCR

Để GitHub Actions tự động push Docker Image lên `ghcr.io` mà không gặp lỗi `403 Forbidden`, cậu chỉ cần bật thiết lập sau một lần trên GitHub:

1. Vào repository: **`thang2k6adu/sync-flow-backend`** trên GitHub.
2. Vào **Settings** > **Actions** > **General**.
3. Cuộn xuống mục **Workflow permissions**:
   * Chọn: **Read and write permissions**.
   * Tích chọn: **Allow GitHub Actions to create and approve pull requests**.
4. Bấm **Save**.

---

## 5. Cấu hình Tự động Deploy lên VPS qua SSH (Tuỳ chọn)

Nếu cậu đã có máy chủ VPS Linux (Ubuntu / Debian), cậu thêm các Secret sau vào GitHub Repository:
1. Vào **Settings** > **Secrets and variables** > **Actions** > **New repository secret**.
2. Thêm các biến sau:

| Secret Name | Ý nghĩa | Ví dụ |
|---|---|---|
| `SSH_HOST` | Địa chỉ IP hoặc Domain của server | `103.123.45.67` |
| `SSH_USER` | Tên người dùng SSH (có quyền Docker) | `ubuntu` hoặc `root` |
| `SSH_KEY` | Private Key SSH (nội dung file `id_rsa`) | `-----BEGIN OPENSSH PRIVATE KEY...` |
| `SSH_PORT` | Cổng SSH (mặc định 22) | `22` |

> [!NOTE]
> Nếu chưa thêm các Secret này, CD workflow sẽ chỉ thực hiện **Build & Push Image lên GHCR** và tự động bỏ qua bước SSH deploy mà không báo lỗi.

---

## 6. Chuẩn bị Server Production (Làm 1 lần duy nhất trên VPS)

Trên máy chủ VPS:
```bash
# 1. Cài đặt Docker & Docker Compose
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 2. Tạo thư mục chứa project
mkdir -p ~/sync-flow-backend
cd ~/sync-flow-backend

# 3. Tạo file cấu hình môi trường .env (tham khảo file .env.prod.example trong repo)
nano .env

# 4. Khi CD chạy xong, toàn bộ service sẽ tự khởi động ngầm:
docker compose -f docker-compose.prod.yml ps
```

---

## 7. Kiểm thử API nhanh chóng (API Test Tooling)

### Cách 1: Chạy toàn bộ test tự động qua Gradle
```bash
# Chạy toàn bộ test 4 microservices
make test

# Hoặc chạy riêng service từ vựng:
cd services/kruzetech-vocab && ./gradlew test
```

### Cách 2: Gọi API trực quan với file `.http`
Dự án đã có sẵn file [`backend/docs/api-tests.http`](./api-tests.http). Cậu có thể mở file này bằng:
* **VS Code**: Cài extension `REST Client` (tác giả Huachao Mao).
* **IntelliJ IDEA**: Hỗ trợ HTTP Client gốc.

Chỉ cần click vào chữ **`Send Request`** ngay trên đầu mỗi endpoint để:
- Tạo user Alice & lấy JWT access token tự động
- Tạo user Bob để test phân quyền
- Tạo Task, sửa Task, xóa Task
- Tạo bộ từ vựng (Deck), thêm thẻ (Card) kèm nghĩa đa tầng & bài tập
- Gọi ôn tập SRS (Study Queue) và gửi telemetry
