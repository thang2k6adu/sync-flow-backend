# ============================================================================
# KRUZETECH Java Boilerplate - Local Development
# ============================================================================
# Run `make help` để xem tất cả commands. Theo quy ước của katech-billing-platform
# (setup / up / down / status / logs / run-<svc>).
# ============================================================================

SHELL := /bin/bash
.DEFAULT_GOAL := help

# Colors
GREEN  := \033[0;32m
YELLOW := \033[0;33m
RED    := \033[0;31m
BLUE   := \033[0;34m
NC     := \033[0m

# Services
SERVICES     := kruzetech-auth kruzetech-task kruzetech-gateway
AUTH_PORT    := 3000
TASK_PORT    := 3010
GATEWAY_PORT := 8088
DB_PORT      := 5433
REDIS_PORT   := 6379

COMPOSE := docker compose --profile app


# ============================================================================
# HELP
# ============================================================================
.PHONY: help
help:  ## Show this help
	@echo ""
	@printf "$(BLUE)KRUZETECH Java Boilerplate - Local Development$(NC)\n"
	@echo ""
	@printf "$(YELLOW)Quick start:$(NC)\n"
	@printf "  $(GREEN)make setup$(NC)          First-time setup (run once)\n"
	@printf "  $(GREEN)make up$(NC)             Build + start everything (infra + 3 services)\n"
	@printf "  $(GREEN)make stop$(NC)           Stop everything (keep containers and data)\n"
	@printf "  $(GREEN)make down$(NC)           Stop and remove containers (keep data)\n"
	@printf "  $(GREEN)make status$(NC)         Show status\n"
	@printf "  $(GREEN)make logs$(NC)           Follow logs of all services\n"
	@echo ""
	@printf "$(YELLOW)Available commands:$(NC)\n"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""


# ============================================================================
# SETUP (run once)
# ============================================================================
.PHONY: setup
setup: check-prereqs env-init infra-up wait-healthy  ## First-time setup
	@printf "\n$(GREEN)[ok] Setup complete!$(NC)\n"
	@printf "Next: $(YELLOW)make up$(NC) to build and start 3 services.\n\n"

.PHONY: check-prereqs
check-prereqs:  ## Check prerequisites (Docker, Java)
	@printf "$(BLUE)-> Checking prerequisites...$(NC)\n"
	@command -v docker >/dev/null 2>&1 || { printf "$(RED)[x] Docker not installed$(NC)\n"; exit 1; }
	@docker info >/dev/null 2>&1 || { printf "$(RED)[x] Docker daemon not running (open Docker Desktop)$(NC)\n"; exit 1; }
	@command -v java >/dev/null 2>&1 || { printf "$(RED)[x] Java not installed (need 21+)$(NC)\n"; exit 1; }
	@java -version 2>&1 | head -1
	@printf "$(GREEN)[ok] Prerequisites OK$(NC)\n"

.PHONY: env-init
env-init:  ## Create .env from .env.example in each service (JWT secrets auto-generated, auth = task)
	@printf "$(BLUE)-> Initializing .env files...$(NC)\n"
	@for svc in $(SERVICES); do \
		if [ ! -f "services/$$svc/.env" ] && [ -f "services/$$svc/.env.example" ]; then \
			cp "services/$$svc/.env.example" "services/$$svc/.env"; \
			printf "  [ok] services/$$svc/.env created\n"; \
		else \
			printf "  -> services/$$svc/.env exists (skip)\n"; \
		fi; \
	done
	@auth=services/kruzetech-auth/.env; task=services/kruzetech-task/.env; \
	if grep -q '^JWT_SECRET=change-me' $$auth; then \
		sed -i "s/^JWT_SECRET=.*/JWT_SECRET=$$(openssl rand -hex 32)/" $$auth; \
		printf "  [ok] JWT_SECRET generated for auth\n"; \
	fi; \
	if grep -q '^JWT_REFRESH_SECRET=change-me' $$auth; then \
		sed -i "s/^JWT_REFRESH_SECRET=.*/JWT_REFRESH_SECRET=$$(openssl rand -hex 32)/" $$auth; \
		printf "  [ok] JWT_REFRESH_SECRET generated for auth\n"; \
	fi; \
	sed -i "s/^JWT_SECRET=.*/JWT_SECRET=$$(grep '^JWT_SECRET=' $$auth | cut -d= -f2-)/" $$task; \
	printf "  [ok] JWT_SECRET of task synced with auth\n"


# ============================================================================
# INFRA (Docker compose: Postgres + Redis)
# ============================================================================
.PHONY: infra-up
infra-up:  ## Start Postgres + Redis containers
	@printf "$(BLUE)-> Starting infrastructure...$(NC)\n"
	@docker compose up -d postgres redis
	@printf "$(GREEN)[ok] Infra started$(NC)\n"

.PHONY: infra-down
infra-down:  ## Stop Postgres + Redis (keep data)
	@printf "$(BLUE)-> Stopping infrastructure...$(NC)\n"
	@docker compose stop postgres redis
	@printf "$(GREEN)[ok] Infra stopped$(NC)\n"

.PHONY: infra-clean
infra-clean:  ## Remove everything + DELETE data volume (reset)
	@printf "$(RED)[!] This will DELETE all DB data (kruzetech_auth and kruzetech_task)!$(NC)\n"
	@read -p "Continue? [y/N] " r && [ "$$r" = "y" ] || exit 1
	@$(COMPOSE) down -v
	@printf "$(GREEN)[ok] Infra cleaned$(NC)\n"

.PHONY: wait-healthy
wait-healthy:  ## Wait until Postgres is healthy
	@printf "$(BLUE)-> Waiting for Postgres to be healthy...$(NC)\n"
	@for i in $$(seq 1 30); do \
		pg=$$(docker inspect --format='{{.State.Health.Status}}' $$(docker compose ps -q postgres) 2>/dev/null); \
		if [ "$$pg" = "healthy" ]; then \
			printf "  $(GREEN)[ok] Healthy ($$i s)$(NC)\n"; \
			exit 0; \
		fi; \
		sleep 1; \
	done; \
	printf "$(RED)[x] Timeout waiting for Postgres$(NC)\n"; exit 1


# ============================================================================
# SERVICES (Docker: auth + task + gateway)
# ============================================================================
.PHONY: up
up: env-init  ## Build + start everything (infra + 3 services)
	@printf "$(BLUE)-> Building and starting services...$(NC)\n"
	@$(COMPOSE) up --build -d
	@printf "$(YELLOW)-> Services booting. Wait ~30-60s, then 'make status'.$(NC)\n"
	@printf "\n$(GREEN)[ok] Started!$(NC)\n"
	@$(MAKE) --no-print-directory status

.PHONY: stop
stop:  ## Stop everything (keep containers and data)
	@printf "$(BLUE)-> Stopping everything...$(NC)\n"
	@$(COMPOSE) stop
	@printf "$(GREEN)[ok] Stopped$(NC)\n"

.PHONY: down
down:  ## Stop and remove containers (Postgres data stays in the volume)
	@printf "$(BLUE)-> Removing containers...$(NC)\n"
	@$(COMPOSE) down
	@printf "$(GREEN)[ok] Down$(NC)\n"

.PHONY: restart
restart: stop up  ## Restart everything

.PHONY: run-auth
run-auth:  ## Run kruzetech-auth locally (:3000, reads services/kruzetech-auth/.env)
	@printf "$(BLUE)-> Running kruzetech-auth on :$(AUTH_PORT)...$(NC)\n"
	@cd services/kruzetech-auth && ./gradlew bootRun

.PHONY: run-task
run-task:  ## Run kruzetech-task locally (:3010, reads services/kruzetech-task/.env)
	@printf "$(BLUE)-> Running kruzetech-task on :$(TASK_PORT)...$(NC)\n"
	@cd services/kruzetech-task && ./gradlew bootRun

.PHONY: run-gateway
run-gateway:  ## Run kruzetech-gateway locally (:8088, reads services/kruzetech-gateway/.env)
	@printf "$(BLUE)-> Running kruzetech-gateway on :$(GATEWAY_PORT)...$(NC)\n"
	@cd services/kruzetech-gateway && ./gradlew bootRun


# ============================================================================
# OBSERVABILITY
# ============================================================================
.PHONY: status
status:  ## Show status of all components
	@echo ""
	@printf "$(BLUE)KRUZETECH Status$(NC)\n"
	@echo "============================================"
	@echo ""
	@printf "$(YELLOW)Containers:$(NC)\n"
	@$(COMPOSE) ps --format "  {{.Name}}: {{.Status}}" 2>/dev/null | grep . || echo "  (down)"
	@echo ""
	@printf "$(YELLOW)Services:$(NC)\n"
	@for svc_port in "auth:$(AUTH_PORT)" "task:$(TASK_PORT)" "gateway:$(GATEWAY_PORT)"; do \
		svc=$${svc_port%%:*}; \
		port=$${svc_port##*:}; \
		health=$$(docker inspect --format='{{.State.Health.Status}}' $$(docker compose ps -q $$svc) 2>/dev/null); \
		if [ "$$health" = "healthy" ]; then \
			printf "  $(GREEN)[ok]$(NC) %-20s :%s UP\n" "kruzetech-$$svc" "$$port"; \
		else \
			printf "  $(RED)[x]$(NC)  %-20s :%s DOWN (%s)\n" "kruzetech-$$svc" "$$port" "$${health:-not running}"; \
		fi; \
	done
	@echo ""
	@printf "$(YELLOW)URLs:$(NC)\n"
	@echo "  API (gateway)   : http://localhost:$(GATEWAY_PORT)/api"
	@echo "  Swagger auth    : http://localhost:$(AUTH_PORT)/api/docs"
	@echo "  Swagger task    : http://localhost:$(TASK_PORT)/api/docs"
	@echo "  Health gateway  : http://localhost:$(GATEWAY_PORT)/actuator/health"
	@echo ""

.PHONY: logs
logs:  ## Follow logs of all services
	@$(COMPOSE) logs -f --tail=100

.PHONY: logs-auth
logs-auth:  ## Follow kruzetech-auth log
	@$(COMPOSE) logs -f --tail=100 auth

.PHONY: logs-task
logs-task:  ## Follow kruzetech-task log
	@$(COMPOSE) logs -f --tail=100 task

.PHONY: logs-gateway
logs-gateway:  ## Follow kruzetech-gateway log
	@$(COMPOSE) logs -f --tail=100 gateway

.PHONY: logs-postgres
logs-postgres:  ## Follow Postgres log
	@$(COMPOSE) logs -f --tail=100 postgres


# ============================================================================
# BUILD / TEST
# ============================================================================
.PHONY: build
build:  ## Build jar of all 3 services (./gradlew bootJar)
	@for svc in $(SERVICES); do \
		printf "$(BLUE)-> Building $$svc...$(NC)\n"; \
		(cd services/$$svc && ./gradlew bootJar -x test) || exit 1; \
	done
	@printf "$(GREEN)[ok] Build complete$(NC)\n"

.PHONY: test
test:  ## Run unit tests of 3 services (H2, no Docker needed)
	@for svc in $(SERVICES); do \
		printf "$(BLUE)-> Testing $$svc...$(NC)\n"; \
		(cd services/$$svc && ./gradlew test) || exit 1; \
	done
	@printf "$(GREEN)[ok] All tests passed$(NC)\n"


# ============================================================================
# DATABASE QUICK ACCESS (psql runs inside the Postgres container)
# ============================================================================
.PHONY: psql-auth
psql-auth:  ## Connect to kruzetech_auth DB
	@docker compose exec postgres psql -U kruzetech_auth -d kruzetech_auth

.PHONY: psql-task
psql-task:  ## Connect to kruzetech_task DB
	@docker compose exec postgres psql -U kruzetech_task -d kruzetech_task


# ============================================================================
# CLEANUP
# ============================================================================
.PHONY: nuke
nuke: infra-clean  ## Remove everything + DELETE all data (DANGER)
	@printf "$(RED)[ok] Nuked. Run 'make setup' to start over.$(NC)\n"
