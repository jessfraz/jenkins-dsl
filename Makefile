.PHONY: all
all: projects/mirrors projects/repo-dockerfiles projects/forks ## Run all the generation scripts.

.PHONY: projects/mirrors
projects/mirrors: generate-mirrors.sh ## Generate DSLs for git mirrors.
	@echo "+ $@"
	./$< | column -t

.PHONY: projects/repo-dockerfiles
projects/repo-dockerfiles: generate-repo-dockerfiles.sh ## Generate DSLs for repos with Dockerfiles.
	@echo "+ $@"
	./$< | column -t

.PHONY: projects/forks
projects/forks: generate-update-forks.sh ## Generate DSLs to update git forks.
	@echo "+ $@"
	./$< | column -t

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-\/]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
