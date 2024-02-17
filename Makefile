.PHONY: help pyenv run format clean check-format lint docs build examples

# Help system from https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.DEFAULT_GOAL := help

help:
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

clean: ## Clean target directory
	./mvnw clean

run: ## Build and Run application
	./mvnw javafx:run

install: clean ## Package and install application
	./mvnw package -Ppackage

icons: ## Generate icons
	./scripts/mk-icns.sh src/main/resources/icons/icon.svg app