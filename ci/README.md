# Sync Flow Backend - Local & Centralized CI

Thư mục này chứa bộ công cụ kiểm thử và đóng gói tự động (CI Pipeline) cho cả 4 microservices:
- `kruzetech-auth`
- `kruzetech-task`
- `kruzetech-vocab`
- `kruzetech-gateway`

Quy trình được thiết kế chuẩn hoá theo kiến trúc CI của Kruzetech, hỗ trợ chạy độc lập từng service hoặc chạy toàn bộ.

---

## Các bước trong Pipeline (`start-ci.sh`)

1. **build-test**: Biên dịch mã nguồn và chạy toàn bộ unit tests bằng Gradle (`./gradlew clean test`).
2. **lint**: Kiểm tra định dạng code (`spotlessCheck` nếu có).
3. **scan-deps**: Biên dịch JAR và quét lỗ hổng bảo mật thư viện (CVE) bằng **Trivy** (tự động bỏ qua nếu máy dev chưa cài Trivy).
4. **scan-secrets**: Quét các khóa bí mật / API token vô tình bị commit bằng **Gitleaks** (chạy qua Docker).
5. **build-image**: Luôn luôn build Docker Image từ `Dockerfile` của service để kiểm tra cú pháp và khả năng đóng gói.
6. **publish-image**: Đẩy Docker Image lên Container Registry (chỉ thực hiện khi có biến môi trường đăng nhập).

---

## Hướng dẫn sử dụng

### 1. Chạy CI cho một service cụ thể:
```bash
# Cú pháp: ./ci/start-ci.sh [auth|task|vocab|gateway]
bash ci/start-ci.sh auth
bash ci/start-ci.sh task
```

### 2. Chạy CI cho toàn bộ services:
```bash
bash ci/start-ci.sh all
# hoặc chạy mặc định
bash ci/start-ci.sh
```

### 3. Chạy CI kèm Push Image lên Registry (GHCR / Docker Hub / Private Registry):
```bash
export IMAGE_REGISTRY=ghcr.io/thang2k6adu/sync-flow-backend
export REGISTRY_USER=thang2k6adu
export REGISTRY_TOKEN=ghp_xxxxxxxxxxxx
bash ci/start-ci.sh auth
```
