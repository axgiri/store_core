.PHONY: auth-up auth-down auth-wait-schema auth-seed auth-wait-app auth-verify-token auth-ready

MK_FILE := $(abspath $(lastword $(MAKEFILE_LIST)))
MK_DIR := $(dir $(MK_FILE))
ENV_FILE := $(MK_DIR)helpers/.envK6
AUTH_MK_FILE := $(abspath $(MK_DIR)../../store_auth/k6/main.mk)

auth-up:
	$(MAKE) -f "$(AUTH_MK_FILE)" up

auth-down:
	$(MAKE) -f "$(AUTH_MK_FILE)" down

auth-wait-schema:
	$(MAKE) -f "$(AUTH_MK_FILE)" wait-schema

auth-seed:
	$(MAKE) -f "$(AUTH_MK_FILE)" seed

auth-wait-app:
	$(MAKE) -f "$(AUTH_MK_FILE)" wait-app

auth-verify-token:
	@set -e; \
	if [ ! -f "$(ENV_FILE)" ]; then \
		echo "Missing env file: $(ENV_FILE)"; \
		exit 1; \
	fi; \
	set -a; . "$(ENV_FILE)"; set +a; \
	if [ -z "$$AUTH_PASSWORD" ]; then \
		echo "AUTH_PASSWORD is required in $(ENV_FILE)"; \
		exit 1; \
	fi; \
	email="$$AUTH_EMAIL"; \
	if [ -z "$$email" ] && [ -n "$$AUTH_USERS" ]; then \
		email=$$(printf '%s' "$$AUTH_USERS" | cut -d',' -f1 | xargs); \
	fi; \
	if [ -z "$$email" ] && [ -n "$$AUTH_USER_EMAIL_TEMPLATE" ]; then \
		email=$$(printf '%s' "$$AUTH_USER_EMAIL_TEMPLATE" | sed 's/%d/1/'); \
	fi; \
	if [ -z "$$email" ]; then \
		echo "Unable to resolve auth email from AUTH_EMAIL / AUTH_USERS / AUTH_USER_EMAIL_TEMPLATE"; \
		exit 1; \
	fi; \
	login_url="$${AUTH_BASE_URL:-http://localhost:52720}/api/v1/users/login"; \
	payload=$$(printf '{"email":"%s","password":"%s"}' "$$email" "$$AUTH_PASSWORD"); \
	response=$$(curl -fsS "$$login_url" -H "Content-Type: application/json" -d "$$payload"); \
	printf '%s' "$$response" | grep -q '"access_token"'; \
	echo "Auth token check passed for $$email"

auth-ready: auth-up auth-wait-schema auth-seed auth-wait-app auth-verify-token