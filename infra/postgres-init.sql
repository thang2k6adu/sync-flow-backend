-- Chạy một lần khi container Postgres khởi tạo volume: mỗi service một DB + một user riêng.
CREATE USER kruzetech_auth WITH PASSWORD 'kruzetech_auth';
CREATE DATABASE kruzetech_auth OWNER kruzetech_auth;

CREATE USER kruzetech_task WITH PASSWORD 'kruzetech_task';
CREATE DATABASE kruzetech_task OWNER kruzetech_task;
