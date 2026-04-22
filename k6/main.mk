.PHONY: ensure-network check-core-creds auth-ready up down down-core wait-schema wait-app run run-test

MK_FILE := $(abspath $(lastword $(MAKEFILE_LIST)))
MK_DIR := $(dir $(MK_FILE))
ROOT_DIR := $(abspath $(MK_DIR)..)
ENV_FILE := $(MK_DIR)helpers/.envK6
RUN_FILE := $(MK_DIR)run.js
COMPOSE_FILE := $(MK_DIR)helpers/docker-compose-k6.yaml
ROOT_ENV_FILE := $(ROOT_DIR)/.env
COMPOSE := docker compose --env-file "$(ROOT_ENV_FILE)" -f "$(COMPOSE_FILE)"
AUTH_HELPER_MK := $(MK_DIR)auth.mk
STORE_NETWORK := store-network
WAIT_URL := http://localhost:52730/api/v1/products/list?page=0&size=1

ensure-network:
	@docker network inspect "$(STORE_NETWORK)" > /dev/null 2>&1 || docker network create "$(STORE_NETWORK)"

check-core-creds:
	@set -e; \
	if [ ! -f "$(ROOT_ENV_FILE)" ]; then \
		echo "Missing env file: $(ROOT_ENV_FILE)"; \
		echo "Create it with GITHUB_PACKAGES_USER and GITHUB_PACKAGES_TOKEN"; \
		exit 1; \
	fi; \
	if ! grep -q '^GITHUB_PACKAGES_USER=' "$(ROOT_ENV_FILE)"; then \
		echo "GITHUB_PACKAGES_USER is missing in $(ROOT_ENV_FILE)"; \
		exit 1; \
	fi; \
	if ! grep -q '^GITHUB_PACKAGES_TOKEN=' "$(ROOT_ENV_FILE)"; then \
		echo "GITHUB_PACKAGES_TOKEN is missing in $(ROOT_ENV_FILE)"; \
		exit 1; \
	fi

auth-ready:
	$(MAKE) -f "$(AUTH_HELPER_MK)" auth-ready

up:
	$(MAKE) -f "$(MK_FILE)" ensure-network
	$(MAKE) -f "$(MK_FILE)" check-core-creds
	$(MAKE) -f "$(MK_FILE)" auth-ready
	$(COMPOSE) up -d --wait

down-core:
	@set -e; \
	if [ -f "$(ROOT_ENV_FILE)" ]; then \
		$(COMPOSE) down --remove-orphans; \
	else \
		GITHUB_PACKAGES_USER=dummy GITHUB_PACKAGES_TOKEN=dummy docker compose -f "$(COMPOSE_FILE)" down --remove-orphans; \
	fi

down:
	-$(MAKE) -f "$(MK_FILE)" down-core
	-$(MAKE) -f "$(AUTH_HELPER_MK)" auth-down

wait-schema:
	@set -e; \
	i=0; \
	until $(COMPOSE) exec -T store-core-db-k6 psql -U postgres -d storecoredb -tAc "SELECT to_regclass('public.persons') IS NOT NULL" | grep -q t; do \
		i=$$((i+1)); \
		if [ $$i -ge 150 ]; then \
			echo "Schema not ready: expected table persons"; \
			exit 1; \
		fi; \
		echo "Waiting for schema... ($$i/150)"; \
		sleep 1; \
	done

wait-app:
	@set -e; \
	i=0; \
	ok=0; \
	until [ $$ok -ge 15 ]; do \
		if curl -fsS "$(WAIT_URL)" > /dev/null; then \
			ok=$$((ok+1)); \
			echo "App HTTP endpoint ready check $$ok/15"; \
		else \
			ok=0; \
			i=$$((i+1)); \
			if [ $$i -ge 90 ]; then \
				echo "App HTTP endpoint not ready: $(WAIT_URL)"; \
				exit 1; \
			fi; \
			echo "Waiting for app HTTP endpoint... ($$i/90)"; \
		fi; \
		sleep 1; \
	done

run-test:
	cd "$(ROOT_DIR)" && set -a; . ./k6/helpers/.envK6; set +a; k6 run "$(RUN_FILE)"

run:
	@set -e; \
	trap '$(MAKE) -f "$(MK_FILE)" down' EXIT INT TERM; \
	$(MAKE) -f "$(MK_FILE)" up; \
	$(MAKE) -f "$(MK_FILE)" wait-schema; \
	$(MAKE) -f "$(MK_FILE)" wait-app; \
	$(MAKE) -f "$(MK_FILE)" run-test
